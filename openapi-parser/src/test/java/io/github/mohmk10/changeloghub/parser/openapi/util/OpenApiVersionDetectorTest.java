package io.github.mohmk10.changeloghub.parser.openapi.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiVersionDetectorTest {

    @Test
    void testDetectOpenApi30() {
        String content = "openapi: 3.0.3\ninfo:\n  title: Test API";
        OpenApiVersion version = OpenApiVersionDetector.detect(content);
        assertThat(version).isEqualTo(OpenApiVersion.OPENAPI_3_0);
    }

    @Test
    void testDetectOpenApi30Json() {
        String content = "{\"openapi\": \"3.0.1\", \"info\": {\"title\": \"Test\"}}";
        OpenApiVersion version = OpenApiVersionDetector.detect(content);
        assertThat(version).isEqualTo(OpenApiVersion.OPENAPI_3_0);
    }

    @Test
    void testDetectOpenApi31() {
        String content = "openapi: 3.1.0\ninfo:\n  title: Test API";
        OpenApiVersion version = OpenApiVersionDetector.detect(content);
        assertThat(version).isEqualTo(OpenApiVersion.OPENAPI_3_1);
    }

    @Test
    void testDetectOpenApi31Json() {
        String content = "{\"openapi\": \"3.1.0\", \"info\": {}}";
        OpenApiVersion version = OpenApiVersionDetector.detect(content);
        assertThat(version).isEqualTo(OpenApiVersion.OPENAPI_3_1);
    }

    @Test
    void testDetectSwagger20() {
        String content = "swagger: \"2.0\"\ninfo:\n  title: Test API";
        OpenApiVersion version = OpenApiVersionDetector.detect(content);
        assertThat(version).isEqualTo(OpenApiVersion.SWAGGER_2_0);
    }

    @Test
    void testDetectSwagger20Json() {
        String content = "{\"swagger\": \"2.0\", \"info\": {\"title\": \"Test\"}}";
        OpenApiVersion version = OpenApiVersionDetector.detect(content);
        assertThat(version).isEqualTo(OpenApiVersion.SWAGGER_2_0);
    }

    @Test
    void testDetectUnknown() {
        String content = "some random content without version";
        OpenApiVersion version = OpenApiVersionDetector.detect(content);
        assertThat(version).isEqualTo(OpenApiVersion.UNKNOWN);
    }

    @Test
    void testDetectNullContent() {
        OpenApiVersion version = OpenApiVersionDetector.detect(null);
        assertThat(version).isEqualTo(OpenApiVersion.UNKNOWN);
    }

    @Test
    void testDetectEmptyContent() {
        OpenApiVersion version = OpenApiVersionDetector.detect("");
        assertThat(version).isEqualTo(OpenApiVersion.UNKNOWN);
    }

    @Test
    void testIsSupportedSwagger20() {
        assertThat(OpenApiVersionDetector.isSupported(OpenApiVersion.SWAGGER_2_0)).isTrue();
    }

    @Test
    void testIsSupportedOpenApi30() {
        assertThat(OpenApiVersionDetector.isSupported(OpenApiVersion.OPENAPI_3_0)).isTrue();
    }

    @Test
    void testIsSupportedOpenApi31() {
        assertThat(OpenApiVersionDetector.isSupported(OpenApiVersion.OPENAPI_3_1)).isTrue();
    }

    @Test
    void testIsSupportedUnknown() {
        assertThat(OpenApiVersionDetector.isSupported(OpenApiVersion.UNKNOWN)).isFalse();
    }
}
