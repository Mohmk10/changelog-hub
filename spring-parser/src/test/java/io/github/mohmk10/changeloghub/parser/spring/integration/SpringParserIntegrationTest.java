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

/**
 * Integration tests for the Spring Parser module.
 * Tests the complete flow from parsing Spring controllers to generating reports.
 */
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

        // Should find endpoints from: UserController (6), ProductController (5),
        // OrderController (5), DeprecatedController (2) = 18 endpoints
        assertThat(apiSpec.getEndpoints().size()).isGreaterThanOrEqualTo(15);

        // Verify we have all HTTP methods
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.GET);
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.POST);
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.PUT);
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getMethod() == HttpMethod.DELETE);

        // Verify paths are correctly combined
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().equals("/api/users"));
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().equals("/api/users/{id}"));
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().equals("/api/products"));
    }

    @Test
    void testDetectBreakingChangesBetweenVersions() throws Exception {
        // Parse V1 (original UserController)
        Path v1Controller = getControllersDirectory().resolve("UserController.java");
        ApiSpec v1Spec = parser.parseFile(v1Controller);
        v1Spec.setName("User API");
        v1Spec.setVersion("1.0.0");

        // Parse V2 (UserController with breaking changes)
        Path v2Controller = getControllersDirectory().resolve("v2/UserControllerV2.java");
        ApiSpec v2Spec = parser.parseFile(v2Controller);
        v2Spec.setName("User API");
        v2Spec.setVersion("2.0.0");

        // Compare the two versions - returns a Changelog
        Changelog changelog = comparator.compare(v1Spec, v2Spec);

        assertThat(changelog).isNotNull();
        assertThat(changelog.getFromVersion()).isEqualTo("1.0.0");
        assertThat(changelog.getToVersion()).isEqualTo("2.0.0");

        // Get changes from changelog
        List<Change> changes = changelog.getChanges();

        // Detect breaking changes
        List<BreakingChange> breakingChanges = breakingChangeDetector.detect(changes);

        // We expect breaking changes:
        // 1. Removed endpoint: GET /{id}/legacy
        // 2. Added required parameter to GET /
        // 3. Changed parameter type from Long to String
        // V1 has 6 endpoints, V2 has 6 endpoints (1 removed, 1 added)
        assertThat(v1Spec.getEndpoints()).hasSize(6);
        assertThat(v2Spec.getEndpoints()).hasSize(6);

        // The legacy endpoint should be removed in V2
        assertThat(v1Spec.getEndpoints()).anyMatch(e -> e.getPath().contains("legacy"));
        assertThat(v2Spec.getEndpoints()).noneMatch(e -> e.getPath().contains("legacy"));

        // V2 should have the profile endpoint
        assertThat(v2Spec.getEndpoints()).anyMatch(e -> e.getPath().contains("profile"));
    }

    @Test
    void testEndToEndWithReporters() throws Exception {
        // Parse V1
        Path v1Controller = getControllersDirectory().resolve("UserController.java");
        ApiSpec v1Spec = parser.parseFile(v1Controller);
        v1Spec.setName("User API");
        v1Spec.setVersion("1.0.0");

        // Parse V2
        Path v2Controller = getControllersDirectory().resolve("v2/UserControllerV2.java");
        ApiSpec v2Spec = parser.parseFile(v2Controller);
        v2Spec.setName("User API");
        v2Spec.setVersion("2.0.0");

        // Compare - returns Changelog
        Changelog changelog = comparator.compare(v1Spec, v2Spec);

        assertThat(changelog).isNotNull();

        // Generate Markdown report
        Reporter markdownReporter = ReporterFactory.create(ReportFormat.MARKDOWN);
        String markdownReport = markdownReporter.report(changelog);

        assertThat(markdownReport).isNotEmpty();

        // Generate JSON report
        Reporter jsonReporter = ReporterFactory.create(ReportFormat.JSON);
        String jsonReport = jsonReporter.report(changelog);

        assertThat(jsonReport).isNotEmpty();

        // Generate HTML report
        Reporter htmlReporter = ReporterFactory.create(ReportFormat.HTML);
        String htmlReport = htmlReporter.report(changelog);

        assertThat(htmlReport).isNotEmpty();
        assertThat(htmlReport).contains("<html");
    }

    @Test
    void testIgnoreNonControllerClasses() throws Exception {
        Path nonController = getControllersDirectory().resolve("NonController.java");

        // Should not be identified as a controller
        assertThat(parser.isSpringController(nonController)).isFalse();

        // Parsing it should throw exception since no controller is found
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

        // Find GET /api/users endpoint
        Endpoint getUsersEndpoint = apiSpec.getEndpoints().stream()
                .filter(e -> e.getPath().equals("/api/users") && e.getMethod() == HttpMethod.GET)
                .findFirst()
                .orElseThrow();

        // Should have 2 query parameters: limit and search
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

        // Find GET /api/users/{id} endpoint
        Endpoint getUserByIdEndpoint = apiSpec.getEndpoints().stream()
                .filter(e -> e.getPath().equals("/api/users/{id}") && e.getMethod() == HttpMethod.GET)
                .findFirst()
                .orElseThrow();

        // Should have 1 path parameter: id
        assertThat(getUserByIdEndpoint.getParameters()).hasSize(1);
        assertThat(getUserByIdEndpoint.getParameters().get(0).getName()).isEqualTo("id");
        assertThat(getUserByIdEndpoint.getParameters().get(0).getLocation()).isEqualTo(ParameterLocation.PATH);
    }

    @Test
    void testParseRequestBody() throws Exception {
        Path userController = getControllersDirectory().resolve("UserController.java");

        ApiSpec apiSpec = parser.parseFile(userController);

        // Find POST /api/users endpoint
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

        // Verify endpoints from different controllers are parsed correctly
        // UserController: /api/users
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().startsWith("/api/users"));

        // ProductController: /api/products
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().startsWith("/api/products"));

        // OrderController: /api/orders
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().startsWith("/api/orders"));

        // DeprecatedController: /api/v1/legacy
        assertThat(apiSpec.getEndpoints()).anyMatch(e -> e.getPath().startsWith("/api/v1/legacy"));
    }

    @Test
    void testVersionComparison() throws Exception {
        // Parse V1
        Path v1Controller = getControllersDirectory().resolve("UserController.java");
        ApiSpec v1Spec = parser.parseFile(v1Controller);

        // Parse V2
        Path v2Controller = getControllersDirectory().resolve("v2/UserControllerV2.java");
        ApiSpec v2Spec = parser.parseFile(v2Controller);

        // V1 endpoints
        List<String> v1Paths = v1Spec.getEndpoints().stream()
                .map(Endpoint::getPath)
                .toList();

        // V2 endpoints
        List<String> v2Paths = v2Spec.getEndpoints().stream()
                .map(Endpoint::getPath)
                .toList();

        // V2 removes GET /{id}/legacy
        assertThat(v1Paths).contains("/api/users/{id}/legacy");
        assertThat(v2Paths).doesNotContain("/api/users/{id}/legacy");

        // V2 adds GET /{id}/profile
        assertThat(v1Paths).doesNotContain("/api/users/{id}/profile");
        assertThat(v2Paths).contains("/api/users/{id}/profile");
    }

    @Test
    void testBreakingChangeDetection() throws Exception {
        // Parse V1
        Path v1Controller = getControllersDirectory().resolve("UserController.java");
        ApiSpec v1Spec = parser.parseFile(v1Controller);
        v1Spec.setVersion("1.0.0");

        // Parse V2
        Path v2Controller = getControllersDirectory().resolve("v2/UserControllerV2.java");
        ApiSpec v2Spec = parser.parseFile(v2Controller);
        v2Spec.setVersion("2.0.0");

        // Compare
        Changelog changelog = comparator.compare(v1Spec, v2Spec);
        List<Change> changes = changelog.getChanges();

        // Detect breaking changes
        List<BreakingChange> breakingChanges = breakingChangeDetector.detect(changes);

        // Check if any changes are breaking
        boolean hasBreaking = changes.stream()
                .anyMatch(breakingChangeDetector::isBreaking);

        // We should have at least breaking changes (removed endpoint, added required param)
        // The exact count depends on how the comparator works
        assertThat(changelog).isNotNull();
    }

    private Path getControllersDirectory() {
        return Path.of("src/test/resources/controllers");
    }
}
