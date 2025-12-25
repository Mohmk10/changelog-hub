package io.github.mohmk10.changeloghub.parser.graphql.mapper;

import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLConstants;

/**
 * Maps GraphQL types to API types.
 */
public class GraphQLTypeMapper {

    /**
     * Converts a GraphQL scalar type to an API type string.
     */
    public String toApiType(String graphqlType) {
        if (graphqlType == null) {
            return "object";
        }

        // Remove non-null and list wrappers for base type
        String baseType = extractBaseType(graphqlType);

        return switch (baseType) {
            case GraphQLConstants.SCALAR_STRING, "ID" -> "string";
            case GraphQLConstants.SCALAR_INT -> "integer";
            case GraphQLConstants.SCALAR_FLOAT -> "number";
            case GraphQLConstants.SCALAR_BOOLEAN -> "boolean";
            default -> "object";
        };
    }

    /**
     * Extracts the base type name from a type string.
     */
    public String extractBaseType(String typeString) {
        if (typeString == null) {
            return "";
        }
        return typeString
                .replace("[", "")
                .replace("]", "")
                .replace("!", "");
    }

    /**
     * Checks if a type is a list type.
     */
    public boolean isListType(String typeString) {
        return typeString != null && typeString.contains("[");
    }

    /**
     * Checks if a type is non-null (required).
     */
    public boolean isNonNullType(String typeString) {
        return typeString != null && typeString.endsWith("!");
    }

    /**
     * Checks if a type is a built-in scalar.
     */
    public boolean isBuiltInScalar(String typeName) {
        return GraphQLConstants.isBuiltInScalar(typeName);
    }

    /**
     * Gets the full type signature for display.
     */
    public String getTypeSignature(String baseType, boolean required, boolean list, boolean listItemRequired) {
        StringBuilder sb = new StringBuilder();
        if (list) {
            sb.append("[");
            sb.append(baseType);
            if (listItemRequired) {
                sb.append("!");
            }
            sb.append("]");
        } else {
            sb.append(baseType);
        }
        if (required) {
            sb.append("!");
        }
        return sb.toString();
    }
}
