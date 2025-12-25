package io.github.mohmk10.changeloghub.parser.openapi.mapper;

import io.github.mohmk10.changeloghub.core.model.Response;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;

public class ResponseMapper {

    public Response map(String statusCode, ApiResponse apiResponse) {
        if (apiResponse == null) {
            return null;
        }

        String contentType = extractContentType(apiResponse.getContent());
        String schemaRef = extractSchemaRef(apiResponse.getContent());

        return new Response(
                statusCode,
                apiResponse.getDescription(),
                contentType,
                schemaRef
        );
    }

    private String extractContentType(Content content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        if (content.containsKey("application/json")) {
            return "application/json";
        }

        return content.keySet().iterator().next();
    }

    @SuppressWarnings("rawtypes")
    private String extractSchemaRef(Content content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        for (Map.Entry<String, MediaType> entry : content.entrySet()) {
            MediaType mediaType = entry.getValue();
            if (mediaType != null && mediaType.getSchema() != null) {
                Schema schema = mediaType.getSchema();
                if (schema.get$ref() != null) {
                    return extractRefName(schema.get$ref());
                }
                if (schema.getType() != null) {
                    return schema.getType();
                }
            }
        }

        return null;
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
