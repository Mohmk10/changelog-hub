package io.github.mohmk10.changeloghub.parser.asyncapi;

import io.github.mohmk10.changeloghub.parser.asyncapi.exception.AsyncApiParseException;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.*;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class DefaultAsyncApiParserTest {

    private DefaultAsyncApiParser parser;

    @BeforeEach
    void setUp() {
        parser = new DefaultAsyncApiParser();
    }

    @Test
    @DisplayName("Should parse user-events-v1.yaml successfully")
    void testParseUserEventsV1() throws AsyncApiParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        assertNotNull(is, "Test resource not found");

        AsyncApiSpec spec = parser.parse(is);

        assertNotNull(spec);
        assertEquals("User Events API", spec.getTitle());
        assertEquals("1.0.0", spec.getApiVersion());
        assertEquals(AsyncApiVersion.V2_6, spec.getAsyncApiVersion());
        assertNotNull(spec.getDescription());

        assertNotNull(spec.getServers());
        assertEquals(2, spec.getServers().size());
        assertTrue(spec.getServers().containsKey("production"));
        assertTrue(spec.getServers().containsKey("staging"));

        assertNotNull(spec.getChannels());
        assertEquals(3, spec.getChannels().size());
        assertTrue(spec.getChannels().containsKey("user/created"));
        assertTrue(spec.getChannels().containsKey("user/updated"));
        assertTrue(spec.getChannels().containsKey("user/{userId}/notifications"));

        assertNotNull(spec.getContact());
        assertEquals("API Support", spec.getContact().getName());
        assertEquals("support@example.com", spec.getContact().getEmail());

        assertNotNull(spec.getLicense());
        assertEquals("Apache 2.0", spec.getLicense().getName());
    }

    @Test
    @DisplayName("Should parse order-events.yaml successfully")
    void testParseOrderEvents() throws AsyncApiParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("order-events.yaml");
        assertNotNull(is, "Test resource not found");

        AsyncApiSpec spec = parser.parse(is);

        assertNotNull(spec);
        assertEquals("Order Events API", spec.getTitle());
        assertEquals("1.0.0", spec.getApiVersion());

        assertNotNull(spec.getServers());
        AsyncServer prodServer = spec.getServers().get("production");
        assertNotNull(prodServer);
        assertEquals("amqp://rabbitmq.prod.example.com:5672", prodServer.getUrl());

        assertEquals(6, spec.getChannels().size());
        assertTrue(spec.getChannels().containsKey("orders/placed"));
        assertTrue(spec.getChannels().containsKey("orders/{orderId}/status"));

        assertNotNull(spec.getComponents());
        assertNotNull(spec.getComponents().getMessages());
        assertEquals(5, spec.getComponents().getMessages().size());

        assertNotNull(spec.getTags());
        assertEquals(3, spec.getTags().size());

        assertNotNull(spec.getExternalDocs());
        assertEquals("https://docs.example.com/orders/events", spec.getExternalDocs().getUrl());
    }

    @Test
    @DisplayName("Should parse channel with parameters")
    void testParseChannelWithParameters() throws AsyncApiParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec spec = parser.parse(is);

        AsyncChannel notifChannel = spec.getChannels().get("user/{userId}/notifications");
        assertNotNull(notifChannel);
        assertNotNull(notifChannel.getParameters());
        assertEquals(1, notifChannel.getParameters().size());

        AsyncChannel.ChannelParameter userIdParam = notifChannel.getParameters().get("userId");
        assertNotNull(userIdParam);
        assertEquals("The unique user identifier", userIdParam.getDescription());
        assertNotNull(userIdParam.getSchema());
        assertEquals("string", userIdParam.getSchema().getType());
        assertEquals("uuid", userIdParam.getSchema().getFormat());
    }

    @Test
    @DisplayName("Should parse publish and subscribe operations")
    void testParseOperations() throws AsyncApiParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec spec = parser.parse(is);

        AsyncChannel createdChannel = spec.getChannels().get("user/created");
        assertNotNull(createdChannel);

        assertNotNull(createdChannel.getPublishOperation());
        assertEquals("publishUserCreated", createdChannel.getPublishOperation().getOperationId());
        assertEquals("Publish user created event", createdChannel.getPublishOperation().getSummary());

        assertNotNull(createdChannel.getSubscribeOperation());
        assertEquals("subscribeUserCreated", createdChannel.getSubscribeOperation().getOperationId());
    }

    @Test
    @DisplayName("Should parse component schemas")
    void testParseSchemas() throws AsyncApiParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec spec = parser.parse(is);

        assertNotNull(spec.getComponents());
        assertNotNull(spec.getComponents().getSchemas());
        assertTrue(spec.getComponents().getSchemas().containsKey("UserCreatedPayload"));

        AsyncSchema userCreatedSchema = spec.getComponents().getSchemas().get("UserCreatedPayload");
        assertNotNull(userCreatedSchema);
        assertEquals("object", userCreatedSchema.getType());
        assertNotNull(userCreatedSchema.getRequiredFields());
        assertTrue(userCreatedSchema.getRequiredFields().contains("userId"));
        assertTrue(userCreatedSchema.getRequiredFields().contains("email"));
        assertTrue(userCreatedSchema.getRequiredFields().contains("createdAt"));

        assertNotNull(userCreatedSchema.getProperties());
        assertTrue(userCreatedSchema.getProperties().containsKey("userId"));
        assertTrue(userCreatedSchema.getProperties().containsKey("email"));
        assertTrue(userCreatedSchema.getProperties().containsKey("username"));
    }

    @Test
    @DisplayName("Should parse enum values in schema")
    void testParseEnumValues() throws AsyncApiParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec spec = parser.parse(is);

        AsyncSchema notifSchema = spec.getComponents().getSchemas().get("NotificationPayload");
        assertNotNull(notifSchema);

        AsyncSchema typeSchema = notifSchema.getProperties().get("type");
        assertNotNull(typeSchema);
        assertNotNull(typeSchema.getEnumValues());
        assertEquals(3, typeSchema.getEnumValues().size());
        assertTrue(typeSchema.getEnumValues().contains("info"));
        assertTrue(typeSchema.getEnumValues().contains("warning"));
        assertTrue(typeSchema.getEnumValues().contains("error"));
    }

    @Test
    @DisplayName("Should detect AsyncAPI version")
    void testDetectVersion() throws AsyncApiParseException {
        String yaml = "asyncapi: '2.6.0'\ninfo:\n  title: Test\n  version: '1.0.0'";
        String version = parser.detectVersion(yaml);
        assertEquals("2.6.0", version);
    }

    @Test
    @DisplayName("Should validate AsyncAPI content")
    void testIsValid() {
        String validYaml = "asyncapi: '2.6.0'\ninfo:\n  title: Test\n  version: '1.0.0'";
        assertTrue(parser.isValid(validYaml));

        String invalidYaml = "info:\n  title: Test";
        assertFalse(parser.isValid(invalidYaml));

        assertFalse(parser.isValid(null));
        assertFalse(parser.isValid(""));
    }

    @Test
    @DisplayName("Should throw exception for invalid content")
    void testParseInvalidContent() {
        assertThrows(AsyncApiParseException.class, () -> parser.parse((String) null));
        assertThrows(AsyncApiParseException.class, () -> parser.parse(""));
        assertThrows(AsyncApiParseException.class, () -> parser.parse("invalid: yaml: content: ["));
    }

    @Test
    @DisplayName("Should throw exception for missing asyncapi field")
    void testParseMissingAsyncApiField() {
        String yaml = "info:\n  title: Test\n  version: '1.0.0'";
        assertThrows(AsyncApiParseException.class, () -> parser.parse(yaml));
    }

    @Test
    @DisplayName("Should throw exception for unsupported version")
    void testParseUnsupportedVersion() {
        String yaml = "asyncapi: '1.0.0'\ninfo:\n  title: Test\n  version: '1.0.0'";
        assertThrows(AsyncApiParseException.class, () -> parser.parse(yaml));
    }

    @Test
    @DisplayName("Should parse minimal valid spec")
    void testParseMinimalSpec() throws AsyncApiParseException {
        String yaml = "asyncapi: '2.6.0'\ninfo:\n  title: Minimal API\n  version: '1.0.0'\nchannels: {}";
        AsyncApiSpec spec = parser.parse(yaml);

        assertNotNull(spec);
        assertEquals("Minimal API", spec.getTitle());
        assertEquals("1.0.0", spec.getApiVersion());
        assertEquals(AsyncApiVersion.V2_6, spec.getAsyncApiVersion());
    }

    @Test
    @DisplayName("Should parse JSON format")
    void testParseJsonFormat() throws AsyncApiParseException {
        String json = "{\"asyncapi\": \"2.6.0\", \"info\": {\"title\": \"JSON API\", \"version\": \"1.0.0\"}, \"channels\": {}}";
        AsyncApiSpec spec = parser.parse(json);

        assertNotNull(spec);
        assertEquals("JSON API", spec.getTitle());
    }

    @Test
    @DisplayName("Should parse message with correlation ID")
    void testParseMessageCorrelationId() throws AsyncApiParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("order-events.yaml");
        AsyncApiSpec spec = parser.parse(is);

        AsyncMessage orderPlaced = spec.getComponents().getMessages().get("OrderPlaced");
        assertNotNull(orderPlaced);
        assertNotNull(orderPlaced.getCorrelationId());
    }

    @Test
    @DisplayName("Should parse oneOf messages in operation")
    void testParseOneOfMessages() throws AsyncApiParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("order-events.yaml");
        AsyncApiSpec spec = parser.parse(is);

        AsyncChannel statusChannel = spec.getChannels().get("orders/{orderId}/status");
        assertNotNull(statusChannel);
        assertNotNull(statusChannel.getSubscribeOperation());

        AsyncOperation subOp = statusChannel.getSubscribeOperation();
        assertNotNull(subOp);
    }

    @Test
    @DisplayName("Should get statistics from parsed spec")
    void testGetStatistics() throws AsyncApiParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec spec = parser.parse(is);

        var stats = spec.getStatistics();
        assertNotNull(stats);
        assertEquals(3, stats.get("channels"));
        assertEquals(2, stats.get("servers"));
        assertTrue(stats.get("messages") > 0);
    }
}
