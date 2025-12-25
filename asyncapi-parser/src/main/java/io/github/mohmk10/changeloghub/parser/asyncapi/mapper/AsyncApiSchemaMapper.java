package io.github.mohmk10.changeloghub.parser.asyncapi.mapper;

import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncSchema;

import java.util.*;

/**
 * Maps AsyncAPI schema definitions to type strings for use with core model.
 * Since the core model uses String types instead of Schema objects,
 * this mapper creates type signatures and schema references.
 */
public class AsyncApiSchemaMapper {

    /**
     * Map an AsyncSchema to a type string.
     */
    public String mapToType(AsyncSchema asyncSchema) {
        if (asyncSchema == null) {
            return "object";
        }

        // Reference
        if (asyncSchema.getRef() != null) {
            return extractRefName(asyncSchema);
        }

        // Type with format
        if (asyncSchema.getType() != null) {
            String type = asyncSchema.getType();
            if (asyncSchema.getFormat() != null) {
                return type + "(" + asyncSchema.getFormat() + ")";
            }
            if ("array".equals(type) && asyncSchema.getItems() != null) {
                return "array<" + mapToType(asyncSchema.getItems()) + ">";
            }
            return type;
        }

        return "object";
    }

    /**
     * Map schema to a schema reference string.
     */
    public String mapToSchemaRef(AsyncSchema asyncSchema) {
        if (asyncSchema == null) {
            return null;
        }

        if (asyncSchema.getRef() != null) {
            return asyncSchema.getRef();
        }

        if (asyncSchema.getName() != null) {
            return "#/components/schemas/" + asyncSchema.getName();
        }

        return null;
    }

    /**
     * Normalize schema type to a standard type string.
     */
    public String normalizeType(String asyncType) {
        if (asyncType == null) {
            return "object";
        }

        switch (asyncType.toLowerCase()) {
            case "string":
                return "string";
            case "integer":
            case "int":
            case "int32":
            case "int64":
            case "long":
                return "integer";
            case "number":
            case "float":
            case "double":
                return "number";
            case "boolean":
            case "bool":
                return "boolean";
            case "array":
                return "array";
            case "object":
            default:
                return "object";
        }
    }

    /**
     * Check if a schema represents a primitive type.
     */
    public boolean isPrimitive(AsyncSchema schema) {
        if (schema == null || schema.getType() == null) {
            return false;
        }
        String type = schema.getType().toLowerCase();
        return type.equals("string") || type.equals("integer") || type.equals("number") ||
               type.equals("boolean") || type.equals("int") || type.equals("long") ||
               type.equals("float") || type.equals("double") || type.equals("bool");
    }

    /**
     * Check if a schema represents an array type.
     */
    public boolean isArray(AsyncSchema schema) {
        return schema != null && "array".equalsIgnoreCase(schema.getType());
    }

    /**
     * Check if a schema represents an object type.
     */
    public boolean isObject(AsyncSchema schema) {
        if (schema == null) {
            return false;
        }
        return "object".equalsIgnoreCase(schema.getType()) ||
               (schema.getType() == null && schema.getProperties() != null && !schema.getProperties().isEmpty());
    }

    /**
     * Check if a schema is a reference.
     */
    public boolean isReference(AsyncSchema schema) {
        return schema != null && schema.getRef() != null;
    }

    /**
     * Extract referenced schema name from $ref.
     */
    public String extractRefName(AsyncSchema schema) {
        if (schema == null || schema.getRef() == null) {
            return null;
        }
        String ref = schema.getRef();
        int lastSlash = ref.lastIndexOf('/');
        return lastSlash >= 0 ? ref.substring(lastSlash + 1) : ref;
    }

    /**
     * Get all property names from a schema.
     */
    public Set<String> getPropertyNames(AsyncSchema schema) {
        Set<String> names = new LinkedHashSet<>();
        if (schema != null && schema.getProperties() != null) {
            names.addAll(schema.getProperties().keySet());
        }
        return names;
    }

    /**
     * Get required property names from a schema.
     */
    public Set<String> getRequiredPropertyNames(AsyncSchema schema) {
        Set<String> names = new LinkedHashSet<>();
        if (schema != null && schema.getRequiredFields() != null) {
            names.addAll(schema.getRequiredFields());
        }
        return names;
    }

    /**
     * Check if a property is required in the schema.
     */
    public boolean isPropertyRequired(AsyncSchema schema, String propertyName) {
        if (schema == null || schema.getRequiredFields() == null) {
            return false;
        }
        return schema.getRequiredFields().contains(propertyName);
    }

    /**
     * Get a property schema by name.
     */
    public AsyncSchema getProperty(AsyncSchema schema, String propertyName) {
        if (schema == null || schema.getProperties() == null) {
            return null;
        }
        return schema.getProperties().get(propertyName);
    }

    /**
     * Create a schema signature for comparison.
     */
    public String createSignature(AsyncSchema schema) {
        if (schema == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        if (schema.getRef() != null) {
            sb.append("$ref:").append(extractRefName(schema));
        } else if (schema.getType() != null) {
            sb.append(schema.getType());
            if (schema.getFormat() != null) {
                sb.append(":").append(schema.getFormat());
            }
            if (isArray(schema) && schema.getItems() != null) {
                sb.append("<").append(createSignature(schema.getItems())).append(">");
            }
        } else if (schema.getProperties() != null) {
            sb.append("object{");
            List<String> props = new ArrayList<>(schema.getProperties().keySet());
            Collections.sort(props);
            for (int i = 0; i < props.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(props.get(i));
            }
            sb.append("}");
        } else {
            sb.append("unknown");
        }

        return sb.toString();
    }
}
