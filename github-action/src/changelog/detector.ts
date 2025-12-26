import * as exec from '@actions/exec';
import { ActionInputs } from '../inputs';
import { parseSpec, ApiSpec } from './parser';
import { compareSpecs, ComparisonResult } from './comparator';
import { generateReport } from './reporter';
import { Logger } from '../utils/logger';
import { readFile, writeFile } from '../utils/file';

const logger = new Logger('Detector');

/**
 * Risk levels for API changes
 */
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

/**
 * Semantic versioning recommendations
 */
export type SemverRecommendation = 'MAJOR' | 'MINOR' | 'PATCH';

/**
 * Change types that can be detected
 */
export type ChangeType = 'ADDED' | 'MODIFIED' | 'REMOVED' | 'DEPRECATED';

/**
 * Severity levels for changes
 */
export type ChangeSeverity = 'BREAKING' | 'DANGEROUS' | 'WARNING' | 'INFO';

/**
 * Represents a detected change in the API
 */
export interface Change {
  /** Type of change */
  type: ChangeType;
  /** Category of the change (e.g., ENDPOINT, PARAMETER, SCHEMA) */
  category: string;
  /** Severity of the change */
  severity: ChangeSeverity;
  /** Path or location of the change */
  path: string;
  /** Human-readable description */
  description: string;
  /** Old value (for modifications) */
  oldValue?: string;
  /** New value (for modifications) */
  newValue?: string;
}

/**
 * Represents a breaking change with additional migration information
 */
export interface BreakingChange extends Change {
  /** Suggestion for how to migrate */
  migrationSuggestion: string;
  /** Impact score (0-100) */
  impactScore: number;
  /** Affected clients/consumers (if identifiable) */
  affectedClients?: string[];
}

/**
 * Result of the changelog detection process
 */
export interface ChangelogResult {
  /** Whether any breaking changes were detected */
  hasBreakingChanges: boolean;
  /** Count of breaking changes */
  breakingChangesCount: number;
  /** Total count of all changes */
  totalChangesCount: number;
  /** Calculated risk level */
  riskLevel: RiskLevel;
  /** Calculated risk score (0-100) */
  riskScore: number;
  /** Recommended semantic version bump */
  semverRecommendation: SemverRecommendation;
  /** Generated changelog content */
  changelog: string;
  /** Path to changelog file if written */
  changelogFile?: string;
  /** All detected changes */
  changes: Change[];
  /** Breaking changes only */
  breakingChanges: BreakingChange[];
  /** Old API spec info */
  oldSpec: { name: string; version: string };
  /** New API spec info */
  newSpec: { name: string; version: string };
}

/**
 * Detects breaking changes between two API specifications.
 *
 * @param inputs - Action configuration inputs
 * @returns Detection results including changes and recommendations
 */
export async function detectBreakingChanges(inputs: ActionInputs): Promise<ChangelogResult> {
  logger.info('Starting breaking change detection...');

  // Get spec contents
  let oldSpecContent: string;
  let newSpecContent: string;

  if (inputs.oldSpec && inputs.newSpec) {
    // Direct file paths provided
    logger.info('Using provided spec files');
    oldSpecContent = await readFile(inputs.oldSpec);
    newSpecContent = await readFile(inputs.newSpec);
  } else {
    // Get from git refs
    logger.info(`Comparing ${inputs.baseRef} with ${inputs.headRef}`);
    oldSpecContent = await getSpecFromRef(inputs.baseRef, inputs.specPath);
    newSpecContent = await getSpecFromRef(inputs.headRef, inputs.specPath);
  }

  // Parse specifications
  logger.info('Parsing API specifications...');
  const oldApiSpec = parseSpec(oldSpecContent, inputs.specPath);
  const newApiSpec = parseSpec(newSpecContent, inputs.specPath);

  logger.info(`Old spec: ${oldApiSpec.name} v${oldApiSpec.version}`);
  logger.info(`New spec: ${newApiSpec.name} v${newApiSpec.version}`);

  // Compare specifications
  logger.info('Comparing specifications...');
  const comparison = compareSpecs(oldApiSpec, newApiSpec);

  // Filter by severity threshold
  const filteredChanges = filterBySeverity(comparison.changes, inputs.severityThreshold);
  const filteredBreakingChanges = comparison.breakingChanges;

  // Generate report
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

  // Calculate risk metrics
  const riskScore = calculateRiskScore(filteredBreakingChanges, filteredChanges);
  const riskLevel = calculateRiskLevel(riskScore);
  const semverRecommendation = getSemverRecommendation(comparison, oldApiSpec, newApiSpec);

  // Optionally write changelog file
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

/**
 * Retrieves the API spec content from a Git reference
 */
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
    // Check if file exists at ref
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

/**
 * Filters changes by severity threshold
 */
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

/**
 * Calculates the risk score based on changes
 */
function calculateRiskScore(breakingChanges: BreakingChange[], allChanges: Change[]): number {
  if (allChanges.length === 0) {
    return 0;
  }

  // Base score from breaking changes
  let score = 0;

  for (const change of breakingChanges) {
    score += change.impactScore;
  }

  // Normalize to 0-100 range
  const maxPossibleScore = breakingChanges.length * 100 || 100;
  const normalizedScore = Math.min(100, Math.round((score / maxPossibleScore) * 100));

  // Add minor points for dangerous/warning changes
  const dangerousCount = allChanges.filter((c) => c.severity === 'DANGEROUS').length;
  const warningCount = allChanges.filter((c) => c.severity === 'WARNING').length;

  const additionalScore = Math.min(20, dangerousCount * 5 + warningCount * 2);

  return Math.min(100, normalizedScore + additionalScore);
}

/**
 * Determines the risk level based on score
 */
function calculateRiskLevel(score: number): RiskLevel {
  if (score >= 75) return 'CRITICAL';
  if (score >= 50) return 'HIGH';
  if (score >= 25) return 'MEDIUM';
  return 'LOW';
}

/**
 * Determines the recommended semantic version bump
 */
function getSemverRecommendation(
  comparison: ComparisonResult,
  _oldSpec: ApiSpec,
  _newSpec: ApiSpec
): SemverRecommendation {
  // If there are breaking changes, recommend MAJOR
  if (comparison.breakingChanges.length > 0) {
    return 'MAJOR';
  }

  // If there are new additions, recommend MINOR
  const hasAdditions = comparison.changes.some((c) => c.type === 'ADDED');
  if (hasAdditions) {
    return 'MINOR';
  }

  // Otherwise, recommend PATCH
  return 'PATCH';
}

/**
 * Detects breaking changes for multiple spec files (batch mode)
 */
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

/**
 * Aggregates multiple changelog results into a single summary
 */
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
