import { ApiSpec } from './parser';
import { Change, BreakingChange } from './detector';
export interface ComparisonResult {
    changes: Change[];
    breakingChanges: BreakingChange[];
    riskScore: number;
    summary: ComparisonSummary;
}
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
export declare function compareSpecs(oldSpec: ApiSpec, newSpec: ApiSpec): ComparisonResult;
//# sourceMappingURL=comparator.d.ts.map