import * as vscode from 'vscode';
import { parseSpec } from '../core/parser';

/**
 * Provides text decorations for API specifications
 */
export class DecorationProvider {
  private deprecatedDecorationType: vscode.TextEditorDecorationType;
  private endpointDecorationType: vscode.TextEditorDecorationType;
  private schemaDecorationType: vscode.TextEditorDecorationType;

  constructor() {
    // Deprecated decoration - strikethrough and dimmed
    this.deprecatedDecorationType = vscode.window.createTextEditorDecorationType({
      textDecoration: 'line-through',
      opacity: '0.6',
      after: {
        contentText: ' ⚠️ deprecated',
        color: new vscode.ThemeColor('editorWarning.foreground'),
        fontStyle: 'italic',
      },
    });

    // Endpoint decoration - subtle highlight
    this.endpointDecorationType = vscode.window.createTextEditorDecorationType({
      backgroundColor: new vscode.ThemeColor('editor.findMatchHighlightBackground'),
      borderRadius: '2px',
    });

    // Schema decoration
    this.schemaDecorationType = vscode.window.createTextEditorDecorationType({
      fontWeight: 'bold',
      color: new vscode.ThemeColor('symbolIcon.classForeground'),
    });
  }

  updateDecorations(document: vscode.TextDocument): void {
    const editor = vscode.window.activeTextEditor;
    if (!editor || editor.document !== document) {
      return;
    }

    if (!this.isApiSpec(document)) {
      this.clearDecorations(editor);
      return;
    }

    try {
      const content = document.getText();
      const spec = parseSpec(content, document.fileName);

      const deprecatedDecorations: vscode.DecorationOptions[] = [];
      const endpointDecorations: vscode.DecorationOptions[] = [];
      const schemaDecorations: vscode.DecorationOptions[] = [];

      // Decorate deprecated endpoints
      for (const endpoint of spec.endpoints) {
        if (endpoint.deprecated) {
          const range = this.findRange(document, endpoint.path);
          if (range) {
            deprecatedDecorations.push({
              range,
              hoverMessage: new vscode.MarkdownString(
                `⚠️ **Deprecated:** ${endpoint.method} ${endpoint.path}`
              ),
            });
          }
        }
      }

      // Decorate HTTP methods
      const methodRegex = /\b(get|post|put|delete|patch|options|head):/gi;
      let match;
      while ((match = methodRegex.exec(content)) !== null) {
        const startPos = document.positionAt(match.index);
        const endPos = document.positionAt(match.index + match[0].length - 1);
        const range = new vscode.Range(startPos, endPos);

        const method = match[1].toUpperCase();
        const color = this.getMethodColor(method);

        endpointDecorations.push({
          range,
          renderOptions: {
            before: {
              contentText: this.getMethodIcon(method),
              margin: '0 4px 0 0',
            },
          },
        });
      }

      // Apply decorations
      editor.setDecorations(this.deprecatedDecorationType, deprecatedDecorations);
      editor.setDecorations(this.endpointDecorationType, endpointDecorations);
      editor.setDecorations(this.schemaDecorationType, schemaDecorations);
    } catch {
      this.clearDecorations(editor);
    }
  }

  private clearDecorations(editor: vscode.TextEditor): void {
    editor.setDecorations(this.deprecatedDecorationType, []);
    editor.setDecorations(this.endpointDecorationType, []);
    editor.setDecorations(this.schemaDecorationType, []);
  }

  private isApiSpec(document: vscode.TextDocument): boolean {
    const ext = document.fileName.split('.').pop()?.toLowerCase();
    if (!['yaml', 'yml', 'json'].includes(ext || '')) {
      return false;
    }

    const text = document.getText().substring(0, 500);
    return text.includes('openapi') || text.includes('swagger') || text.includes('asyncapi');
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

  private getMethodIcon(method: string): string {
    switch (method) {
      case 'GET':
        return '🔍';
      case 'POST':
        return '➕';
      case 'PUT':
        return '✏️';
      case 'DELETE':
        return '🗑️';
      case 'PATCH':
        return '🔧';
      default:
        return '📡';
    }
  }

  private getMethodColor(method: string): string {
    switch (method) {
      case 'GET':
        return '#61affe';
      case 'POST':
        return '#49cc90';
      case 'PUT':
        return '#fca130';
      case 'DELETE':
        return '#f93e3e';
      case 'PATCH':
        return '#50e3c2';
      default:
        return '#9012fe';
    }
  }

  dispose(): void {
    this.deprecatedDecorationType.dispose();
    this.endpointDecorationType.dispose();
    this.schemaDecorationType.dispose();
  }
}
