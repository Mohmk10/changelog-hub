import { ChangelogResult } from './changelog/detector';
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
export declare function setOutputs(result: ChangelogResult): void;
export declare function writeSummary(result: ChangelogResult): Promise<void>;
export declare function logSummary(result: ChangelogResult): void;
