# Changelog Hub GitHub Action

Automatically detect API breaking changes in your CI/CD workflows. This GitHub Action analyzes your API specifications (OpenAPI, AsyncAPI, GraphQL, gRPC) and reports breaking changes on pull requests.

## Features

- **Multi-format Support**: OpenAPI 3.x, Swagger 2.0, AsyncAPI 2.x, GraphQL, Protocol Buffers
- **Breaking Change Detection**: Identifies removed endpoints, modified parameters, schema changes
- **Risk Assessment**: Calculates risk scores and provides severity levels
- **PR Integration**: Posts detailed comments on pull requests
- **Check Runs**: Creates GitHub Check runs with inline annotations
- **Semver Recommendations**: Suggests appropriate version bumps (MAJOR, MINOR, PATCH)
- **Migration Suggestions**: Provides actionable migration guidance

## Quick Start

```yaml
name: API Breaking Changes

on:
  pull_request:
    paths:
      - 'api/**'
      - 'openapi.yaml'

jobs:
  check-breaking-changes:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Required for git diff

      - name: Check for Breaking Changes
        uses: Mohmk10/changelog-hub@v1
        with:
          spec-path: 'api/openapi.yaml'
          fail-on-breaking: true
```

## Inputs

| Input | Description | Required | Default |
|-------|-------------|----------|---------|
| `old-spec` | Path to the old API spec file or Git ref | No | - |
| `new-spec` | Path to the new API spec file | No | - |
| `spec-path` | Path to the API spec file in the repository | No | `api/openapi.yaml` |
| `base-ref` | Base Git ref for comparison | No | `${{ github.base_ref }}` |
| `head-ref` | Head Git ref for comparison | No | `${{ github.head_ref }}` |
| `format` | Output format: `console`, `markdown`, `json` | No | `markdown` |
| `fail-on-breaking` | Fail the action if breaking changes are detected | No | `true` |
| `comment-on-pr` | Post a comment on the PR with the changelog | No | `true` |
| `create-check` | Create a GitHub Check with the results | No | `true` |
| `github-token` | GitHub token for API access | No | `${{ github.token }}` |
| `severity-threshold` | Minimum severity to report: `INFO`, `WARNING`, `DANGEROUS`, `BREAKING` | No | `INFO` |

## Outputs

| Output | Description |
|--------|-------------|
| `has-breaking-changes` | Whether breaking changes were detected (`true`/`false`) |
| `breaking-changes-count` | Number of breaking changes |
| `total-changes-count` | Total number of changes |
| `risk-level` | Risk level: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `risk-score` | Risk score (0-100) |
| `semver-recommendation` | Recommended version bump: `MAJOR`, `MINOR`, `PATCH` |
| `changelog` | Generated changelog content |
| `changelog-file` | Path to the generated changelog file |
| `summary` | Brief summary of changes |

## Usage Examples

### Basic Usage

```yaml
- name: Check API Changes
  uses: Mohmk10/changelog-hub@v1
  with:
    spec-path: 'openapi.yaml'
```

### Compare Specific Branches

```yaml
- name: Compare API Versions
  uses: Mohmk10/changelog-hub@v1
  with:
    spec-path: 'api/openapi.yaml'
    base-ref: 'main'
    head-ref: 'feature/new-api'
```

### Compare Specific Files

```yaml
- name: Compare Spec Files
  uses: Mohmk10/changelog-hub@v1
  with:
    old-spec: 'api/openapi-v1.yaml'
    new-spec: 'api/openapi-v2.yaml'
```

### Don't Fail on Breaking Changes

```yaml
- name: Report Breaking Changes
  uses: Mohmk10/changelog-hub@v1
  with:
    spec-path: 'api/openapi.yaml'
    fail-on-breaking: false
```

### Use Outputs in Subsequent Steps

```yaml
- name: Check API Changes
  id: api-check
  uses: Mohmk10/changelog-hub@v1
  with:
    spec-path: 'api/openapi.yaml'
    fail-on-breaking: false

- name: Handle Breaking Changes
  if: steps.api-check.outputs.has-breaking-changes == 'true'
  run: |
    echo "Breaking changes detected!"
    echo "Risk level: ${{ steps.api-check.outputs.risk-level }}"
    echo "Recommended bump: ${{ steps.api-check.outputs.semver-recommendation }}"
```

### JSON Output for Further Processing

```yaml
- name: Get API Changes as JSON
  id: api-changes
  uses: Mohmk10/changelog-hub@v1
  with:
    spec-path: 'api/openapi.yaml'
    format: json

- name: Process Changes
  run: |
    echo '${{ steps.api-changes.outputs.changelog }}' | jq '.breakingChanges'
```

### Multiple API Specs

```yaml
jobs:
  check-apis:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        api:
          - path: 'api/users/openapi.yaml'
            name: 'Users API'
          - path: 'api/orders/openapi.yaml'
            name: 'Orders API'
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Check ${{ matrix.api.name }}
        uses: Mohmk10/changelog-hub@v1
        with:
          spec-path: ${{ matrix.api.path }}
```

## Breaking Change Categories

The action detects the following types of breaking changes:

### Endpoints
- Removed endpoints
- Changed HTTP methods
- Changed paths

### Parameters
- Removed parameters
- New required parameters
- Parameter type changes
- Parameter becoming required

### Request Body
- New required request body
- Request body becoming required
- Removed content types

### Responses
- Removed response codes
- Changed response schemas

### Schemas
- Removed schemas
- Removed properties
- Property type changes
- Properties becoming required

### Security
- Removed security schemes
- Changed authentication requirements

## PR Comment Example

When enabled, the action posts a detailed comment on your PR:

```markdown
## 🔴 Changelog Hub: 2 Breaking Changes Detected

### Summary

| Metric | Value |
|--------|-------|
| Total Changes | 5 |
| Breaking Changes | 2 |
| Risk Level | 🟠 HIGH |
| Risk Score | 65/100 |
| Recommended Bump | **MAJOR** |

### Breaking Changes

#### `DELETE /users/{id}`
- **Type:** REMOVED
- **Description:** Endpoint removed
- **Impact Score:** 80/100
- **Migration:** Remove all references to this endpoint

#### `POST /users`
- **Type:** MODIFIED
- **Description:** Parameter 'email' is now required
- **Impact Score:** 50/100
- **Migration:** Ensure 'email' is provided in all requests
```

## Risk Levels

| Level | Score Range | Description |
|-------|-------------|-------------|
| LOW | 0-24 | Minor changes, low risk |
| MEDIUM | 25-49 | Some changes may affect clients |
| HIGH | 50-74 | Significant changes, likely impacts |
| CRITICAL | 75-100 | Major breaking changes |

## Supported Formats

### OpenAPI / Swagger
- OpenAPI 3.0.x, 3.1.x
- Swagger 2.0
- YAML and JSON formats

### AsyncAPI
- AsyncAPI 2.x
- Channel/operation analysis

### GraphQL
- SDL schema files
- Query/Mutation/Subscription detection

### Protocol Buffers
- Proto3 syntax
- Service/RPC method analysis

## Development

### Building

```bash
cd github-action
npm install
npm run build
```

### Testing

```bash
npm test
```

### Linting

```bash
npm run lint
```

## License

MIT License - see [LICENSE](../LICENSE) for details.

## Contributing

Contributions are welcome! Please read our [Contributing Guide](../CONTRIBUTING.md) for details.

## Support

- [Open an Issue](https://github.com/Mohmk10/changelog-hub/issues)
- [Documentation](https://github.com/Mohmk10/changelog-hub#readme)
