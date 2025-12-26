import * as exec from '@actions/exec';
import { ActionInputs } from '../inputs';
import { parseSpec, ApiSpec } from './parser';
import { compareSpecs, ComparisonResult } from './comparator';
import { generateReport } from './reporter';
import { Logger } from '../utils/logger';
import { readFile, writeFile } from '../utils/file';

const logger = new Logger('Detector');

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type SemverRecommendation = 'MAJOR' | 'MINOR' | 'PATCH';

export type ChangeType = 'ADDED' | 'MODIFIED' | 'REMOVED' | 'DEPRECATED';

export type ChangeSeverity = 'BREAKING' | 'DANGEROUS' | 'WARNING' | 'INFO';

export interface Change {
  
  type: ChangeType;
  
  category: string;
  
  severity: ChangeSeverity;
  
  path: string;
  
  description: string;
  
  oldValue?: string;
  
  newValue?: string;
}

export interface BreakingChange extends Change {
  
  migrationSuggestion: string;
  
  impactScore: number;
  
  affectedClients?: string[];
}

export interface ChangelogResult {
  
  hasBreakingChanges: boolean;
  
  breakingChangesCount: number;
  
  totalChangesCount: number;
  
  riskLevel: RiskLevel;
  
  riskScore: number;
  
  semverRecommendation: SemverRecommendation;
  
  changelog: string;
  
  changelogFile?: string;
  
  changes: Change[];
  
  breakingChanges: BreakingChange[];
  
  oldSpec: { name: string; version: string };
  
  newSpec: { name: string; version: string };
}

export async function detectBreakingChanges(inputs: ActionInputs): Promise<ChangelogResult> {
  logger.info('Starting breaking change detection...');

  let oldSpecContent: string;
  let newSpecContent: string;

  if (inputs.oldSpec && inputs.newSpec) {
    
    logger.info('Using provided spec files');
    oldSpecContent = await readFile(inputs.oldSpec);
    newSpecContent = await readFile(inputs.newSpec);
  } else {
    
    logger.info(`Comparing ${inputs.baseRef} with ${inputs.headRef}`);
    oldSpecContent = await getSpecFromRef(inputs.baseRef, inputs.specPath);
    newSpecContent = await getSpecFromRef(inputs.headRef, inputs.specPath);
  }

  logger.info('Parsing API specifications...');
  const oldApiSpec = parseSpec(oldSpecContent, inputs.specPath);
  const newApiSpec = parseSpec(newSpecContent, inputs.specPath);

  logger.info(`Old spec: ${oldApiSpec.name} v${oldApiSpec.version}`);
  logger.info(`New spec: ${newApiSpec.name} v${newApiSpec.version}`);

  logger.info('Comparing specifications...');
  const comparison = compareSpecs(oldApiSpec, newApiSpec);

  const filteredChanges = filterBySeverity(comparison.changes, inputs.severityThreshold);
  const filteredBreakingChanges = comparison.breakingChanges;

  logger.info('Generating report...');
  const changelog = generateReport(
    {
      ...comparison,
      changes: filteredChanges,
    },
    inputs.format,
    {
      oldSpec: oldApiSpec,
      newSpec: newApiSpec,
    }
  );

  const riskScore = calculateRiskScore(filteredBreakingChanges, filteredChanges);
  const riskLevel = calculateRiskLevel(riskScore);
  const semverRecommendation = getSemverRecommendation(comparison, oldApiSpec, newApiSpec);

  let changelogFile: string | undefined;
  if (inputs.format !== 'console') {
    const extension = inputs.format === 'json' ? 'json' : 'md';
    changelogFile = `changelog-${Date.now()}.${extension}`;
    await writeFile(changelogFile, changelog);
    logger.info(`Changelog written to ${changelogFile}`);
  }

  const result: ChangelogResult = {
    hasBreakingChanges: filteredBreakingChanges.length > 0,
    breakingChangesCount: filteredBreakingChanges.length,
    totalChangesCount: filteredChanges.length,
    riskLevel,
    riskScore,
    semverRecommendation,
    changelog,
    changelogFile,
    changes: filteredChanges,
    breakingChanges: filteredBreakingChanges,
    oldSpec: { name: oldApiSpec.name, version: oldApiSpec.version },
    newSpec: { name: newApiSpec.name, version: newApiSpec.version },
  };

  logger.info(`Detection complete: ${result.breakingChangesCount} breaking, ${result.totalChangesCount} total`);

  return result;
}

