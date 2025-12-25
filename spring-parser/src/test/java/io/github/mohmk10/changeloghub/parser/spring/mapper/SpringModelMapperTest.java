package io.github.mohmk10.changeloghub.parser.spring.mapper;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringController;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringMethod;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SpringModelMapperTest {

    private SpringModelMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SpringModelMapper();
    }

    @Test
    void testMapToApiSpec() {
        SpringController controller = createController("UserController", "/api/users");
        SpringMethod method = createMethod("getUsers", "GET", "");
        controller.getMethods().add(method);

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), "Test API", "1.0.0");

        assertThat(apiSpec).isNotNull();
        assertThat(apiSpec.getName()).isEqualTo("Test API");
        assertThat(apiSpec.getVersion()).isEqualTo("1.0.0");
        assertThat(apiSpec.getEndpoints()).hasSize(1);
    }

    @Test
    void testMapToApiSpecWithDefaultNameAndVersion() {
        SpringController controller = createController("UserController", "/api/users");

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), null, null);

        assertThat(apiSpec.getName()).isEqualTo("Spring Boot API");
        assertThat(apiSpec.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void testMapEndpointPath() {
        SpringController controller = createController("UserController", "/api/users");
        SpringMethod method = createMethod("getUserById", "GET", "/{id}");
        controller.getMethods().add(method);

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), "API", "1.0");

        Endpoint endpoint = apiSpec.getEndpoints().get(0);
        assertThat(endpoint.getPath()).isEqualTo("/api/users/{id}");
    }

    @Test
    void testMapEndpointHttpMethod() {
        SpringController controller = createController("UserController", "/api/users");
        controller.getMethods().add(createMethod("getUsers", "GET", ""));
        controller.getMethods().add(createMethod("createUser", "POST", ""));
        controller.getMethods().add(createMethod("updateUser", "PUT", "/{id}"));
        controller.getMethods().add(createMethod("deleteUser", "DELETE", "/{id}"));

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), "API", "1.0");

        assertThat(apiSpec.getEndpoints()).hasSize(4);
        assertThat(apiSpec.getEndpoints()).extracting(Endpoint::getMethod)
                .containsExactlyInAnyOrder(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);
    }

    @Test
    void testMapDeprecatedEndpoint() {
        SpringController controller = createController("UserController", "/api/users");
        SpringMethod method = createMethod("oldMethod", "GET", "/legacy");
        method.setDeprecated(true);
        controller.getMethods().add(method);

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), "API", "1.0");

        assertThat(apiSpec.getEndpoints().get(0).isDeprecated()).isTrue();
    }

    @Test
    void testMapDeprecatedController() {
        SpringController controller = createController("OldController", "/api/old");
        controller.setDeprecated(true);
        controller.getMethods().add(createMethod("get", "GET", ""));

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), "API", "1.0");

        assertThat(apiSpec.getEndpoints().get(0).isDeprecated()).isTrue();
    }

    @Test
    void testMapMultipleControllers() {
        SpringController userController = createController("UserController", "/api/users");
        userController.getMethods().add(createMethod("getUsers", "GET", ""));
        userController.getMethods().add(createMethod("createUser", "POST", ""));

        SpringController productController = createController("ProductController", "/api/products");
        productController.getMethods().add(createMethod("getProducts", "GET", ""));

        ApiSpec apiSpec = mapper.mapToApiSpec(Arrays.asList(userController, productController), "API", "1.0");

        assertThat(apiSpec.getEndpoints()).hasSize(3);
    }

    @Test
    void testMapEmptyControllerList() {
        ApiSpec apiSpec = mapper.mapToApiSpec(Collections.emptyList(), "API", "1.0");

        assertThat(apiSpec.getEndpoints()).isEmpty();
    }

    @Test
    void testMapControllerWithNoMethods() {
        SpringController controller = createController("EmptyController", "/api/empty");

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), "API", "1.0");

        assertThat(apiSpec.getEndpoints()).isEmpty();
    }

    @Test
    void testMapEndpointWithParameters() {
        SpringController controller = createController("UserController", "/api/users");
        SpringMethod method = createMethod("getUser", "GET", "/{id}");

        SpringParameter pathParam = new SpringParameter();
        pathParam.setName("id");
        pathParam.setLocation(SpringParameter.Location.PATH);
        pathParam.setRequired(true);
        pathParam.setJavaType("Long");

        SpringParameter queryParam = new SpringParameter();
        queryParam.setName("includeDetails");
        queryParam.setLocation(SpringParameter.Location.QUERY);
        queryParam.setRequired(false);
        queryParam.setJavaType("Boolean");

        method.getParameters().add(pathParam);
        method.getParameters().add(queryParam);
        controller.getMethods().add(method);

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), "API", "1.0");

        Endpoint endpoint = apiSpec.getEndpoints().get(0);
        assertThat(endpoint.getParameters()).hasSize(2);
    }

    @Test
    void testMapEndpointWithRequestBody() {
        SpringController controller = createController("UserController", "/api/users");
        SpringMethod method = createMethod("createUser", "POST", "");

        SpringParameter bodyParam = new SpringParameter();
        bodyParam.setName("user");
        bodyParam.setLocation(SpringParameter.Location.BODY);
        bodyParam.setRequired(true);
        bodyParam.setJavaType("CreateUserRequest");

        method.getParameters().add(bodyParam);
        controller.getMethods().add(method);

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), "API", "1.0");

        Endpoint endpoint = apiSpec.getEndpoints().get(0);
        assertThat(endpoint.getRequestBody()).isNotNull();
    }

    @Test
    void testMapEndpointOperationId() {
        SpringController controller = createController("UserController", "/api/users");
        SpringMethod method = createMethod("getUserById", "GET", "/{id}");
        controller.getMethods().add(method);

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), "API", "1.0");

        Endpoint endpoint = apiSpec.getEndpoints().get(0);
        assertThat(endpoint.getOperationId()).isEqualTo("getUserById");
    }

    @Test
    void testMapPatchMethod() {
        SpringController controller = createController("UserController", "/api/users");
        controller.getMethods().add(createMethod("patchUser", "PATCH", "/{id}"));

        ApiSpec apiSpec = mapper.mapToApiSpec(List.of(controller), "API", "1.0");

        assertThat(apiSpec.getEndpoints().get(0).getMethod()).isEqualTo(HttpMethod.PATCH);
    }

    private SpringController createController(String name, String basePath) {
        SpringController controller = new SpringController();
        controller.setClassName(name);
        controller.setBasePath(basePath);
        controller.setDeprecated(false);
        return controller;
    }

    private SpringMethod createMethod(String name, String httpMethod, String path) {
        SpringMethod method = new SpringMethod();
        method.setMethodName(name);
        method.setHttpMethod(httpMethod);
        method.setPath(path);
        method.setDeprecated(false);
        method.setReturnType("void");
        return method;
    }
}
