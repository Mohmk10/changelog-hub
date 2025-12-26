import * as vscode from 'vscode';
import { parseSpec } from '../core/parser';
import { isInlineWarningsEnabled } from '../utils/config';
import { Logger } from '../utils/logger';

/**
 * Provides diagnostics for API specification files
 */
export class DiagnosticProvider {
  constructor(private diagnosticCollection: vscode.DiagnosticCollection) {}

  updateDiagnostics(document: vscode.TextDocument): void {
    if (!this.isApiSpec(document)) {
      return;
    }

    if (!isInlineWarningsEnabled()) {
      this.diagnosticCollection.delete(document.uri);
      return;
    }

    const diagnostics: vscode.Diagnostic[] = [];

    try {
      const content = document.getText();
      const spec = parseSpec(content, document.fileName);

      // Check for deprecated endpoints
      for (const endpoint of spec.endpoints) {
        if (endpoint.deprecated) {
          const range = this.findRange(document, endpoint.path);
          if (range) {
            const diagnostic = new vscode.Diagnostic(
              range,
              `Deprecated endpoint: ${endpoint.method} ${endpoint.path}`,
              vscode.DiagnosticSeverity.Warning
            );
            diagnostic.code = 'deprecated-endpoint';
            diagnostic.source = 'Changelog Hub';
            diagnostic.tags = [vscode.DiagnosticTag.Deprecated];
            diagnostics.push(diagnostic);
          }
        }

        // Check for missing descriptions
        if (!endpoint.description && !endpoint.summary) {
          const range = this.findRange(document, endpoint.path);
          if (range) {
            const diagnostic = new vscode.Diagnostic(
              range,
              `Missing description for ${endpoint.method} ${endpoint.path}`,
              vscode.DiagnosticSeverity.Information
            );
            diagnostic.code = 'missing-description';
            diagnostic.source = 'Changelog Hub';
            diagnostics.push(diagnostic);
          }
        }

        // Check for missing response definitions
        if (endpoint.responses.length === 0) {
          const range = this.findRange(document, endpoint.path);
          if (range) {
            const diagnostic = new vscode.Diagnostic(
              range,
              `No response definitions for ${endpoint.method} ${endpoint.path}`,
              vscode.DiagnosticSeverity.Warning
            );
            diagnostic.code = 'missing-responses';
            diagnostic.source = 'Changelog Hub';
            diagnostics.push(diagnostic);
          }
        }
      }

      // Check schemas
      for (const schema of spec.schemas) {
        if (schema.properties.length === 0 && schema.type === 'object') {
          const range = this.findRange(document, schema.name);
          if (range) {
            const diagnostic = new vscode.Diagnostic(
              range,
              `Schema '${schema.name}' has no properties defined`,
              vscode.DiagnosticSeverity.Information
            );
            diagnostic.code = 'empty-schema';
            diagnostic.source = 'Changelog Hub';
            diagnostics.push(diagnostic);
          }
        }
      }
    } catch (error) {
      // Parse error
      const message = error instanceof Error ? error.message : 'Unknown error';
      if (!message.includes('Unsupported') && !message.includes('Unknown')) {
        const diagnostic = new vscode.Diagnostic(
          new vscode.Range(0, 0, 0, 1),
          `Failed to parse API spec: ${message}`,
          vscode.DiagnosticSeverity.Error
        );
        diagnostic.source = 'Changelog Hub';
        diagnostics.push(diagnostic);
      }
    }

    this.diagnosticCollection.set(document.uri, diagnostics);
  }

  private isApiSpec(document: vscode.TextDocument): boolean {
    const ext = document.fileName.split('.').pop()?.toLowerCase();
    if (!['yaml', 'yml', 'json', 'graphql', 'gql', 'proto'].includes(ext || '')) {
      return false;
    }

    // Quick content check
    const text = document.getText().substring(0, 500);
    return (
      text.includes('openapi') ||
      text.includes('swagger') ||
      text.includes('asyncapi') ||
      text.includes('type Query') ||
      text.includes('service ')
    );
  }

  private findRange(
    document: vscode.TextDocument,
    searchText: string
  ): vscode.Range | undefined {
    const text = document.getText();
    const index = text.indexOf(searchText);
    if (index >= 0) {
      const position = document.positionAt(index);
      return new vscode.Range(position, position.translate(0, searchText.length));
    }
    return undefined;
  }
}
