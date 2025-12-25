package io.github.mohmk10.changeloghub.parser.openapi.mapper;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiModelMapperTest {

    private OpenApiModelMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OpenApiModelMapper();
    }

    @Test
    void testMapBasicApi() {
        OpenAPI openApi = new OpenAPI();
        Info info = new Info();
        info.setTitle("Test API");
        info.setVersion("1.0.0");
        info.setDescription("A test API");
        openApi.setInfo(info);

        ApiSpec result = mapper.map(openApi);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test API");
        assertThat(result.getVersion()).isEqualTo("1.0.0");
        assertThat(result.getMetadata().get("description")).isEqualTo("A test API");
        assertThat(result.getType()).isEqualTo(ApiType.REST);
    }

    @Test
    void testMapWithEndpoints() {
        OpenAPI openApi = createApiWithEndpoints();

        ApiSpec result = mapper.map(openApi);

        assertThat(result).isNotNull();
        assertThat(result.getEndpoints()).hasSize(2);
    }

    @Test
    void testMapWithParameters() {
        OpenAPI openApi = new OpenAPI();
        Info info = new Info();
        info.setTitle("API with Parameters");
        info.setVersion("1.0.0");
        openApi.setInfo(info);

        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        Operation getOp = new Operation();
        getOp.setOperationId("search");

        QueryParameter param = new QueryParameter();
        param.setName("query");
        param.setIn("query");
        param.setRequired(true);
        param.setSchema(new StringSchema());
        getOp.addParametersItem(param);

        getOp.setResponses(createSuccessResponse());
        pathItem.setGet(getOp);
        paths.addPathItem("/search", pathItem);
        openApi.setPaths(paths);

        ApiSpec result = mapper.map(openApi);

        assertThat(result.getEndpoints()).hasSize(1);
        assertThat(result.getEndpoints().get(0).getParameters()).hasSize(1);
        assertThat(result.getEndpoints().get(0).getParameters().get(0).getName()).isEqualTo("query");
    }

    @Test
    void testMapWithResponses() {
        OpenAPI openApi = new OpenAPI();
        Info info = new Info();
        info.setTitle("API with Responses");
        info.setVersion("1.0.0");
        openApi.setInfo(info);

        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        Operation getOp = new Operation();
        getOp.setOperationId("getItem");

        ApiResponses responses = new ApiResponses();
        ApiResponse okResponse = new ApiResponse();
        okResponse.setDescription("Success");
        responses.addApiResponse("200", okResponse);

        ApiResponse notFound = new ApiResponse();
        notFound.setDescription("Not found");
        responses.addApiResponse("404", notFound);

        getOp.setResponses(responses);
        pathItem.setGet(getOp);
        paths.addPathItem("/items/{id}", pathItem);
        openApi.setPaths(paths);

        ApiSpec result = mapper.map(openApi);

        assertThat(result.getEndpoints()).hasSize(1);
        assertThat(result.getEndpoints().get(0).getResponses()).hasSize(2);
    }

    @Test
    void testMapWithComponents() {
        OpenAPI openApi = createApiWithEndpoints();

        ApiSpec result = mapper.map(openApi);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(ApiType.REST);
    }

    @Test
    void testMapNullOpenApi() {
        ApiSpec result = mapper.map(null);
        assertThat(result).isNull();
    }

    @Test
    void testMapWithNullInfo() {
        OpenAPI openApi = new OpenAPI();

        ApiSpec result = mapper.map(openApi);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Unknown API");
        assertThat(result.getVersion()).isEqualTo("0.0.0");
    }

    @Test
    void testMapWithEmptyPaths() {
        OpenAPI openApi = new OpenAPI();
        Info info = new Info();
        info.setTitle("Empty API");
        info.setVersion("1.0.0");
        openApi.setInfo(info);
        openApi.setPaths(new Paths());

        ApiSpec result = mapper.map(openApi);

        assertThat(result).isNotNull();
        assertThat(result.getEndpoints()).isEmpty();
    }

    @Test
    void testMapWithNullPaths() {
        OpenAPI openApi = new OpenAPI();
        Info info = new Info();
        info.setTitle("No Paths API");
        info.setVersion("1.0.0");
        openApi.setInfo(info);

        ApiSpec result = mapper.map(openApi);

        assertThat(result).isNotNull();
        assertThat(result.getEndpoints()).isEmpty();
    }

    private OpenAPI createApiWithEndpoints() {
        OpenAPI openApi = new OpenAPI();
        Info info = new Info();
        info.setTitle("Multi-Endpoint API");
        info.setVersion("2.0.0");
        openApi.setInfo(info);

        Paths paths = new Paths();

        PathItem usersPath = new PathItem();
        Operation getUsersOp = new Operation();
        getUsersOp.setOperationId("getUsers");
        getUsersOp.setResponses(createSuccessResponse());
        usersPath.setGet(getUsersOp);
        paths.addPathItem("/users", usersPath);

        PathItem ordersPath = new PathItem();
        Operation getOrdersOp = new Operation();
        getOrdersOp.setOperationId("getOrders");
        getOrdersOp.setResponses(createSuccessResponse());
        ordersPath.setGet(getOrdersOp);
        paths.addPathItem("/orders", ordersPath);

        openApi.setPaths(paths);

        return openApi;
    }

    private ApiResponses createSuccessResponse() {
        ApiResponses responses = new ApiResponses();
        ApiResponse response = new ApiResponse();
        response.setDescription("Success");
        responses.addApiResponse("200", response);
        return responses;
    }
}
