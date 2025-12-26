package io.github.mohmk10.changeloghub.parser.grpc.mapper;

import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoEnum;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoEnumValue;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoField;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoMessage;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoConstants;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoFieldType;

import java.util.*;

public class GrpcTypeMapper {

    public Map<String, Object> mapMessage(ProtoMessage message) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("name", message.getName());
        schema.put("fullName", message.getFullName());
        schema.put("type", "object");
        schema.put("deprecated", message.isDeprecated());

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (ProtoField field : message.getFields()) {
            properties.put(field.getName(), mapFieldToSchema(field));

            if (field.isRequired()) {
                required.add(field.getName());
            }
        }

        schema.put("properties", properties);
        schema.put("required", required);
        schema.put("fieldCount", message.getFieldCount());

        return schema;
    }

    public Map<String, Object> mapFieldToSchema(ProtoField field) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("name", field.getName());
        schema.put("number", field.getNumber());
        schema.put("deprecated", field.isDeprecated());

        if (field.isMap()) {
            schema.put("type", "object");
            schema.put("description", "map<" + field.getMapKeyType().orElse("?") +
                    ", " + field.getMapValueType().orElse("?") + ">");
            schema.put("isMap", true);
            schema.put("mapKeyType", field.getMapKeyType().orElse(null));
            schema.put("mapValueType", field.getMapValueType().orElse(null));
            return schema;
        }

        if (field.isRepeated()) {
            schema.put("type", "array");
            schema.put("items", Map.of("type", field.getType().getApiType()));
            schema.put("isRepeated", true);
            return schema;
        }

        ProtoFieldType type = field.getType();
        schema.put("type", type.getApiType());

        String format = getFormatForType(type);
        if (format != null) {
            schema.put("format", format);
        }

        if (type == ProtoFieldType.MESSAGE) {
            schema.put("$ref", "#/definitions/" + field.getTypeName());
        }

        if (type == ProtoFieldType.ENUM) {
            schema.put("$ref", "#/definitions/" + field.getTypeName());
        }

        field.getDefaultValue().ifPresent(v -> schema.put("default", v));

        return schema;
    }

    public Map<String, Object> mapEnum(ProtoEnum protoEnum) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("name", protoEnum.getName());
        schema.put("fullName", protoEnum.getFullName());
        schema.put("type", "string");
        schema.put("deprecated", protoEnum.isDeprecated());

        List<String> enumValues = new ArrayList<>();
        Map<String, Integer> valueNumbers = new LinkedHashMap<>();

        for (ProtoEnumValue value : protoEnum.getValues()) {
            enumValues.add(value.getName());
            valueNumbers.put(value.getName(), value.getNumber());
        }

        schema.put("enum", enumValues);
        schema.put("valueNumbers", valueNumbers);

        return schema;
    }

    private String getFormatForType(ProtoFieldType type) {
        return switch (type) {
            case INT64, UINT64, SINT64, FIXED64, SFIXED64 -> "int64";
            case INT32, UINT32, SINT32, FIXED32, SFIXED32 -> "int32";
            case FLOAT -> "float";
            case DOUBLE -> "double";
            case BYTES -> "byte";
            default -> null;
        };
    }

    public Map<String, Map<String, Object>> mapMessages(List<ProtoMessage> messages) {
        Map<String, Map<String, Object>> schemas = new LinkedHashMap<>();

        for (ProtoMessage message : messages) {
            schemas.put(message.getFullName(), mapMessage(message));

            for (ProtoMessage nested : message.getNestedMessages()) {
                schemas.put(nested.getFullName(), mapMessage(nested));
            }

            for (ProtoEnum nestedEnum : message.getNestedEnums()) {
                schemas.put(nestedEnum.getFullName(), mapEnum(nestedEnum));
            }
        }

        return schemas;
    }

    public Map<String, Map<String, Object>> mapEnums(List<ProtoEnum> enums) {
        Map<String, Map<String, Object>> schemas = new LinkedHashMap<>();

        for (ProtoEnum protoEnum : enums) {
            schemas.put(protoEnum.getFullName(), mapEnum(protoEnum));
        }

        return schemas;
    }

    public Map<String, Map<String, Object>> buildSchemaMap(List<ProtoMessage> messages, List<ProtoEnum> enums) {
        Map<String, Map<String, Object>> schemas = new LinkedHashMap<>();
        schemas.putAll(mapMessages(messages));
        schemas.putAll(mapEnums(enums));
        return schemas;
    }
}
