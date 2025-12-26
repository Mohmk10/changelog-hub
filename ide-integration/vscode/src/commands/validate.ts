import * as vscode from 'vscode';
import { parseSpec, detectSpecType } from '../core/parser';
import { Logger } from '../utils/logger';

interface ValidationResult {
  valid: boolean;
  type: string;
  errors: string[];
  warnings: string[];
}

export async function validateCommand(
  uri?: vscode.Uri,
  diagnosticCollection?: vscode.DiagnosticCollection
): Promise<void> {
  try {
    
    let fileUri = uri;
    if (!fileUri) {
      const editor = vscode.window.activeTextEditor;
      if (editor) {
        fileUri = editor.document.uri;
      } else {
        const files = await vscode.window.showOpenDialog({
          canSelectMany: true,
          openLabel: 'Select API Spec(s) to Validate',
          filters: {
            'API Specs': ['yaml', 'yml', 'json', 'graphql', 'gql', 'proto'],
          },
        });
        if (!files || files.length === 0) {
          return;
        }
        
        for (const file of files) {
          await validateFile(file, diagnosticCollection);
        }
        return;
      }
    }

    await validateFile(fileUri, diagnosticCollection);
  } catch (error) {
    if (error instanceof Error) {
      Logger.error(`Validate failed: ${error.message}`);
      vscode.window.showErrorMessage(`Changelog Hub: ${error.message}`);
    }
  }
}

async function validateFile(
  fileUri: vscode.Uri,
  diagnosticCollection?: vscode.DiagnosticCollection
): Promise<void> {
  await vscode.window.withProgress(
    {
      location: vscode.ProgressLocation.Notification,
      title: `Validating ${fileUri.fsPath.split('/').pop()}...`,
      cancellable: false,
    },
    async (progress) => {
      progress.report({ increment: 30 });

      const content = (await vscode.workspace.fs.readFile(fileUri)).toString();
      const result = validateSpec(content, fileUri.fsPath);

      progress.report({ increment: 70 });

      if (diagnosticCollection) {
        const diagnostics: vscode.Diagnostic[] = [];

        for (const error of result.errors) {
          diagnostics.push(
            new vscode.Diagnostic(
              new vscode.Range(0, 0, 0, 1),
              error,
              vscode.DiagnosticSeverity.Error
            )
          );
        }

        for (const warning of result.warnings) {
          diagnostics.push(
            new vscode.Diagnostic(
              new vscode.Range(0, 0, 0, 1),
              warning,
              vscode.DiagnosticSeverity.Warning
            )
          );
        }

        diagnosticCollection.set(fileUri, diagnostics);
      }

      if (result.valid) {
        if (result.warnings.length > 0) {
          vscode.window.showWarningMessage(
            `Changelog Hub: Valid ${result.type.toUpperCase()} spec with ${result.warnings.length} warning(s)`
          );
        } else {
          vscode.window.showInformationMessage(
            `Changelog Hub: Valid ${result.type.toUpperCase()} specification`
          );
        }
      } else {
        vscode.window.showErrorMessage(
          `Changelog Hub: Invalid specification - ${result.errors.join(', ')}`
        );
      }
    }
  );
}

function validateSpec(content: string, filename: string): ValidationResult {
  const result: ValidationResult = {
    valid: true,
    type: 'unknown',
    errors: [],
    warnings: [],
  };

  try {
    
    result.type = detectSpecType(content, filename);

    if (result.type === 'unknown') {
      result.valid = false;
      result.errors.push('Unable to detect specification type');
      return result;
    }

    const spec = parseSpec(content, filename);

    if (!spec.name || spec.name === 'Untitled API') {
      result.warnings.push('API name not specified');
    }

    if (!spec.version || spec.version === '1.0.0') {
      result.warnings.push('API version not explicitly specified');
    }

    if (spec.endpoints.length === 0) {
      result.warnings.push('No endpoints defined');
    }

    const deprecatedCount = spec.endpoints.filter((e) => e.deprecated).length;
    if (deprecatedCount > 0) {
      result.warnings.push(`${deprecatedCount} deprecated endpoint(s) found`);
    }

    const noDescCount = spec.endpoints.filter(
      (e) => !e.description && !e.summary
    ).length;
    if (noDescCount > 0) {
      result.warnings.push(`${noDescCount} endpoint(s) missing description`);
    }

    const noResponseCount = spec.endpoints.filter(
      (e) => e.responses.length === 0
    ).length;
    if (noResponseCount > 0) {
      result.warnings.push(`${noResponseCount} endpoint(s) missing response definitions`);
    }

    const emptySchemas = spec.schemas.filter(
      (s) => s.properties.length === 0 && s.type === 'object'
    );
    if (emptySchemas.length > 0) {
      result.warnings.push(
        `${emptySchemas.length} schema(s) with no properties: ${emptySchemas.map((s) => s.name).join(', ')}`
      );
    }
  } catch (error) {
    result.valid = false;
    const message = error instanceof Error ? error.message : String(error);
    result.errors.push(`Parse error: ${message}`);
  }

  return result;
}
