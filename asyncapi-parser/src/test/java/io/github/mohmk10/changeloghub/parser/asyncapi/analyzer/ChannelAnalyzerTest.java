package io.github.mohmk10.changeloghub.parser.asyncapi.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChannelAnalyzer.
 */
class ChannelAnalyzerTest {

    private ChannelAnalyzer analyzer;
    private ObjectMapper yamlMapper;

    @BeforeEach
    void setUp() {
        analyzer = new ChannelAnalyzer();
        yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    @Test
    @DisplayName("Should analyze channels from YAML")
    void testAnalyzeChannels() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        JsonNode rootNode = yamlMapper.readTree(is);
        JsonNode channelsNode = rootNode.get("channels");

        Map<String, AsyncChannel> channels = analyzer.analyzeChannels(channelsNode);

        assertNotNull(channels);
        assertEquals(3, channels.size());
        assertTrue(channels.containsKey("user/created"));
        assertTrue(channels.containsKey("user/updated"));
        assertTrue(channels.containsKey("user/{userId}/notifications"));
    }

    @Test
    @DisplayName("Should analyze channel with publish operation")
    void testAnalyzeChannelWithPublish() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        JsonNode rootNode = yamlMapper.readTree(is);
        JsonNode channelsNode = rootNode.get("channels");

        Map<String, AsyncChannel> channels = analyzer.analyzeChannels(channelsNode);
        AsyncChannel createdChannel = channels.get("user/created");

        assertNotNull(createdChannel);
        assertNotNull(createdChannel.getPublishOperation());
        assertEquals("publishUserCreated", createdChannel.getPublishOperation().getOperationId());
    }

    @Test
    @DisplayName("Should analyze channel with subscribe operation")
    void testAnalyzeChannelWithSubscribe() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        JsonNode rootNode = yamlMapper.readTree(is);
        JsonNode channelsNode = rootNode.get("channels");

        Map<String, AsyncChannel> channels = analyzer.analyzeChannels(channelsNode);
        AsyncChannel createdChannel = channels.get("user/created");

        assertNotNull(createdChannel);
        assertNotNull(createdChannel.getSubscribeOperation());
        assertEquals("subscribeUserCreated", createdChannel.getSubscribeOperation().getOperationId());
    }

    @Test
    @DisplayName("Should analyze channel parameters")
    void testAnalyzeChannelParameters() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        JsonNode rootNode = yamlMapper.readTree(is);
        JsonNode channelsNode = rootNode.get("channels");

        Map<String, AsyncChannel> channels = analyzer.analyzeChannels(channelsNode);
        AsyncChannel notifChannel = channels.get("user/{userId}/notifications");

        assertNotNull(notifChannel);
        assertNotNull(notifChannel.getParameters());
        assertEquals(1, notifChannel.getParameters().size());

        AsyncChannel.ChannelParameter userIdParam = notifChannel.getParameters().get("userId");
        assertNotNull(userIdParam);
        assertEquals("The unique user identifier", userIdParam.getDescription());
        assertNotNull(userIdParam.getSchema());
    }

    @Test
    @DisplayName("Should extract channel parameters from path")
    void testExtractChannelParameters() {
        List<String> params = analyzer.extractChannelParameters("user/{userId}/orders/{orderId}");

        assertNotNull(params);
        assertEquals(2, params.size());
        assertTrue(params.contains("userId"));
        assertTrue(params.contains("orderId"));
    }

    @Test
    @DisplayName("Should return empty list for channel without parameters")
    void testExtractNoParameters() {
        List<String> params = analyzer.extractChannelParameters("user/created");

        assertNotNull(params);
        assertTrue(params.isEmpty());
    }

    @Test
    @DisplayName("Should handle null channel name")
    void testExtractNullChannelName() {
        List<String> params = analyzer.extractChannelParameters(null);

        assertNotNull(params);
        assertTrue(params.isEmpty());
    }

    @Test
    @DisplayName("Should analyze channel description")
    void testAnalyzeChannelDescription() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        JsonNode rootNode = yamlMapper.readTree(is);
        JsonNode channelsNode = rootNode.get("channels");

        Map<String, AsyncChannel> channels = analyzer.analyzeChannels(channelsNode);
        AsyncChannel createdChannel = channels.get("user/created");

        assertNotNull(createdChannel);
        assertEquals("Channel for user creation events", createdChannel.getDescription());
    }

    @Test
    @DisplayName("Should check if channel is deprecated")
    void testIsDeprecated() throws Exception {
        // Create a deprecated channel node
        String yaml = "deprecated: true\ndescription: Test channel";
        JsonNode channelNode = yamlMapper.readTree(yaml);

        assertTrue(analyzer.isDeprecated(channelNode));
    }

    @Test
    @DisplayName("Should return false for non-deprecated channel")
    void testIsNotDeprecated() throws Exception {
        String yaml = "description: Test channel";
        JsonNode channelNode = yamlMapper.readTree(yaml);

        assertFalse(analyzer.isDeprecated(channelNode));
    }

    @Test
    @DisplayName("Should handle null channels node")
    void testAnalyzeNullChannels() {
        Map<String, AsyncChannel> channels = analyzer.analyzeChannels(null);

        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    @DisplayName("Should get channel names")
    void testGetChannelNames() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("user-events-v1.yaml");
        JsonNode rootNode = yamlMapper.readTree(is);
        JsonNode channelsNode = rootNode.get("channels");

        List<String> names = analyzer.getChannelNames(channelsNode);

        assertNotNull(names);
        assertEquals(3, names.size());
    }
}
