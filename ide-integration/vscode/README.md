# Changelog Hub - VS Code Extension

API breaking change detector with automatic changelog generation for VS Code.

## Features

- **Compare API Specs**: Compare two API specification files to detect breaking changes
- **Git Integration**: Compare current file with any Git ref (branch, tag, commit)
- **Analyze Specs**: View detailed analysis of API specifications
- **Validate Specs**: Validate API specifications for common issues
- **Generate Changelogs**: Automatically generate changelogs from API changes
- **Inline Warnings**: See deprecated endpoints and issues directly in the editor
- **Hover Information**: View endpoint details on hover
- **Quick Fixes**: Apply quick fixes for common issues

## Supported Formats

| Format | Extensions |
|--------|------------|
| OpenAPI 3.x | `.yaml`, `.yml`, `.json` |
| Swagger 2.x | `.yaml`, `.yml`, `.json` |
| AsyncAPI 2.x | `.yaml`, `.yml`, `.json` |
| GraphQL | `.graphql`, `.gql` |
| Protocol Buffers | `.proto` |

## Commands

Access commands via Command Palette (`Ctrl+Shift+P` / `Cmd+Shift+P`):

| Command | Description |
|---------|-------------|
| `Changelog Hub: Compare API Specs` | Compare two API specification files |
| `Changelog Hub: Compare with Git Ref` | Compare current file with a Git ref |
| `Changelog Hub: Analyze API Spec` | Analyze current API specification |
| `Changelog Hub: Validate API Spec` | Validate current API specification |
| `Changelog Hub: Generate Changelog` | Generate changelog from Git history |

## Views

The extension adds a "Changelog Hub" view container in the Activity Bar with:

- **API Explorer**: Browse all API specs in your workspace
- **Breaking Changes**: View detected breaking changes
- **Changelog**: View all detected changes grouped by severity

## Configuration

| Setting | Default | Description |
|---------|---------|-------------|
| `changelogHub.defaultFormat` | `markdown` | Output format (markdown, json, html) |
| `changelogHub.autoDetectSpecs` | `true` | Auto-detect API specs in workspace |
| `changelogHub.specPatterns` | `[...]` | Glob patterns to detect API specs |
| `changelogHub.showInlineWarnings` | `true` | Show inline warnings for issues |
| `changelogHub.baseRef` | `main` | Default Git ref for comparison |
| `changelogHub.severityThreshold` | `INFO` | Minimum severity to display |

## Context Menu

Right-click on API spec files to access:

- Compare API Specs
- Compare with Git Ref
- Analyze API Spec
- Validate API Spec

## Breaking Change Detection

The extension detects the following breaking changes:

### Endpoint Changes
- Removed endpoints
- Changed HTTP methods
- Changed paths

### Parameter Changes
- Removed parameters
- Changed parameter types
- Made optional parameters required
- Changed parameter locations

### Schema Changes
- Removed properties
- Changed property types
- Added required properties

### Security Changes
- Removed security schemes
- Changed authentication types

## Risk Assessment

Each comparison produces a risk score and level:

| Risk Level | Score | Description |
|------------|-------|-------------|
| LOW | 0-24 | Minor changes, backward compatible |
| MEDIUM | 25-49 | Some changes, review recommended |
| HIGH | 50-74 | Significant changes, migration needed |
| CRITICAL | 75-100 | Major breaking changes |

## Semantic Versioning

The extension recommends version bumps based on changes:

| Recommendation | When |
|----------------|------|
| MAJOR | Breaking changes detected |
| MINOR | New features added, backward compatible |
| PATCH | Bug fixes, no API changes |

## Requirements

- VS Code 1.85.0 or higher
- Git (for Git comparison features)

## Installation

1. Open VS Code
2. Go to Extensions (Ctrl+Shift+X)
3. Search for "Changelog Hub"
4. Click Install

Or install from VSIX:
```bash
code --install-extension changelog-hub-1.0.0.vsix
```

## Development

```bash
# Install dependencies
npm install

# Compile
npm run compile

# Watch mode
npm run watch

# Package extension
npm run package
```

## License

MIT
