import { ChangelogResult } from '../changelog/detector';
/**
 * Creates a new GitHub Check Run with changelog results.
 *
 * @param token - GitHub token for API access
 * @param result - Changelog detection results
 * @returns Check run ID
 */
export declare function createCheckRun(token: string, result: ChangelogResult): Promise<number>;
/**
 * Updates an existing GitHub Check Run.
 *
 * @param token - GitHub token for API access
 * @param checkRunId - ID of the check run to update
 * @param result - Changelog detection results
 */
export declare function updateCheckRun(token: string, checkRunId: number, result: ChangelogResult): Promise<void>;
/**
 * Creates an in-progress check run.
 *
 * @param token - GitHub token for API access
 * @returns Check run ID
 */
export declare function createInProgressCheckRun(token: string): Promise<number>;
/**
 * Finds an existing Changelog Hub check run for the current SHA.
 *
 * @param token - GitHub token for API access
 * @returns Check run ID if found, null otherwise
 */
export declare function findExistingCheckRun(token: string): Promise<number | null>;
/**
 * Creates a summary check run for multiple spec analyses.
 */
export declare function createSummaryCheckRun(token: string, results: ChangelogResult[]): Promise<number>;
//# sourceMappingURL=check.d.ts.map