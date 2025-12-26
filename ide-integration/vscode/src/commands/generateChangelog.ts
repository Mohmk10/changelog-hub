import * as vscode from 'vscode';
import * as path from 'path';
import { execSync } from 'child_process';
import { parseSpec } from '../core/parser';
import { compareSpecs } from '../core/comparator';
import { generateReport } from '../core/reporter';
import { Logger } from '../utils/logger';
import { getConfig, getSpecPatterns } from '../utils/config';
import { ChangelogProvider } from '../views/changelogView';
import { ComparisonResult } from '../types';

export async function generateChangelogCommand(
  changelogProvider?: ChangelogProvider
): Promise<void> {
  try {
    
    const workspaceFolders = vscode.workspace.workspaceFolders;
    if (!workspaceFolders || workspaceFolders.length === 0) {
      vscode.window.showErrorMessage('Changelog Hub: No workspace folder open');
      return;
    }

    const workspaceFolder = workspaceFolders[0];

    const specPatterns = getSpecPatterns();
    const specFiles: vscode.Uri[] = [];

    for (const pattern of specPatterns) {
      const files = await vscode.workspace.findFiles(pattern, '**/node_modules/**');
      specFiles.push(...files);
    }

    if (specFiles.length === 0) {
      vscode.window.showWarningMessage(
        'Changelog Hub: No API spec files found in workspace'
      );
      return;
    }

    const specItems = specFiles.map((file) => ({
      label: path.basename(file.fsPath),
      description: vscode.workspace.asRelativePath(file),
      uri: file,
    }));

    const selected = await vscode.window.showQuickPick(specItems, {
      placeHolder: 'Select API spec to generate changelog for',
    });

    if (!selected) {
      return;
    }

    const refOptions = await getGitRefs(workspaceFolder.uri.fsPath);

    if (refOptions.length < 2) {
      vscode.window.showWarningMessage(
        'Changelog Hub: Need at least 2 Git refs (tags/branches) to generate changelog'
      );
      return;
    }

    const fromRef = await vscode.window.showQuickPick(refOptions, {
      placeHolder: 'Select FROM version (older)',
    });

    if (!fromRef) {
      return;
    }

    const toOptions = refOptions.filter((r) => r.label !== fromRef.label);
    const toRef = await vscode.window.showQuickPick(toOptions, {
      placeHolder: 'Select TO version (newer)',
    });

    if (!toRef) {
      return;
    }

    await vscode.window.withProgress(
      {
        location: vscode.ProgressLocation.Notification,
        title: 'Generating changelog...',
        cancellable: false,
      },
      async (progress) => {
        const relativePath = vscode.workspace.asRelativePath(selected.uri);

        progress.report({ increment: 20, message: `Getting ${fromRef.label}...` });

        let oldContent: string;
        try {
          oldContent = execSync(`git show ${fromRef.label}:${relativePath}`, {
            cwd: workspaceFolder.uri.fsPath,
            encoding: 'utf-8',
          });
        } catch {
          throw new Error(`Could not get file from ${fromRef.label}`);
        }

        progress.report({ increment: 20, message: `Getting ${toRef.label}...` });

        let newContent: string;
        try {
          newContent = execSync(`git show ${toRef.label}:${relativePath}`, {
            cwd: workspaceFolder.uri.fsPath,
            encoding: 'utf-8',
          });
        } catch {
          throw new Error(`Could not get file from ${toRef.label}`);
        }

        progress.report({ increment: 20, message: 'Comparing versions...' });

        const oldSpec = parseSpec(oldContent, selected.uri.fsPath);
        const newSpec = parseSpec(newContent, selected.uri.fsPath);
        const result = compareSpecs(oldSpec, newSpec);

        result.fromVersion = fromRef.label;
        result.toVersion = toRef.label;

        progress.report({ increment: 20, message: 'Generating changelog...' });

        if (changelogProvider) {
          changelogProvider.setChanges(result.changes);
        }

        const config = getConfig();
        const report = generateReport(result, config.defaultFormat);

        progress.report({ increment: 20 });

        const action = await vscode.window.showQuickPick(
          [
            { label: 'Preview', description: 'Open in editor' },
            { label: 'Save to CHANGELOG.md', description: 'Save to file' },
            { label: 'Append to CHANGELOG.md', description: 'Append to existing file' },
          ],
          { placeHolder: 'What would you like to do with the changelog?' }
        );

        if (!action) {
          return;
        }

        if (action.label === 'Preview') {
          await showPreview(report, config.defaultFormat);
        } else if (action.label === 'Save to CHANGELOG.md') {
          await saveChangelog(workspaceFolder.uri, report, false);
        } else {
          await saveChangelog(workspaceFolder.uri, report, true);
        }

        showSummary(result, fromRef.label, toRef.label);
      }
    );
  } catch (error) {
    if (error instanceof Error) {
      Logger.error(`Generate changelog failed: ${error.message}`);
      vscode.window.showErrorMessage(`Changelog Hub: ${error.message}`);
    }
  }
}

async function getGitRefs(
  cwd: string
): Promise<Array<{ label: string; description: string }>> {
  const refs: Array<{ label: string; description: string }> = [];

  try {
    
    const tags = execSync('git tag --sort=-v:refname', { cwd, encoding: 'utf-8' })
      .split('\n')
      .filter((t) => t.trim());

    for (const tag of tags.slice(0, 10)) {
      refs.push({ label: tag, description: 'Tag' });
    }

    const branches = execSync('git branch -a --format="%(refname:short)"', {
      cwd,
      encoding: 'utf-8',
    })
      .split('\n')
      .filter((b) => b.trim() && !b.includes('HEAD'));

    for (const branch of branches.slice(0, 5)) {
      refs.push({ label: branch, description: 'Branch' });
    }

    refs.push({ label: 'HEAD', description: 'Current commit' });
    refs.push({ label: 'HEAD~1', description: 'Previous commit' });
  } catch {
    
  }

  return refs;
}

async function showPreview(report: string, format: string): Promise<void> {
  const doc = await vscode.workspace.openTextDocument({
    content: report,
    language: format === 'json' ? 'json' : format === 'html' ? 'html' : 'markdown',
  });

  await vscode.window.showTextDocument(doc, {
    preview: true,
    viewColumn: vscode.ViewColumn.One,
  });
}

async function saveChangelog(
  workspaceUri: vscode.Uri,
  report: string,
  append: boolean
): Promise<void> {
  const changelogUri = vscode.Uri.joinPath(workspaceUri, 'CHANGELOG.md');

  let content = report;

  if (append) {
    try {
      const existing = (await vscode.workspace.fs.readFile(changelogUri)).toString();
      content = report + '\n\n---\n\n' + existing;
    } catch {
      
    }
  }

  await vscode.workspace.fs.writeFile(changelogUri, Buffer.from(content));

  const doc = await vscode.workspace.openTextDocument(changelogUri);
  await vscode.window.showTextDocument(doc);

  vscode.window.showInformationMessage('Changelog Hub: CHANGELOG.md saved');
}

function showSummary(result: ComparisonResult, fromRef: string, toRef: string): void {
  const breakingCount = result.breakingChanges.length;
  const message = breakingCount > 0
    ? `${breakingCount} breaking change(s) between ${fromRef} and ${toRef}. Recommended: ${result.semverRecommendation}`
    : `No breaking changes between ${fromRef} and ${toRef}. ${result.changes.length} change(s) found.`;

  if (breakingCount > 0) {
    vscode.window.showWarningMessage(`Changelog Hub: ${message}`);
  } else {
    vscode.window.showInformationMessage(`Changelog Hub: ${message}`);
  }
}
