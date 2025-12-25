package io.github.mohmk10.changeloghub.parser.asyncapi.mapper;

import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.RequestBody;
import io.github.mohmk10.changeloghub.core.model.Response;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.*;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.OperationType;

import java.util.*;

/**
 * Maps AsyncAPI channel operations to core Endpoint model.
 *
 * Mapping convention:
 * - PUBLISH operation → POST method (sending messages)
 * - SUBSCRIBE operation → GET method (receiving messages)
 */
public class AsyncApiEndpointMapper {

    private final AsyncApiParameterMapper parameterMapper;
    private final AsyncApiSchemaMapper schemaMapper;

    public AsyncApiEndpointMapper() {
        this.schemaMapper = new AsyncApiSchemaMapper();
        this.parameterMapper = new AsyncApiParameterMapper(schemaMapper);
    }

    public AsyncApiEndpointMapper(AsyncApiParameterMapper parameterMapper, AsyncApiSchemaMapper schemaMapper) {
        this.parameterMapper = parameterMapper;
        this.schemaMapper = schemaMapper;
    }

    /**
     * Map a channel to endpoints (one per operation).
     */
    public List<Endpoint> mapChannel(AsyncChannel channel) {
        List<Endpoint> endpoints = new ArrayList<>();

        if (channel == null) {
            return endpoints;
        }

        String path = getChannelPath(channel);

        // Publish operation (AsyncAPI 2.x)
        if (channel.getPublishOperation() != null) {
            Endpoint endpoint = mapOperation(path, channel, channel.getPublishOperation());
            if (endpoint != null) {
                endpoints.add(endpoint);
            }
        }

        // Subscribe operation (AsyncAPI 2.x)
        if (channel.getSubscribeOperation() != null) {
            Endpoint endpoint = mapOperation(path, channel, channel.getSubscribeOperation());
            if (endpoint != null) {
                endpoints.add(endpoint);
            }
        }

        // If no operations but messages exist (AsyncAPI 3.x style), create generic endpoints
        if (endpoints.isEmpty() && channel.getMessages() != null && !channel.getMessages().isEmpty()) {
            // Default to publish behavior for channels with messages
            Endpoint endpoint = createEndpointForMessages(path, channel);
            if (endpoint != null) {
                endpoints.add(endpoint);
            }
        }

        return endpoints;
    }

    /**
     * Map an operation to an endpoint.
     */
    public Endpoint mapOperation(String path, AsyncChannel channel, AsyncOperation operation) {
        if (operation == null) {
            return null;
        }

        Endpoint.Builder builder = Endpoint.builder()
                .path(path)
                .method(getHttpMethod(operation.getType()))
                .deprecated(operation.isDeprecated() || (channel != null && channel.isDeprecated()));

        // Operation ID as endpoint ID
        if (operation.getOperationId() != null) {
            builder.operationId(operation.getOperationId());
        } else {
            builder.operationId(generateOperationId(path, operation.getType()));
        }

        // Summary and description
        if (operation.getSummary() != null) {
            builder.summary(operation.getSummary());
        }
        if (operation.getDescription() != null) {
            builder.description(operation.getDescription());
        }

        // Tags
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            builder.tags(new ArrayList<>(operation.getTags()));
        }

        // Parameters from channel and message
        List<Parameter> parameters = new ArrayList<>();
        if (channel != null) {
            parameters.addAll(parameterMapper.mapChannelParameters(channel));
        }

        // Get message parameters
        AsyncMessage message = getOperationMessage(operation);
        if (message != null) {
            parameters.addAll(parameterMapper.mapAllMessageParameters(message));

            // Request body (message payload)
            if (message.getPayload() != null) {
                RequestBody requestBody = new RequestBody();
                requestBody.setContentType(message.getContentType() != null ? message.getContentType() : "application/json");
                requestBody.setSchemaRef(schemaMapper.mapToSchemaRef(message.getPayload()));
                requestBody.setRequired(true);
                builder.requestBody(requestBody);
            }
        }
        builder.parameters(parameters);

