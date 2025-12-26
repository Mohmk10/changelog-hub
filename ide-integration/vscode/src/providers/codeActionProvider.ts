import * as vscode from 'vscode';

export class CodeActionProvider implements vscode.CodeActionProvider {
  static readonly providedCodeActionKinds = [
    vscode.CodeActionKind.QuickFix,
    vscode.CodeActionKind.Refactor,
  ];

  constructor(private diagnosticCollection: vscode.DiagnosticCollection) {}

  provideCodeActions(
    document: vscode.TextDocument,
    range: vscode.Range,
    context: vscode.CodeActionContext,
    _token: vscode.CancellationToken
  ): vscode.ProviderResult<vscode.CodeAction[]> {
    const actions: vscode.CodeAction[] = [];

    for (const diagnostic of context.diagnostics) {
      if (diagnostic.source !== 'Changelog Hub') {
        continue;
      }

      switch (diagnostic.code) {
        case 'deprecated-endpoint':
          actions.push(this.createDeprecationAction(document, diagnostic));
          break;
        case 'missing-description':
          actions.push(this.createAddDescriptionAction(document, diagnostic));
          break;
        case 'missing-responses':
          actions.push(this.createAddResponseAction(document, diagnostic));
          break;
        case 'empty-schema':
          actions.push(this.createAddPropertiesAction(document, diagnostic));
          break;
      }
    }

    if (this.isApiSpecFile(document)) {
      const compareAction = new vscode.CodeAction(
        'Compare with Git ref...',
        vscode.CodeActionKind.Empty
      );
      compareAction.command = {
        command: 'changelogHub.compareWithGit',
        title: 'Compare with Git ref',
        arguments: [document.uri],
      };
      actions.push(compareAction);

      const analyzeAction = new vscode.CodeAction(
        'Analyze API specification',
        vscode.CodeActionKind.Empty
      );
      analyzeAction.command = {
        command: 'changelogHub.analyze',
        title: 'Analyze API specification',
        arguments: [document.uri],
      };
      actions.push(analyzeAction);
    }

    return actions;
  }

  private createDeprecationAction(
    document: vscode.TextDocument,
    diagnostic: vscode.Diagnostic
  ): vscode.CodeAction {
    const action = new vscode.CodeAction(
      'Remove deprecated marker',
      vscode.CodeActionKind.QuickFix
    );
    action.diagnostics = [diagnostic];
    action.isPreferred = false;

    const text = document.getText();
    const deprecatedMatch = text.match(/deprecated:\s*true/i);
    if (deprecatedMatch && deprecatedMatch.index !== undefined) {
      const startPos = document.positionAt(deprecatedMatch.index);
      const endPos = document.positionAt(
        deprecatedMatch.index + deprecatedMatch[0].length
      );
      action.edit = new vscode.WorkspaceEdit();
      action.edit.delete(document.uri, new vscode.Range(startPos, endPos));
    }

    return action;
  }

  private createAddDescriptionAction(
    document: vscode.TextDocument,
    diagnostic: vscode.Diagnostic
  ): vscode.CodeAction {
    const action = new vscode.CodeAction(
      'Add description',
      vscode.CodeActionKind.QuickFix
    );
    action.diagnostics = [diagnostic];
    action.isPreferred = true;

    const line = diagnostic.range.start.line;
    const lineText = document.lineAt(line).text;
    const indent = lineText.match(/^\s*/)?.[0] || '';

    action.edit = new vscode.WorkspaceEdit();
    action.edit.insert(
      document.uri,
      new vscode.Position(line + 1, 0),
      `${indent}  description: "TODO: Add description"\n`
    );

    return action;
  }

  private createAddResponseAction(
    document: vscode.TextDocument,
    diagnostic: vscode.Diagnostic
  ): vscode.CodeAction {
    const action = new vscode.CodeAction(
      'Add response definitions',
      vscode.CodeActionKind.QuickFix
    );
    action.diagnostics = [diagnostic];

    const line = diagnostic.range.start.line;
    const lineText = document.lineAt(line).text;
    const indent = lineText.match(/^\s*/)?.[0] || '';

    const responseTemplate = `${indent}  responses:
${indent}    '200':
${indent}      description: Successful response
${indent}      content:
${indent}        application/json:
${indent}          schema:
${indent}            type: object
`;

    action.edit = new vscode.WorkspaceEdit();
    action.edit.insert(
      document.uri,
      new vscode.Position(line + 1, 0),
      responseTemplate
    );

    return action;
  }

  private createAddPropertiesAction(
    document: vscode.TextDocument,
    diagnostic: vscode.Diagnostic
  ): vscode.CodeAction {
    const action = new vscode.CodeAction(
      'Add properties template',
      vscode.CodeActionKind.QuickFix
    );
    action.diagnostics = [diagnostic];

    const line = diagnostic.range.start.line;
    const lineText = document.lineAt(line).text;
    const indent = lineText.match(/^\s*/)?.[0] || '';

    const propertiesTemplate = `${indent}  properties:
${indent}    id:
${indent}      type: string
${indent}      description: Unique identifier
${indent}    name:
${indent}      type: string
${indent}      description: Name
`;

    action.edit = new vscode.WorkspaceEdit();

    let insertLine = line + 1;
    for (let i = line + 1; i < document.lineCount; i++) {
      const text = document.lineAt(i).text;
      if (text.trim().startsWith('properties:')) {
        
        return action;
      }
      if (!text.startsWith(indent + ' ') && text.trim() !== '') {
        insertLine = i;
        break;
      }
    }

    action.edit.insert(
      document.uri,
      new vscode.Position(insertLine, 0),
      propertiesTemplate
    );

    return action;
  }

  private isApiSpecFile(document: vscode.TextDocument): boolean {
    const ext = document.fileName.split('.').pop()?.toLowerCase();
    return ['yaml', 'yml', 'json', 'graphql', 'gql', 'proto'].includes(ext || '');
  }
}
