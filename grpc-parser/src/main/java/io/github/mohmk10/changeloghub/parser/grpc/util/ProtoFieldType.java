package io.github.mohmk10.changeloghub.parser.grpc.util;

public enum ProtoFieldType {
    
    DOUBLE("double", "number"),
    FLOAT("float", "number"),
    INT32("int32", "integer"),
    INT64("int64", "integer"),
    UINT32("uint32", "integer"),
    UINT64("uint64", "integer"),
    SINT32("sint32", "integer"),
    SINT64("sint64", "integer"),
    FIXED32("fixed32", "integer"),
    FIXED64("fixed64", "integer"),
    SFIXED32("sfixed32", "integer"),
    SFIXED64("sfixed64", "integer"),
    BOOL("bool", "boolean"),
    STRING("string", "string"),
    BYTES("bytes", "string"),

    MESSAGE("message", "object"),
    ENUM("enum", "string"),
    MAP("map", "object"),
    ONEOF("oneof", "object"),

    UNKNOWN("unknown", "string");

    private final String protoName;
    private final String apiType;

    ProtoFieldType(String protoName, String apiType) {
        this.protoName = protoName;
        this.apiType = apiType;
    }

    public String getProtoName() {
        return protoName;
    }

    public String getApiType() {
        return apiType;
    }

    public boolean isScalar() {
        return this != MESSAGE && this != ENUM && this != MAP && this != ONEOF && this != UNKNOWN;
    }

    public boolean isNumeric() {
        return switch (this) {
            case DOUBLE, FLOAT, INT32, INT64, UINT32, UINT64,
                 SINT32, SINT64, FIXED32, FIXED64, SFIXED32, SFIXED64 -> true;
            default -> false;
        };
    }

    public boolean isInteger() {
        return switch (this) {
            case INT32, INT64, UINT32, UINT64, SINT32, SINT64,
                 FIXED32, FIXED64, SFIXED32, SFIXED64 -> true;
            default -> false;
        };
    }

    public static ProtoFieldType fromString(String typeName) {
        if (typeName == null) {
            return UNKNOWN;
        }

        String normalized = typeName.toLowerCase().trim();

        if (normalized.startsWith("map<")) {
            return MAP;
        }

        for (ProtoFieldType type : values()) {
            if (type.protoName.equals(normalized)) {
                return type;
            }
        }

        return MESSAGE;
    }

    public static boolean isWireCompatible(ProtoFieldType oldType, ProtoFieldType newType) {
        if (oldType == newType) {
            return true;
        }

        if (oldType.isInteger() && newType.isInteger()) {
            return true;
        }

        if ((oldType == SINT32 || oldType == SINT64) &&
            (newType == SINT32 || newType == SINT64)) {
            return true;
        }

        if ((oldType == FIXED32 || oldType == SFIXED32 || oldType == FLOAT) &&
            (newType == FIXED32 || newType == SFIXED32 || newType == FLOAT)) {
            return true;
        }

        if ((oldType == FIXED64 || oldType == SFIXED64 || oldType == DOUBLE) &&
            (newType == FIXED64 || newType == SFIXED64 || newType == DOUBLE)) {
            return true;
        }

        if ((oldType == STRING || oldType == BYTES) &&
            (newType == STRING || newType == BYTES)) {
            return true;
        }

        return false;
    }
}
