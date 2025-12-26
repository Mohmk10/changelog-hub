package io.github.mohmk10.changeloghub.parser.graphql.mapper;

import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLConstants;

public class GraphQLTypeMapper {

    public String toApiType(String graphqlType) {
        if (graphqlType == null) {
            return "object";
        }

        String baseType = extractBaseType(graphqlType);

        return switch (baseType) {
            case GraphQLConstants.SCALAR_STRING, "ID" -> "string";
            case GraphQLConstants.SCALAR_INT -> "integer";
            case GraphQLConstants.SCALAR_FLOAT -> "number";
            case GraphQLConstants.SCALAR_BOOLEAN -> "boolean";
            default -> "object";
        };
    }

    public String extractBaseType(String typeString) {
        if (typeString == null) {
            return "";
        }
        return typeString
                .replace("[", "")
                .replace("]", "")
                .replace("!", "");
    }

    public boolean isListType(String typeString) {
        return typeString != null && typeString.contains("[");
    }

    public boolean isNonNullType(String typeString) {
        return typeString != null && typeString.endsWith("!");
    }

    public boolean isBuiltInScalar(String typeName) {
        return GraphQLConstants.isBuiltInScalar(typeName);
    }

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
