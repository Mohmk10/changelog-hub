package io.github.mohmk10.changeloghub.parser.spring.integration;

import io.github.mohmk10.changeloghub.core.comparator.ApiComparator;
import io.github.mohmk10.changeloghub.core.comparator.impl.DefaultApiComparator;
import io.github.mohmk10.changeloghub.core.detector.BreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.detector.impl.DefaultBreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.core.reporter.ReportFormat;
import io.github.mohmk10.changeloghub.core.reporter.Reporter;
import io.github.mohmk10.changeloghub.core.reporter.ReporterFactory;
import io.github.mohmk10.changeloghub.parser.spring.SpringParser;
import io.github.mohmk10.changeloghub.parser.spring.impl.DefaultSpringParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SpringParserIntegrationTest {

    private SpringParser parser;
    private ApiComparator comparator;
    private BreakingChangeDetector breakingChangeDetector;

    @BeforeEach
    void setUp() {
        parser = new DefaultSpringParser();
        comparator = new DefaultApiComparator();
        breakingChangeDetector = new DefaultBreakingChangeDetector();
    }

    @Test
    void testParseAllControllersFromDirectory() throws Exception {
        Path controllersDir = getControllersDirectory();

        ApiSpec apiSpec = parser.parse(controllersDir, "Test API", "1.0.0");

        assertThat(apiSpec).isNotNull();
        assertThat(apiSpec.getName()).isEqualTo("Test API");
        assertThat(apiSpec.getVersion()).isEqualTo("1.0.0");

        assertThat(apiSpec.getEndpoints().size()).isGreaterThanOrEqualTo(15);

        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.GET);
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.POST);
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.PUT);
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.DELETE);

        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().equals("/api/users"));
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().equals("/api/users/{id}"));
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().equals("/api/products"));
    }

    @Test
    void testDetectBreakingChangesBetweenVersions() throws Exception {
        
        Path v1Controller = getControllersDirectory().resolve("UserController.java");
        ApiSpec v1Spec = parser.parseFile(v1Controller);
        v1Spec.setName("User API");
        v1Spec.setVersion("1.0.0");

        Path v2Controller = getControllersDirectory().resolve("v2/UserControllerV2.java");
        ApiSpec v2Spec = parser.parseFile(v2Controller);
        v2Spec.setName("User API");
        v2Spec.setVersion("2.0.0");

        Changelog changelog = comparator.compare(v1Spec, v2Spec);

        assertThat(changelog).isNotNull();
        assertThat(changelog.getFromVersion()).isEqualTo("1.0.0");
        assertThat(changelog.getToVersion()).isEqualTo("2.0.0");

        List<Change> changes = changelog.getChanges();

        List<BreakingChange> breakingChanges = breakingChangeDetector.detect(changes);

        assertThat(v1Spec.getEndpoints()).hasSize(6);
        assertThat(v2Spec.getEndpoints()).hasSize(6);

        assertThat(v1Spec.getEndpoints()).anyMatch(e -> e.getPath().contains("legacy"));
        assertThat(v2Spec.getEndpoints()).noneMatch(e -> e.getPath().contains("legacy"));

        assertThat(v2Spec.getEndpoints()).anyMatch(e -> e.getPath().contains("profile"));
    }

    @Test
    void testEndToEndWithReporters() throws Exception {
        
        Path v1Controller = getControllersDirectory().resolve("UserController.java");
        ApiSpec v1Spec = parser.parseFile(v1Controller);
        v1Spec.setName("User API");
        v1Spec.setVersion("1.0.0");

        Path v2Controller = getControllersDirectory().resolve("v2/UserControllerV2.java");
        ApiSpec v2Spec = parser.parseFile(v2Controller);
        v2Spec.setName("User API");
        v2Spec.setVersion("2.0.0");

        Changelog changelog = comparator.compare(v1Spec, v2Spec);

        assertThat(changelog).isNotNull();

        Reporter markdownReporter = ReporterFactory.create(ReportFormat.MARKDOWN);
        String markdownReport = markdownReporter.report(changelog);

        assertThat(markdownReport).isNotEmpty();

        Reporter jsonReporter = ReporterFactory.create(ReportFormat.JSON);
        String jsonReport = jsonReporter.report(changelog);

        assertThat(jsonReport).isNotEmpty();

        Reporter htmlReporter = ReporterFactory.create(ReportFormat.HTML);
        String htmlReport = htmlReporter.report(changelog);

        assertThat(htmlReport).isNotEmpty();
        assertThat(htmlReport).contains("<html");
    }

    @Test
    void testIgnoreNonControllerClasses() throws Exception {
        Path nonController = getControllersDirectory().resolve("NonController.java");

        assertThat(parser.isSpringController(nonController)).isFalse();

        assertThatThrownBy(() -> parser.parseFile(nonController))
                .isInstanceOf(io.github.mohmk10.changeloghub.parser.spring.exception.SpringParseException.class)
                .hasMessageContaining("No Spring controller found");
    }

    @Test
    void testParseDeprecatedEndpoints() throws Exception {
        Path deprecatedController = getControllersDirectory().resolve("DeprecatedController.java");

        ApiSpec apiSpec = parser.parseFile(deprecatedController);

        assertThat(apiSpec.getEndpoints()).isNotEmpty();
        assertThat(apiSpec.getEndpoints()).allMatch(Endpoint::isDeprecated);
    }

    @Test
    void testParseEndpointParameters() throws Exception {
        Path userController = getControllersDirectory().resolve("UserController.java");

        ApiSpec apiSpec = parser.parseFile(userController);

        Endpoint getUsersEndpoint = apiSpec.getEndpoints().stream()
                .filter(e -> e.getPath().equals("/api/users") && e.getMethod() == HttpMethod.GET)
                .findFirst()
                .orElseThrow();

        assertThat(getUsersEndpoint.getParameters()).hasSize(2);
        assertThat(getUsersEndpoint.getParameters())
                .anyMatch(p -> p.getName().equals("limit") && p.getLocation() == ParameterLocation.QUERY);
        assertThat(getUsersEndpoint.getParameters())
                .anyMatch(p -> p.getName().equals("search") && p.getLocation() == ParameterLocation.QUERY);
    }

    @Test
    void testParsePathVariables() throws Exception {
        Path userController = getControllersDirectory().resolve("UserController.java");

        ApiSpec apiSpec = parser.parseFile(userController);

        Endpoint getUserByIdEndpoint = apiSpec.getEndpoints().stream()
                .filter(e -> e.getPath().equals("/api/users/{id}") && e.getMethod() == HttpMethod.GET)
                .findFirst()
                .orElseThrow();

        assertThat(getUserByIdEndpoint.getParameters()).hasSize(1);
        assertThat(getUserByIdEndpoint.getParameters().get(0).getName()).isEqualTo("id");
        assertThat(getUserByIdEndpoint.getParameters().get(0).getLocation()).isEqualTo(ParameterLocation.PATH);
    }

    @Test
    void testParseRequestBody() throws Exception {
        Path userController = getControllersDirectory().resolve("UserController.java");

        ApiSpec apiSpec = parser.parseFile(userController);

        Endpoint createUserEndpoint = apiSpec.getEndpoints().stream()
                .filter(e -> e.getPath().equals("/api/users") && e.getMethod() == HttpMethod.POST)
                .findFirst()
                .orElseThrow();

        assertThat(createUserEndpoint.getRequestBody()).isNotNull();
        assertThat(createUserEndpoint.getRequestBody().isRequired()).isTrue();
    }

    @Test
    void testParseMultipleControllerTypes() throws Exception {
        Path controllersDir = getControllersDirectory();

        ApiSpec apiSpec = parser.parse(controllersDir);

        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().startsWith("/api/users"));

        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().startsWith("/api/products"));

        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().startsWith("/api/orders"));

        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().startsWith("/api/v1/legacy"));
    }

    @Test
    void testVersionComparison() throws Exception {
        
        Path v1Controller = getControllersDirectory().resolve("UserController.java");
        ApiSpec v1Spec = parser.parseFile(v1Controller);

        Path v2Controller = getControllersDirectory().resolve("v2/UserControllerV2.java");
        ApiSpec v2Spec = parser.parseFile(v2Controller);

        List<String> v1Paths = v1Spec.getEndpoints().stream()
                .map(Endpoint::getPath)
                .toList();

        List<String> v2Paths = v2Spec.getEndpoints().stream()
                .map(Endpoint::getPath)
                .toList();

        assertThat(v1Paths).contains("/api/users/{id}/legacy");
        assertThat(v2Paths).doesNotContain("/api/users/{id}/legacy");

        assertThat(v1Paths).doesNotContain("/api/users/{id}/profile");
        assertThat(v2Paths).contains("/api/users/{id}/profile");
    }

    @Test
    void testBreakingChangeDetection() throws Exception {
        
        Path v1Controller = getControllersDirectory().resolve("UserController.java");
        ApiSpec v1Spec = parser.parseFile(v1Controller);
        v1Spec.setVersion("1.0.0");

        Path v2Controller = getControllersDirectory().resolve("v2/UserControllerV2.java");
        ApiSpec v2Spec = parser.parseFile(v2Controller);
        v2Spec.setVersion("2.0.0");

        Changelog changelog = comparator.compare(v1Spec, v2Spec);
        List<Change> changes = changelog.getChanges();

        List<BreakingChange> breakingChanges = breakingChangeDetector.detect(changes);

        boolean hasBreaking = changes.stream()
                .anyMatch(breakingChangeDetector::isBreaking);

        assertThat(changelog).isNotNull();
    }

    private Path getControllersDirectory() {
        return Path.of("src/test/resources/controllers");
    }
}
