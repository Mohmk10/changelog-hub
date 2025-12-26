import * as vscode from 'vscode';
import { Change } from '../types';

type ChangeTreeItem = SeverityItem | ChangeItem | NoChangesItem;

/**
 * Provides tree data for the Changelog view
 */
export class ChangelogProvider implements vscode.TreeDataProvider<ChangeTreeItem> {
  private _onDidChangeTreeData = new vscode.EventEmitter<ChangeTreeItem | undefined>();
  readonly onDidChangeTreeData = this._onDidChangeTreeData.event;

  private changes: Change[] = [];

  constructor(private context: vscode.ExtensionContext) {}

  refresh(): void {
    this._onDidChangeTreeData.fire(undefined);
  }

  setChanges(changes: Change[]): void {
    this.changes = changes;
    this.refresh();
  }

  clearChanges(): void {
    this.changes = [];
    this.refresh();
  }

  getTreeItem(element: ChangeTreeItem): vscode.TreeItem {
    return element;
  }

  getChildren(element?: ChangeTreeItem): Thenable<ChangeTreeItem[]> {
    if (!element) {
      if (this.changes.length === 0) {
        return Promise.resolve([new NoChangesItem()]);
      }

      // Group by severity
      const severities = new Map<string, Change[]>();
      const order = ['BREAKING', 'DANGEROUS', 'WARNING', 'INFO'];

      for (const change of this.changes) {
        const severity = change.severity;
        if (!severities.has(severity)) {
          severities.set(severity, []);
        }
        severities.get(severity)!.push(change);
      }

      const items: SeverityItem[] = [];
      for (const severity of order) {
        const changes = severities.get(severity);
        if (changes && changes.length > 0) {
          items.push(new SeverityItem(severity, changes));
        }
      }

      return Promise.resolve(items);
    }

    if (element instanceof SeverityItem) {
      return Promise.resolve(
        element.changes.map((change) => new ChangeItem(change))
      );
    }

    return Promise.resolve([]);
  }
}

class SeverityItem extends vscode.TreeItem {
  constructor(
    public readonly severity: string,
    public readonly changes: Change[]
  ) {
    super(severity, vscode.TreeItemCollapsibleState.Expanded);

    this.tooltip = `${changes.length} ${severity.toLowerCase()} change(s)`;
    this.description = `${changes.length}`;
    this.iconPath = this.getSeverityIcon(severity);
    this.contextValue = 'severity';
  }

  private getSeverityIcon(severity: string): vscode.ThemeIcon {
    switch (severity) {
      case 'BREAKING':
        return new vscode.ThemeIcon('error', new vscode.ThemeColor('errorForeground'));
      case 'DANGEROUS':
        return new vscode.ThemeIcon(
          'warning',
          new vscode.ThemeColor('problemsWarningIcon.foreground')
        );
      case 'WARNING':
        return new vscode.ThemeIcon(
          'info',
          new vscode.ThemeColor('problemsInfoIcon.foreground')
        );
      case 'INFO':
        return new vscode.ThemeIcon('circle-outline');
      default:
        return new vscode.ThemeIcon('circle');
    }
  }
}

class ChangeItem extends vscode.TreeItem {
  constructor(public readonly change: Change) {
    super(change.path, vscode.TreeItemCollapsibleState.None);

    this.tooltip = new vscode.MarkdownString();
    this.tooltip.appendMarkdown(`### ${change.path}\n\n`);
    this.tooltip.appendMarkdown(`**Type:** ${change.type}\n\n`);
    this.tooltip.appendMarkdown(`**Category:** ${change.category}\n\n`);
    this.tooltip.appendMarkdown(`**Description:** ${change.description}`);

    if (change.oldValue !== undefined) {
      this.tooltip.appendMarkdown(`\n\n**Old Value:** \`${JSON.stringify(change.oldValue)}\``);
    }
    if (change.newValue !== undefined) {
      this.tooltip.appendMarkdown(`\n\n**New Value:** \`${JSON.stringify(change.newValue)}\``);
    }

    this.description = change.description.substring(0, 50);
    this.iconPath = this.getTypeIcon(change.type);
    this.contextValue = 'change';
  }

  private getTypeIcon(type: string): vscode.ThemeIcon {
    switch (type) {
      case 'ADDED':
        return new vscode.ThemeIcon(
          'diff-added',
          new vscode.ThemeColor('gitDecoration.addedResourceForeground')
        );
      case 'REMOVED':
        return new vscode.ThemeIcon(
          'diff-removed',
          new vscode.ThemeColor('gitDecoration.deletedResourceForeground')
        );
      case 'MODIFIED':
        return new vscode.ThemeIcon(
          'diff-modified',
          new vscode.ThemeColor('gitDecoration.modifiedResourceForeground')
        );
      case 'DEPRECATED':
        return new vscode.ThemeIcon(
          'circle-slash',
          new vscode.ThemeColor('problemsWarningIcon.foreground')
        );
      default:
        return new vscode.ThemeIcon('circle');
    }
  }
}

class NoChangesItem extends vscode.TreeItem {
  constructor() {
    super('No changes detected', vscode.TreeItemCollapsibleState.None);

    this.tooltip = 'Run a comparison to see changes';
    this.description = 'Run comparison to analyze';
    this.iconPath = new vscode.ThemeIcon('info');
    this.contextValue = 'noChanges';

    this.command = {
      command: 'changelogHub.compare',
      title: 'Compare API Specs',
    };
  }
}
