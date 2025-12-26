import { ComparisonResult } from './comparator';
import { ApiSpec } from './parser';
export interface ReportOptions {
    oldSpec: ApiSpec;
    newSpec: ApiSpec;
    includeMigration?: boolean;
    includeTimestamp?: boolean;
}
export declare function generateReport(result: ComparisonResult, format: string, options: ReportOptions): string;
export declare function generateShortSummary(result: ComparisonResult): string;
export declare function generateReleaseNotes(result: ComparisonResult, options: ReportOptions): string;
//# sourceMappingURL=reporter.d.ts.map