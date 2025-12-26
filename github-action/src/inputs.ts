import * as core from '@actions/core';

export type OutputFormat = 'console' | 'markdown' | 'json';

export type SeverityThreshold = 'INFO' | 'WARNING' | 'DANGEROUS' | 'BREAKING';

export interface ActionInputs {
  
  oldSpec?: string;
  
  newSpec?: string;
  
  specPath: string;
  
  baseRef: string;
  
  headRef: string;
  
  format: OutputFormat;
  
  failOnBreaking: boolean;
  
  commentOnPr: boolean;
  
  createCheck: boolean;
  
  githubToken: string;
  
  includePatterns?: string[];
  
  excludePatterns?: string[];
  
  severityThreshold: SeverityThreshold;
}

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

function getBooleanInput(name: string, defaultValue: boolean): boolean {
  const input = core.getInput(name);
  if (!input) {
    return defaultValue;
  }
  return input.toLowerCase() === 'true';
}

function isValidFormat(format: string): format is OutputFormat {
  return ['console', 'markdown', 'json'].includes(format);
}

function isValidSeverity(severity: string): severity is SeverityThreshold {
  return ['INFO', 'WARNING', 'DANGEROUS', 'BREAKING'].includes(severity);
}

function parsePatterns(input: string): string[] {
  return input
    .split(/[,\n]/)
    .map((p) => p.trim())
    .filter((p) => p.length > 0);
}

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