        // Responses (for async, typically a 202 Accepted or void)
        builder.responses(createAsyncResponses(operation));

        return builder.build();
    }

    /**
     * Map an AsyncAPI 3.x operation.
     */
    public Endpoint mapOperationV3(String operationId, AsyncOperation operation,
                                    Map<String, AsyncChannel> channels) {
        if (operation == null) {
            return null;
        }

        // Resolve channel reference
        AsyncChannel channel = null;
        String path = null;
        if (operation.getChannelRef() != null) {
            String channelName = extractChannelName(operation.getChannelRef());
            channel = channels != null ? channels.get(channelName) : null;
            path = channel != null ? getChannelPath(channel) : "/" + channelName;
        } else {
            path = "/" + operationId;
        }

        Endpoint.Builder builder = Endpoint.builder()
                .path(path)
                .method(getHttpMethod(operation.getType()))
                .operationId(operationId)
                .deprecated(operation.isDeprecated());

        if (operation.getSummary() != null) {
            builder.summary(operation.getSummary());
        }
        if (operation.getDescription() != null) {
            builder.description(operation.getDescription());
        }
        if (operation.getTags() != null) {
            builder.tags(new ArrayList<>(operation.getTags()));
        }

        // Parameters
        List<Parameter> parameters = new ArrayList<>();
        if (channel != null) {
            parameters.addAll(parameterMapper.mapChannelParameters(channel));
        }

        AsyncMessage message = getOperationMessage(operation);
        if (message != null) {
            parameters.addAll(parameterMapper.mapAllMessageParameters(message));
            if (message.getPayload() != null) {
                RequestBody requestBody = new RequestBody();
                requestBody.setContentType(message.getContentType() != null ? message.getContentType() : "application/json");
                requestBody.setSchemaRef(schemaMapper.mapToSchemaRef(message.getPayload()));
                requestBody.setRequired(true);
                builder.requestBody(requestBody);
            }
        }
        builder.parameters(parameters);

        builder.responses(createAsyncResponses(operation));

        return builder.build();
    }

    /**
     * Map all channels to endpoints.
     */
    public List<Endpoint> mapAllChannels(Map<String, AsyncChannel> channels) {
        List<Endpoint> endpoints = new ArrayList<>();
        if (channels != null) {
            for (AsyncChannel channel : channels.values()) {
                endpoints.addAll(mapChannel(channel));
            }
        }
        return endpoints;
    }

    /**
     * Map all AsyncAPI 3.x operations.
     */
    public List<Endpoint> mapAllOperationsV3(Map<String, AsyncOperation> operations,
                                              Map<String, AsyncChannel> channels) {
        List<Endpoint> endpoints = new ArrayList<>();
        if (operations != null) {
            for (Map.Entry<String, AsyncOperation> entry : operations.entrySet()) {
                Endpoint endpoint = mapOperationV3(entry.getKey(), entry.getValue(), channels);
                if (endpoint != null) {
                    endpoints.add(endpoint);
                }
            }
        }
        return endpoints;
    }

    /**
     * Get HTTP method from operation type.
     */
    public HttpMethod getHttpMethod(OperationType type) {
        if (type == null) {
            return HttpMethod.POST;
        }
        String method = type.getHttpMethod();
        return HttpMethod.valueOf(method);
    }

    /**
     * Get channel path (address or name).
     */
    public String getChannelPath(AsyncChannel channel) {
        if (channel == null) {
            return "/";
        }
        // Prefer address (AsyncAPI 3.x) over name
        if (channel.getAddress() != null && !channel.getAddress().isEmpty()) {
            return normalizeChannelPath(channel.getAddress());
        }
        return normalizeChannelPath(channel.getName());
    }

    /**
     * Normalize channel path to URL format.
     */
    private String normalizeChannelPath(String channelName) {
        if (channelName == null || channelName.isEmpty()) {
            return "/";
        }
        // Ensure path starts with /
        if (!channelName.startsWith("/")) {
            return "/" + channelName;
        }
        return channelName;
    }

    /**
     * Generate operation ID from path and type.
     */
    private String generateOperationId(String path, OperationType type) {
        String prefix = type == OperationType.PUBLISH ? "publish_" : "subscribe_";
        String safePath = path.replaceAll("[^a-zA-Z0-9]", "_")
                              .replaceAll("_+", "_")
                              .replaceAll("^_|_$", "");
        return prefix + safePath;
    }

    /**
     * Get the primary message from an operation.
     */
    private AsyncMessage getOperationMessage(AsyncOperation operation) {
        if (operation == null) {
            return null;
        }
        // Return single message if available
        if (operation.getMessage() != null) {
            return operation.getMessage();
        }
        // Return first message from list
        if (operation.getMessages() != null && !operation.getMessages().isEmpty()) {
            return operation.getMessages().get(0);
        }
        return null;
    }

    /**
     * Create endpoint for channel messages (when no explicit operations).
     */
    private Endpoint createEndpointForMessages(String path, AsyncChannel channel) {
        Endpoint.Builder builder = Endpoint.builder()
                .path(path)
                .method(HttpMethod.POST)
                .operationId(generateOperationId(path, OperationType.PUBLISH))
                .deprecated(channel.isDeprecated());

        if (channel.getDescription() != null) {
            builder.description(channel.getDescription());
        }

        // Get first message
        if (channel.getMessages() != null && !channel.getMessages().isEmpty()) {
            AsyncMessage firstMessage = channel.getMessages().values().iterator().next();
            builder.parameters(parameterMapper.mapEndpointParameters(channel, firstMessage));
            if (firstMessage.getPayload() != null) {
                RequestBody requestBody = new RequestBody();
                requestBody.setContentType(firstMessage.getContentType() != null ? firstMessage.getContentType() : "application/json");
                requestBody.setSchemaRef(schemaMapper.mapToSchemaRef(firstMessage.getPayload()));
                requestBody.setRequired(true);
                builder.requestBody(requestBody);
            }
        } else {
            builder.parameters(parameterMapper.mapChannelParameters(channel));
        }

        builder.responses(createDefaultAsyncResponses());

        return builder.build();
    }

    /**
     * Create async responses (typically 202 Accepted).
     */
    private List<Response> createAsyncResponses(AsyncOperation operation) {
        List<Response> responses = new ArrayList<>();

        // 202 Accepted for async operations
        Response accepted = new Response();
        accepted.setStatusCode("202");
        accepted.setDescription("Message accepted for processing");
        accepted.setContentType("application/json");
        responses.add(accepted);

        // 400 Bad Request
        Response badRequest = new Response();
        badRequest.setStatusCode("400");
        badRequest.setDescription("Invalid message format");
        responses.add(badRequest);

        return responses;
    }

    /**
     * Create default async responses.
     */
    private List<Response> createDefaultAsyncResponses() {
        List<Response> responses = new ArrayList<>();

        Response accepted = new Response();
        accepted.setStatusCode("202");
        accepted.setDescription("Message accepted");
        responses.add(accepted);

        return responses;
    }

    /**
     * Extract channel name from reference.
     */
    private String extractChannelName(String channelRef) {
        if (channelRef == null) {
            return null;
        }
        // Handle #/channels/channelName format
        int lastSlash = channelRef.lastIndexOf('/');
        return lastSlash >= 0 ? channelRef.substring(lastSlash + 1) : channelRef;
    }

    /**
     * Create endpoint signature for comparison.
     */
    public String createEndpointSignature(Endpoint endpoint) {
        if (endpoint == null) {
            return "null";
        }
        return endpoint.getMethod() + " " + endpoint.getPath();
    }
}
