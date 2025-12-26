package io.github.mohmk10.changeloghub.parser.spring.mapper;

import io.github.mohmk10.changeloghub.core.model.Response;
import io.github.mohmk10.changeloghub.parser.spring.extractor.TypeExtractor;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringMethod;

import java.util.ArrayList;
import java.util.List;

public class SpringResponseMapper {

    private final TypeExtractor typeExtractor;

    public SpringResponseMapper() {
        this.typeExtractor = new TypeExtractor();
    }

    public List<Response> mapResponses(SpringMethod springMethod) {
        List<Response> responses = new ArrayList<>();

        Response successResponse = createSuccessResponse(springMethod);
        responses.add(successResponse);

        return responses;
    }

    private Response createSuccessResponse(SpringMethod springMethod) {
        Response response = new Response();

        String statusCode = springMethod.getResponseStatus();
        if (statusCode == null || statusCode.isEmpty()) {
            statusCode = inferStatusCode(springMethod);
        }
        response.setStatusCode(statusCode);

        response.setDescription(getStatusDescription(statusCode));

        if (!springMethod.getProduces().isEmpty()) {
            response.setContentType(springMethod.getProduces().get(0));
        } else {
            response.setContentType("application/json");
        }

        String returnType = springMethod.getReturnType();
        if (returnType != null && !"void".equals(returnType)) {
            String apiType = typeExtractor.javaTypeToApiType(returnType);
            response.setSchemaRef(apiType);
        }

        return response;
    }

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
