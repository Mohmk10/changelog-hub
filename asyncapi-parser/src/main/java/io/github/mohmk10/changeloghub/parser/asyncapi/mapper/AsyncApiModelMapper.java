package io.github.mohmk10.changeloghub.parser.asyncapi.mapper;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Maps complete AsyncAPI specification to core ApiSpec model.
 */
public class AsyncApiModelMapper {

    private final AsyncApiEndpointMapper endpointMapper;
    private final AsyncApiSchemaMapper schemaMapper;

    public AsyncApiModelMapper() {
        this.schemaMapper = new AsyncApiSchemaMapper();
        AsyncApiParameterMapper parameterMapper = new AsyncApiParameterMapper(schemaMapper);
        this.endpointMapper = new AsyncApiEndpointMapper(parameterMapper, schemaMapper);
    }

    public AsyncApiModelMapper(AsyncApiEndpointMapper endpointMapper, AsyncApiSchemaMapper schemaMapper) {
        this.endpointMapper = endpointMapper;
        this.schemaMapper = schemaMapper;
    }

    /**
     * Map AsyncApiSpec to core ApiSpec.
     */
    public ApiSpec map(AsyncApiSpec asyncSpec) {
        if (asyncSpec == null) {
            return null;
        }

        ApiSpec.Builder builder = ApiSpec.builder();

        // Name and version
        builder.name(asyncSpec.getTitle() != null ? asyncSpec.getTitle() : "Untitled AsyncAPI");
        builder.version(asyncSpec.getApiVersion() != null ? asyncSpec.getApiVersion() : "1.0.0");

        // Type is ASYNCAPI
        builder.type(ApiType.ASYNCAPI);

        // Map endpoints from channels/operations
        List<Endpoint> endpoints = mapEndpoints(asyncSpec);
        builder.endpoints(endpoints);

        // Metadata
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("asyncapi_version", asyncSpec.getAsyncApiVersion() != null ?
                asyncSpec.getAsyncApiVersion().getVersion() : "2.6.0");
        if (asyncSpec.getDescription() != null) {
            metadata.put("description", asyncSpec.getDescription());
        }
        if (asyncSpec.getContact() != null) {
            Map<String, String> contact = new HashMap<>();
            if (asyncSpec.getContact().getName() != null) {
                contact.put("name", asyncSpec.getContact().getName());
            }
            if (asyncSpec.getContact().getEmail() != null) {
                contact.put("email", asyncSpec.getContact().getEmail());
            }
            if (asyncSpec.getContact().getUrl() != null) {
                contact.put("url", asyncSpec.getContact().getUrl());
            }
            if (!contact.isEmpty()) {
                metadata.put("contact", contact);
            }
        }
        if (asyncSpec.getLicense() != null) {
            Map<String, String> license = new HashMap<>();
            if (asyncSpec.getLicense().getName() != null) {
                license.put("name", asyncSpec.getLicense().getName());
            }
            if (asyncSpec.getLicense().getUrl() != null) {
                license.put("url", asyncSpec.getLicense().getUrl());
            }
            if (!license.isEmpty()) {
                metadata.put("license", license);
            }
        }
        if (asyncSpec.getExternalDocs() != null && asyncSpec.getExternalDocs().getUrl() != null) {
            metadata.put("externalDocsUrl", asyncSpec.getExternalDocs().getUrl());
        }
        if (asyncSpec.getTags() != null && !asyncSpec.getTags().isEmpty()) {
            List<String> tagNames = new ArrayList<>();
            for (AsyncApiSpec.Tag tag : asyncSpec.getTags()) {
                tagNames.add(tag.getName());
            }
            metadata.put("tags", tagNames);
        }

        // Add server info
        String baseUrl = extractBaseUrl(asyncSpec);
        if (baseUrl != null) {
            metadata.put("baseUrl", baseUrl);
        }

        builder.metadata(metadata);

        // Parsed at
        builder.parsedAt(LocalDateTime.now());

        return builder.build();
    }

    /**
     * Map endpoints from channels and operations.
     */
    public List<Endpoint> mapEndpoints(AsyncApiSpec asyncSpec) {
        List<Endpoint> endpoints = new ArrayList<>();

        if (asyncSpec == null) {
            return endpoints;
        }

        // Check AsyncAPI version
        boolean isV3 = asyncSpec.getAsyncApiVersion() != null &&
                       asyncSpec.getAsyncApiVersion().isV3();

        if (isV3) {
            // AsyncAPI 3.x: operations are separate from channels
            if (asyncSpec.getOperations() != null && !asyncSpec.getOperations().isEmpty()) {
                endpoints.addAll(endpointMapper.mapAllOperationsV3(
                        asyncSpec.getOperations(), asyncSpec.getChannels()));
            } else if (asyncSpec.getChannels() != null) {
                // Fallback to channel-based mapping
                endpoints.addAll(endpointMapper.mapAllChannels(asyncSpec.getChannels()));
            }
        } else {
            // AsyncAPI 2.x: operations embedded in channels
            if (asyncSpec.getChannels() != null) {
                endpoints.addAll(endpointMapper.mapAllChannels(asyncSpec.getChannels()));
            }
        }

        return endpoints;
    }

