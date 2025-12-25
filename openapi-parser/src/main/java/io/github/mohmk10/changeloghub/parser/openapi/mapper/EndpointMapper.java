package io.github.mohmk10.changeloghub.parser.openapi.mapper;

import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.RequestBody;
import io.github.mohmk10.changeloghub.core.model.Response;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EndpointMapper {

    private final ParameterMapper parameterMapper;
    private final ResponseMapper responseMapper;

    public EndpointMapper() {
        this.parameterMapper = new ParameterMapper();
        this.responseMapper = new ResponseMapper();
    }

    public EndpointMapper(ParameterMapper parameterMapper, ResponseMapper responseMapper) {
        this.parameterMapper = parameterMapper;
        this.responseMapper = responseMapper;
    }

    public List<Endpoint> map(String path, PathItem pathItem) {
        List<Endpoint> endpoints = new ArrayList<>();

        if (pathItem == null) {
            return endpoints;
        }

        List<io.swagger.v3.oas.models.parameters.Parameter> commonParams = pathItem.getParameters();

        if (pathItem.getGet() != null) {
            endpoints.add(mapOperation(path, HttpMethod.GET, pathItem.getGet(), commonParams));
        }
        if (pathItem.getPost() != null) {
            endpoints.add(mapOperation(path, HttpMethod.POST, pathItem.getPost(), commonParams));
        }
        if (pathItem.getPut() != null) {
            endpoints.add(mapOperation(path, HttpMethod.PUT, pathItem.getPut(), commonParams));
        }
        if (pathItem.getDelete() != null) {
            endpoints.add(mapOperation(path, HttpMethod.DELETE, pathItem.getDelete(), commonParams));
        }
        if (pathItem.getPatch() != null) {
            endpoints.add(mapOperation(path, HttpMethod.PATCH, pathItem.getPatch(), commonParams));
        }
        if (pathItem.getHead() != null) {
            endpoints.add(mapOperation(path, HttpMethod.HEAD, pathItem.getHead(), commonParams));
        }
        if (pathItem.getOptions() != null) {
            endpoints.add(mapOperation(path, HttpMethod.OPTIONS, pathItem.getOptions(), commonParams));
        }

        return endpoints;
    }

    private Endpoint mapOperation(String path, HttpMethod method, Operation operation,
                                   List<io.swagger.v3.oas.models.parameters.Parameter> commonParams) {
        Endpoint.Builder builder = Endpoint.builder()
                .path(path)
                .method(method)
                .operationId(operation.getOperationId())
                .summary(operation.getSummary())
                .deprecated(Boolean.TRUE.equals(operation.getDeprecated()));

        List<Parameter> parameters = mapParameters(operation.getParameters(), commonParams);
        for (Parameter param : parameters) {
            builder.addParameter(param);
        }

        if (operation.getRequestBody() != null) {
            builder.requestBody(mapRequestBody(operation.getRequestBody()));
        }

        List<Response> responses = mapResponses(operation.getResponses());
        for (Response response : responses) {
            builder.addResponse(response);
        }

        return builder.build();
    }

    private List<Parameter> mapParameters(List<io.swagger.v3.oas.models.parameters.Parameter> operationParams,
                                           List<io.swagger.v3.oas.models.parameters.Parameter> commonParams) {
        List<Parameter> result = new ArrayList<>();

        if (commonParams != null) {
            for (io.swagger.v3.oas.models.parameters.Parameter param : commonParams) {
                Parameter mapped = parameterMapper.map(param);
                if (mapped != null) {
                    result.add(mapped);
                }
            }
        }

        if (operationParams != null) {
            for (io.swagger.v3.oas.models.parameters.Parameter param : operationParams) {
                Parameter mapped = parameterMapper.map(param);
                if (mapped != null) {
                    result.add(mapped);
                }
            }
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    private RequestBody mapRequestBody(io.swagger.v3.oas.models.parameters.RequestBody requestBody) {
        if (requestBody == null) {
            return null;
        }

        Content content = requestBody.getContent();
        String contentType = null;
        String schemaRef = null;

        if (content != null && !content.isEmpty()) {
            if (content.containsKey("application/json")) {
                contentType = "application/json";
            } else {
                contentType = content.keySet().iterator().next();
            }

            MediaType mediaType = content.get(contentType);
            if (mediaType != null && mediaType.getSchema() != null) {
                Schema schema = mediaType.getSchema();
                if (schema.get$ref() != null) {
                    schemaRef = extractRefName(schema.get$ref());
                } else if (schema.getType() != null) {
                    schemaRef = schema.getType();
                }
            }
        }

        return new RequestBody(
                contentType,
                schemaRef,
                Boolean.TRUE.equals(requestBody.getRequired())
        );
    }

    private List<Response> mapResponses(ApiResponses apiResponses) {
        List<Response> responses = new ArrayList<>();

        if (apiResponses == null) {
            return responses;
        }

        for (Map.Entry<String, io.swagger.v3.oas.models.responses.ApiResponse> entry : apiResponses.entrySet()) {
            Response response = responseMapper.map(entry.getKey(), entry.getValue());
            if (response != null) {
                responses.add(response);
            }
        }

        return responses;
    }

    private String extractRefName(String ref) {
        if (ref == null) {
            return null;
        }
        int lastSlash = ref.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < ref.length() - 1) {
            return ref.substring(lastSlash + 1);
        }
        return ref;
    }
}
