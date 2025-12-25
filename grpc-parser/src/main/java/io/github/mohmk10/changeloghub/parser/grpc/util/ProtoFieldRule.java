package io.github.mohmk10.changeloghub.parser.grpc.util;

/**
 * Enum representing Protocol Buffers field rules.
 */
public enum ProtoFieldRule {
    /**
     * Optional field (proto3 default, explicit in proto2).
     */
    OPTIONAL("optional"),

    /**
     * Required field (proto2 only, deprecated).
     */
    REQUIRED("required"),

    /**
     * Repeated field (zero or more values).
     */
    REPEATED("repeated"),

    /**
     * Map field (key-value pairs).
     */
    MAP("map"),

    /**
     * Singular field (proto3 default when no rule specified).
     */
    SINGULAR("singular");

    private final String keyword;

    ProtoFieldRule(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isCollection() {
        return this == REPEATED || this == MAP;
    }

    public boolean isRequired() {
        return this == REQUIRED;
    }

    public static ProtoFieldRule fromString(String rule) {
        if (rule == null || rule.isBlank()) {
            return SINGULAR;
        }

        String normalized = rule.toLowerCase().trim();
        for (ProtoFieldRule r : values()) {
            if (r.keyword.equals(normalized)) {
                return r;
            }
        }

        return SINGULAR;
    }
}
