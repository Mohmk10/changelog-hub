# Changelog Hub - IntelliJ Plugin

API Breaking Change Detector with automatic changelog generation for all JetBrains IDEs.

## Features

- **Compare API Specs**: Compare two API specification files to detect breaking changes
- **Git Integration**: Compare current file with any Git ref (branch, tag, commit)
- **Analyze Specs**: View detailed analysis of API specifications
- **Validate Specs**: Validate API specifications for common issues
- **Generate Changelogs**: Automatically generate changelogs from API changes
- **Inline Warnings**: See deprecated endpoints and issues directly in the editor
- **Inspections**: Code inspections for deprecated endpoints and missing descriptions
- **Syntax Highlighting**: Enhanced highlighting for HTTP methods and paths

## Supported Formats

| Format | Extensions |
|--------|------------|
| OpenAPI 3.x | `.yaml`, `.yml`, `.json` |
| Swagger 2.x | `.yaml`, `.yml`, `.json` |
| AsyncAPI 2.x | `.yaml`, `.yml`, `.json` |
| GraphQL | `.graphql`, `.gql` |
| Protocol Buffers | `.proto` |

## Actions

Access actions via:
- **Tools Menu**: Tools > Changelog Hub
- **Editor Context Menu**: Right-click in editor
- **Project View Context Menu**: Right-click on files

| Action | Description |
|--------|-------------|
| Compare API Specs | Compare two API specification files |
| Compare with Git Ref | Compare current file with a Git ref |
| Analyze API Spec | Analyze current API specification |
| Validate API Spec | Validate current API specification |
| Generate Changelog | Generate changelog from comparison |

## Tool Window

The plugin adds a "Changelog Hub" tool window with three tabs:

- **Breaking Changes**: View detected breaking changes
- **API Explorer**: Browse all API specs in your project
- **Changelog**: View all detected changes grouped by severity

## Settings

Configure the plugin in Settings > Tools > Changelog Hub:

| Setting | Default | Description |
|---------|---------|-------------|
| Default format | `markdown` | Output format (markdown, json, html) |
| Default Git ref | `main` | Default branch for comparison |
| Show inline warnings | `true` | Display warnings in editor |
| Minimum severity | `INFO` | Minimum severity to display |
| Auto-detect specs | `true` | Auto-detect API specs in project |

## Inspections

| Inspection | Level | Description |
|------------|-------|-------------|
| Deprecated endpoint | Warning | Warns when endpoint is marked deprecated |
| Missing description | Weak Warning | Warns when operation lacks description |

## Risk Assessment

Each comparison produces a risk score and level:

| Risk Level | Score | Description |
|------------|-------|-------------|
| LOW | 0 breaking | No breaking changes |
| MEDIUM | 1-2 breaking | Minor breaking changes |
| HIGH | 3-4 breaking | Significant breaking changes |
| CRITICAL | 5+ breaking | Major breaking changes |

## Semantic Versioning

The plugin recommends version bumps based on changes:

| Recommendation | When |
|----------------|------|
| MAJOR | Breaking changes detected |
| MINOR | New features added, backward compatible |
| PATCH | Bug fixes, no API changes |

## Requirements

- IntelliJ IDEA 2023.3 or higher (or any JetBrains IDE)
- Git (for Git comparison features)

## Installation

1. Open your JetBrains IDE
2. Go to Settings > Plugins > Marketplace
3. Search for "Changelog Hub"
4. Click Install

Or install from disk:
1. Download the plugin ZIP
2. Go to Settings > Plugins > Install Plugin from Disk
3. Select the ZIP file

## Development

```bash
# Build the plugin
./gradlew build

# Run tests
./gradlew test

# Run IDE with plugin
./gradlew runIde

# Package plugin
./gradlew buildPlugin
```

## License

MIT
