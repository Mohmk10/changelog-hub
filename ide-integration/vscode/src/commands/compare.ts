import * as vscode from 'vscode';
import { parseSpec } from '../core/parser';
import { compareSpecs } from '../core/comparator';
import { generateReport } from '../core/reporter';
import { Logger } from '../utils/logger';
import { getConfig } from '../utils/config';
import { BreakingChangesProvider } from '../views/breakingChangesView';
import { ChangelogProvider } from '../views/changelogView';
import { ComparisonResult } from '../types';

export async function compareCommand(
  uri?: vscode.Uri,
  breakingChangesProvider?: BreakingChangesProvider,
  changelogProvider?: ChangelogProvider
): Promise<void> {
  try {
    
    const oldFiles = await vscode.window.showOpenDialog({
      canSelectMany: false,
      openLabel: 'Select OLD API Spec (base)',
      filters: {
        'API Specs': ['yaml', 'yml', 'json', 'graphql', 'gql', 'proto'],
      },
    });

    if (!oldFiles || oldFiles.length === 0) {
      return;
    }

    let newUri = uri;
    if (!newUri) {
      
      const activeEditor = vscode.window.activeTextEditor;
      if (activeEditor && isApiSpec(activeEditor.document.uri)) {
        const useActive = await vscode.window.showQuickPick(['Yes', 'No'], {
          placeHolder: `Use current file (${activeEditor.document.fileName.split('/').pop()}) as the new spec?`,
        });
        if (useActive === 'Yes') {
          newUri = activeEditor.document.uri;
        }
      }

      if (!newUri) {
        const newFiles = await vscode.window.showOpenDialog({
          canSelectMany: false,
          openLabel: 'Select NEW API Spec (head)',
          filters: {
            'API Specs': ['yaml', 'yml', 'json', 'graphql', 'gql', 'proto'],
          },
        });

        if (!newFiles || newFiles.length === 0) {
          return;
        }
        newUri = newFiles[0];
      }
    }

    await vscode.window.withProgress(
      {
        location: vscode.ProgressLocation.Notification,
        title: 'Comparing API specifications...',
        cancellable: false,
      },
      async (progress) => {
        progress.report({ increment: 0 });

        const oldContent = (await vscode.workspace.fs.readFile(oldFiles[0])).toString();
        const newContent = (await vscode.workspace.fs.readFile(newUri!)).toString();

        progress.report({ increment: 30, message: 'Parsing specifications...' });

        const oldSpec = parseSpec(oldContent, oldFiles[0].fsPath);
        const newSpec = parseSpec(newContent, newUri!.fsPath);

        progress.report({ increment: 30, message: 'Comparing...' });

        const result = compareSpecs(oldSpec, newSpec);

        progress.report({ increment: 30, message: 'Generating report...' });

        const config = getConfig();
        const format = config.defaultFormat;
        const report = generateReport(result, format);

        progress.report({ increment: 10 });

        if (breakingChangesProvider) {
          breakingChangesProvider.setBreakingChanges(result.breakingChanges);
        }
        if (changelogProvider) {
          changelogProvider.setChanges(result.changes);
        }

        await showResults(result, report, format);
      }
    );
  } catch (error) {
    if (error instanceof Error) {
      Logger.error(`Compare failed: ${error.message}`);
      vscode.window.showErrorMessage(`Changelog Hub: ${error.message}`);
    }
  }
}

async function showResults(
  result: ComparisonResult,
  report: string,
  format: string
): Promise<void> {
  
  const doc = await vscode.workspace.openTextDocument({
    content: report,
    language: format === 'json' ? 'json' : format === 'html' ? 'html' : 'markdown',
  });

  await vscode.window.showTextDocument(doc, {
    preview: true,
    viewColumn: vscode.ViewColumn.Beside,
  });

  const breakingCount = result.breakingChanges.length;
  if (breakingCount > 0) {
    const action = await vscode.window.showWarningMessage(
      `Changelog Hub: ${breakingCount} breaking change(s) detected! Risk: ${result.riskLevel}`,
      'View Details',
      'Dismiss'
    );
    if (action === 'View Details') {
      vscode.commands.executeCommand('changelogHub.breakingChanges.focus');
    }
  } else {
    vscode.window.showInformationMessage(
      `Changelog Hub: No breaking changes. ${result.changes.length} change(s) found. Recommended bump: ${result.semverRecommendation}`
    );
  }
}

function isApiSpec(uri: vscode.Uri): boolean {
  const ext = uri.fsPath.split('.').pop()?.toLowerCase();
  return ['yaml', 'yml', 'json', 'graphql', 'gql', 'proto'].includes(ext || '');
}
