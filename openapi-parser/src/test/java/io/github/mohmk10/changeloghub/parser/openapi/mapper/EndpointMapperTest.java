package io.github.mohmk10.changeloghub.parser.openapi.mapper;

import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointMapperTest {

    private EndpointMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EndpointMapper();
    }

    @Test
    void testMapGetOperation() {
        PathItem pathItem = new PathItem();
        Operation getOp = new Operation();
        getOp.setOperationId("getUsers");
        getOp.setSummary("Get all users");
        getOp.setResponses(createSuccessResponse());
        pathItem.setGet(getOp);

        List<Endpoint> endpoints = mapper.map("/users", pathItem);

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(endpoints.get(0).getPath()).isEqualTo("/users");
        assertThat(endpoints.get(0).getOperationId()).isEqualTo("getUsers");
        assertThat(endpoints.get(0).getSummary()).isEqualTo("Get all users");
    }

    @Test
    void testMapPostOperation() {
        PathItem pathItem = new PathItem();
        Operation postOp = new Operation();
        postOp.setOperationId("createUser");
        postOp.setSummary("Create a user");
        postOp.setResponses(createSuccessResponse());
        pathItem.setPost(postOp);

        List<Endpoint> endpoints = mapper.map("/users", pathItem);

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getMethod()).isEqualTo(HttpMethod.POST);
    }

    @Test
    void testMapPutOperation() {
        PathItem pathItem = new PathItem();
        Operation putOp = new Operation();
        putOp.setOperationId("updateUser");
        putOp.setResponses(createSuccessResponse());
        pathItem.setPut(putOp);

        List<Endpoint> endpoints = mapper.map("/users/{id}", pathItem);

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getMethod()).isEqualTo(HttpMethod.PUT);
    }

    @Test
    void testMapDeleteOperation() {
        PathItem pathItem = new PathItem();
        Operation deleteOp = new Operation();
        deleteOp.setOperationId("deleteUser");
        deleteOp.setResponses(createSuccessResponse());
        pathItem.setDelete(deleteOp);

        List<Endpoint> endpoints = mapper.map("/users/{id}", pathItem);

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getMethod()).isEqualTo(HttpMethod.DELETE);
    }

    @Test
    void testMapWithParameters() {
        PathItem pathItem = new PathItem();
        Operation getOp = new Operation();
        getOp.setOperationId("getUserById");
        getOp.setResponses(createSuccessResponse());

        PathParameter pathParam = new PathParameter();
        pathParam.setName("userId");
        pathParam.setIn("path");
        pathParam.setRequired(true);
        pathParam.setSchema(new StringSchema());
        getOp.addParametersItem(pathParam);

        QueryParameter queryParam = new QueryParameter();
        queryParam.setName("include");
        queryParam.setIn("query");
        queryParam.setSchema(new StringSchema());
        getOp.addParametersItem(queryParam);

        pathItem.setGet(getOp);

        List<Endpoint> endpoints = mapper.map("/users/{userId}", pathItem);

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getParameters()).hasSize(2);
    }

    @Test
    void testMapWithRequestBody() {
        PathItem pathItem = new PathItem();
        Operation postOp = new Operation();
        postOp.setOperationId("createUser");
        postOp.setResponses(createSuccessResponse());

        RequestBody requestBody = new RequestBody();
        requestBody.setRequired(true);
        requestBody.setDescription("User data");
        Content content = new Content();
        MediaType mediaType = new MediaType();
        Schema<?> schema = new Schema<>();
        schema.set$ref("#/components/schemas/User");
        mediaType.setSchema(schema);
        content.addMediaType("application/json", mediaType);
        requestBody.setContent(content);
        postOp.setRequestBody(requestBody);

        pathItem.setPost(postOp);

        List<Endpoint> endpoints = mapper.map("/users", pathItem);

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getRequestBody()).isNotNull();
        assertThat(endpoints.get(0).getRequestBody().getContentType()).isEqualTo("application/json");
        assertThat(endpoints.get(0).getRequestBody().isRequired()).isTrue();
    }

    @Test
    void testMapDeprecatedEndpoint() {
        PathItem pathItem = new PathItem();
        Operation getOp = new Operation();
        getOp.setOperationId("getOldUsers");
        getOp.setDeprecated(true);
        getOp.setResponses(createSuccessResponse());
        pathItem.setGet(getOp);

        List<Endpoint> endpoints = mapper.map("/v1/users", pathItem);

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).isDeprecated()).isTrue();
    }

    @Test
    void testMapMultipleOperations() {
        PathItem pathItem = new PathItem();

        Operation getOp = new Operation();
        getOp.setOperationId("getUsers");
        getOp.setResponses(createSuccessResponse());
        pathItem.setGet(getOp);

        Operation postOp = new Operation();
        postOp.setOperationId("createUser");
        postOp.setResponses(createSuccessResponse());
        pathItem.setPost(postOp);

        List<Endpoint> endpoints = mapper.map("/users", pathItem);

        assertThat(endpoints).hasSize(2);
    }

    @Test
    void testMapWithResponses() {
        PathItem pathItem = new PathItem();
        Operation getOp = new Operation();
        getOp.setOperationId("getUser");

        ApiResponses responses = new ApiResponses();
        ApiResponse okResponse = new ApiResponse();
        okResponse.setDescription("Success");
        responses.addApiResponse("200", okResponse);

        ApiResponse notFoundResponse = new ApiResponse();
        notFoundResponse.setDescription("Not found");
        responses.addApiResponse("404", notFoundResponse);

        getOp.setResponses(responses);
        pathItem.setGet(getOp);

        List<Endpoint> endpoints = mapper.map("/users/{id}", pathItem);

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getResponses()).hasSize(2);
    }

    @Test
    void testMapNullPathItem() {
        List<Endpoint> endpoints = mapper.map("/users", null);
        assertThat(endpoints).isEmpty();
    }

    @Test
    void testMapWithCommonParameters() {
        PathItem pathItem = new PathItem();

        PathParameter commonParam = new PathParameter();
        commonParam.setName("version");
        commonParam.setIn("path");
        commonParam.setRequired(true);
        commonParam.setSchema(new StringSchema());
        pathItem.addParametersItem(commonParam);

        Operation getOp = new Operation();
        getOp.setOperationId("getResource");
        getOp.setResponses(createSuccessResponse());
        pathItem.setGet(getOp);

        List<Endpoint> endpoints = mapper.map("/{version}/resource", pathItem);

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getParameters()).hasSize(1);
        assertThat(endpoints.get(0).getParameters().get(0).getName()).isEqualTo("version");
    }

    private ApiResponses createSuccessResponse() {
        ApiResponses responses = new ApiResponses();
        ApiResponse response = new ApiResponse();
        response.setDescription("Success");
        responses.addApiResponse("200", response);
        return responses;
    }
}
