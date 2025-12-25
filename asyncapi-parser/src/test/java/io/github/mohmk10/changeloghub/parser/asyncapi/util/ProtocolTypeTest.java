package io.github.mohmk10.changeloghub.parser.asyncapi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProtocolType.
 */
class ProtocolTypeTest {

    @Test
    @DisplayName("Should parse Kafka protocol")
    void testParseKafka() {
        ProtocolType protocol = ProtocolType.fromString("kafka");
        assertEquals(ProtocolType.KAFKA, protocol);
        assertTrue(protocol.isMessageBroker());
        assertFalse(protocol.isSecure());
    }

    @Test
    @DisplayName("Should parse Kafka secure protocol")
    void testParseKafkaSecure() {
        ProtocolType protocol = ProtocolType.fromString("kafka-secure");
        assertEquals(ProtocolType.KAFKA_SECURE, protocol);
        assertTrue(protocol.isMessageBroker());
        assertTrue(protocol.isSecure());
    }

    @Test
    @DisplayName("Should parse AMQP protocol")
    void testParseAmqp() {
        ProtocolType protocol = ProtocolType.fromString("amqp");
        assertEquals(ProtocolType.AMQP, protocol);
        assertTrue(protocol.isMessageBroker());
        assertFalse(protocol.isSecure());
    }

    @Test
    @DisplayName("Should parse AMQPS protocol")
    void testParseAmqps() {
        ProtocolType protocol = ProtocolType.fromString("amqps");
        assertEquals(ProtocolType.AMQPS, protocol);
        assertTrue(protocol.isMessageBroker());
        assertTrue(protocol.isSecure());
    }

    @Test
    @DisplayName("Should parse MQTT protocol")
    void testParseMqtt() {
        ProtocolType protocol = ProtocolType.fromString("mqtt");
        assertEquals(ProtocolType.MQTT, protocol);
        assertTrue(protocol.isMessageBroker());
        assertFalse(protocol.isSecure());
    }

    @Test
    @DisplayName("Should parse MQTTS protocol")
    void testParseMqtts() {
        ProtocolType protocol = ProtocolType.fromString("mqtts");
        assertEquals(ProtocolType.MQTTS, protocol);
        assertTrue(protocol.isMessageBroker());
        assertTrue(protocol.isSecure());
    }

    @Test
    @DisplayName("Should parse WebSocket protocols")
    void testParseWebSocket() {
        ProtocolType ws = ProtocolType.fromString("ws");
        assertEquals(ProtocolType.WS, ws);
        assertFalse(ws.isSecure());

        ProtocolType wss = ProtocolType.fromString("wss");
        assertEquals(ProtocolType.WSS, wss);
        assertTrue(wss.isSecure());
    }

    @ParameterizedTest
    @ValueSource(strings = {"kafka", "amqp", "mqtt", "stomp", "jms", "redis", "pulsar", "nats"})
    @DisplayName("Should identify message brokers")
    void testMessageBrokers(String protocol) {
        ProtocolType type = ProtocolType.fromString(protocol);
        assertNotNull(type);
        assertTrue(type.isMessageBroker());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sns", "sqs", "googlepubsub"})
    @DisplayName("Should identify cloud services")
    void testCloudServices(String protocol) {
        ProtocolType type = ProtocolType.fromString(protocol);
        assertNotNull(type);
        assertTrue(type.isCloudService());
    }

    @Test
    @DisplayName("IBM MQ is not a cloud service")
    void testIbmMqNotCloudService() {
        ProtocolType type = ProtocolType.fromString("ibmmq");
        assertNotNull(type);
        assertFalse(type.isCloudService());
        assertTrue(type.isMessageBroker());
    }

    @Test
    @DisplayName("Should handle case insensitivity")
    void testCaseInsensitivity() {
        assertEquals(ProtocolType.KAFKA, ProtocolType.fromString("KAFKA"));
        assertEquals(ProtocolType.KAFKA, ProtocolType.fromString("Kafka"));
        assertEquals(ProtocolType.AMQP, ProtocolType.fromString("AMQP"));
    }

    @Test
    @DisplayName("Should return UNKNOWN for unknown protocol")
    void testUnknownProtocol() {
        ProtocolType protocol = ProtocolType.fromString("unknown_protocol");
        assertEquals(ProtocolType.UNKNOWN, protocol);
    }

    @Test
    @DisplayName("Should return UNKNOWN for null input")
    void testNullInput() {
        ProtocolType protocol = ProtocolType.fromString(null);
        assertEquals(ProtocolType.UNKNOWN, protocol);
    }

    @Test
    @DisplayName("Should check protocol compatibility")
    void testProtocolCompatibility() {
        // Kafka secure is compatible with Kafka
        assertTrue(ProtocolType.KAFKA_SECURE.isCompatibleWith(ProtocolType.KAFKA));
        assertTrue(ProtocolType.KAFKA.isCompatibleWith(ProtocolType.KAFKA_SECURE));

        // AMQPS is compatible with AMQP
        assertTrue(ProtocolType.AMQPS.isCompatibleWith(ProtocolType.AMQP));

        // Kafka is not compatible with AMQP
        assertFalse(ProtocolType.KAFKA.isCompatibleWith(ProtocolType.AMQP));
    }
}
