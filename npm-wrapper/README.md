# @mohmk10/changelog-hub

A powerful CLI tool for detecting breaking changes in API specifications. Supports OpenAPI, AsyncAPI, GraphQL, and Protocol Buffers.

## Installation

### Global Installation

```bash
npm install -g @mohmk10/changelog-hub
```

### Using npx

```bash
npx @mohmk10/changelog-hub compare old.yaml new.yaml
```

## Usage

### Compare Command

Compare two API specifications and detect breaking changes:

```bash
changelog-hub compare <old-spec> <new-spec> [options]
```

**Options:**

| Option | Description | Default |
|--------|-------------|---------|
| `-f, --format <format>` | Output format (console, markdown, json, html) | `console` |
| `-o, --output <file>` | Write output to file | - |
| `--fail-on-breaking` | Exit with non-zero code if breaking changes detected | `false` |
| `-v, --verbose` | Show verbose output | `false` |
| `--severity <level>` | Minimum severity to report (INFO, WARNING, DANGEROUS, BREAKING) | `INFO` |
| `--no-deprecations` | Exclude deprecation warnings | - |
| `-c, --config <file>` | Path to configuration file | - |

**Examples:**

```bash
# Basic comparison
changelog-hub compare api-v1.yaml api-v2.yaml

# Output as markdown to file
changelog-hub compare api-v1.yaml api-v2.yaml -f markdown -o CHANGELOG.md

# Fail CI pipeline on breaking changes
changelog-hub compare api-v1.yaml api-v2.yaml --fail-on-breaking

# Output as JSON
changelog-hub compare api-v1.yaml api-v2.yaml -f json

# Only show breaking changes
changelog-hub compare api-v1.yaml api-v2.yaml --severity BREAKING
```

### Analyze Command

Analyze a single API specification:

```bash
changelog-hub analyze <spec> [options]
```

**Options:**

| Option | Description |
|--------|-------------|
| `-v, --verbose` | Show detailed analysis |
| `--endpoints` | List all endpoints |
| `--schemas` | List all schemas |
| `--security` | List security schemes |
| `-f, --format <format>` | Output format (console, json) |

**Examples:**

```bash
# Basic analysis
changelog-hub analyze api.yaml

# Show all endpoints
changelog-hub analyze api.yaml --endpoints

# Verbose analysis with all details
changelog-hub analyze api.yaml -v

# Output as JSON
changelog-hub analyze api.yaml -f json
```

### Validate Command

Validate API specification files:

```bash
changelog-hub validate <specs...> [options]
```

**Options:**

| Option | Description |
|--------|-------------|
| `-v, --verbose` | Show verbose validation output |
| `--strict` | Enable strict validation mode |
| `-f, --format <format>` | Output format (console, json) |

**Examples:**

```bash
# Validate single file
changelog-hub validate api.yaml

# Validate multiple files
changelog-hub validate api-v1.yaml api-v2.yaml

# Strict validation
changelog-hub validate api.yaml --strict

# Output as JSON
changelog-hub validate api.yaml -f json
```

### Version Command

Display version information:

```bash
changelog-hub version [options]
```

**Options:**

| Option | Description |
|--------|-------------|
| `--json` | Output version info as JSON |

## Supported Formats

| Format | Extensions | Description |
|--------|------------|-------------|
| OpenAPI | `.yaml`, `.yml`, `.json` | OpenAPI 3.x and Swagger 2.x |
| AsyncAPI | `.yaml`, `.yml`, `.json` | AsyncAPI 2.x |
| GraphQL | `.graphql`, `.gql` | GraphQL Schema Definition Language |
| Protocol Buffers | `.proto` | Protocol Buffer service definitions |

## Configuration

Create a configuration file in your project root:

**.changelog-hub.yaml**

```yaml
defaultFormat: console
failOnBreaking: false
specPath: api/openapi.yaml
severityThreshold: INFO
includeDeprecations: true
customRules: []
```

**.changelog-hub.json**

```json
{
  "defaultFormat": "console",
  "failOnBreaking": false,
  "specPath": "api/openapi.yaml",
  "severityThreshold": "INFO",
  "includeDeprecations": true,
  "customRules": []
}
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `defaultFormat` | string | `console` | Default output format |
| `failOnBreaking` | boolean | `false` | Exit with error on breaking changes |
| `specPath` | string | `api/openapi.yaml` | Default spec path pattern |
| `severityThreshold` | string | `INFO` | Minimum severity to report |
| `includeDeprecations` | boolean | `true` | Include deprecation warnings |
| `customRules` | array | `[]` | Custom detection rules |

## Breaking Change Detection

The tool detects the following types of breaking changes:

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
- Changed property formats

### Security Changes

- Removed security schemes
- Changed authentication types

## Risk Scoring

Each comparison produces a risk assessment:

| Risk Level | Score Range | Description |
|------------|-------------|-------------|
| LOW | 0-24 | Minor changes, backward compatible |
| MEDIUM | 25-49 | Some changes, review recommended |
| HIGH | 50-74 | Significant changes, migration needed |
| CRITICAL | 75-100 | Major breaking changes |

## Semantic Versioning Recommendations

Based on the changes detected, the tool recommends:

| Recommendation | When |
|----------------|------|
| MAJOR | Breaking changes detected |
| MINOR | New features added, backward compatible |
| PATCH | Bug fixes, no API changes |
| NONE | No changes detected |

## CI/CD Integration

### GitHub Actions

```yaml
- name: Check API Breaking Changes
  run: npx @mohmk10/changelog-hub compare old-api.yaml new-api.yaml --fail-on-breaking
```

### GitLab CI

```yaml
api-check:
  script:
    - npx @mohmk10/changelog-hub compare old-api.yaml new-api.yaml --fail-on-breaking
```

### Jenkins

```groovy
stage('API Check') {
    steps {
        sh 'npx @mohmk10/changelog-hub compare old-api.yaml new-api.yaml --fail-on-breaking'
    }
}
```

## Programmatic Usage

```typescript
import {
  detectBreakingChanges,
  hasBreakingChanges,
  getBreakingChangesSummary,
  generateReport,
} from '@mohmk10/changelog-hub';

// Detect breaking changes
const result = detectBreakingChanges('old.yaml', 'new.yaml', {
  severityThreshold: 'WARNING',
  includeDeprecations: true,
});

console.log(`Found ${result.breakingChanges.length} breaking changes`);
console.log(`Risk level: ${result.riskLevel}`);
console.log(`Recommended version bump: ${result.semverRecommendation}`);

// Quick check
if (hasBreakingChanges('old.yaml', 'new.yaml')) {
  console.log('Breaking changes detected!');
}

// Get summary
const summary = getBreakingChangesSummary('old.yaml', 'new.yaml');
console.log(`${summary.count} breaking changes: ${summary.changes.join(', ')}`);

// Generate report
const report = generateReport(result, 'markdown');
console.log(report);
```

## License

MIT