async function getSpecFromRef(ref: string, path: string): Promise<string> {
  logger.debug(`Getting spec from ref: ${ref}:${path}`);

  let output = '';
  let errorOutput = '';

  try {
    const exitCode = await exec.exec('git', ['show', `${ref}:${path}`], {
      listeners: {
        stdout: (data) => {
          output += data.toString();
        },
        stderr: (data) => {
          errorOutput += data.toString();
        },
      },
      silent: true,
    });

    if (exitCode !== 0) {
      throw new Error(`Git command failed with exit code ${exitCode}: ${errorOutput}`);
    }
  } catch (error) {
    
    const message = error instanceof Error ? error.message : String(error);
    if (message.includes('does not exist') || message.includes('not found')) {
      throw new Error(`Spec file '${path}' not found at ref '${ref}'`);
    }
    throw error;
  }

  if (!output.trim()) {
    throw new Error(`Empty spec file at ${ref}:${path}`);
  }

  return output;
}

function filterBySeverity(changes: Change[], threshold: string): Change[] {
  const severityOrder: Record<string, number> = {
    INFO: 0,
    WARNING: 1,
    DANGEROUS: 2,
    BREAKING: 3,
  };

  const thresholdLevel = severityOrder[threshold] ?? 0;

  return changes.filter((change) => {
    const changeLevel = severityOrder[change.severity] ?? 0;
    return changeLevel >= thresholdLevel;
  });
}

function calculateRiskScore(breakingChanges: BreakingChange[], allChanges: Change[]): number {
  if (allChanges.length === 0) {
    return 0;
  }

  let score = 0;

  for (const change of breakingChanges) {
    score += change.impactScore;
  }

  const maxPossibleScore = breakingChanges.length * 100 || 100;
  const normalizedScore = Math.min(100, Math.round((score / maxPossibleScore) * 100));

  const dangerousCount = allChanges.filter((c) => c.severity === 'DANGEROUS').length;
  const warningCount = allChanges.filter((c) => c.severity === 'WARNING').length;

  const additionalScore = Math.min(20, dangerousCount * 5 + warningCount * 2);

  return Math.min(100, normalizedScore + additionalScore);
}

function calculateRiskLevel(score: number): RiskLevel {
  if (score >= 75) return 'CRITICAL';
  if (score >= 50) return 'HIGH';
  if (score >= 25) return 'MEDIUM';
  return 'LOW';
}

function getSemverRecommendation(
  comparison: ComparisonResult,
  _oldSpec: ApiSpec,
  _newSpec: ApiSpec
): SemverRecommendation {
  
  if (comparison.breakingChanges.length > 0) {
    return 'MAJOR';
  }

  const hasAdditions = comparison.changes.some((c) => c.type === 'ADDED');
  if (hasAdditions) {
    return 'MINOR';
  }

  return 'PATCH';
}

export async function detectBreakingChangesMultiple(
  inputs: ActionInputs,
  specPaths: string[]
): Promise<ChangelogResult[]> {
  const results: ChangelogResult[] = [];

  for (const specPath of specPaths) {
    const modifiedInputs = { ...inputs, specPath };
    try {
      const result = await detectBreakingChanges(modifiedInputs);
      results.push(result);
    } catch (error) {
      logger.warn(`Failed to analyze ${specPath}: ${error}`);
    }
  }

  return results;
}

export function aggregateResults(results: ChangelogResult[]): ChangelogResult {
  const allChanges: Change[] = [];
  const allBreakingChanges: BreakingChange[] = [];

  for (const result of results) {
    allChanges.push(...result.changes);
    allBreakingChanges.push(...result.breakingChanges);
  }

  const riskScore = Math.max(...results.map((r) => r.riskScore), 0);
  const riskLevel = calculateRiskLevel(riskScore);

  let semverRecommendation: SemverRecommendation = 'PATCH';
  if (allBreakingChanges.length > 0) {
    semverRecommendation = 'MAJOR';
  } else if (allChanges.some((c) => c.type === 'ADDED')) {
    semverRecommendation = 'MINOR';
  }

  return {
    hasBreakingChanges: allBreakingChanges.length > 0,
    breakingChangesCount: allBreakingChanges.length,
    totalChangesCount: allChanges.length,
    riskLevel,
    riskScore,
    semverRecommendation,
    changelog: results.map((r) => r.changelog).join('\n\n---\n\n'),
    changes: allChanges,
    breakingChanges: allBreakingChanges,
    oldSpec: results[0]?.oldSpec ?? { name: 'Unknown', version: '0.0.0' },
    newSpec: results[0]?.newSpec ?? { name: 'Unknown', version: '0.0.0' },
  };
}
