import { ChangelogResult } from '../changelog/detector';
export declare function createCheckRun(token: string, result: ChangelogResult): Promise<number>;
export declare function updateCheckRun(token: string, checkRunId: number, result: ChangelogResult): Promise<void>;
export declare function createInProgressCheckRun(token: string): Promise<number>;
export declare function findExistingCheckRun(token: string): Promise<number | null>;
export declare function createSummaryCheckRun(token: string, results: ChangelogResult[]): Promise<number>;
//# sourceMappingURL=check.d.ts.map