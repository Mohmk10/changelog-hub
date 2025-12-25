package io.github.mohmk10.changeloghub.parser.spring.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ControllerAnalyzerTest {

    private ControllerAnalyzer analyzer;
    private JavaParser javaParser;

    @BeforeEach
    void setUp() {
        analyzer = new ControllerAnalyzer();
        javaParser = new JavaParser();
    }

    @Test
    void testIsControllerWithRestController() {
        String code = """
                @RestController
                public class TestController {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.isController(clazz)).isTrue();
    }

    @Test
    void testIsControllerWithControllerAndResponseBody() {
        String code = """
                @Controller
                @ResponseBody
                public class TestController {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.isController(clazz)).isTrue();
    }

    @Test
    void testIsNotControllerWithOnlyController() {
        String code = """
                @Controller
                public class TestController {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.isController(clazz)).isFalse();
    }

    @Test
    void testIsNotControllerWithService() {
        String code = """
                @Service
                public class TestService {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.isController(clazz)).isFalse();
    }

    @Test
    void testGetBasePath() {
        String code = """
                @RestController
                @RequestMapping("/api/users")
                public class UserController {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.getBasePath(clazz)).isEqualTo("/api/users");
    }

    @Test
    void testGetBasePathWithValueAttribute() {
        String code = """
                @RestController
                @RequestMapping(value = "/api/products")
                public class ProductController {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.getBasePath(clazz)).isEqualTo("/api/products");
    }

    @Test
    void testGetBasePathWithPathAttribute() {
        String code = """
                @RestController
                @RequestMapping(path = "/api/orders")
                public class OrderController {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.getBasePath(clazz)).isEqualTo("/api/orders");
    }

    @Test
    void testGetBasePathNoMapping() {
        String code = """
                @RestController
                public class RootController {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.getBasePath(clazz)).isEmpty();
    }

    @Test
    void testGetEndpointMethods() {
        String code = """
                @RestController
                public class TestController {
                    @GetMapping
                    public String get() { return null; }

                    @PostMapping
                    public void post() {}

                    public void helper() {}
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        List<MethodDeclaration> methods = analyzer.getEndpointMethods(clazz);

        assertThat(methods).hasSize(2);
    }

    @Test
    void testIsDeprecated() {
        String code = """
                @Deprecated
                @RestController
                public class OldController {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.isDeprecated(clazz)).isTrue();
    }

    @Test
    void testIsNotDeprecated() {
        String code = """
                @RestController
                public class NewController {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.isDeprecated(clazz)).isFalse();
    }

    @Test
    void testGetClassName() {
        String code = """
                @RestController
                public class MyController {
                }
                """;

        ClassOrInterfaceDeclaration clazz = parseClass(code);
        assertThat(analyzer.getClassName(clazz)).isEqualTo("MyController");
    }

    private ClassOrInterfaceDeclaration parseClass(String code) {
        CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
        return cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
    }
}
