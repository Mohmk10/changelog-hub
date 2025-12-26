import { ApiSpec } from './parser';
import { Change, BreakingChange } from './detector';
/**
 * Result of comparing two API specifications
 */
export interface ComparisonResult {
    /** All detected changes */
    changes: Change[];
    /** Breaking changes only */
    breakingChanges: BreakingChange[];
    /** Calculated risk score */
    riskScore: number;
    /** Summary statistics */
    summary: ComparisonSummary;
}
/**
 * Summary statistics for the comparison
 */
export interface ComparisonSummary {
    endpointsAdded: number;
    endpointsRemoved: number;
    endpointsModified: number;
    endpointsDeprecated: number;
    schemasAdded: number;
    schemasRemoved: number;
    schemasModified: number;
    parametersAdded: number;
    parametersRemoved: number;
    parametersModified: number;
}
/**
 * Compares two API specifications and identifies all changes.
 *
 * @param oldSpec - The previous/base API specification
 * @param newSpec - The new/head API specification
 * @returns Comparison result with all changes
 */
export declare function compareSpecs(oldSpec: ApiSpec, newSpec: ApiSpec): ComparisonResult;
//# sourceMappingURL=comparator.d.ts.map