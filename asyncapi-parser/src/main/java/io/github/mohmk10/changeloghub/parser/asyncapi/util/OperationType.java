package io.github.mohmk10.changeloghub.parser.asyncapi.util;

public enum OperationType {
    
    PUBLISH("publish", "send"),

    SUBSCRIBE("subscribe", "receive");

    private final String v2Name;
    private final String v3Name;

    OperationType(String v2Name, String v3Name) {
        this.v2Name = v2Name;
        this.v3Name = v3Name;
    }

    public String getV2Name() {
        return v2Name;
    }

    public String getV3Name() {
        return v3Name;
    }

    public static OperationType fromV2Name(String name) {
        if (name == null) {
            return null;
        }
        for (OperationType type : values()) {
            if (type.v2Name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public static OperationType fromV3Action(String action) {
        if (action == null) {
            return null;
        }
        for (OperationType type : values()) {
            if (type.v3Name.equalsIgnoreCase(action)) {
                return type;
            }
        }
        return null;
    }

    public String getHttpMethod() {
        return this == PUBLISH ? "POST" : "GET";
    }

    public boolean isProducer() {
        return this == PUBLISH;
    }

    public boolean isConsumer() {
        return this == SUBSCRIBE;
    }
}
