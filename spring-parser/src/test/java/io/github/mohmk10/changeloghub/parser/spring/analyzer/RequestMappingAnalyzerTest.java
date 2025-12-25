package io.github.mohmk10.changeloghub.parser.spring.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class RequestMappingAnalyzerTest {

    private RequestMappingAnalyzer analyzer;
    private JavaParser javaParser;

    @BeforeEach
    void setUp() {
        analyzer = new RequestMappingAnalyzer();
        javaParser = new JavaParser();
    }

    @Test
    void testIsEndpointMethodWithGetMapping() {
        MethodDeclaration method = parseMethod("@GetMapping public void test() {}");
        assertThat(analyzer.isEndpointMethod(method)).isTrue();
    }

    @Test
    void testIsEndpointMethodWithPostMapping() {
        MethodDeclaration method = parseMethod("@PostMapping public void test() {}");
        assertThat(analyzer.isEndpointMethod(method)).isTrue();
    }

    @Test
    void testIsEndpointMethodWithRequestMapping() {
        MethodDeclaration method = parseMethod("@RequestMapping public void test() {}");
        assertThat(analyzer.isEndpointMethod(method)).isTrue();
    }

    @Test
    void testIsNotEndpointMethod() {
        MethodDeclaration method = parseMethod("public void helper() {}");
        assertThat(analyzer.isEndpointMethod(method)).isFalse();
    }

    @Test
    void testGetHttpMethodFromGetMapping() {
        MethodDeclaration method = parseMethod("@GetMapping public void test() {}");
        assertThat(analyzer.getHttpMethod(method)).isEqualTo("GET");
    }

    @Test
    void testGetHttpMethodFromPostMapping() {
        MethodDeclaration method = parseMethod("@PostMapping public void test() {}");
        assertThat(analyzer.getHttpMethod(method)).isEqualTo("POST");
    }

    @Test
    void testGetHttpMethodFromPutMapping() {
        MethodDeclaration method = parseMethod("@PutMapping public void test() {}");
        assertThat(analyzer.getHttpMethod(method)).isEqualTo("PUT");
    }

    @Test
    void testGetHttpMethodFromDeleteMapping() {
        MethodDeclaration method = parseMethod("@DeleteMapping public void test() {}");
        assertThat(analyzer.getHttpMethod(method)).isEqualTo("DELETE");
    }

    @Test
    void testGetHttpMethodFromPatchMapping() {
        MethodDeclaration method = parseMethod("@PatchMapping public void test() {}");
        assertThat(analyzer.getHttpMethod(method)).isEqualTo("PATCH");
    }

    @Test
    void testGetHttpMethodFromRequestMapping() {
        MethodDeclaration method = parseMethod("@RequestMapping(method = RequestMethod.POST) public void test() {}");
        assertThat(analyzer.getHttpMethod(method)).isEqualTo("POST");
    }

    @Test
    void testGetPath() {
        MethodDeclaration method = parseMethod("@GetMapping(\"/users\") public void test() {}");
        assertThat(analyzer.getPath(method)).isEqualTo("/users");
    }

    @Test
    void testGetPathWithValueAttribute() {
        MethodDeclaration method = parseMethod("@GetMapping(value = \"/products\") public void test() {}");
        assertThat(analyzer.getPath(method)).isEqualTo("/products");
    }

    @Test
    void testGetPathWithPathAttribute() {
        MethodDeclaration method = parseMethod("@GetMapping(path = \"/orders\") public void test() {}");
        assertThat(analyzer.getPath(method)).isEqualTo("/orders");
    }

    @Test
    void testGetPathEmpty() {
        MethodDeclaration method = parseMethod("@GetMapping public void test() {}");
        assertThat(analyzer.getPath(method)).isEmpty();
    }

    @Test
    void testGetOperationId() {
        MethodDeclaration method = parseMethod("@GetMapping public void getUserById() {}");
        assertThat(analyzer.getOperationId(method)).isEqualTo("getUserById");
    }

    @Test
    void testIsDeprecated() {
        MethodDeclaration method = parseMethod("@Deprecated @GetMapping public void test() {}");
        assertThat(analyzer.isDeprecated(method)).isTrue();
    }

    @Test
    void testIsNotDeprecated() {
        MethodDeclaration method = parseMethod("@GetMapping public void test() {}");
        assertThat(analyzer.isDeprecated(method)).isFalse();
    }

    @Test
    void testGetResponseStatusCreated() {
        MethodDeclaration method = parseMethod("@ResponseStatus(HttpStatus.CREATED) @PostMapping public void test() {}");
        Optional<String> status = analyzer.getResponseStatus(method);
        assertThat(status).isPresent().contains("201");
    }

    @Test
    void testGetResponseStatusNoContent() {
        MethodDeclaration method = parseMethod("@ResponseStatus(HttpStatus.NO_CONTENT) @DeleteMapping public void test() {}");
        Optional<String> status = analyzer.getResponseStatus(method);
        assertThat(status).isPresent().contains("204");
    }

    @Test
    void testGetResponseStatusNotPresent() {
        MethodDeclaration method = parseMethod("@GetMapping public void test() {}");
        Optional<String> status = analyzer.getResponseStatus(method);
        assertThat(status).isEmpty();
    }

    private MethodDeclaration parseMethod(String methodCode) {
        String code = "class Test { " + methodCode + " }";
        CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
        return cu.findFirst(MethodDeclaration.class).orElseThrow();
    }
}
