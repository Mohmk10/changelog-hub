import { ApiSpec, ComparisonResult } from '../types';
import { parseSpec } from './parser';
import { compareSpecs } from './comparator';
import { readFile, fileExists } from '../utils/file';

export interface DetectionOptions {
  
  severityThreshold?: 'INFO' | 'WARNING' | 'DANGEROUS' | 'BREAKING';
  
  includeDeprecations?: boolean;
}

export function detectBreakingChanges(
  oldSpecPath: string,
  newSpecPath: string,
  options: DetectionOptions = {}
): ComparisonResult {
  
  if (!fileExists(oldSpecPath)) {
    throw new Error(`File not found: ${oldSpecPath}`);
  }
  if (!fileExists(newSpecPath)) {
    throw new Error(`File not found: ${newSpecPath}`);
  }

  const oldContent = readFile(oldSpecPath);
  const newContent = readFile(newSpecPath);

  const oldSpec = parseSpec(oldContent, oldSpecPath);
  const newSpec = parseSpec(newContent, newSpecPath);

  let result = compareSpecs(oldSpec, newSpec);

  if (options.severityThreshold) {
    result = filterBySeverity(result, options.severityThreshold);
  }

  if (options.includeDeprecations === false) {
    result = {
      ...result,
      changes: result.changes.filter((c) => c.type !== 'DEPRECATED'),
    };
  }

  return result;
}

export function detectBreakingChangesFromSpecs(
  oldSpec: ApiSpec,
  newSpec: ApiSpec,
  options: DetectionOptions = {}
): ComparisonResult {
  let result = compareSpecs(oldSpec, newSpec);

  if (options.severityThreshold) {
    result = filterBySeverity(result, options.severityThreshold);
  }

  if (options.includeDeprecations === false) {
    result = {
      ...result,
      changes: result.changes.filter((c) => c.type !== 'DEPRECATED'),
    };
  }

  return result;
}

function filterBySeverity(
  result: ComparisonResult,
  threshold: string
): ComparisonResult {
  const severityOrder: Record<string, number> = {
    INFO: 0,
    WARNING: 1,
    DANGEROUS: 2,
    BREAKING: 3,
  };

  const thresholdLevel = severityOrder[threshold] ?? 0;

  const filteredChanges = result.changes.filter((change) => {
    const changeLevel = severityOrder[change.severity] ?? 0;
    return changeLevel >= thresholdLevel;
  });

  return {
    ...result,
    changes: filteredChanges,
    totalChanges: filteredChanges.length,
  };
}

export function hasBreakingChanges(oldSpecPath: string, newSpecPath: string): boolean {
  const result = detectBreakingChanges(oldSpecPath, newSpecPath);
  return result.breakingChanges.length > 0;
}

export function getBreakingChangesSummary(
  oldSpecPath: string,
  newSpecPath: string
): {
  count: number;
  riskLevel: string;
  recommendation: string;
  changes: string[];
} {
  const result = detectBreakingChanges(oldSpecPath, newSpecPath);

  return {
    count: result.breakingChanges.length,
    riskLevel: result.riskLevel,
    recommendation: result.semverRecommendation,
    changes: result.breakingChanges.map((c) => c.description),
  };
}
