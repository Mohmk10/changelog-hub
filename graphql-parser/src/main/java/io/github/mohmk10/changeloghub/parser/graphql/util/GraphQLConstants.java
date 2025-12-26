package io.github.mohmk10.changeloghub.parser.graphql.util;

public final class GraphQLConstants {

    private GraphQLConstants() {
    }

    public static final String QUERY_TYPE = "Query";
    public static final String MUTATION_TYPE = "Mutation";
    public static final String SUBSCRIPTION_TYPE = "Subscription";

    public static final String SCALAR_INT = "Int";
    public static final String SCALAR_FLOAT = "Float";
    public static final String SCALAR_STRING = "String";
    public static final String SCALAR_BOOLEAN = "Boolean";
    public static final String SCALAR_ID = "ID";

    public static final String DIRECTIVE_DEPRECATED = "deprecated";
    public static final String DIRECTIVE_SKIP = "skip";
    public static final String DIRECTIVE_INCLUDE = "include";
    public static final String DIRECTIVE_SPECIFIED_BY = "specifiedBy";

    public static final String DEPRECATED_REASON = "reason";
    public static final String DEFAULT_DEPRECATED_REASON = "No longer supported";

    public static final String NON_NULL_SUFFIX = "!";
    public static final String LIST_PREFIX = "[";
    public static final String LIST_SUFFIX = "]";

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
