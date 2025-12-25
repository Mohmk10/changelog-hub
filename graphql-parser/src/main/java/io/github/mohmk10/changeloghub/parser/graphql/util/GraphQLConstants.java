package io.github.mohmk10.changeloghub.parser.graphql.util;

/**
 * Constants used in GraphQL parsing.
 */
public final class GraphQLConstants {

    private GraphQLConstants() {
    }

    // Root operation type names
    public static final String QUERY_TYPE = "Query";
    public static final String MUTATION_TYPE = "Mutation";
    public static final String SUBSCRIPTION_TYPE = "Subscription";

    // Built-in scalar types
    public static final String SCALAR_INT = "Int";
    public static final String SCALAR_FLOAT = "Float";
    public static final String SCALAR_STRING = "String";
    public static final String SCALAR_BOOLEAN = "Boolean";
    public static final String SCALAR_ID = "ID";

    // Built-in directives
    public static final String DIRECTIVE_DEPRECATED = "deprecated";
    public static final String DIRECTIVE_SKIP = "skip";
    public static final String DIRECTIVE_INCLUDE = "include";
    public static final String DIRECTIVE_SPECIFIED_BY = "specifiedBy";

    // Directive arguments
    public static final String DEPRECATED_REASON = "reason";
    public static final String DEFAULT_DEPRECATED_REASON = "No longer supported";

    // Type wrappers
    public static final String NON_NULL_SUFFIX = "!";
    public static final String LIST_PREFIX = "[";
    public static final String LIST_SUFFIX = "]";

    // GraphQL endpoint
    public static final String GRAPHQL_ENDPOINT = "/graphql";

    public static boolean isBuiltInScalar(String typeName) {
        return SCALAR_INT.equals(typeName) ||
                SCALAR_FLOAT.equals(typeName) ||
                SCALAR_STRING.equals(typeName) ||
                SCALAR_BOOLEAN.equals(typeName) ||
                SCALAR_ID.equals(typeName);
    }

    public static boolean isRootOperationType(String typeName) {
        return QUERY_TYPE.equals(typeName) ||
                MUTATION_TYPE.equals(typeName) ||
                SUBSCRIPTION_TYPE.equals(typeName);
    }
}
