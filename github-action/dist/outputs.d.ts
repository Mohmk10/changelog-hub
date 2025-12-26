import { ChangelogResult } from './changelog/detector';
/**
 * Output keys for the GitHub Action
 */
export declare enum OutputKey {
    HAS_BREAKING_CHANGES = "has-breaking-changes",
    BREAKING_CHANGES_COUNT = "breaking-changes-count",
    TOTAL_CHANGES_COUNT = "total-changes-count",
    RISK_LEVEL = "risk-level",
    RISK_SCORE = "risk-score",
    SEMVER_RECOMMENDATION = "semver-recommendation",
    CHANGELOG = "changelog",
    CHANGELOG_FILE = "changelog-file",
    SUMMARY = "summary"
}
/**
 * Sets all output values from the changelog result.
 * These outputs can be used by subsequent steps in the workflow.
 *
 * @param result - The changelog detection result
 */
export declare function setOutputs(result: ChangelogResult): void;
/**
 * Writes the changelog to the GitHub Actions summary
 */
export declare function writeSummary(result: ChangelogResult): Promise<void>;
/**
 * Logs the result summary to the action output
 */
export declare function logSummary(result: ChangelogResult): void;
