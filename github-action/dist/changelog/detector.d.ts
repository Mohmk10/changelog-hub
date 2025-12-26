import { ActionInputs } from '../inputs';
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
    oldSpec: {
        name: string;
        version: string;
    };
    newSpec: {
        name: string;
        version: string;
    };
}
export declare function detectBreakingChanges(inputs: ActionInputs): Promise<ChangelogResult>;
export declare function detectBreakingChangesMultiple(inputs: ActionInputs, specPaths: string[]): Promise<ChangelogResult[]>;
export declare function aggregateResults(results: ChangelogResult[]): ChangelogResult;
//# sourceMappingURL=detector.d.ts.map