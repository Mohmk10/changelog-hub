import * as core from '@actions/core';

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
export function getInputs(): ActionInputs {
  const format = core.getInput('format') || 'markdown';
  if (!isValidFormat(format)) {
    throw new Error(`Invalid format: ${format}. Must be one of: console, markdown, json`);
  }

  const severityThreshold = core.getInput('severity-threshold') || 'INFO';
  if (!isValidSeverity(severityThreshold)) {
    throw new Error(
      `Invalid severity-threshold: ${severityThreshold}. Must be one of: INFO, WARNING, DANGEROUS, BREAKING`
    );
  }

  const includePatterns = core.getInput('include-patterns');
  const excludePatterns = core.getInput('exclude-patterns');

  return {
    oldSpec: core.getInput('old-spec') || undefined,
    newSpec: core.getInput('new-spec') || undefined,
    specPath: core.getInput('spec-path') || 'api/openapi.yaml',
    baseRef: core.getInput('base-ref') || process.env.GITHUB_BASE_REF || 'main',
    headRef: core.getInput('head-ref') || process.env.GITHUB_HEAD_REF || 'HEAD',
    format: format as OutputFormat,
    failOnBreaking: getBooleanInput('fail-on-breaking', true),
    commentOnPr: getBooleanInput('comment-on-pr', true),
    createCheck: getBooleanInput('create-check', true),
    githubToken: core.getInput('github-token') || process.env.GITHUB_TOKEN || '',
    includePatterns: includePatterns ? parsePatterns(includePatterns) : undefined,
    excludePatterns: excludePatterns ? parsePatterns(excludePatterns) : undefined,
    severityThreshold: severityThreshold as SeverityThreshold,
  };
}

/**
 * Gets a boolean input with a default value
 */
function getBooleanInput(name: string, defaultValue: boolean): boolean {
  const input = core.getInput(name);
  if (!input) {
    return defaultValue;
  }
  return input.toLowerCase() === 'true';
}

/**
 * Validates if the format is a valid output format
 */
function isValidFormat(format: string): format is OutputFormat {
  return ['console', 'markdown', 'json'].includes(format);
}

/**
 * Validates if the severity is a valid severity threshold
 */
function isValidSeverity(severity: string): severity is SeverityThreshold {
  return ['INFO', 'WARNING', 'DANGEROUS', 'BREAKING'].includes(severity);
}

/**
 * Parses a comma or newline separated list of patterns
 */
function parsePatterns(input: string): string[] {
  return input
    .split(/[,\n]/)
    .map((p) => p.trim())
    .filter((p) => p.length > 0);
}

/**
 * Validates that required inputs are present for the action to run
 */
export function validateInputs(inputs: ActionInputs): void {
  if (!inputs.specPath && !inputs.oldSpec) {
    throw new Error('Either spec-path or old-spec must be provided');
  }

  if (!inputs.githubToken && (inputs.commentOnPr || inputs.createCheck)) {
    core.warning(
      'github-token is required for PR comments and check runs. These features will be disabled.'
    );
  }
}
