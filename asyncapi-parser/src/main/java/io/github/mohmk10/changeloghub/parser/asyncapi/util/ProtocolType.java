package io.github.mohmk10.changeloghub.parser.asyncapi.util;

/**
 * Enum representing messaging protocols supported by AsyncAPI.
 */
public enum ProtocolType {
    KAFKA("kafka", "Apache Kafka", 9092),
    KAFKA_SECURE("kafka-secure", "Apache Kafka (TLS)", 9093),
    AMQP("amqp", "AMQP 0-9-1 (RabbitMQ)", 5672),
    AMQPS("amqps", "AMQP 0-9-1 (TLS)", 5671),
    AMQP1("amqp1", "AMQP 1.0", 5672),
    MQTT("mqtt", "MQTT", 1883),
    MQTTS("mqtts", "MQTT (TLS)", 8883),
    SECURE_MQTT("secure-mqtt", "MQTT (TLS)", 8883),
    WS("ws", "WebSocket", 80),
    WSS("wss", "WebSocket (TLS)", 443),
    STOMP("stomp", "STOMP", 61613),
    STOMPS("stomps", "STOMP (TLS)", 61614),
    JMS("jms", "Java Message Service", 0),
    SNS("sns", "Amazon SNS", 0),
    SQS("sqs", "Amazon SQS", 0),
    NATS("nats", "NATS", 4222),
    REDIS("redis", "Redis Pub/Sub", 6379),
    MERCURE("mercure", "Mercure", 80),
    IBMMQ("ibmmq", "IBM MQ", 1414),
    GOOGLEPUBSUB("googlepubsub", "Google Cloud Pub/Sub", 0),
    PULSAR("pulsar", "Apache Pulsar", 6650),
    HTTP("http", "HTTP", 80),
    HTTPS("https", "HTTPS", 443),
    UNKNOWN("unknown", "Unknown Protocol", 0);

    private final String protocol;
    private final String description;
    private final int defaultPort;

    ProtocolType(String protocol, String description, int defaultPort) {
        this.protocol = protocol;
        this.description = description;
        this.defaultPort = defaultPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getDescription() {
        return description;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * Parse protocol type from string.
     */
    public static ProtocolType fromString(String protocolStr) {
        if (protocolStr == null || protocolStr.isBlank()) {
            return UNKNOWN;
        }

        String normalized = protocolStr.trim().toLowerCase();

        for (ProtocolType type : values()) {
            if (type.protocol.equals(normalized)) {
                return type;
            }
        }

        return UNKNOWN;
    }

    /**
     * Check if this protocol uses TLS/SSL.
     */
    public boolean isSecure() {
        return this == KAFKA_SECURE || this == AMQPS || this == MQTTS ||
               this == SECURE_MQTT || this == WSS || this == STOMPS || this == HTTPS;
    }

    /**
     * Check if this protocol is a message queue/broker.
     */
    public boolean isMessageBroker() {
        return this == KAFKA || this == KAFKA_SECURE ||
               this == AMQP || this == AMQPS || this == AMQP1 ||
               this == MQTT || this == MQTTS || this == SECURE_MQTT ||
               this == STOMP || this == STOMPS ||
               this == JMS || this == NATS || this == REDIS ||
               this == IBMMQ || this == PULSAR;
    }

    /**
     * Check if this protocol is a cloud messaging service.
     */
    public boolean isCloudService() {
        return this == SNS || this == SQS || this == GOOGLEPUBSUB;
    }

    /**
     * Check if protocols are compatible (same family).
     */
    public boolean isCompatibleWith(ProtocolType other) {
        if (this == other) {
            return true;
        }
        // Kafka family
        if ((this == KAFKA || this == KAFKA_SECURE) &&
            (other == KAFKA || other == KAFKA_SECURE)) {
            return true;
        }
        // AMQP family
        if ((this == AMQP || this == AMQPS) &&
            (other == AMQP || other == AMQPS)) {
            return true;
        }
        // MQTT family
        if ((this == MQTT || this == MQTTS || this == SECURE_MQTT) &&
            (other == MQTT || other == MQTTS || other == SECURE_MQTT)) {
            return true;
        }
        // WebSocket family
        if ((this == WS || this == WSS) && (other == WS || other == WSS)) {
            return true;
        }
        // STOMP family
        if ((this == STOMP || this == STOMPS) && (other == STOMP || other == STOMPS)) {
            return true;
        }
        // HTTP family
        if ((this == HTTP || this == HTTPS) && (other == HTTP || other == HTTPS)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return protocol;
    }
}
