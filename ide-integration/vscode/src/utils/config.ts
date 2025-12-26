import * as vscode from 'vscode';
import { ExtensionConfig, OutputFormat, ChangeSeverity } from '../types';

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

export async function updateConfig<T>(
  key: keyof ExtensionConfig,
  value: T,
  global: boolean = false
): Promise<void> {
  const config = vscode.workspace.getConfiguration('changelogHub');
  await config.update(key, value, global);
}

export function getDefaultGitRef(): string {
  return getConfig().baseRef;
}

export function getSpecPatterns(): string[] {
  return getConfig().specPatterns;
}

export function isInlineWarningsEnabled(): boolean {
  return getConfig().showInlineWarnings;
}

export function getSeverityThreshold(): ChangeSeverity {
  return getConfig().severityThreshold;
}
