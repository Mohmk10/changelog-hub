/**
 * Supported output formats for the changelog
 */
export type OutputFormat = 'console' | 'markdown' | 'json';
/**
 * Severity levels for filtering changes
 */
export type SeverityThreshold = 'INFO' | 'WARNING' | 'DANGEROUS' | 'BREAKING';
/**
 * Configuration inputs for the GitHub Action
 */
export interface ActionInputs {
    /** Path to the old API spec file or Git ref */
    oldSpec?: string;
    /** Path to the new API spec file (defaults to current) */
    newSpec?: string;
    /** Path to the API spec file in the repository */
    specPath: string;
    /** Base Git ref for comparison (branch, tag, or commit) */
    baseRef: string;
    /** Head Git ref for comparison */
    headRef: string;
    /** Output format: console, markdown, json */
    format: OutputFormat;
    /** Fail the action if breaking changes are detected */
    failOnBreaking: boolean;
    /** Post a comment on the PR with the changelog */
    commentOnPr: boolean;
    /** Create a GitHub Check with the results */
    createCheck: boolean;
    /** GitHub token for API access */
    githubToken: string;
    /** Glob patterns for files to include in analysis */
    includePatterns?: string[];
    /** Glob patterns for files to exclude from analysis */
    excludePatterns?: string[];
    /** Minimum severity to report */
    severityThreshold: SeverityThreshold;
}
/**
 * Parses and validates action inputs from the workflow configuration.
 * @returns Validated action inputs
 */
export declare function getInputs(): ActionInputs;
/**
 * Validates that required inputs are present for the action to run
 */
export declare function validateInputs(inputs: ActionInputs): void;
