import * as vscode from 'vscode';
import { execSync } from 'child_process';
import { parseSpec } from '../core/parser';
import { compareSpecs } from '../core/comparator';
import { generateReport } from '../core/reporter';
import { Logger } from '../utils/logger';
import { getConfig } from '../utils/config';
import { BreakingChangesProvider } from '../views/breakingChangesView';
import { ChangelogProvider } from '../views/changelogView';

export async function compareWithGitCommand(
  uri?: vscode.Uri,
  breakingChangesProvider?: BreakingChangesProvider,
  changelogProvider?: ChangelogProvider
): Promise<void> {
  try {
    // Get current file
    const editor = vscode.window.activeTextEditor;
    const fileUri = uri || editor?.document.uri;

    if (!fileUri) {
      vscode.window.showErrorMessage('Changelog Hub: No API spec file selected');
      return;
    }

    // Get Git ref
    const config = getConfig();
    const defaultRef = config.baseRef;

    // Show quick pick with common options
    const refOptions = [
      { label: defaultRef, description: 'Default branch' },
      { label: 'HEAD~1', description: 'Previous commit' },
      { label: 'HEAD~5', description: '5 commits ago' },
      { label: 'Custom...', description: 'Enter a custom ref' },
    ];

    const selected = await vscode.window.showQuickPick(refOptions, {
      placeHolder: 'Select Git ref to compare with',
    });

    if (!selected) {
      return;
    }

    let gitRef = selected.label;
    if (gitRef === 'Custom...') {
      const customRef = await vscode.window.showInputBox({
        prompt: 'Enter Git ref to compare with (branch, tag, or commit)',
        placeHolder: 'main, HEAD~1, v1.0.0, abc123',
      });
      if (!customRef) {
        return;
      }
      gitRef = customRef;
    }

    // Get workspace folder
    const workspaceFolder = vscode.workspace.getWorkspaceFolder(fileUri);
    if (!workspaceFolder) {
      vscode.window.showErrorMessage('Changelog Hub: File is not in a workspace');
      return;
    }

    await vscode.window.withProgress(
      {
        location: vscode.ProgressLocation.Notification,
        title: `Comparing with ${gitRef}...`,
        cancellable: false,
      },
      async (progress) => {
        // Get relative path
        const relativePath = vscode.workspace.asRelativePath(fileUri);

        progress.report({ increment: 20, message: 'Getting old version from Git...' });

        // Get old content from Git
        let oldContent: string;
        try {
          oldContent = execSync(`git show ${gitRef}:${relativePath}`, {
            cwd: workspaceFolder.uri.fsPath,
            encoding: 'utf-8',
          });
        } catch (error) {
          throw new Error(
            `Could not get file from ${gitRef}. Make sure the ref exists and the file was present.`
          );
        }

        progress.report({ increment: 20, message: 'Reading current version...' });

        // Get new content
        const newContent = (await vscode.workspace.fs.readFile(fileUri)).toString();

        progress.report({ increment: 20, message: 'Parsing specifications...' });

        // Parse and compare
        const oldSpec = parseSpec(oldContent, fileUri.fsPath);
        const newSpec = parseSpec(newContent, fileUri.fsPath);

        progress.report({ increment: 20, message: 'Comparing...' });

        const result = compareSpecs(oldSpec, newSpec);

        progress.report({ increment: 20 });

        // Update views
        if (breakingChangesProvider) {
          breakingChangesProvider.setBreakingChanges(result.breakingChanges);
        }
        if (changelogProvider) {
          changelogProvider.setChanges(result.changes);
        }

        // Show results
        const format = config.defaultFormat;
        const report = generateReport(result, format);

        const doc = await vscode.workspace.openTextDocument({
          content: report,
          language: format === 'json' ? 'json' : format === 'html' ? 'html' : 'markdown',
        });

        await vscode.window.showTextDocument(doc, {
          preview: true,
          viewColumn: vscode.ViewColumn.Beside,
        });

        // Notification
        const breakingCount = result.breakingChanges.length;
        if (breakingCount > 0) {
          vscode.window.showWarningMessage(
            `Changelog Hub: ${breakingCount} breaking change(s) since ${gitRef}. Risk: ${result.riskLevel}`
          );
        } else {
          vscode.window.showInformationMessage(
            `Changelog Hub: No breaking changes since ${gitRef}. ${result.changes.length} change(s) found.`
          );
        }
      }
    );
  } catch (error) {
    if (error instanceof Error) {
      Logger.error(`Git compare failed: ${error.message}`);
      vscode.window.showErrorMessage(`Changelog Hub: ${error.message}`);
    }
  }
}
