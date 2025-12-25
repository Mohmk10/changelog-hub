package io.github.mohmk10.changeloghub.parser.openapi.impl;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.parser.openapi.exception.OpenApiParseException;
import io.github.mohmk10.changeloghub.parser.openapi.exception.UnsupportedVersionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultOpenApiParserTest {

    private DefaultOpenApiParser parser;

    @BeforeEach
    void setUp() {
        parser = new DefaultOpenApiParser();
    }

    @Test
    void testParseOpenApi30Yaml() throws Exception {
        String content = loadResource("petstore-openapi-3.0.yaml");

        ApiSpec result = parser.parse(content);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Petstore API");
        assertThat(result.getVersion()).isEqualTo("1.0.0");
        assertThat(result.getType()).isEqualTo(ApiType.REST);
        assertThat(result.getEndpoints()).isNotEmpty();

        assertThat(result.getEndpoints().stream()
                .anyMatch(e -> e.getPath().equals("/pets") && e.getMethod() == HttpMethod.GET))
                .isTrue();
        assertThat(result.getEndpoints().stream()
                .anyMatch(e -> e.getPath().equals("/pets") && e.getMethod() == HttpMethod.POST))
                .isTrue();
    }

    @Test
    void testParseOpenApi31Yaml() throws Exception {
        String content = loadResource("minimal-openapi-3.1.yaml");

        ApiSpec result = parser.parse(content);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Minimal API");
        assertThat(result.getVersion()).isEqualTo("3.1.0");
        assertThat(result.getEndpoints()).hasSize(3);
    }

    @Test
    void testParseSwagger20Json() throws Exception {
        String content = loadResource("simple-api-swagger-2.0.json");

        ApiSpec result = parser.parse(content);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Simple API");
        assertThat(result.getVersion()).isEqualTo("2.0.0");
        assertThat(result.getType()).isEqualTo(ApiType.REST);
        assertThat(result.getEndpoints()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void testParseInvalidContent() {
        String invalidContent = "this is not valid openapi or swagger content";

        assertThatThrownBy(() -> parser.parse(invalidContent))
                .isInstanceOf(UnsupportedVersionException.class);
    }

    @Test
    void testParseNullContent() {
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(OpenApiParseException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    void testParseEmptyContent() {
        assertThatThrownBy(() -> parser.parse(""))
                .isInstanceOf(OpenApiParseException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    void testParseFile() throws Exception {
        String content = loadResource("petstore-openapi-3.0.yaml");
        Path tempFile = Files.createTempFile("test-openapi", ".yaml");
        Files.writeString(tempFile, content);

        try {
            ApiSpec result = parser.parseFile(tempFile);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Petstore API");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testParseFileNotExists() {
        Path nonExistentFile = Path.of("/non/existent/file.yaml");

        assertThatThrownBy(() -> parser.parseFile(nonExistentFile))
                .isInstanceOf(OpenApiParseException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void testParseFileNull() {
        assertThatThrownBy(() -> parser.parseFile(null))
                .isInstanceOf(OpenApiParseException.class)
                .hasMessageContaining("null");
    }

    @Test
    void testSupportsOpenApi30() throws Exception {
        String content = loadResource("petstore-openapi-3.0.yaml");

        assertThat(parser.supports(content)).isTrue();
    }

    @Test
    void testSupportsOpenApi31() throws Exception {
        String content = loadResource("minimal-openapi-3.1.yaml");

        assertThat(parser.supports(content)).isTrue();
    }

    @Test
    void testSupportsSwagger20() throws Exception {
        String content = loadResource("simple-api-swagger-2.0.json");

        assertThat(parser.supports(content)).isTrue();
    }

    @Test
    void testSupportsInvalidContent() {
        assertThat(parser.supports("random content")).isFalse();
    }

    @Test
    void testSupportsNullContent() {
        assertThat(parser.supports(null)).isFalse();
    }

    @Test
    void testSupportsEmptyContent() {
        assertThat(parser.supports("")).isFalse();
    }

    @Test
    void testParseWithParameters() throws Exception {
        String content = loadResource("petstore-openapi-3.0.yaml");

        ApiSpec result = parser.parse(content);

        assertThat(result.getEndpoints().stream()
                .filter(e -> e.getPath().equals("/pets") && e.getMethod() == HttpMethod.GET)
                .findFirst()
                .map(e -> e.getParameters().size())
                .orElse(0))
                .isGreaterThan(0);
    }

    @Test
    void testParseWithDeprecatedEndpoint() throws Exception {
        String content = loadResource("petstore-openapi-3.0.yaml");

        ApiSpec result = parser.parse(content);

        assertThat(result.getEndpoints().stream()
                .anyMatch(e -> e.isDeprecated()))
                .isTrue();
    }

    @Test
    void testParseWithResponses() throws Exception {
        String content = loadResource("petstore-openapi-3.0.yaml");

        ApiSpec result = parser.parse(content);

        assertThat(result.getEndpoints().stream()
                .allMatch(e -> e.getResponses() != null && !e.getResponses().isEmpty()))
                .isTrue();
    }

    private String loadResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
