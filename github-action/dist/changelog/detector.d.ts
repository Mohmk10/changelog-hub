import { ActionInputs } from '../inputs';
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
    oldSpec: {
        name: string;
        version: string;
    };
    /** New API spec info */
    newSpec: {
        name: string;
        version: string;
    };
}
/**
 * Detects breaking changes between two API specifications.
 *
 * @param inputs - Action configuration inputs
 * @returns Detection results including changes and recommendations
 */
export declare function detectBreakingChanges(inputs: ActionInputs): Promise<ChangelogResult>;
/**
 * Detects breaking changes for multiple spec files (batch mode)
 */
export declare function detectBreakingChangesMultiple(inputs: ActionInputs, specPaths: string[]): Promise<ChangelogResult[]>;
/**
 * Aggregates multiple changelog results into a single summary
 */
export declare function aggregateResults(results: ChangelogResult[]): ChangelogResult;
//# sourceMappingURL=detector.d.ts.map