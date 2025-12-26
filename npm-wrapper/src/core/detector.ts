import { ApiSpec, ComparisonResult } from '../types';
import { parseSpec } from './parser';
import { compareSpecs } from './comparator';
import { readFile, fileExists } from '../utils/file';

/**
 * Options for breaking change detection
 */
export interface DetectionOptions {
  /** Minimum severity to include */
  severityThreshold?: 'INFO' | 'WARNING' | 'DANGEROUS' | 'BREAKING';
  /** Include deprecated items */
  includeDeprecations?: boolean;
}

/**
 * Detects breaking changes between two API specification files.
 *
 * @param oldSpecPath - Path to the old/base specification file
 * @param newSpecPath - Path to the new/head specification file
 * @param options - Detection options
 * @returns Comparison result
 */
export function detectBreakingChanges(
  oldSpecPath: string,
  newSpecPath: string,
  options: DetectionOptions = {}
): ComparisonResult {
  // Validate files exist
  if (!fileExists(oldSpecPath)) {
    throw new Error(`File not found: ${oldSpecPath}`);
  }
  if (!fileExists(newSpecPath)) {
    throw new Error(`File not found: ${newSpecPath}`);
  }

  // Read and parse specifications
  const oldContent = readFile(oldSpecPath);
  const newContent = readFile(newSpecPath);

  const oldSpec = parseSpec(oldContent, oldSpecPath);
  const newSpec = parseSpec(newContent, newSpecPath);

  // Compare specifications
  let result = compareSpecs(oldSpec, newSpec);

  // Apply severity filter if specified
  if (options.severityThreshold) {
    result = filterBySeverity(result, options.severityThreshold);
  }

  // Filter out deprecations if not included
  if (options.includeDeprecations === false) {
    result = {
      ...result,
      changes: result.changes.filter((c) => c.type !== 'DEPRECATED'),
    };
  }

  return result;
}

/**
 * Detects breaking changes from parsed API specifications.
 *
 * @param oldSpec - Old/base API specification
 * @param newSpec - New/head API specification
 * @param options - Detection options
 * @returns Comparison result
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

/**
 * Quick check if there are any breaking changes.
 *
 * @param oldSpecPath - Path to the old specification
 * @param newSpecPath - Path to the new specification
 * @returns true if breaking changes detected
 */
export function hasBreakingChanges(oldSpecPath: string, newSpecPath: string): boolean {
  const result = detectBreakingChanges(oldSpecPath, newSpecPath);
  return result.breakingChanges.length > 0;
}

/**
 * Get summary of breaking changes.
 *
 * @param oldSpecPath - Path to the old specification
 * @param newSpecPath - Path to the new specification
 * @returns Summary object
 */
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
