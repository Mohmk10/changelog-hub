![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) ![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white) ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white) ![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white) ![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge) ![License](https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge)

# Changelog Hub

API Breaking Change Detector - Automatically detects breaking changes in your APIs (REST, GraphQL, gRPC, AsyncAPI, Spring Boot) and generates changelogs.

## Features

- **Multi-protocol**: OpenAPI/Swagger, GraphQL, gRPC/Protobuf, AsyncAPI, Spring Boot
- **Smart Detection**: 50+ types of breaking changes detected
- **Scoring**: Risk Score (0-100), Stability Grade (A-F)
- **Notifications**: Slack, Discord, Teams, Email, Webhook
- **Integrations**: CLI, Maven Plugin, Gradle Plugin, GitHub Action, VS Code, IntelliJ

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.mohmk10</groupId>
    <artifactId>core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```kotlin
implementation("io.github.mohmk10:core:1.0.0-SNAPSHOT")
```

### CLI (npm)

```bash
npm install -g @mohmk10/changelog-hub
```

### GitHub Action

```yaml
- uses: mohmk10/changelog-hub@v1
  with:
    old-spec: api/v1/openapi.yaml
    new-spec: api/v2/openapi.yaml
    fail-on-breaking: true
```

## Quick Start

```java
OpenApiParser parser = new DefaultOpenApiParser();
ApiSpec oldSpec = parser.parse(Paths.get("api-v1.yaml"));
ApiSpec newSpec = parser.parse(Paths.get("api-v2.yaml"));

ApiComparator comparator = new DefaultApiComparator();
Changelog changelog = comparator.compare(oldSpec, newSpec);

BreakingChangeDetector detector = new DefaultBreakingChangeDetector();
List<BreakingChange> breakingChanges = detector.detect(changelog);

Reporter reporter = new MarkdownReporter();
reporter.generate(changelog, breakingChanges);
```

## Modules

| Module | Description | Tests |
|--------|-------------|-------|
| core | Central engine, comparators, detectors, reporters | 16 |
| openapi-parser | OpenAPI 3.x / Swagger 2.x parser | 7 |
| asyncapi-parser | AsyncAPI 2.x parser for event-driven APIs | 8 |
| graphql-parser | GraphQL schema parser | 7 |
| grpc-parser | Protocol Buffers / gRPC parser | 5 |
| spring-parser | Spring Boot annotations parser | 8 |
| cli | Command-line interface (Picocli) | 4 |
| maven-plugin | Maven plugin for CI/CD | 4 |
| gradle-plugin | Gradle plugin (Kotlin DSL) | 8 |
| git-integration | Git operations with JGit | 11 |
| notification-service | Multi-channel notifications | 14 |
| analytics-engine | Metrics, scoring, reports | 28 |
| npm-wrapper | Node.js CLI wrapper | 5 |
| github-action | GitHub Actions integration | 5 |
| vscode-extension | VS Code extension | 3 |
| intellij-plugin | IntelliJ IDEA plugin | 3 |

**Total: 136 test files**

## Architecture

```
                          +------------------+
                          |   Changelog Hub  |
                          +--------+---------+
                                   |
         +-------------------------+-------------------------+
         |                         |                         |
+--------v--------+      +---------v---------+      +--------v--------+
|     Parsers     |      |       Core        |      |   Integrations  |
+-----------------+      +-------------------+      +-----------------+
| - OpenAPI       |      | - Comparator      |      | - CLI           |
| - AsyncAPI      |      | - Detector        |      | - Maven Plugin  |
| - GraphQL       |      | - Generator       |      | - Gradle Plugin |
| - gRPC          |      | - Reporter        |      | - GitHub Action |
| - Spring Boot   |      +-------------------+      | - VS Code       |
+-----------------+               |                 | - IntelliJ      |
                                  |                 +-----------------+
                    +-------------+-------------+
                    |                           |
           +--------v--------+         +--------v--------+
           |   Analytics     |         |  Notifications  |
           +-----------------+         +-----------------+
           | - Metrics       |         | - Slack         |
           | - Risk Score    |         | - Discord       |
           | - Stability     |         | - Teams         |
           | - Trends        |         | - Email         |
           +-----------------+         | - Webhook       |
                                       +-----------------+
```

## License

Apache License 2.0

## Author

**Mohamed Makan** - [https://github.com/Mohmk10](https://github.com/Mohmk10)
