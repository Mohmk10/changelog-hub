package io.github.mohmk10.changeloghub.parser.asyncapi.mapper;

import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.RequestBody;
import io.github.mohmk10.changeloghub.core.model.Response;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.*;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.OperationType;

import java.util.*;

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

    public List<Endpoint> mapChannel(AsyncChannel channel) {
        List<Endpoint> endpoints = new ArrayList<>();

        if (channel == null) {
            return endpoints;
        }

        String path = getChannelPath(channel);

        if (channel.getPublishOperation() != null) {
            Endpoint endpoint = mapOperation(path, channel, channel.getPublishOperation());
            if (endpoint != null) {
                endpoints.add(endpoint);
            }
        }

        if (channel.getSubscribeOperation() != null) {
            Endpoint endpoint = mapOperation(path, channel, channel.getSubscribeOperation());
            if (endpoint != null) {
                endpoints.add(endpoint);
            }
        }

        if (endpoints.isEmpty() && channel.getMessages() != null && !channel.getMessages().isEmpty()) {
            
            Endpoint endpoint = createEndpointForMessages(path, channel);
            if (endpoint != null) {
                endpoints.add(endpoint);
            }
        }

        return endpoints;
    }

    public Endpoint mapOperation(String path, AsyncChannel channel, AsyncOperation operation) {
        if (operation == null) {
            return null;
        }

        Endpoint.Builder builder = Endpoint.builder()
                .path(path)
                .method(getHttpMethod(operation.getType()))
                .deprecated(operation.isDeprecated() || (channel != null && channel.isDeprecated()));

        if (operation.getOperationId() != null) {
            builder.operationId(operation.getOperationId());
        } else {
            builder.operationId(generateOperationId(path, operation.getType()));
        }

        if (operation.getSummary() != null) {
            builder.summary(operation.getSummary());
        }
        if (operation.getDescription() != null) {
            builder.description(operation.getDescription());
        }

        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            builder.tags(new ArrayList<>(operation.getTags()));
        }

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

    public Endpoint mapOperationV3(String operationId, AsyncOperation operation,
                                    Map<String, AsyncChannel> channels) {
        if (operation == null) {
            return null;
        }

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

    public List<Endpoint> mapAllChannels(Map<String, AsyncChannel> channels) {
        List<Endpoint> endpoints = new ArrayList<>();
        if (channels != null) {
            for (AsyncChannel channel : channels.values()) {
                endpoints.addAll(mapChannel(channel));
            }
        }
        return endpoints;
    }

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

    public HttpMethod getHttpMethod(OperationType type) {
        if (type == null) {
            return HttpMethod.POST;
        }
        String method = type.getHttpMethod();
        return HttpMethod.valueOf(method);
    }

    public String getChannelPath(AsyncChannel channel) {
        if (channel == null) {
            return "/";
        }
        
        if (channel.getAddress() != null && !channel.getAddress().isEmpty()) {
            return normalizeChannelPath(channel.getAddress());
        }
        return normalizeChannelPath(channel.getName());
    }

    private String normalizeChannelPath(String channelName) {
        if (channelName == null || channelName.isEmpty()) {
            return "/";
        }
        
        if (!channelName.startsWith("/")) {
            return "/" + channelName;
        }
        return channelName;
    }

    private String generateOperationId(String path, OperationType type) {
        String prefix = type == OperationType.PUBLISH ? "publish_" : "subscribe_";
        String safePath = path.replaceAll("[^a-zA-Z0-9]", "_")
                              .replaceAll("_+", "_")
                              .replaceAll("^_|_$", "");
        return prefix + safePath;
    }

    private AsyncMessage getOperationMessage(AsyncOperation operation) {
        if (operation == null) {
            return null;
        }
        
        if (operation.getMessage() != null) {
            return operation.getMessage();
        }
        
        if (operation.getMessages() != null && !operation.getMessages().isEmpty()) {
            return operation.getMessages().get(0);
        }
        return null;
    }

    private Endpoint createEndpointForMessages(String path, AsyncChannel channel) {
        Endpoint.Builder builder = Endpoint.builder()
                .path(path)
                .method(HttpMethod.POST)
                .operationId(generateOperationId(path, OperationType.PUBLISH))
                .deprecated(channel.isDeprecated());

        if (channel.getDescription() != null) {
            builder.description(channel.getDescription());
        }

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

    private List<Response> createAsyncResponses(AsyncOperation operation) {
        List<Response> responses = new ArrayList<>();

        Response accepted = new Response();
        accepted.setStatusCode("202");
        accepted.setDescription("Message accepted for processing");
        accepted.setContentType("application/json");
        responses.add(accepted);

        Response badRequest = new Response();
        badRequest.setStatusCode("400");
        badRequest.setDescription("Invalid message format");
        responses.add(badRequest);

        return responses;
    }

    private List<Response> createDefaultAsyncResponses() {
        List<Response> responses = new ArrayList<>();

        Response accepted = new Response();
        accepted.setStatusCode("202");
        accepted.setDescription("Message accepted");
        responses.add(accepted);

        return responses;
    }

    private String extractChannelName(String channelRef) {
        if (channelRef == null) {
            return null;
        }
        
        int lastSlash = channelRef.lastIndexOf('/');
        return lastSlash >= 0 ? channelRef.substring(lastSlash + 1) : channelRef;
    }

    public String createEndpointSignature(Endpoint endpoint) {
        if (endpoint == null) {
            return "null";
        }
        return endpoint.getMethod() + " " + endpoint.getPath();
    }
}
