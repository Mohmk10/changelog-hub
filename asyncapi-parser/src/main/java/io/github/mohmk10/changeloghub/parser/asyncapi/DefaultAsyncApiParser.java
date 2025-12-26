package io.github.mohmk10.changeloghub.parser.asyncapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.mohmk10.changeloghub.parser.asyncapi.analyzer.*;
import io.github.mohmk10.changeloghub.parser.asyncapi.exception.AsyncApiParseException;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.*;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiConstants;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class DefaultAsyncApiParser implements AsyncApiParser {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncApiParser.class);

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;
    private final ChannelAnalyzer channelAnalyzer;
    private final OperationAnalyzer operationAnalyzer;
    private final ServerAnalyzer serverAnalyzer;
    private final MessageAnalyzer messageAnalyzer;
    private final SchemaAnalyzer schemaAnalyzer;

    public DefaultAsyncApiParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
        this.schemaAnalyzer = new SchemaAnalyzer();
        this.messageAnalyzer = new MessageAnalyzer(schemaAnalyzer);
        this.operationAnalyzer = new OperationAnalyzer(messageAnalyzer);
        this.channelAnalyzer = new ChannelAnalyzer(operationAnalyzer, messageAnalyzer, schemaAnalyzer);
        this.serverAnalyzer = new ServerAnalyzer();
    }

    @Override
    public AsyncApiSpec parse(String content) throws AsyncApiParseException {
        if (content == null || content.trim().isEmpty()) {
            throw new AsyncApiParseException("Content cannot be null or empty");
        }

        try {
            JsonNode rootNode = parseToJsonNode(content);
            return parseFromJsonNode(rootNode);
        } catch (AsyncApiParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AsyncApiParseException("Failed to parse AsyncAPI content: " + e.getMessage(), e);
        }
    }

    @Override
    public AsyncApiSpec parseFile(File file) throws AsyncApiParseException {
        if (file == null) {
            throw new AsyncApiParseException("File cannot be null");
        }
        if (!file.exists()) {
            throw new AsyncApiParseException("File not found: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new AsyncApiParseException("File cannot be read: " + file.getAbsolutePath());
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            return parse(content);
        } catch (IOException e) {
            throw new AsyncApiParseException("Failed to read file: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public AsyncApiSpec parseFile(String filePath) throws AsyncApiParseException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new AsyncApiParseException("File path cannot be null or empty");
        }
        return parseFile(new File(filePath));
    }

    @Override
    public AsyncApiSpec parseUrl(URL url) throws AsyncApiParseException {
        if (url == null) {
            throw new AsyncApiParseException("URL cannot be null");
        }

        try (InputStream is = url.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return parse(content.toString());
        } catch (IOException e) {
            throw new AsyncApiParseException("Failed to fetch URL: " + url, e);
        }
    }

    @Override
    public AsyncApiSpec parseUrl(String urlString) throws AsyncApiParseException {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new AsyncApiParseException("URL string cannot be null or empty");
        }

        try {
            return parseUrl(new URL(urlString));
        } catch (java.net.MalformedURLException e) {
            throw new AsyncApiParseException("Invalid URL: " + urlString, e);
        }
    }

    @Override
    public AsyncApiSpec parse(InputStream inputStream) throws AsyncApiParseException {
        if (inputStream == null) {
            throw new AsyncApiParseException("Input stream cannot be null");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return parse(content.toString());
        } catch (IOException e) {
            throw new AsyncApiParseException("Failed to read input stream", e);
        }
    }

    @Override
    public boolean isValid(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        try {
            JsonNode rootNode = parseToJsonNode(content);
            return rootNode.has(AsyncApiConstants.ASYNCAPI) || rootNode.has("asyncapi");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String detectVersion(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        try {
            JsonNode rootNode = parseToJsonNode(content);
            if (rootNode.has(AsyncApiConstants.ASYNCAPI)) {
                return rootNode.get(AsyncApiConstants.ASYNCAPI).asText();
            }
            if (rootNode.has("asyncapi")) {
                return rootNode.get("asyncapi").asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private JsonNode parseToJsonNode(String content) throws AsyncApiParseException {
        String trimmed = content.trim();

        try {
            return yamlMapper.readTree(content);
        } catch (Exception yamlEx) {
            
            if (trimmed.startsWith("{")) {
                try {
                    return jsonMapper.readTree(content);
                } catch (Exception jsonEx) {
                    throw new AsyncApiParseException("Failed to parse content as YAML or JSON", jsonEx);
                }
            }
            throw new AsyncApiParseException("Failed to parse YAML content", yamlEx);
        }
    }

    private AsyncApiSpec parseFromJsonNode(JsonNode rootNode) throws AsyncApiParseException {
        
        if (!rootNode.has(AsyncApiConstants.ASYNCAPI)) {
            throw new AsyncApiParseException("Missing required 'asyncapi' field");
        }

        String versionStr = rootNode.get(AsyncApiConstants.ASYNCAPI).asText();
        AsyncApiVersion version = AsyncApiVersion.fromString(versionStr);

        if (version == null || !version.isSupported()) {
            throw new AsyncApiParseException("Unsupported AsyncAPI version: " + versionStr);
        }

        logger.debug("Parsing AsyncAPI version: {}", version.getVersion());

        AsyncApiSpec.Builder builder = AsyncApiSpec.builder()
                .asyncApiVersion(version);

        if (rootNode.has(AsyncApiConstants.INFO)) {
            parseInfo(builder, rootNode.get(AsyncApiConstants.INFO));
        }

        if (rootNode.has(AsyncApiConstants.SERVERS)) {
            builder.servers(serverAnalyzer.analyzeServers(rootNode.get(AsyncApiConstants.SERVERS)));
        }

        if (rootNode.has(AsyncApiConstants.CHANNELS)) {
            builder.channels(channelAnalyzer.analyzeChannels(rootNode.get(AsyncApiConstants.CHANNELS)));
        }

        if (version.isV3() && rootNode.has(AsyncApiConstants.OPERATIONS)) {
            builder.operations(operationAnalyzer.analyzeOperationsV3(rootNode.get(AsyncApiConstants.OPERATIONS)));
        }

        if (rootNode.has(AsyncApiConstants.COMPONENTS)) {
            builder.components(parseComponents(rootNode.get(AsyncApiConstants.COMPONENTS)));
        }

        if (rootNode.has(AsyncApiConstants.TAGS)) {
            builder.tags(parseTags(rootNode.get(AsyncApiConstants.TAGS)));
        }

        if (rootNode.has(AsyncApiConstants.EXTERNAL_DOCS)) {
            builder.externalDocs(parseExternalDocs(rootNode.get(AsyncApiConstants.EXTERNAL_DOCS)));
        }

        return builder.build();
    }

    private void parseInfo(AsyncApiSpec.Builder builder, JsonNode infoNode) {
        if (infoNode.has(AsyncApiConstants.TITLE)) {
            builder.title(infoNode.get(AsyncApiConstants.TITLE).asText());
        }

        if (infoNode.has(AsyncApiConstants.VERSION)) {
            builder.apiVersion(infoNode.get(AsyncApiConstants.VERSION).asText());
        }

        if (infoNode.has(AsyncApiConstants.DESCRIPTION)) {
            builder.description(infoNode.get(AsyncApiConstants.DESCRIPTION).asText());
        }

        if (infoNode.has(AsyncApiConstants.TERMS_OF_SERVICE)) {
            builder.termsOfService(infoNode.get(AsyncApiConstants.TERMS_OF_SERVICE).asText());
        }

        if (infoNode.has(AsyncApiConstants.CONTACT)) {
            builder.contact(parseContact(infoNode.get(AsyncApiConstants.CONTACT)));
        }

        if (infoNode.has(AsyncApiConstants.LICENSE)) {
            builder.license(parseLicense(infoNode.get(AsyncApiConstants.LICENSE)));
        }
    }

    private AsyncApiSpec.Contact parseContact(JsonNode contactNode) {
        AsyncApiSpec.Contact contact = new AsyncApiSpec.Contact();
        if (contactNode.has(AsyncApiConstants.NAME)) {
            contact.setName(contactNode.get(AsyncApiConstants.NAME).asText());
        }
        if (contactNode.has(AsyncApiConstants.EMAIL)) {
            contact.setEmail(contactNode.get(AsyncApiConstants.EMAIL).asText());
        }
        if (contactNode.has(AsyncApiConstants.URL)) {
            contact.setUrl(contactNode.get(AsyncApiConstants.URL).asText());
        }
        return contact;
    }

    private AsyncApiSpec.License parseLicense(JsonNode licenseNode) {
        AsyncApiSpec.License license = new AsyncApiSpec.License();
        if (licenseNode.has(AsyncApiConstants.NAME)) {
            license.setName(licenseNode.get(AsyncApiConstants.NAME).asText());
        }
        if (licenseNode.has(AsyncApiConstants.URL)) {
            license.setUrl(licenseNode.get(AsyncApiConstants.URL).asText());
        }
        return license;
    }

    private AsyncApiSpec.Components parseComponents(JsonNode componentsNode) {
        AsyncApiSpec.Components components = new AsyncApiSpec.Components();

        if (componentsNode.has(AsyncApiConstants.SCHEMAS)) {
            components.setSchemas(schemaAnalyzer.analyzeSchemas(
                    componentsNode.get(AsyncApiConstants.SCHEMAS)));
        }

        if (componentsNode.has(AsyncApiConstants.MESSAGES)) {
            components.setMessages(messageAnalyzer.analyzeMessages(
                    componentsNode.get(AsyncApiConstants.MESSAGES)));
        }

        if (componentsNode.has(AsyncApiConstants.SECURITY_SCHEMES)) {
            components.setSecuritySchemes(parseSecuritySchemes(
                    componentsNode.get(AsyncApiConstants.SECURITY_SCHEMES)));
        }

        if (componentsNode.has(AsyncApiConstants.PARAMETERS)) {
            components.setParameters(parseComponentParameters(
                    componentsNode.get(AsyncApiConstants.PARAMETERS)));
        }

        if (componentsNode.has(AsyncApiConstants.CORRELATION_IDS)) {
            components.setCorrelationIds(parseCorrelationIds(
                    componentsNode.get(AsyncApiConstants.CORRELATION_IDS)));
        }

        if (componentsNode.has(AsyncApiConstants.SERVER_BINDINGS)) {
            components.setServerBindings(parseBindingsMap(
                    componentsNode.get(AsyncApiConstants.SERVER_BINDINGS)));
        }

        if (componentsNode.has(AsyncApiConstants.CHANNEL_BINDINGS)) {
            components.setChannelBindings(parseBindingsMap(
                    componentsNode.get(AsyncApiConstants.CHANNEL_BINDINGS)));
        }

        if (componentsNode.has(AsyncApiConstants.OPERATION_BINDINGS)) {
            components.setOperationBindings(parseBindingsMap(
                    componentsNode.get(AsyncApiConstants.OPERATION_BINDINGS)));
        }

        if (componentsNode.has(AsyncApiConstants.MESSAGE_BINDINGS)) {
            components.setMessageBindings(parseBindingsMap(
                    componentsNode.get(AsyncApiConstants.MESSAGE_BINDINGS)));
        }

        return components;
    }

    private Map<String, Object> parseSecuritySchemes(JsonNode node) {
        Map<String, Object> schemes = new LinkedHashMap<>();
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                schemes.put(entry.getKey(), parseObjectNode(entry.getValue()));
            }
        }
        return schemes;
    }

    private Map<String, AsyncChannel.ChannelParameter> parseComponentParameters(JsonNode node) {
        Map<String, AsyncChannel.ChannelParameter> params = new LinkedHashMap<>();
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                AsyncChannel.ChannelParameter param = new AsyncChannel.ChannelParameter();
                param.setName(entry.getKey());

                JsonNode paramNode = entry.getValue();
                if (paramNode.has(AsyncApiConstants.DESCRIPTION)) {
                    param.setDescription(paramNode.get(AsyncApiConstants.DESCRIPTION).asText());
                }
                if (paramNode.has("schema")) {
                    param.setSchema(schemaAnalyzer.analyzeSchema(paramNode.get("schema")));
                }
                if (paramNode.has("location")) {
                    param.setLocation(paramNode.get("location").asText());
                }

                params.put(entry.getKey(), param);
            }
        }
        return params;
    }

    private Map<String, Object> parseCorrelationIds(JsonNode node) {
        Map<String, Object> ids = new LinkedHashMap<>();
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                ids.put(entry.getKey(), parseObjectNode(entry.getValue()));
            }
        }
        return ids;
    }

    private Map<String, Object> parseBindingsMap(JsonNode node) {
        Map<String, Object> bindings = new LinkedHashMap<>();
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                bindings.put(entry.getKey(), parseObjectNode(entry.getValue()));
            }
        }
        return bindings;
    }

    private Map<String, Object> parseObjectNode(JsonNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                map.put(entry.getKey(), parseValue(entry.getValue()));
            }
        }
        return map;
    }

    private Object parseValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        } else if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            return node.numberValue();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : node) {
                list.add(parseValue(item));
            }
            return list;
        } else if (node.isObject()) {
            return parseObjectNode(node);
        }
        return null;
    }

    private List<AsyncApiSpec.Tag> parseTags(JsonNode tagsNode) {
        List<AsyncApiSpec.Tag> tags = new ArrayList<>();
        if (tagsNode != null && tagsNode.isArray()) {
            for (JsonNode tagNode : tagsNode) {
                AsyncApiSpec.Tag tag = new AsyncApiSpec.Tag();
                if (tagNode.has(AsyncApiConstants.NAME)) {
                    tag.setName(tagNode.get(AsyncApiConstants.NAME).asText());
                }
                if (tagNode.has(AsyncApiConstants.DESCRIPTION)) {
                    tag.setDescription(tagNode.get(AsyncApiConstants.DESCRIPTION).asText());
                }
                if (tagNode.has(AsyncApiConstants.EXTERNAL_DOCS)) {
                    tag.setExternalDocs(parseExternalDocs(tagNode.get(AsyncApiConstants.EXTERNAL_DOCS)));
                }
                tags.add(tag);
            }
        }
        return tags;
    }

    private AsyncApiSpec.ExternalDocs parseExternalDocs(JsonNode docsNode) {
        AsyncApiSpec.ExternalDocs docs = new AsyncApiSpec.ExternalDocs();
        if (docsNode.has(AsyncApiConstants.URL)) {
            docs.setUrl(docsNode.get(AsyncApiConstants.URL).asText());
        }
        if (docsNode.has(AsyncApiConstants.DESCRIPTION)) {
            docs.setDescription(docsNode.get(AsyncApiConstants.DESCRIPTION).asText());
        }
        return docs;
    }
}
