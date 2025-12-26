import * as vscode from 'vscode';
import { BreakingChange } from '../types';

type TreeItem = BreakingChangeItem | CategoryItem | NoChangesItem;

export class BreakingChangesProvider implements vscode.TreeDataProvider<TreeItem> {
  private _onDidChangeTreeData = new vscode.EventEmitter<TreeItem | undefined>();
  readonly onDidChangeTreeData = this._onDidChangeTreeData.event;

  private breakingChanges: BreakingChange[] = [];

  constructor(private context: vscode.ExtensionContext) {}

  refresh(): void {
    this._onDidChangeTreeData.fire(undefined);
  }

  setBreakingChanges(changes: BreakingChange[]): void {
    this.breakingChanges = changes;
    this.refresh();
  }

  clearBreakingChanges(): void {
    this.breakingChanges = [];
    this.refresh();
  }

  getTreeItem(element: TreeItem): vscode.TreeItem {
    return element;
  }

  getChildren(element?: TreeItem): Thenable<TreeItem[]> {
    if (!element) {
      if (this.breakingChanges.length === 0) {
        return Promise.resolve([new NoChangesItem()]);
      }

      const categories = new Map<string, BreakingChange[]>();
      for (const change of this.breakingChanges) {
        const category = change.category;
        if (!categories.has(category)) {
          categories.set(category, []);
        }
        categories.get(category)!.push(change);
      }

      const items: CategoryItem[] = [];
      for (const [category, changes] of categories) {
        items.push(new CategoryItem(category, changes));
      }

      return Promise.resolve(items);
    }

    if (element instanceof CategoryItem) {
      return Promise.resolve(
        element.changes.map((change) => new BreakingChangeItem(change))
      );
    }

    return Promise.resolve([]);
  }
}

class CategoryItem extends vscode.TreeItem {
  constructor(
    public readonly category: string,
    public readonly changes: BreakingChange[]
  ) {
    super(category, vscode.TreeItemCollapsibleState.Expanded);

    this.tooltip = `${changes.length} breaking change(s) in ${category}`;
    this.description = `${changes.length}`;
    this.iconPath = this.getCategoryIcon(category);
    this.contextValue = 'category';
  }

  private getCategoryIcon(category: string): vscode.ThemeIcon {
    switch (category) {
      case 'ENDPOINT':
        return new vscode.ThemeIcon(
          'symbol-method',
          new vscode.ThemeColor('errorForeground')
        );
      case 'PARAMETER':
        return new vscode.ThemeIcon(
          'symbol-parameter',
          new vscode.ThemeColor('errorForeground')
        );
      case 'SCHEMA':
        return new vscode.ThemeIcon(
          'symbol-class',
          new vscode.ThemeColor('errorForeground')
        );
      case 'SECURITY':
        return new vscode.ThemeIcon(
          'shield',
          new vscode.ThemeColor('errorForeground')
        );
      default:
        return new vscode.ThemeIcon('error');
    }
  }
}

class BreakingChangeItem extends vscode.TreeItem {
  constructor(public readonly change: BreakingChange) {
    super(change.path, vscode.TreeItemCollapsibleState.None);

    this.tooltip = new vscode.MarkdownString();
    this.tooltip.appendMarkdown(`### ${change.path}\n\n`);
    this.tooltip.appendMarkdown(`**Type:** ${change.type}\n\n`);
    this.tooltip.appendMarkdown(`**Description:** ${change.description}\n\n`);
    this.tooltip.appendMarkdown(`**Impact Score:** ${change.impactScore}/100\n\n`);
    this.tooltip.appendMarkdown(`**Migration:** ${change.migrationSuggestion}`);

    this.description = change.type;
    this.iconPath = this.getTypeIcon(change.type);
    this.contextValue = 'breakingChange';
  }

  private getTypeIcon(type: string): vscode.ThemeIcon {
    switch (type) {
      case 'REMOVED':
        return new vscode.ThemeIcon('trash', new vscode.ThemeColor('errorForeground'));
      case 'MODIFIED':
        return new vscode.ThemeIcon('edit', new vscode.ThemeColor('errorForeground'));
      default:
        return new vscode.ThemeIcon('error');
    }
  }
}

class NoChangesItem extends vscode.TreeItem {
  constructor() {
    super('No breaking changes', vscode.TreeItemCollapsibleState.None);

    this.tooltip = 'Run a comparison to detect breaking changes';
    this.description = 'Run comparison to analyze';
    this.iconPath = new vscode.ThemeIcon(
      'check',
      new vscode.ThemeColor('testing.iconPassed')
    );
    this.contextValue = 'noChanges';

    this.command = {
      command: 'changelogHub.compare',
      title: 'Compare API Specs',
    };
  }
}
