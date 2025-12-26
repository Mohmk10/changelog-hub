import { ApiSpec, ComparisonResult } from '../types';
export interface DetectionOptions {
    severityThreshold?: 'INFO' | 'WARNING' | 'DANGEROUS' | 'BREAKING';
    includeDeprecations?: boolean;
}
export declare function detectBreakingChanges(oldSpecPath: string, newSpecPath: string, options?: DetectionOptions): ComparisonResult;
export declare function detectBreakingChangesFromSpecs(oldSpec: ApiSpec, newSpec: ApiSpec, options?: DetectionOptions): ComparisonResult;
export declare function hasBreakingChanges(oldSpecPath: string, newSpecPath: string): boolean;
export declare function getBreakingChangesSummary(oldSpecPath: string, newSpecPath: string): {
    count: number;
    riskLevel: string;
    recommendation: string;
    changes: string[];
};
//# sourceMappingURL=detector.d.ts.map