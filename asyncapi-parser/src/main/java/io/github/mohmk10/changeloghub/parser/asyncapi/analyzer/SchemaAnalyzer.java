package io.github.mohmk10.changeloghub.parser.asyncapi.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncSchema;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiConstants;

import java.util.*;

public class SchemaAnalyzer {

    public AsyncSchema analyzeSchema(JsonNode schemaNode) {
        if (schemaNode == null || schemaNode.isNull()) {
            return null;
        }

        AsyncSchema.Builder builder = AsyncSchema.builder();

        if (schemaNode.has(AsyncApiConstants.REF)) {
            builder.ref(schemaNode.get(AsyncApiConstants.REF).asText());
            return builder.build();
        }

        if (schemaNode.has(AsyncApiConstants.TYPE)) {
            builder.type(schemaNode.get(AsyncApiConstants.TYPE).asText());
        }

        if (schemaNode.has(AsyncApiConstants.FORMAT)) {
            builder.format(schemaNode.get(AsyncApiConstants.FORMAT).asText());
        }

        if (schemaNode.has(AsyncApiConstants.DESCRIPTION)) {
            builder.description(schemaNode.get(AsyncApiConstants.DESCRIPTION).asText());
        }

        if (schemaNode.has(AsyncApiConstants.REQUIRED)) {
            builder.requiredFields(parseRequiredFields(schemaNode.get(AsyncApiConstants.REQUIRED)));
        }

        if (schemaNode.has(AsyncApiConstants.PROPERTIES)) {
            builder.properties(parseProperties(schemaNode.get(AsyncApiConstants.PROPERTIES)));
        }

        if (schemaNode.has(AsyncApiConstants.ITEMS)) {
            builder.items(analyzeSchema(schemaNode.get(AsyncApiConstants.ITEMS)));
        }

        if (schemaNode.has(AsyncApiConstants.ENUM)) {
            builder.enumValues(parseEnumValues(schemaNode.get(AsyncApiConstants.ENUM)));
        }

        if (schemaNode.has(AsyncApiConstants.DEFAULT)) {
            builder.defaultValue(parseDefaultValue(schemaNode.get(AsyncApiConstants.DEFAULT)));
        }

        if (schemaNode.has(AsyncApiConstants.DEPRECATED)) {
            builder.deprecated(schemaNode.get(AsyncApiConstants.DEPRECATED).asBoolean(false));
        }

        return builder.build();
    }

    public AsyncSchema analyzeSchema(String name, JsonNode schemaNode) {
        AsyncSchema schema = analyzeSchema(schemaNode);
        if (schema != null) {
            schema.setName(name);
        }
        return schema;
    }

    public List<String> parseRequiredFields(JsonNode requiredNode) {
        List<String> required = new ArrayList<>();
        if (requiredNode != null && requiredNode.isArray()) {
            for (JsonNode field : requiredNode) {
                required.add(field.asText());
            }
        }
        return required;
    }

    public Map<String, AsyncSchema> parseProperties(JsonNode propertiesNode) {
        Map<String, AsyncSchema> properties = new LinkedHashMap<>();
        if (propertiesNode != null && propertiesNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                AsyncSchema propSchema = analyzeSchema(entry.getValue());
                if (propSchema != null) {
                    propSchema.setName(entry.getKey());
                    properties.put(entry.getKey(), propSchema);
                }
            }
        }
        return properties;
    }

    public List<String> parseEnumValues(JsonNode enumNode) {
        List<String> values = new ArrayList<>();
        if (enumNode != null && enumNode.isArray()) {
            for (JsonNode value : enumNode) {
                values.add(value.asText());
            }
        }
        return values;
    }

    private Object parseDefaultValue(JsonNode defaultNode) {
        if (defaultNode.isTextual()) {
            return defaultNode.asText();
        } else if (defaultNode.isNumber()) {
            return defaultNode.numberValue();
        } else if (defaultNode.isBoolean()) {
            return defaultNode.asBoolean();
        } else if (defaultNode.isNull()) {
            return null;
        } else {
            return defaultNode.toString();
        }
    }

    public String getSchemaType(JsonNode schemaNode) {
        if (schemaNode == null) {
            return null;
        }
        if (schemaNode.has(AsyncApiConstants.REF)) {
            return "reference";
        }
        if (schemaNode.has(AsyncApiConstants.TYPE)) {
            return schemaNode.get(AsyncApiConstants.TYPE).asText();
        }
        if (schemaNode.has(AsyncApiConstants.ALL_OF)) {
            return "allOf";
        }
        if (schemaNode.has(AsyncApiConstants.ONE_OF)) {
            return "oneOf";
        }
        if (schemaNode.has(AsyncApiConstants.ANY_OF)) {
            return "anyOf";
        }
        return "unknown";
    }

    public boolean isFieldRequired(JsonNode schemaNode, String fieldName) {
        if (schemaNode == null || !schemaNode.has(AsyncApiConstants.REQUIRED)) {
            return false;
        }
        JsonNode required = schemaNode.get(AsyncApiConstants.REQUIRED);
        if (required.isArray()) {
            for (JsonNode req : required) {
                if (fieldName.equals(req.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<String> getFieldNames(JsonNode schemaNode) {
        Set<String> names = new LinkedHashSet<>();
        if (schemaNode != null && schemaNode.has(AsyncApiConstants.PROPERTIES)) {
            Iterator<String> fieldNames = schemaNode.get(AsyncApiConstants.PROPERTIES).fieldNames();
            while (fieldNames.hasNext()) {
                names.add(fieldNames.next());
            }
        }
        return names;
    }

    public Map<String, AsyncSchema> analyzeSchemas(JsonNode schemasNode) {
        Map<String, AsyncSchema> schemas = new LinkedHashMap<>();
        if (schemasNode != null && schemasNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = schemasNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                AsyncSchema schema = analyzeSchema(entry.getKey(), entry.getValue());
                if (schema != null) {
                    schemas.put(entry.getKey(), schema);
                }
            }
        }
        return schemas;
    }
}
