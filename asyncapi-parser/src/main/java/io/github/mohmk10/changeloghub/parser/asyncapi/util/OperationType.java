package io.github.mohmk10.changeloghub.parser.asyncapi.util;

/**
 * Enum representing AsyncAPI operation types.
 */
public enum OperationType {
    /**
     * Publish operation - the application sends messages to this channel.
     * In AsyncAPI 2.x: publish
     * In AsyncAPI 3.x: send action
     */
    PUBLISH("publish", "send"),

    /**
     * Subscribe operation - the application receives messages from this channel.
     * In AsyncAPI 2.x: subscribe
     * In AsyncAPI 3.x: receive action
     */
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

    /**
     * Parse operation type from AsyncAPI 2.x name.
     */
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

    /**
     * Parse operation type from AsyncAPI 3.x action name.
     */
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

    /**
     * Get the HTTP method equivalent for mapping to REST-like endpoints.
     */
    public String getHttpMethod() {
        return this == PUBLISH ? "POST" : "GET";
    }

    /**
     * Check if this operation type is a producer (sends messages).
     */
    public boolean isProducer() {
        return this == PUBLISH;
    }

    /**
     * Check if this operation type is a consumer (receives messages).
     */
    public boolean isConsumer() {
        return this == SUBSCRIBE;
    }
}
