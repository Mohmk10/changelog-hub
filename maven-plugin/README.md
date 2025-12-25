# Changelog Hub - Maven Plugin

Maven plugin for API breaking change detection in builds. Integrates Changelog Hub into your Maven build lifecycle.

## Installation

Add the plugin to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.mohmk10</groupId>
            <artifactId>changelog-hub-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </plugin>
    </plugins>
</build>
```

## Goals

| Goal | Description | Default Phase |
|------|-------------|---------------|
| `changelog:compare` | Compare two API specs and generate changelog | verify |
| `changelog:analyze` | Analyze a single API specification | verify |
| `changelog:validate` | Validate an API specification file | validate |
| `changelog:detect` | Detect breaking changes (CI/CD optimized) | verify |

## Usage

### Compare Two API Specifications

```bash
mvn changelog:compare \
  -Dchangelog.oldSpec=api-v1.yaml \
  -Dchangelog.newSpec=api-v2.yaml
```

With output file:
```bash
mvn changelog:compare \
  -Dchangelog.oldSpec=api-v1.yaml \
  -Dchangelog.newSpec=api-v2.yaml \
  -Dchangelog.format=markdown \
  -Dchangelog.outputFile=CHANGELOG.md
```

### Detect Breaking Changes (CI/CD)

```bash
# Fails build if breaking changes detected (default)
mvn changelog:detect \
  -Dchangelog.oldSpec=api-v1.yaml \
  -Dchangelog.newSpec=api-v2.yaml

# Allow breaking changes
mvn changelog:detect \
  -Dchangelog.oldSpec=api-v1.yaml \
  -Dchangelog.newSpec=api-v2.yaml \
  -Dchangelog.failOnBreaking=false
```

### Analyze API Specification

```bash
mvn changelog:analyze -Dchangelog.spec=api.yaml

# With verbose output
mvn changelog:analyze -Dchangelog.spec=api.yaml -Dchangelog.verbose=true
```

### Validate API Specification

```bash
mvn changelog:validate -Dchangelog.spec=api.yaml

# Strict mode (warnings are errors)
mvn changelog:validate -Dchangelog.spec=api.yaml -Dchangelog.strict=true
```

## Configuration

### Common Parameters

| Parameter | Property | Default | Description |
|-----------|----------|---------|-------------|
| skip | changelog.skip | false | Skip plugin execution |
| verbose | changelog.verbose | false | Enable verbose output |
| format | changelog.format | console | Output format: console, markdown, json, html |
| outputDirectory | changelog.outputDirectory | ${project.build.directory}/changelog | Output directory |

### Compare/Detect Parameters

| Parameter | Property | Required | Default | Description |
|-----------|----------|----------|---------|-------------|
| oldSpec | changelog.oldSpec | Yes | - | Old (baseline) API spec |
| newSpec | changelog.newSpec | Yes | - | New API spec |
| failOnBreaking | changelog.failOnBreaking | No | false (compare) / true (detect) | Fail on breaking changes |
| outputFile | changelog.outputFile | No | - | Output file path |

### Analyze Parameters

| Parameter | Property | Required | Description |
|-----------|----------|----------|-------------|
| spec | changelog.spec | Yes | API specification to analyze |
| outputFile | changelog.outputFile | No | Output file path |

### Validate Parameters

| Parameter | Property | Required | Default | Description |
|-----------|----------|----------|---------|-------------|
| spec | changelog.spec | Yes | - | API specification to validate |
| strict | changelog.strict | No | false | Treat warnings as errors |
| failOnError | changelog.failOnError | No | true | Fail build on errors |

## Build Lifecycle Integration

### Automatic Execution

```xml
<plugin>
    <groupId>io.github.mohmk10</groupId>
    <artifactId>changelog-hub-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>detect-breaking-changes</id>
            <phase>verify</phase>
            <goals>
                <goal>detect</goal>
            </goals>
            <configuration>
                <oldSpec>${project.basedir}/src/main/resources/api-v1.yaml</oldSpec>
                <newSpec>${project.basedir}/src/main/resources/api.yaml</newSpec>
                <failOnBreaking>true</failOnBreaking>
                <format>markdown</format>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### CI/CD Pipeline Example

```yaml
# GitHub Actions
- name: Check API Breaking Changes
  run: |
    mvn changelog:detect \
      -Dchangelog.oldSpec=api/v1/openapi.yaml \
      -Dchangelog.newSpec=api/v2/openapi.yaml \
      -Dchangelog.format=markdown \
      -Dchangelog.outputFile=BREAKING-CHANGES.md
```

## Output Formats

| Format | Extension | Description |
|--------|-----------|-------------|
| console | - | Colored terminal output (default) |
| markdown | .md | GitHub-flavored Markdown |
| json | .json | Structured JSON |
| html | .html | Standalone HTML report |

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Error or breaking changes detected (with `failOnBreaking=true`) |

## Examples

### Example POM Configuration

```xml
<project>
    <build>
        <plugins>
            <plugin>
                <groupId>io.github.mohmk10</groupId>
                <artifactId>changelog-hub-maven-plugin</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <configuration>
                    <format>markdown</format>
                    <verbose>true</verbose>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Generate All Formats

```bash
# Markdown
mvn changelog:compare -Dchangelog.oldSpec=v1.yaml -Dchangelog.newSpec=v2.yaml \
  -Dchangelog.format=markdown -Dchangelog.outputFile=CHANGELOG.md

# JSON
mvn changelog:compare -Dchangelog.oldSpec=v1.yaml -Dchangelog.newSpec=v2.yaml \
  -Dchangelog.format=json -Dchangelog.outputFile=changelog.json

# HTML
mvn changelog:compare -Dchangelog.oldSpec=v1.yaml -Dchangelog.newSpec=v2.yaml \
  -Dchangelog.format=html -Dchangelog.outputFile=changelog.html
```

## Supported API Formats

- OpenAPI 3.0.x
- OpenAPI 3.1.x
- Swagger 2.0

---

For more information: https://github.com/Mohmk10/changelog-hub
