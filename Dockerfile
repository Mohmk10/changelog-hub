FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Mohamed Makan <kouyatemakan100@gmail.com>"
LABEL description="API Breaking Change Detector - Detects breaking changes in REST, GraphQL, gRPC, AsyncAPI and Spring Boot APIs"
LABEL version="1.0.0"

RUN addgroup -S changelog && adduser -S changelog -G changelog

WORKDIR /app

COPY cli/target/changelog-hub-cli-1.0.0.jar /app/changelog-hub.jar

USER changelog

ENTRYPOINT ["java", "-jar", "/app/changelog-hub.jar"]
