package io.github.mohmk10.changeloghub.parser.asyncapi.mapper;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.parser.asyncapi.DefaultAsyncApiParser;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncApiSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AsyncApiModelMapper.
 */
class AsyncApiModelMapperTest {

    private AsyncApiModelMapper mapper;
    private DefaultAsyncApiParser parser;

    @BeforeEach
    void setUp() {
        mapper = new AsyncApiModelMapper();
        parser = new DefaultAsyncApiParser();
    }

    @Test
    @DisplayName("Should map AsyncApiSpec to ApiSpec")
    void testMapToApiSpec() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        ApiSpec apiSpec = mapper.map(asyncSpec);

        assertNotNull(apiSpec);
        assertEquals("User Events API", apiSpec.getName());
        assertEquals("1.0.0", apiSpec.getVersion());
        assertEquals(ApiType.ASYNCAPI, apiSpec.getType());
        assertNotNull(apiSpec.getMetadata());
    }

    @Test
    @DisplayName("Should map channels to endpoints")
    void testMapEndpoints() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        List<Endpoint> endpoints = mapper.mapEndpoints(asyncSpec);

        assertNotNull(endpoints);
        assertFalse(endpoints.isEmpty());

        // Each channel with publish/subscribe should generate endpoints
        // user/created: publish + subscribe = 2 endpoints
        // user/updated: publish = 1 endpoint
        // user/{userId}/notifications: subscribe = 1 endpoint
        assertTrue(endpoints.size() >= 4);
    }

    @Test
    @DisplayName("Should map PUBLISH operation to POST method")
    void testPublishMapsToPost() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        List<Endpoint> endpoints = mapper.mapEndpoints(asyncSpec);

        boolean hasPostEndpoint = endpoints.stream()
                .anyMatch(e -> e.getMethod() == HttpMethod.POST && e.getPath().contains("user"));
        assertTrue(hasPostEndpoint, "Should have POST endpoint for publish operation");
    }

    @Test
    @DisplayName("Should map SUBSCRIBE operation to GET method")
    void testSubscribeMapsToGet() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        List<Endpoint> endpoints = mapper.mapEndpoints(asyncSpec);

        boolean hasGetEndpoint = endpoints.stream()
                .anyMatch(e -> e.getMethod() == HttpMethod.GET && e.getPath().contains("user"));
        assertTrue(hasGetEndpoint, "Should have GET endpoint for subscribe operation");
    }

    @Test
    @DisplayName("Should extract base URL from server")
    void testExtractBaseUrl() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        String baseUrl = mapper.extractBaseUrl(asyncSpec);

        assertNotNull(baseUrl);
        assertTrue(baseUrl.contains("kafka"));
    }

    @Test
    @DisplayName("Should store metadata including description and contact")
    void testMapMetadata() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        ApiSpec apiSpec = mapper.map(asyncSpec);

        assertNotNull(apiSpec.getMetadata());
        assertTrue(apiSpec.getMetadata().containsKey("asyncapi_version"));

        // Contact info should be in metadata
        if (apiSpec.getMetadata().containsKey("contact")) {
            @SuppressWarnings("unchecked")
            Map<String, String> contact = (Map<String, String>) apiSpec.getMetadata().get("contact");
            assertNotNull(contact);
        }
    }

    @Test
    @DisplayName("Should get channel names")
    void testGetChannelNames() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        List<String> channelNames = mapper.getChannelNames(asyncSpec);

        assertNotNull(channelNames);
        assertEquals(3, channelNames.size());
        assertTrue(channelNames.contains("user/created"));
        assertTrue(channelNames.contains("user/updated"));
        assertTrue(channelNames.contains("user/{userId}/notifications"));
    }

    @Test
    @DisplayName("Should get operation IDs")
    void testGetOperationIds() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        List<String> operationIds = mapper.getOperationIds(asyncSpec);

        assertNotNull(operationIds);
        assertFalse(operationIds.isEmpty());
        assertTrue(operationIds.contains("publishUserCreated"));
        assertTrue(operationIds.contains("subscribeUserCreated"));
    }

    @Test
    @DisplayName("Should get message names")
    void testGetMessageNames() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        List<String> messageNames = mapper.getMessageNames(asyncSpec);

        assertNotNull(messageNames);
        assertFalse(messageNames.isEmpty());
        assertTrue(messageNames.contains("UserCreated"));
        assertTrue(messageNames.contains("UserUpdated"));
    }

    @Test
    @DisplayName("Should get server names")
    void testGetServerNames() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        List<String> serverNames = mapper.getServerNames(asyncSpec);

        assertNotNull(serverNames);
        assertEquals(2, serverNames.size());
        assertTrue(serverNames.contains("production"));
        assertTrue(serverNames.contains("staging"));
    }

    @Test
    @DisplayName("Should get statistics")
    void testGetStatistics() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        Map<String, Integer> stats = mapper.getStatistics(asyncSpec);

        assertNotNull(stats);
        assertEquals(3, (int) stats.get("channels"));
        assertEquals(2, (int) stats.get("servers"));
        assertTrue(stats.get("messages") > 0);
        assertTrue(stats.get("operations") > 0);
    }

    @Test
    @DisplayName("Should validate spec")
    void testIsValidSpec() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        assertTrue(mapper.isValidSpec(asyncSpec));

        // Invalid spec (no title)
        AsyncApiSpec invalidSpec = AsyncApiSpec.builder().build();
        assertFalse(mapper.isValidSpec(invalidSpec));

        // Null spec
        assertFalse(mapper.isValidSpec(null));
    }

    @Test
    @DisplayName("Should handle null spec")
    void testMapNullSpec() {
        ApiSpec result = mapper.map(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should map order-events correctly")
    void testMapOrderEvents() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("order-events.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        ApiSpec apiSpec = mapper.map(asyncSpec);

        assertNotNull(apiSpec);
        assertEquals("Order Events API", apiSpec.getName());
        assertEquals("1.0.0", apiSpec.getVersion());

        List<Endpoint> endpoints = mapper.mapEndpoints(asyncSpec);
        assertNotNull(endpoints);
        assertFalse(endpoints.isEmpty());

        // Should have external docs in metadata
        if (asyncSpec.getExternalDocs() != null) {
            assertTrue(apiSpec.getMetadata().containsKey("externalDocsUrl"));
        }
    }

    @Test
    @DisplayName("Should include parsed timestamp")
    void testParsedTimestamp() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        AsyncApiSpec asyncSpec = parser.parse(is);

        ApiSpec apiSpec = mapper.map(asyncSpec);

        assertNotNull(apiSpec.getParsedAt());
    }
}
