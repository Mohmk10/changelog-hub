package io.github.mohmk10.changeloghub.parser.spring.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class ParameterAnalyzerTest {

    private ParameterAnalyzer analyzer;
    private JavaParser javaParser;

    @BeforeEach
    void setUp() {
        analyzer = new ParameterAnalyzer();
        javaParser = new JavaParser();
    }

    @Test
    void testAnalyzeRequestParam() {
        Parameter param = parseParameter("void test(@RequestParam String name) {}");
        Optional<SpringParameter> result = analyzer.analyzeParameter(param);

        assertThat(result).isPresent();
        assertThat(result.get().getLocation()).isEqualTo(SpringParameter.Location.QUERY);
        assertThat(result.get().getName()).isEqualTo("name");
        assertThat(result.get().getJavaType()).isEqualTo("String");
    }

    @Test
    void testAnalyzePathVariable() {
        Parameter param = parseParameter("void test(@PathVariable Long id) {}");
        Optional<SpringParameter> result = analyzer.analyzeParameter(param);

        assertThat(result).isPresent();
        assertThat(result.get().getLocation()).isEqualTo(SpringParameter.Location.PATH);
        assertThat(result.get().getName()).isEqualTo("id");
    }

    @Test
    void testAnalyzeRequestBody() {
        Parameter param = parseParameter("void test(@RequestBody UserRequest request) {}");
        Optional<SpringParameter> result = analyzer.analyzeParameter(param);

        assertThat(result).isPresent();
        assertThat(result.get().getLocation()).isEqualTo(SpringParameter.Location.BODY);
        assertThat(result.get().getName()).isEqualTo("request");
    }

    @Test
    void testAnalyzeRequestHeader() {
        Parameter param = parseParameter("void test(@RequestHeader(\"X-Token\") String token) {}");
        Optional<SpringParameter> result = analyzer.analyzeParameter(param);

        assertThat(result).isPresent();
        assertThat(result.get().getLocation()).isEqualTo(SpringParameter.Location.HEADER);
        assertThat(result.get().getName()).isEqualTo("X-Token");
    }

    @Test
    void testAnalyzeCookieValue() {
        Parameter param = parseParameter("void test(@CookieValue(\"session\") String sessionId) {}");
        Optional<SpringParameter> result = analyzer.analyzeParameter(param);

        assertThat(result).isPresent();
        assertThat(result.get().getLocation()).isEqualTo(SpringParameter.Location.COOKIE);
        assertThat(result.get().getName()).isEqualTo("session");
    }

    @Test
    void testAnalyzeUnannotatedParameter() {
        Parameter param = parseParameter("void test(String value) {}");
        Optional<SpringParameter> result = analyzer.analyzeParameter(param);

        assertThat(result).isEmpty();
    }

    @Test
    void testGetParameterNameFromAnnotation() {
        Parameter param = parseParameter("void test(@RequestParam(name = \"user_name\") String name) {}");
        String name = analyzer.getParameterName(param);

        assertThat(name).isEqualTo("user_name");
    }

    @Test
    void testGetParameterNameFromValue() {
        Parameter param = parseParameter("void test(@RequestParam(\"userId\") Long id) {}");
        String name = analyzer.getParameterName(param);

        assertThat(name).isEqualTo("userId");
    }

    @Test
    void testIsRequiredDefault() {
        Parameter param = parseParameter("void test(@RequestParam String name) {}");
        assertThat(analyzer.isRequired(param)).isTrue();
    }

    @Test
    void testIsRequiredFalse() {
        Parameter param = parseParameter("void test(@RequestParam(required = false) String name) {}");
        assertThat(analyzer.isRequired(param)).isFalse();
    }

    @Test
    void testIsRequiredWithDefaultValue() {
        Parameter param = parseParameter("void test(@RequestParam(defaultValue = \"10\") Integer limit) {}");
        assertThat(analyzer.isRequired(param)).isFalse();
    }

    @Test
    void testPathVariableIsRequiredByDefault() {
        Parameter param = parseParameter("void test(@PathVariable Long id) {}");
        assertThat(analyzer.isRequired(param)).isTrue();
    }

    @Test
    void testRequestBodyIsRequiredByDefault() {
        Parameter param = parseParameter("void test(@RequestBody UserRequest request) {}");
        assertThat(analyzer.isRequired(param)).isTrue();
    }

    @Test
    void testGetDefaultValue() {
        Parameter param = parseParameter("void test(@RequestParam(defaultValue = \"20\") Integer limit) {}");
        Optional<String> defaultValue = analyzer.getDefaultValue(param);

        assertThat(defaultValue).isPresent().contains("20");
    }

    @Test
    void testGetDefaultValueNotPresent() {
        Parameter param = parseParameter("void test(@RequestParam String name) {}");
        Optional<String> defaultValue = analyzer.getDefaultValue(param);

        assertThat(defaultValue).isEmpty();
    }

    @Test
    void testAnalyzeMultipleParameters() {
        String code = """
                void test(
                    @PathVariable Long id,
                    @RequestParam(required = false) String filter,
                    @RequestBody UserRequest request
                ) {}
                """;
        MethodDeclaration method = parseMethod("class Test { " + code + " }");
        List<SpringParameter> params = analyzer.analyzeParameters(method);

        assertThat(params).hasSize(3);
        assertThat(params.get(0).getLocation()).isEqualTo(SpringParameter.Location.PATH);
        assertThat(params.get(1).getLocation()).isEqualTo(SpringParameter.Location.QUERY);
        assertThat(params.get(2).getLocation()).isEqualTo(SpringParameter.Location.BODY);
    }

    private Parameter parseParameter(String methodCode) {
        String code = "class Test { " + methodCode + " }";
        CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();
        return method.getParameters().get(0);
    }

    private MethodDeclaration parseMethod(String code) {
        CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
        return cu.findFirst(MethodDeclaration.class).orElseThrow();
    }
}
