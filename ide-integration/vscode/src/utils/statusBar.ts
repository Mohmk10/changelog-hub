import * as vscode from 'vscode';

/**
 * Manages the status bar item for the extension
 */
export class StatusBarManager {
  private statusBarItem: vscode.StatusBarItem;
  private isAnalyzing: boolean = false;

  constructor(context: vscode.ExtensionContext) {
    this.statusBarItem = vscode.window.createStatusBarItem(
      vscode.StatusBarAlignment.Right,
      100
    );

    this.statusBarItem.command = 'changelogHub.compare';
    this.statusBarItem.text = '$(git-compare) Changelog Hub';
    this.statusBarItem.tooltip = 'Click to compare API specs';

    context.subscriptions.push(this.statusBarItem);
  }

  show(): void {
    this.statusBarItem.show();
  }

  hide(): void {
    this.statusBarItem.hide();
  }

  showAnalyzing(): void {
    this.isAnalyzing = true;
    this.statusBarItem.text = '$(sync~spin) Analyzing...';
    this.statusBarItem.tooltip = 'Analyzing API specification';
  }

  showReady(): void {
    this.isAnalyzing = false;
    this.statusBarItem.text = '$(git-compare) Changelog Hub';
    this.statusBarItem.tooltip = 'Click to compare API specs';
  }

  showBreakingChanges(count: number): void {
    this.isAnalyzing = false;
    if (count > 0) {
      this.statusBarItem.text = `$(warning) ${count} Breaking Change${count > 1 ? 's' : ''}`;
      this.statusBarItem.tooltip = `${count} breaking change(s) detected. Click to view details.`;
      this.statusBarItem.backgroundColor = new vscode.ThemeColor(
        'statusBarItem.warningBackground'
      );
    } else {
      this.statusBarItem.text = '$(check) No Breaking Changes';
      this.statusBarItem.tooltip = 'No breaking changes detected';
      this.statusBarItem.backgroundColor = undefined;
    }
  }

  showError(message: string): void {
    this.isAnalyzing = false;
    this.statusBarItem.text = '$(error) Changelog Hub';
    this.statusBarItem.tooltip = `Error: ${message}`;
    this.statusBarItem.backgroundColor = new vscode.ThemeColor(
      'statusBarItem.errorBackground'
    );
  }

  update(text: string, tooltip?: string): void {
    this.statusBarItem.text = text;
    if (tooltip) {
      this.statusBarItem.tooltip = tooltip;
    }
  }
}
