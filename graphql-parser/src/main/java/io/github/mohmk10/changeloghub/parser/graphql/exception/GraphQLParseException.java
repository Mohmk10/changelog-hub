package io.github.mohmk10.changeloghub.parser.graphql.exception;

/**
 * Exception thrown when parsing GraphQL schemas fails.
 */
public class GraphQLParseException extends RuntimeException {

    public GraphQLParseException(String message) {
        super(message);
    }

    public GraphQLParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public static GraphQLParseException invalidSchema(String details) {
        return new GraphQLParseException("Invalid GraphQL schema: " + details);
    }

    public static GraphQLParseException fileNotFound(String path) {
        return new GraphQLParseException("GraphQL schema file not found: " + path);
    }

    public static GraphQLParseException parseError(String details, Throwable cause) {
        return new GraphQLParseException("Failed to parse GraphQL schema: " + details, cause);
    }

    public static GraphQLParseException ioError(String path, Throwable cause) {
        return new GraphQLParseException("Failed to read GraphQL schema file: " + path, cause);
    }

    public static GraphQLParseException emptySchema() {
        return new GraphQLParseException("GraphQL schema content is empty or null");
    }

    public static GraphQLParseException validationError(String details) {
        return new GraphQLParseException("GraphQL schema validation failed: " + details);
    }
}
