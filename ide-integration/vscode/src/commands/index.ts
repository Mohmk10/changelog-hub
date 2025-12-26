import * as vscode from 'vscode';
import { compareCommand } from './compare';
import { compareWithGitCommand } from './compareWithGit';
import { analyzeCommand } from './analyze';
import { validateCommand } from './validate';
import { generateChangelogCommand } from './generateChangelog';
import { BreakingChangesProvider } from '../views/breakingChangesView';
import { ChangelogProvider } from '../views/changelogView';

export function registerCommands(
  context: vscode.ExtensionContext,
  diagnosticCollection: vscode.DiagnosticCollection,
  breakingChangesProvider: BreakingChangesProvider,
  changelogProvider: ChangelogProvider
): void {
  // Compare command
  context.subscriptions.push(
    vscode.commands.registerCommand('changelogHub.compare', (uri?: vscode.Uri) =>
      compareCommand(uri, breakingChangesProvider, changelogProvider)
    )
  );

  // Compare with Git command
  context.subscriptions.push(
    vscode.commands.registerCommand('changelogHub.compareWithGit', (uri?: vscode.Uri) =>
      compareWithGitCommand(uri, breakingChangesProvider, changelogProvider)
    )
  );

  // Analyze command
  context.subscriptions.push(
    vscode.commands.registerCommand('changelogHub.analyze', analyzeCommand)
  );

  // Validate command
  context.subscriptions.push(
    vscode.commands.registerCommand('changelogHub.validate', (uri?: vscode.Uri) =>
      validateCommand(uri, diagnosticCollection)
    )
  );

  // Generate changelog command
  context.subscriptions.push(
    vscode.commands.registerCommand('changelogHub.generateChangelog', () =>
      generateChangelogCommand(changelogProvider)
    )
  );

  // Show breaking changes command
  context.subscriptions.push(
    vscode.commands.registerCommand('changelogHub.showBreakingChanges', () => {
      vscode.commands.executeCommand('changelogHub.breakingChanges.focus');
    })
  );

  // Refresh views command
  context.subscriptions.push(
    vscode.commands.registerCommand('changelogHub.refreshViews', () => {
      breakingChangesProvider.refresh();
      changelogProvider.refresh();
    })
  );
}
