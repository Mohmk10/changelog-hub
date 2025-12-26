package io.github.mohmk10.changeloghub.parser.grpc.util;

public enum ProtoFieldRule {
    
    OPTIONAL("optional"),

    REQUIRED("required"),

    REPEATED("repeated"),

    MAP("map"),

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
