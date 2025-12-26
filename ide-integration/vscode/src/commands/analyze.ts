import * as vscode from 'vscode';
import { parseSpec } from '../core/parser';
import { Logger } from '../utils/logger';
import { ApiSpec } from '../types';

export async function analyzeCommand(uri?: vscode.Uri): Promise<void> {
  try {
    
    let fileUri = uri;
    if (!fileUri) {
      const editor = vscode.window.activeTextEditor;
      if (editor) {
        fileUri = editor.document.uri;
      } else {
        const files = await vscode.window.showOpenDialog({
          canSelectMany: false,
          openLabel: 'Select API Spec to Analyze',
          filters: {
            'API Specs': ['yaml', 'yml', 'json', 'graphql', 'gql', 'proto'],
          },
        });
        if (!files || files.length === 0) {
          return;
        }
        fileUri = files[0];
      }
    }

    await vscode.window.withProgress(
      {
        location: vscode.ProgressLocation.Notification,
        title: 'Analyzing API specification...',
        cancellable: false,
      },
      async (progress) => {
        progress.report({ increment: 30 });

        const content = (await vscode.workspace.fs.readFile(fileUri!)).toString();

        progress.report({ increment: 40, message: 'Parsing...' });

        const spec = parseSpec(content, fileUri!.fsPath);

        progress.report({ increment: 30 });

        await showAnalysis(spec, fileUri!);
      }
    );
  } catch (error) {
    if (error instanceof Error) {
      Logger.error(`Analyze failed: ${error.message}`);
      vscode.window.showErrorMessage(`Changelog Hub: ${error.message}`);
    }
  }
}

async function showAnalysis(spec: ApiSpec, uri: vscode.Uri): Promise<void> {
  const lines: string[] = [];

  lines.push('# API Specification Analysis');
  lines.push('');
  lines.push(`**File:** ${uri.fsPath.split('/').pop()}`);
  lines.push('');
  lines.push('## Overview');
  lines.push('');
  lines.push(`| Property | Value |`);
  lines.push(`|----------|-------|`);
  lines.push(`| Name | ${spec.name} |`);
  lines.push(`| Version | ${spec.version} |`);
  lines.push(`| Type | ${spec.type.toUpperCase()} |`);
  lines.push(`| Endpoints | ${spec.endpoints.length} |`);
  lines.push(`| Schemas | ${spec.schemas.length} |`);
  lines.push(`| Security Schemes | ${spec.security.length} |`);
  lines.push('');

  if (spec.endpoints.length > 0) {
    lines.push('## Endpoints');
    lines.push('');
    lines.push('| Method | Path | Summary | Deprecated |');
    lines.push('|--------|------|---------|------------|');

    for (const endpoint of spec.endpoints) {
      const deprecated = endpoint.deprecated ? '⚠️ Yes' : 'No';
      const summary = endpoint.summary || endpoint.description || '-';
      lines.push(`| ${endpoint.method} | \`${endpoint.path}\` | ${summary.substring(0, 50)} | ${deprecated} |`);
    }
    lines.push('');
  }

  if (spec.schemas.length > 0) {
    lines.push('## Schemas');
    lines.push('');

    for (const schema of spec.schemas) {
      lines.push(`### ${schema.name}`);
      lines.push('');
      if (schema.description) {
        lines.push(`${schema.description}`);
        lines.push('');
      }
      lines.push(`| Property | Type | Required |`);
      lines.push(`|----------|------|----------|`);

      for (const prop of schema.properties) {
        const required = prop.required ? '✓' : '';
        lines.push(`| ${prop.name} | ${prop.type} | ${required} |`);
      }
      lines.push('');
    }
  }

  if (spec.security.length > 0) {
    lines.push('## Security Schemes');
    lines.push('');
    lines.push('| Name | Type | Description |');
    lines.push('|------|------|-------------|');

    for (const sec of spec.security) {
      const desc = sec.description || '-';
      lines.push(`| ${sec.name} | ${sec.type} | ${desc.substring(0, 50)} |`);
    }
    lines.push('');
  }

  lines.push('## Statistics');
  lines.push('');

  const deprecatedCount = spec.endpoints.filter((e) => e.deprecated).length;
  const paramsCount = spec.endpoints.reduce((sum, e) => sum + e.parameters.length, 0);
  const requiredParams = spec.endpoints.reduce(
    (sum, e) => sum + e.parameters.filter((p) => p.required).length,
    0
  );

  lines.push(`- **Deprecated endpoints:** ${deprecatedCount}`);
  lines.push(`- **Total parameters:** ${paramsCount}`);
  lines.push(`- **Required parameters:** ${requiredParams}`);
  lines.push(`- **Total schema properties:** ${spec.schemas.reduce((sum, s) => sum + s.properties.length, 0)}`);
  lines.push('');

  lines.push('---');
  lines.push(`*Analyzed at ${new Date().toISOString()} by Changelog Hub*`);

  const doc = await vscode.workspace.openTextDocument({
    content: lines.join('\n'),
    language: 'markdown',
  });

  await vscode.window.showTextDocument(doc, {
    preview: true,
    viewColumn: vscode.ViewColumn.Beside,
  });
}
