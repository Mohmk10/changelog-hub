package io.github.mohmk10.changeloghub.parser.spring.mapper;

import io.github.mohmk10.changeloghub.core.model.Response;
import io.github.mohmk10.changeloghub.parser.spring.extractor.TypeExtractor;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps Spring method return types to core Response models.
 */
public class SpringResponseMapper {

    private final TypeExtractor typeExtractor;

    public SpringResponseMapper() {
        this.typeExtractor = new TypeExtractor();
    }

    /**
     * Map a Spring method to responses.
     */
    public List<Response> mapResponses(SpringMethod springMethod) {
        List<Response> responses = new ArrayList<>();

        // Add the primary success response
        Response successResponse = createSuccessResponse(springMethod);
        responses.add(successResponse);

        return responses;
    }

    /**
     * Create a success response from Spring method.
     */
    private Response createSuccessResponse(SpringMethod springMethod) {
        Response response = new Response();

        // Set status code
        String statusCode = springMethod.getResponseStatus();
        if (statusCode == null || statusCode.isEmpty()) {
            statusCode = inferStatusCode(springMethod);
        }
        response.setStatusCode(statusCode);

        // Set description based on status code
        response.setDescription(getStatusDescription(statusCode));

        // Set content type
        if (!springMethod.getProduces().isEmpty()) {
            response.setContentType(springMethod.getProduces().get(0));
        } else {
            response.setContentType("application/json");
        }

        // Set schema/return type
        String returnType = springMethod.getReturnType();
        if (returnType != null && !"void".equals(returnType)) {
            String apiType = typeExtractor.javaTypeToApiType(returnType);
            response.setSchemaRef(apiType);
        }

        return response;
    }

    /**
     * Infer status code from HTTP method.
     */
    private String inferStatusCode(SpringMethod springMethod) {
        String httpMethod = springMethod.getHttpMethod();
        String returnType = springMethod.getReturnType();

        if ("POST".equals(httpMethod)) {
            return "201";
        }

        if ("DELETE".equals(httpMethod) && ("void".equals(returnType) || returnType == null)) {
            return "204";
        }

        return "200";
    }

    /**
     * Get description for a status code.
     */
    private String getStatusDescription(String statusCode) {
        switch (statusCode) {
            case "200":
                return "OK";
            case "201":
                return "Created";
            case "202":
                return "Accepted";
            case "204":
                return "No Content";
            case "400":
                return "Bad Request";
            case "401":
                return "Unauthorized";
            case "403":
                return "Forbidden";
            case "404":
                return "Not Found";
            case "500":
                return "Internal Server Error";
            default:
                return "Response";
        }
    }
}
