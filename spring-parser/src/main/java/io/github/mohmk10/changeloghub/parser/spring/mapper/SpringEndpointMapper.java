package io.github.mohmk10.changeloghub.parser.spring.mapper;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.parser.spring.extractor.PathExtractor;
import io.github.mohmk10.changeloghub.parser.spring.extractor.TypeExtractor;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringController;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringMethod;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps Spring method models to core Endpoint models.
 */
public class SpringEndpointMapper {

    private final PathExtractor pathExtractor;
    private final SpringParameterMapper parameterMapper;
    private final SpringResponseMapper responseMapper;
    private final TypeExtractor typeExtractor;

    public SpringEndpointMapper() {
        this.pathExtractor = new PathExtractor();
        this.parameterMapper = new SpringParameterMapper();
        this.responseMapper = new SpringResponseMapper();
        this.typeExtractor = new TypeExtractor();
    }

    /**
     * Map all methods from a controller to endpoints.
     */
    public List<Endpoint> mapEndpoints(SpringController controller) {
        List<Endpoint> endpoints = new ArrayList<>();

        for (SpringMethod method : controller.getMethods()) {
            endpoints.add(mapEndpoint(controller, method));
        }

        return endpoints;
    }

    /**
     * Map a single Spring method to an endpoint.
     */
    public Endpoint mapEndpoint(SpringController controller, SpringMethod springMethod) {
        Endpoint.Builder builder = Endpoint.builder();

        // Set HTTP method
        builder.method(mapHttpMethod(springMethod.getHttpMethod()));

        // Combine paths
        String fullPath = pathExtractor.combinePaths(controller.getBasePath(), springMethod.getPath());
        fullPath = pathExtractor.toOpenApiPath(fullPath);
        builder.path(fullPath);

        // Set operation ID
        builder.operationId(springMethod.getMethodName());

        // Set summary
        if (springMethod.getSummary() != null) {
            builder.summary(springMethod.getSummary());
        }

        // Set deprecated (controller or method level)
        builder.deprecated(controller.isDeprecated() || springMethod.isDeprecated());

        // Map parameters
        List<Parameter> parameters = parameterMapper.mapParameters(springMethod.getParameters());
        builder.parameters(parameters);

        // Map request body
        RequestBody requestBody = mapRequestBody(springMethod);
        if (requestBody != null) {
            builder.requestBody(requestBody);
        }

        // Map responses
        List<Response> responses = responseMapper.mapResponses(springMethod);
        builder.responses(responses);

        return builder.build();
    }

    /**
     * Map HTTP method string to HttpMethod enum.
     */
    private HttpMethod mapHttpMethod(String method) {
        if (method == null) {
            return HttpMethod.GET;
        }

        switch (method.toUpperCase()) {
            case "GET":
                return HttpMethod.GET;
            case "POST":
                return HttpMethod.POST;
            case "PUT":
                return HttpMethod.PUT;
            case "DELETE":
                return HttpMethod.DELETE;
            case "PATCH":
                return HttpMethod.PATCH;
            case "HEAD":
                return HttpMethod.HEAD;
            case "OPTIONS":
                return HttpMethod.OPTIONS;
            default:
                return HttpMethod.GET;
        }
    }

    /**
     * Map request body from Spring parameters.
     */
    private RequestBody mapRequestBody(SpringMethod springMethod) {
        SpringParameter bodyParam = parameterMapper.getRequestBody(springMethod.getParameters());

        if (bodyParam == null) {
            return null;
        }

        RequestBody requestBody = new RequestBody();
        requestBody.setRequired(bodyParam.isRequired());

        // Set content type
        if (!springMethod.getConsumes().isEmpty()) {
            requestBody.setContentType(springMethod.getConsumes().get(0));
        } else {
            requestBody.setContentType("application/json");
        }

        // Set schema based on Java type
        String apiType = typeExtractor.javaTypeToApiType(bodyParam.getJavaType());
        requestBody.setSchemaRef(apiType);

        return requestBody;
    }
}
