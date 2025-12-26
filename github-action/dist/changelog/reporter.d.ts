import { ComparisonResult } from './comparator';
import { ApiSpec } from './parser';
/**
 * Report generation options
 */
export interface ReportOptions {
    /** Old API spec info */
    oldSpec: ApiSpec;
    /** New API spec info */
    newSpec: ApiSpec;
    /** Include migration suggestions */
    includeMigration?: boolean;
    /** Include timestamps */
    includeTimestamp?: boolean;
}
/**
 * Generates a report from comparison results in the specified format.
 *
 * @param result - Comparison result
 * @param format - Output format (console, markdown, json)
 * @param options - Additional options for report generation
 * @returns Formatted report string
 */
export declare function generateReport(result: ComparisonResult, format: string, options: ReportOptions): string;
/**
 * Generates a short summary suitable for PR titles or commit messages
 */
export declare function generateShortSummary(result: ComparisonResult): string;
/**
 * Generates a release notes section
 */
export declare function generateReleaseNotes(result: ComparisonResult, options: ReportOptions): string;
//# sourceMappingURL=reporter.d.ts.map