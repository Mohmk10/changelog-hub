import { ApiSpec, ComparisonResult, ChangeSeverity } from '../types';
import { parseSpec } from './parser';
import { compareSpecs } from './comparator';

/**
 * Options for breaking change detection
 */
export interface DetectionOptions {
  severityThreshold?: ChangeSeverity;
  includeDeprecations?: boolean;
}

/**
 * Detects breaking changes between two API specification contents.
 */
export function detectBreakingChanges(
  oldContent: string,
  newContent: string,
  filename: string,
  options: DetectionOptions = {}
): ComparisonResult {
  const oldSpec = parseSpec(oldContent, filename);
  const newSpec = parseSpec(newContent, filename);

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

/**
 * Detects breaking changes from parsed specs
 */
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

/**
 * Filters comparison result by severity threshold
 */
function filterBySeverity(
  result: ComparisonResult,
  threshold: ChangeSeverity
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

/**
 * Quick check if there are any breaking changes
 */
export function hasBreakingChanges(
  oldContent: string,
  newContent: string,
  filename: string
): boolean {
  const result = detectBreakingChanges(oldContent, newContent, filename);
  return result.breakingChanges.length > 0;
}

/**
 * Get summary of breaking changes
 */
export function getBreakingChangesSummary(
  oldContent: string,
  newContent: string,
  filename: string
): {
  count: number;
  riskLevel: string;
  recommendation: string;
  changes: string[];
} {
  const result = detectBreakingChanges(oldContent, newContent, filename);

  return {
    count: result.breakingChanges.length,
    riskLevel: result.riskLevel,
    recommendation: result.semverRecommendation,
    changes: result.breakingChanges.map((c) => c.description),
  };
}
