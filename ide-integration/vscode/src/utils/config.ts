import * as vscode from 'vscode';
import { ExtensionConfig, OutputFormat, ChangeSeverity } from '../types';

/**
 * Get extension configuration
 */
export function getConfig(): ExtensionConfig {
  const config = vscode.workspace.getConfiguration('changelogHub');

  return {
    defaultFormat: config.get<OutputFormat>('defaultFormat', 'markdown'),
    autoDetectSpecs: config.get<boolean>('autoDetectSpecs', true),
    specPatterns: config.get<string[]>('specPatterns', [
      '**/openapi.yaml',
      '**/swagger.yaml',
      '**/api.yaml',
      '**/*.graphql',
      '**/*.proto',
    ]),
    showInlineWarnings: config.get<boolean>('showInlineWarnings', true),
    baseRef: config.get<string>('baseRef', 'main'),
    severityThreshold: config.get<ChangeSeverity>('severityThreshold', 'INFO'),
  };
}

/**
 * Update a configuration value
 */
export async function updateConfig<T>(
  key: keyof ExtensionConfig,
  value: T,
  global: boolean = false
): Promise<void> {
  const config = vscode.workspace.getConfiguration('changelogHub');
  await config.update(key, value, global);
}

/**
 * Get the default Git ref for comparison
 */
export function getDefaultGitRef(): string {
  return getConfig().baseRef;
}

/**
 * Get spec file patterns
 */
export function getSpecPatterns(): string[] {
  return getConfig().specPatterns;
}

/**
 * Check if inline warnings are enabled
 */
export function isInlineWarningsEnabled(): boolean {
  return getConfig().showInlineWarnings;
}

/**
 * Get severity threshold
 */
export function getSeverityThreshold(): ChangeSeverity {
  return getConfig().severityThreshold;
}
