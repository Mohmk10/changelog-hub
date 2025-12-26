import * as vscode from 'vscode';
import * as path from 'path';
import { parseSpec } from '../core/parser';
import { getSpecPatterns } from '../utils/config';
import { ApiSpec, Endpoint, Schema } from '../types';

type TreeItemType = ApiFileItem | EndpointItem | SchemaItem | PropertyItem;

export class ApiExplorerProvider implements vscode.TreeDataProvider<TreeItemType> {
  private _onDidChangeTreeData = new vscode.EventEmitter<TreeItemType | undefined>();
  readonly onDidChangeTreeData = this._onDidChangeTreeData.event;

  private apiSpecs: Map<string, ApiSpec> = new Map();

  constructor(private context: vscode.ExtensionContext) {
    this.refresh();
  }

  refresh(): void {
    this.scanWorkspace().then(() => {
      this._onDidChangeTreeData.fire(undefined);
    });
  }

  private async scanWorkspace(): Promise<void> {
    this.apiSpecs.clear();

    const patterns = getSpecPatterns();
    for (const pattern of patterns) {
      const files = await vscode.workspace.findFiles(pattern, '**/node_modules/**');
      for (const file of files) {
        try {
          const content = (await vscode.workspace.fs.readFile(file)).toString();
          const spec = parseSpec(content, file.fsPath);
          this.apiSpecs.set(file.fsPath, spec);
        } catch {
          
        }
      }
    }
  }

  getTreeItem(element: TreeItemType): vscode.TreeItem {
    return element;
  }

  getChildren(element?: TreeItemType): Thenable<TreeItemType[]> {
    if (!element) {
      
      const items: ApiFileItem[] = [];
      for (const [filePath, spec] of this.apiSpecs) {
        items.push(new ApiFileItem(filePath, spec));
      }
      return Promise.resolve(items);
    }

    if (element instanceof ApiFileItem) {
      
      const items: TreeItemType[] = [];

      if (element.spec.endpoints.length > 0) {
        for (const endpoint of element.spec.endpoints) {
          items.push(new EndpointItem(endpoint, element.filePath));
        }
      }

      if (element.spec.schemas.length > 0) {
        for (const schema of element.spec.schemas) {
          items.push(new SchemaItem(schema, element.filePath));
        }
      }

      return Promise.resolve(items);
    }

    if (element instanceof SchemaItem) {
      
      const items: PropertyItem[] = [];
      for (const prop of element.schema.properties) {
        items.push(new PropertyItem(prop.name, prop.type, prop.required));
      }
      return Promise.resolve(items);
    }

    return Promise.resolve([]);
  }
}

class ApiFileItem extends vscode.TreeItem {
  constructor(
    public readonly filePath: string,
    public readonly spec: ApiSpec
  ) {
    super(spec.name, vscode.TreeItemCollapsibleState.Expanded);

    this.tooltip = `${spec.name} v${spec.version} (${spec.type.toUpperCase()})`;
    this.description = `v${spec.version} • ${spec.endpoints.length} endpoints`;
    this.iconPath = new vscode.ThemeIcon('file-code');
    this.contextValue = 'apiFile';

    this.command = {
      command: 'vscode.open',
      title: 'Open File',
      arguments: [vscode.Uri.file(filePath)],
    };
  }
}

class EndpointItem extends vscode.TreeItem {
  constructor(
    public readonly endpoint: Endpoint,
    public readonly filePath: string
  ) {
    super(`${endpoint.method} ${endpoint.path}`, vscode.TreeItemCollapsibleState.None);

    this.tooltip = endpoint.summary || endpoint.description || endpoint.path;
    this.description = endpoint.deprecated ? '⚠️ deprecated' : '';
    this.iconPath = this.getMethodIcon(endpoint.method);
    this.contextValue = 'endpoint';

    if (endpoint.deprecated) {
      this.iconPath = new vscode.ThemeIcon(
        'warning',
        new vscode.ThemeColor('problemsWarningIcon.foreground')
      );
    }
  }

  private getMethodIcon(method: string): vscode.ThemeIcon {
    switch (method.toUpperCase()) {
      case 'GET':
        return new vscode.ThemeIcon('arrow-down', new vscode.ThemeColor('charts.blue'));
      case 'POST':
        return new vscode.ThemeIcon('arrow-up', new vscode.ThemeColor('charts.green'));
      case 'PUT':
        return new vscode.ThemeIcon('arrow-swap', new vscode.ThemeColor('charts.orange'));
      case 'DELETE':
        return new vscode.ThemeIcon('trash', new vscode.ThemeColor('charts.red'));
      case 'PATCH':
        return new vscode.ThemeIcon('edit', new vscode.ThemeColor('charts.purple'));
      default:
        return new vscode.ThemeIcon('symbol-method');
    }
  }
}

class SchemaItem extends vscode.TreeItem {
  constructor(
    public readonly schema: Schema,
    public readonly filePath: string
  ) {
    super(
      schema.name,
      schema.properties.length > 0
        ? vscode.TreeItemCollapsibleState.Collapsed
        : vscode.TreeItemCollapsibleState.None
    );

    this.tooltip = schema.description || `Schema: ${schema.name}`;
    this.description = `${schema.properties.length} properties`;
    this.iconPath = new vscode.ThemeIcon('symbol-class');
    this.contextValue = 'schema';
  }
}

class PropertyItem extends vscode.TreeItem {
  constructor(
    public readonly name: string,
    public readonly type: string,
    public readonly required: boolean
  ) {
    super(name, vscode.TreeItemCollapsibleState.None);

    this.tooltip = `${name}: ${type}${required ? ' (required)' : ''}`;
    this.description = type;
    this.iconPath = new vscode.ThemeIcon(
      required ? 'symbol-key' : 'symbol-property'
    );
    this.contextValue = 'property';
  }
}
