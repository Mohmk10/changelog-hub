import * as vscode from 'vscode';
import { parseSpec } from '../core/parser';
import { ApiSpec, Endpoint, Schema } from '../types';

export class HoverProvider implements vscode.HoverProvider {
  provideHover(
    document: vscode.TextDocument,
    position: vscode.Position,
    _token: vscode.CancellationToken
  ): vscode.ProviderResult<vscode.Hover> {
    try {
      const content = document.getText();
      const spec = parseSpec(content, document.fileName);

      const pathRange = document.getWordRangeAtPosition(
        position,
        /\/[a-zA-Z0-9\/_\-{}]+/
      );
      if (pathRange) {
        const path = document.getText(pathRange);
        const endpoint = spec.endpoints.find((e) => e.path === path);
        if (endpoint) {
          return new vscode.Hover(this.createEndpointHover(endpoint), pathRange);
        }
      }

      const wordRange = document.getWordRangeAtPosition(position);
      if (wordRange) {
        const word = document.getText(wordRange);
        const schema = spec.schemas.find((s) => s.name === word);
        if (schema) {
          return new vscode.Hover(this.createSchemaHover(schema), wordRange);
        }
      }

      const methodRange = document.getWordRangeAtPosition(
        position,
        /\b(get|post|put|delete|patch|options|head)\b/i
      );
      if (methodRange) {
        const method = document.getText(methodRange).toUpperCase();
        const line = document.lineAt(position.line).text;

        const pathMatch = line.match(/\/[a-zA-Z0-9\/_\-{}]+/);
        if (pathMatch) {
          const endpoint = spec.endpoints.find(
            (e) => e.method === method && e.path === pathMatch[0]
          );
          if (endpoint) {
            return new vscode.Hover(this.createEndpointHover(endpoint), methodRange);
          }
        }
      }

      return null;
    } catch {
      return null;
    }
  }

  private createEndpointHover(endpoint: Endpoint): vscode.MarkdownString {
    const md = new vscode.MarkdownString();
    md.isTrusted = true;

    md.appendMarkdown(`### ${endpoint.method} ${endpoint.path}\n\n`);

    if (endpoint.deprecated) {
      md.appendMarkdown(`⚠️ **Deprecated**\n\n`);
    }

    if (endpoint.summary) {
      md.appendMarkdown(`${endpoint.summary}\n\n`);
    }

    if (endpoint.description && endpoint.description !== endpoint.summary) {
      md.appendMarkdown(`${endpoint.description}\n\n`);
    }

    if (endpoint.tags.length > 0) {
      md.appendMarkdown(`**Tags:** ${endpoint.tags.join(', ')}\n\n`);
    }

    if (endpoint.parameters.length > 0) {
      md.appendMarkdown(`**Parameters:**\n`);
      for (const param of endpoint.parameters) {
        const required = param.required ? ' *(required)*' : '';
        md.appendMarkdown(
          `- \`${param.name}\` (${param.location}): ${param.type}${required}\n`
        );
      }
      md.appendMarkdown('\n');
    }

    if (endpoint.responses.length > 0) {
      md.appendMarkdown(`**Responses:**\n`);
      for (const resp of endpoint.responses) {
        md.appendMarkdown(`- \`${resp.statusCode}\`: ${resp.description}\n`);
      }
    }

    return md;
  }

  private createSchemaHover(schema: Schema): vscode.MarkdownString {
    const md = new vscode.MarkdownString();
    md.isTrusted = true;

    md.appendMarkdown(`### Schema: ${schema.name}\n\n`);

    if (schema.description) {
      md.appendMarkdown(`${schema.description}\n\n`);
    }

    md.appendMarkdown(`**Type:** ${schema.type}\n\n`);

    if (schema.properties.length > 0) {
      md.appendMarkdown(`**Properties:**\n`);
      for (const prop of schema.properties) {
        const required = prop.required ? ' *(required)*' : '';
        let line = `- \`${prop.name}\`: ${prop.type}${required}`;
        if (prop.description) {
          line += ` - ${prop.description}`;
        }
        md.appendMarkdown(line + '\n');
      }
    }

    return md;
  }
}
