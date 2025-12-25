package io.github.mohmk10.changeloghub.parser.openapi.mapper;

import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.ParameterLocation;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Optional;

public class ParameterMapper {

    public Parameter map(io.swagger.v3.oas.models.parameters.Parameter param) {
        if (param == null) {
            return null;
        }

        String type = extractType(param.getSchema());
        String defaultValue = extractDefaultValueAsString(param.getSchema());

        return new Parameter(
                param.getName(),
                mapLocation(param.getIn()),
                type,
                Boolean.TRUE.equals(param.getRequired()),
                defaultValue,
                param.getDescription()
        );
    }

    private ParameterLocation mapLocation(String in) {
        if (in == null) {
            return ParameterLocation.QUERY;
        }

        switch (in.toLowerCase()) {
            case "path":
                return ParameterLocation.PATH;
            case "query":
                return ParameterLocation.QUERY;
            case "header":
                return ParameterLocation.HEADER;
            case "cookie":
                return ParameterLocation.COOKIE;
            default:
                return ParameterLocation.QUERY;
        }
    }

    @SuppressWarnings("rawtypes")
    private String extractType(Schema schema) {
        if (schema == null) {
            return "string";
        }

        String type = schema.getType();
        if (type != null) {
            return type;
        }

        if (schema.get$ref() != null) {
            return extractRefName(schema.get$ref());
        }

        return "string";
    }

    @SuppressWarnings("rawtypes")
    private String extractDefaultValueAsString(Schema schema) {
        if (schema == null) {
            return null;
        }
        Object defaultValue = schema.getDefault();
        return defaultValue != null ? String.valueOf(defaultValue) : null;
    }

    private String extractRefName(String ref) {
        if (ref == null) {
            return "object";
        }
        int lastSlash = ref.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < ref.length() - 1) {
            return ref.substring(lastSlash + 1);
        }
        return ref;
    }
}
