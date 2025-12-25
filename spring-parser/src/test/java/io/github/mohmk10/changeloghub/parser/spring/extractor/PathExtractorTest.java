package io.github.mohmk10.changeloghub.parser.spring.extractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PathExtractorTest {

    private PathExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new PathExtractor();
    }

    @Test
    void testCombinePathsSimple() {
        String result = extractor.combinePaths("/api/users", "/profile");
        assertThat(result).isEqualTo("/api/users/profile");
    }

    @Test
    void testCombinePathsWithTrailingSlash() {
        String result = extractor.combinePaths("/api/users/", "/profile");
        assertThat(result).isEqualTo("/api/users/profile");
    }

    @Test
    void testCombinePathsWithNoLeadingSlash() {
        String result = extractor.combinePaths("/api/users", "profile");
        assertThat(result).isEqualTo("/api/users/profile");
    }

    @Test
    void testCombinePathsEmptyBasePath() {
        String result = extractor.combinePaths("", "/users");
        assertThat(result).isEqualTo("/users");
    }

    @Test
    void testCombinePathsEmptyMethodPath() {
        String result = extractor.combinePaths("/api/users", "");
        assertThat(result).isEqualTo("/api/users");
    }

    @Test
    void testCombinePathsBothEmpty() {
        String result = extractor.combinePaths("", "");
        assertThat(result).isEqualTo("/");
    }

    @Test
    void testCombinePathsNullBasePath() {
        String result = extractor.combinePaths(null, "/users");
        assertThat(result).isEqualTo("/users");
    }

    @Test
    void testCombinePathsNullMethodPath() {
        String result = extractor.combinePaths("/api", null);
        assertThat(result).isEqualTo("/api");
    }

    @Test
    void testNormalizePathAddsLeadingSlash() {
        String result = extractor.normalizePath("api/users");
        assertThat(result).isEqualTo("/api/users");
    }

    @Test
    void testNormalizePathRemovesTrailingSlash() {
        String result = extractor.normalizePath("/api/users/");
        assertThat(result).isEqualTo("/api/users");
    }

    @Test
    void testNormalizePathPreservesPathVariables() {
        String result = extractor.normalizePath("/api/users/{id}");
        assertThat(result).isEqualTo("/api/users/{id}");
    }

    @Test
    void testExtractPathVariables() {
        List<String> variables = extractor.extractPathVariables("/api/users/{userId}/orders/{orderId}");
        assertThat(variables).containsExactly("userId", "orderId");
    }

    @Test
    void testExtractPathVariablesNoVariables() {
        List<String> variables = extractor.extractPathVariables("/api/users");
        assertThat(variables).isEmpty();
    }

    @Test
    void testExtractPathVariablesSingleVariable() {
        List<String> variables = extractor.extractPathVariables("/api/users/{id}");
        assertThat(variables).containsExactly("id");
    }

    @Test
    void testHasPathVariables() {
        assertThat(extractor.hasPathVariables("/api/users/{id}")).isTrue();
        assertThat(extractor.hasPathVariables("/api/users")).isFalse();
    }

    @Test
    void testNormalizePathEmpty() {
        String result = extractor.normalizePath("");
        assertThat(result).isEmpty();
    }

    @Test
    void testNormalizePathNull() {
        String result = extractor.normalizePath(null);
        assertThat(result).isEmpty();
    }

    @Test
    void testToOpenApiPathRemovesRegex() {
        String result = extractor.toOpenApiPath("/users/{id:\\d+}/posts/{postId:[a-z]+}");
        assertThat(result).isEqualTo("/users/{id}/posts/{postId}");
    }

    @Test
    void testToOpenApiPathNoRegex() {
        String result = extractor.toOpenApiPath("/users/{id}");
        assertThat(result).isEqualTo("/users/{id}");
    }

    @Test
    void testToOpenApiPathNull() {
        String result = extractor.toOpenApiPath(null);
        assertThat(result).isEqualTo("/");
    }

    @Test
    void testExtractPathVariablesWithRegex() {
        List<String> variables = extractor.extractPathVariables("/users/{id:\\d+}");
        assertThat(variables).containsExactly("id");
    }
}
