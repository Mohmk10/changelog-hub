# Changelog Hub CLI - Demo

This folder contains example OpenAPI specifications for testing the Changelog Hub CLI.

## Test Files

| File | Version | Description |
|------|---------|-------------|
| `api-v1.yaml` | 1.0.0 | Baseline API specification |
| `api-v2-breaking.yaml` | 2.0.0 | Contains breaking changes (path modifications, removed endpoints) |
| `api-v2-minor.yaml` | 1.1.0 | Contains minor changes (deprecations, new optional params) |

## Quick Start

```bash
# From the project root directory
cd /path/to/changelog-hub

# Build the CLI (if not already done)
mvn clean package -pl cli -am -DskipTests

# Set up an alias for convenience
alias changelog-hub="java -jar cli/target/cli-1.0.0-SNAPSHOT.jar"
```

## Usage Examples

### Compare APIs (Console Output)

```bash
# Compare with breaking changes - Console output with colors
changelog-hub compare cli/demo/api-v1.yaml cli/demo/api-v2-breaking.yaml

# Compare minor version changes
changelog-hub compare cli/demo/api-v1.yaml cli/demo/api-v2-minor.yaml
```

### Generate Reports

```bash
# Generate Markdown changelog
changelog-hub compare cli/demo/api-v1.yaml cli/demo/api-v2-breaking.yaml \
  -f markdown -o cli/demo/CHANGELOG.md

# Generate JSON report
changelog-hub compare cli/demo/api-v1.yaml cli/demo/api-v2-breaking.yaml \
  -f json -o cli/demo/changelog.json

# Generate HTML report
changelog-hub compare cli/demo/api-v1.yaml cli/demo/api-v2-breaking.yaml \
  -f html -o cli/demo/changelog.html
```

### CI/CD Integration

```bash
# Fail the build if breaking changes are detected
changelog-hub compare old-api.yaml new-api.yaml --fail-on-breaking

# Quiet mode for scripts
changelog-hub compare old.yaml new.yaml -q --fail-on-breaking
```

### Analyze a Single API

```bash
# Display API statistics
changelog-hub analyze cli/demo/api-v1.yaml

# Verbose output with endpoint details
changelog-hub analyze cli/demo/api-v1.yaml -v

# JSON output
changelog-hub analyze cli/demo/api-v1.yaml -f json
```

### Validate an API Specification

```bash
# Basic validation
changelog-hub validate cli/demo/api-v1.yaml

# Strict validation mode
changelog-hub validate cli/demo/api-v1.yaml --strict
```

## Output Formats

| Format | Option | Description |
|--------|--------|-------------|
| Console | `-f console` | Colored terminal output (default) |
| Markdown | `-f markdown` | GitHub-flavored Markdown |
| JSON | `-f json` | Structured JSON for programmatic use |
| HTML | `-f html` | Standalone HTML report with embedded CSS |

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Error or breaking changes detected (with `--fail-on-breaking`) |

## Generated Files

After running the compare commands with output options:

- `CHANGELOG.md` - Markdown changelog report
- `changelog.json` - JSON structured report

---

For more information: https://github.com/Mohmk10/changelog-hub
