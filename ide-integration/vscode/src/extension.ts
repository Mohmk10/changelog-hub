import * as vscode from 'vscode';
import { registerCommands } from './commands';
import { ApiExplorerProvider } from './views/apiExplorer';
import { BreakingChangesProvider } from './views/breakingChangesView';
import { ChangelogProvider } from './views/changelogView';
import { DiagnosticProvider } from './providers/diagnosticProvider';
import { HoverProvider } from './providers/hoverProvider';
import { CodeActionProvider } from './providers/codeActionProvider';
import { DecorationProvider } from './providers/decorationProvider';
import { StatusBarManager } from './utils/statusBar';
import { Logger } from './utils/logger';

let diagnosticCollection: vscode.DiagnosticCollection;
let apiExplorerProvider: ApiExplorerProvider;
let breakingChangesProvider: BreakingChangesProvider;
let changelogProvider: ChangelogProvider;

export function activate(context: vscode.ExtensionContext): void {
  Logger.info('Changelog Hub extension activating...');

  diagnosticCollection = vscode.languages.createDiagnosticCollection('changelogHub');
  context.subscriptions.push(diagnosticCollection);

  apiExplorerProvider = new ApiExplorerProvider(context);
  breakingChangesProvider = new BreakingChangesProvider(context);
  changelogProvider = new ChangelogProvider(context);

  context.subscriptions.push(
    vscode.window.registerTreeDataProvider('changelogHub.apiExplorer', apiExplorerProvider)
  );
  context.subscriptions.push(
    vscode.window.registerTreeDataProvider('changelogHub.breakingChanges', breakingChangesProvider)
  );
  context.subscriptions.push(
    vscode.window.registerTreeDataProvider('changelogHub.changelog', changelogProvider)
  );

  registerCommands(context, diagnosticCollection, breakingChangesProvider, changelogProvider);

  const diagnosticProvider = new DiagnosticProvider(diagnosticCollection);
  const hoverProvider = new HoverProvider();
  const codeActionProvider = new CodeActionProvider(diagnosticCollection);
  const decorationProvider = new DecorationProvider();

  const documentSelectors: vscode.DocumentSelector = [
    { language: 'yaml', scheme: 'file' },
    { language: 'json', scheme: 'file' },
    { language: 'graphql', scheme: 'file' },
    { language: 'proto3', scheme: 'file' },
  ];

  context.subscriptions.push(
    vscode.languages.registerHoverProvider(documentSelectors, hoverProvider)
  );

  context.subscriptions.push(
    vscode.languages.registerCodeActionsProvider(documentSelectors, codeActionProvider, {
      providedCodeActionKinds: CodeActionProvider.providedCodeActionKinds,
    })
  );

  context.subscriptions.push(
    vscode.workspace.onDidChangeTextDocument((e) => {
      if (isApiSpec(e.document)) {
        diagnosticProvider.updateDiagnostics(e.document);
        decorationProvider.updateDecorations(e.document);
      }
    })
  );

  context.subscriptions.push(
    vscode.workspace.onDidOpenTextDocument((document) => {
      if (isApiSpec(document)) {
        diagnosticProvider.updateDiagnostics(document);
        decorationProvider.updateDecorations(document);
        apiExplorerProvider.refresh();
      }
    })
  );

  context.subscriptions.push(
    vscode.workspace.onDidSaveTextDocument((document) => {
      if (isApiSpec(document)) {
        diagnosticProvider.updateDiagnostics(document);
        apiExplorerProvider.refresh();
      }
    })
  );

  context.subscriptions.push(
    vscode.window.onDidChangeActiveTextEditor((editor) => {
      if (editor && isApiSpec(editor.document)) {
        decorationProvider.updateDecorations(editor.document);
      }
    })
  );

  const statusBar = new StatusBarManager(context);
  statusBar.show();

  vscode.workspace.textDocuments.forEach((document) => {
    if (isApiSpec(document)) {
      diagnosticProvider.updateDiagnostics(document);
    }
  });

  apiExplorerProvider.refresh();

  Logger.info('Changelog Hub extension activated successfully');
}

export function deactivate(): void {
  Logger.info('Changelog Hub extension deactivated');
}

function isApiSpec(document: vscode.TextDocument): boolean {
  const ext = document.fileName.split('.').pop()?.toLowerCase();
  if (!['yaml', 'yml', 'json', 'graphql', 'gql', 'proto'].includes(ext || '')) {
    return false;
  }

  const text = document.getText().substring(0, 1000);
  return (
    text.includes('openapi') ||
    text.includes('swagger') ||
    text.includes('asyncapi') ||
    text.includes('type Query') ||
    text.includes('type Mutation') ||
    text.includes('service ') ||
    text.includes('rpc ')
  );
}
