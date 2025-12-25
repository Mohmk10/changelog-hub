package io.github.mohmk10.changeloghub.parser.openapi.mapper;

import io.github.mohmk10.changeloghub.core.model.Response;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseMapperTest {

    private ResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ResponseMapper();
    }

    @Test
    void testMapSuccessResponse() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Success response");

        Content content = new Content();
        MediaType mediaType = new MediaType();
        Schema<?> schema = new Schema<>();
        schema.set$ref("#/components/schemas/User");
        mediaType.setSchema(schema);
        content.addMediaType("application/json", mediaType);
        apiResponse.setContent(content);

        Response result = mapper.map("200", apiResponse);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo("200");
        assertThat(result.getDescription()).isEqualTo("Success response");
        assertThat(result.getContentType()).isEqualTo("application/json");
        assertThat(result.getSchemaRef()).isEqualTo("User");
    }

    @Test
    void testMapErrorResponse() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Not found");

        Response result = mapper.map("404", apiResponse);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo("404");
        assertThat(result.getDescription()).isEqualTo("Not found");
    }

    @Test
    void testMapDefaultResponse() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Default response");

        Response result = mapper.map("default", apiResponse);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo("default");
    }

    @Test
    void testMapWithObjectSchema() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Object response");

        Content content = new Content();
        MediaType mediaType = new MediaType();
        mediaType.setSchema(new ObjectSchema());
        content.addMediaType("application/json", mediaType);
        apiResponse.setContent(content);

        Response result = mapper.map("200", apiResponse);

        assertThat(result.getSchemaRef()).isEqualTo("object");
    }

    @Test
    void testMapWithoutContent() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("No content");

        Response result = mapper.map("204", apiResponse);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo("204");
        assertThat(result.getContentType()).isNull();
        assertThat(result.getSchemaRef()).isNull();
    }

    @Test
    void testMapNullResponse() {
        Response result = mapper.map("200", null);
        assertThat(result).isNull();
    }

    @Test
    void testMapWithMultipleContentTypes() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Multiple content types");

        Content content = new Content();
        MediaType jsonType = new MediaType();
        jsonType.setSchema(new ObjectSchema());
        content.addMediaType("application/json", jsonType);

        MediaType xmlType = new MediaType();
        xmlType.setSchema(new ObjectSchema());
        content.addMediaType("application/xml", xmlType);

        apiResponse.setContent(content);

        Response result = mapper.map("200", apiResponse);

        assertThat(result.getContentType()).isEqualTo("application/json");
    }
}
