# Changelog Hub

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()
[![Java](https://img.shields.io/badge/java-21-orange)]()
[![Spring Boot](https://img.shields.io/badge/spring%20boot-3.4.1-green)]()

Multi-format API changelog generator and analyzer. Automatically detect and document API changes across OpenAPI, GraphQL, gRPC, and AsyncAPI specifications.

## Modules

- **core** - Shared models and utilities
- **openapi-parser** - OpenAPI/Swagger specification parser
- **graphql-parser** - GraphQL schema parser
- **grpc-parser** - gRPC/Protocol Buffer parser
- **asyncapi-parser** - AsyncAPI specification parser
- **spring-parser** - Spring Boot annotations parser
- **git-integration** - Git repository integration
- **cli** - Command-line interface
- **maven-plugin** - Maven build integration
- **gradle-plugin** - Gradle build integration
- **notification-service** - Changelog notification service
- **analytics-engine** - API change analytics

## Build

```bash
mvn clean install
```

## License

MIT
