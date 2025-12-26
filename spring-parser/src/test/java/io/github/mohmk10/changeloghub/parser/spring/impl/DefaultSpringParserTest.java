package io.github.mohmk10.changeloghub.parser.spring.impl;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.parser.spring.SpringParser;
import io.github.mohmk10.changeloghub.parser.spring.exception.SpringParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DefaultSpringParserTest {

    private SpringParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new DefaultSpringParser();
    }

    @Test
    void testParseDirectory() throws Exception {
        Path controllersDir = getControllersDirectory();

        ApiSpec apiSpec = parser.parse(controllersDir);

        assertThat(apiSpec).isNotNull();
        assertThat(apiSpec.getEndpoints()).isNotEmpty();
    }

    @Test
    void testParseSingleFile() throws Exception {
        Path userController = getControllersDirectory().resolve("UserController.java");

        ApiSpec apiSpec = parser.parseFile(userController);

        assertThat(apiSpec).isNotNull();
        assertThat(apiSpec.getEndpoints()).hasSize(6);
    }

    @Test
    void testParseMultipleControllers() throws Exception {
        Path controllersDir = getControllersDirectory();

        ApiSpec apiSpec = parser.parse(controllersDir);

        assertThat(apiSpec.getEndpoints().size()).isGreaterThanOrEqualTo(15);
    }

    @Test
    void testIgnoreNonControllers() throws Exception {
        Path nonController = getControllersDirectory().resolve("NonController.java");

        assertThat(parser.isSpringController(nonController)).isFalse();
    }

    @Test
    void testIsSpringController() throws Exception {
        Path userController = getControllersDirectory().resolve("UserController.java");
        Path nonController = getControllersDirectory().resolve("NonController.java");

        assertThat(parser.isSpringController(userController)).isTrue();
        assertThat(parser.isSpringController(nonController)).isFalse();
    }

    @Test
    void testParseWithApiNameAndVersion() throws Exception {
        Path controllersDir = getControllersDirectory();

        ApiSpec apiSpec = parser.parse(controllersDir, "My API", "2.0.0");

        assertThat(apiSpec.getName()).isEqualTo("My API");
        assertThat(apiSpec.getVersion()).isEqualTo("2.0.0");
    }

    @Test
    void testParseNonExistentDirectory() {
        Path nonExistent = Path.of("/non/existent/path");

        assertThatThrownBy(() -> parser.parse(nonExistent))
                .isInstanceOf(SpringParseException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testParseNonExistentFile() {
        Path nonExistent = Path.of("/non/existent/file.java");

        assertThatThrownBy(() -> parser.parseFile(nonExistent))
                .isInstanceOf(SpringParseException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testEndpointPaths() throws Exception {
        Path userController = getControllersDirectory().resolve("UserController.java");

        ApiSpec apiSpec = parser.parseFile(userController);

        List<String> paths = apiSpec.getEndpoints().stream()
                .map(Endpoint::getPath)
                .toList();

        assertThat(paths).contains(
                "/api/users",
                "/api/users/{id}",
                "/api/users/{id}/legacy"
        );
    }

    @Test
    void testEndpointMethods() throws Exception {
        Path userController = getControllersDirectory().resolve("UserController.java");

        ApiSpec apiSpec = parser.parseFile(userController);

        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.GET);
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.POST);
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.PUT);
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.DELETE);
    }

    @Test
    void testDeprecatedEndpoint() throws Exception {
        Path userController = getControllersDirectory().resolve("UserController.java");

        ApiSpec apiSpec = parser.parseFile(userController);

        Endpoint deprecatedEndpoint = apiSpec.getEndpoints().stream()
                .filter(e -> e.getPath().contains("legacy"))
                .findFirst()
                .orElseThrow();

        assertThat(deprecatedEndpoint.isDeprecated()).isTrue();
    }

    @Test
    void testDeprecatedController() throws Exception {
        Path deprecatedController = getControllersDirectory().resolve("DeprecatedController.java");

        ApiSpec apiSpec = parser.parseFile(deprecatedController);

        assertThat(apiSpec.getEndpoints()).allMatch(Endpoint::isDeprecated);
    }

    @Test
    void testParseEmptyDirectory() throws Exception {
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectory(emptyDir);

        ApiSpec apiSpec = parser.parse(emptyDir);

        assertThat(apiSpec.getEndpoints()).isEmpty();
    }

    private Path getControllersDirectory() {
        return Path.of("src/test/resources/controllers");
    }
}