    /**
     * Extract base URL from servers.
     */
    public String extractBaseUrl(AsyncApiSpec asyncSpec) {
        if (asyncSpec == null || asyncSpec.getServers() == null || asyncSpec.getServers().isEmpty()) {
            return null;
        }

        // Try to find a production server first
        for (Map.Entry<String, AsyncServer> entry : asyncSpec.getServers().entrySet()) {
            String serverName = entry.getKey().toLowerCase();
            if (serverName.contains("prod") || serverName.contains("production")) {
                return buildServerUrl(entry.getValue());
            }
        }

        // Otherwise return first server
        AsyncServer firstServer = asyncSpec.getServers().values().iterator().next();
        return buildServerUrl(firstServer);
    }

    /**
     * Build URL from server definition.
     */
    private String buildServerUrl(AsyncServer server) {
        if (server == null || server.getUrl() == null) {
            return null;
        }

        String url = server.getUrl();

        // Add protocol if not present
        if (!url.contains("://") && server.getProtocol() != null) {
            String protocol = server.getProtocol().name().toLowerCase();
            // Map messaging protocols to URL schemes
            switch (protocol) {
                case "kafka":
                    url = "kafka://" + url;
                    break;
                case "amqp":
                case "amqps":
                    url = protocol + "://" + url;
                    break;
                case "mqtt":
                case "mqtts":
                    url = protocol + "://" + url;
                    break;
                case "ws":
                    url = "ws://" + url;
                    break;
                case "wss":
                    url = "wss://" + url;
                    break;
                default:
                    url = protocol + "://" + url;
            }
        }

        return url;
    }

    /**
     * Get all channel names from spec.
     */
    public List<String> getChannelNames(AsyncApiSpec asyncSpec) {
        List<String> names = new ArrayList<>();
        if (asyncSpec != null && asyncSpec.getChannels() != null) {
            names.addAll(asyncSpec.getChannels().keySet());
        }
        return names;
    }

    /**
     * Get all operation IDs from spec.
     */
    public List<String> getOperationIds(AsyncApiSpec asyncSpec) {
        List<String> ids = new ArrayList<>();

        if (asyncSpec == null) {
            return ids;
        }

        // AsyncAPI 3.x operations
        if (asyncSpec.getOperations() != null) {
            ids.addAll(asyncSpec.getOperations().keySet());
        }

        // AsyncAPI 2.x channel operations
        if (asyncSpec.getChannels() != null) {
            for (AsyncChannel channel : asyncSpec.getChannels().values()) {
                if (channel.getPublishOperation() != null &&
                    channel.getPublishOperation().getOperationId() != null) {
                    ids.add(channel.getPublishOperation().getOperationId());
                }
                if (channel.getSubscribeOperation() != null &&
                    channel.getSubscribeOperation().getOperationId() != null) {
                    ids.add(channel.getSubscribeOperation().getOperationId());
                }
            }
        }

        return ids;
    }

    /**
     * Get all message names from spec.
     */
    public List<String> getMessageNames(AsyncApiSpec asyncSpec) {
        Set<String> names = new LinkedHashSet<>();

        if (asyncSpec == null) {
            return new ArrayList<>(names);
        }

        // Component messages
        if (asyncSpec.getComponents() != null && asyncSpec.getComponents().getMessages() != null) {
            names.addAll(asyncSpec.getComponents().getMessages().keySet());
        }

        // Channel messages
        if (asyncSpec.getChannels() != null) {
            for (AsyncChannel channel : asyncSpec.getChannels().values()) {
                if (channel.getMessages() != null) {
                    names.addAll(channel.getMessages().keySet());
                }
            }
        }

        return new ArrayList<>(names);
    }

    /**
     * Get all server names from spec.
     */
    public List<String> getServerNames(AsyncApiSpec asyncSpec) {
        List<String> names = new ArrayList<>();
        if (asyncSpec != null && asyncSpec.getServers() != null) {
            names.addAll(asyncSpec.getServers().keySet());
        }
        return names;
    }

    /**
     * Get statistics from mapped spec.
     */
    public Map<String, Integer> getStatistics(AsyncApiSpec asyncSpec) {
        Map<String, Integer> stats = new LinkedHashMap<>();

        if (asyncSpec == null) {
            stats.put("channels", 0);
            stats.put("operations", 0);
            stats.put("messages", 0);
            stats.put("servers", 0);
            stats.put("schemas", 0);
            return stats;
        }

        stats.put("channels", asyncSpec.getChannels() != null ? asyncSpec.getChannels().size() : 0);
        stats.put("operations", getOperationIds(asyncSpec).size());
        stats.put("messages", getMessageNames(asyncSpec).size());
        stats.put("servers", asyncSpec.getServers() != null ? asyncSpec.getServers().size() : 0);
        stats.put("schemas", asyncSpec.getComponents() != null && asyncSpec.getComponents().getSchemas() != null ?
                asyncSpec.getComponents().getSchemas().size() : 0);

        return stats;
    }

    /**
     * Check if spec is valid (has at least channels or operations).
     */
    public boolean isValidSpec(AsyncApiSpec asyncSpec) {
        if (asyncSpec == null) {
            return false;
        }

        // Must have title
        if (asyncSpec.getTitle() == null || asyncSpec.getTitle().isEmpty()) {
            return false;
        }

        // Must have channels or operations
        boolean hasChannels = asyncSpec.getChannels() != null && !asyncSpec.getChannels().isEmpty();
        boolean hasOperations = asyncSpec.getOperations() != null && !asyncSpec.getOperations().isEmpty();

        return hasChannels || hasOperations;
    }
}
