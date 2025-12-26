package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.ApiMetrics;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsCalculatorTest {

    private MetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DefaultMetricsCalculator();
    }

    @Test
    void calculate_withNullSpec_shouldReturnEmptyMetrics() {
        ApiMetrics metrics = calculator.calculate(null);
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalEndpoints()).isZero();
    }

    @Test
    void calculate_withEmptySpec_shouldReturnEmptyMetrics() {
        ApiSpec spec = new ApiSpec();
        spec.setEndpoints(new ArrayList<>());

        ApiMetrics metrics = calculator.calculate(spec);
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalEndpoints()).isZero();
    }

    @Test
    void calculate_shouldCountEndpoints() {
        ApiSpec spec = createApiSpec(5, 0);

        ApiMetrics metrics = calculator.calculate(spec);
        assertThat(metrics.getTotalEndpoints()).isEqualTo(5);
    }

    @Test
    void calculate_shouldCountDeprecatedEndpoints() {
        ApiSpec spec = createApiSpec(5, 2);

        ApiMetrics metrics = calculator.calculate(spec);
        assertThat(metrics.getDeprecatedEndpoints()).isEqualTo(2);
    }

    @Test
    void calculate_shouldCalculateDocumentationCoverage() {
        ApiSpec spec = new ApiSpec();
        List<Endpoint> endpoints = new ArrayList<>();

        endpoints.add(createEndpoint("/api/users", HttpMethod.GET, "Get all users"));
        endpoints.add(createEndpoint("/api/users/{id}", HttpMethod.GET, "Get user by ID"));
        endpoints.add(createEndpoint("/api/posts", HttpMethod.GET, null));
        endpoints.add(createEndpoint("/api/comments", HttpMethod.GET, ""));

        spec.setEndpoints(endpoints);

        ApiMetrics metrics = calculator.calculate(spec);
        assertThat(metrics.getDocumentationCoverage()).isEqualTo(0.5);
    }

    @Test
    void calculate_shouldCalculateComplexityScore() {
        ApiSpec spec = createApiSpec(20, 3);

        ApiMetrics metrics = calculator.calculate(spec);
        assertThat(metrics.getComplexityScore()).isGreaterThan(0);
    }

    @Test
    void calculate_withFullDocumentation_shouldReturn100PercentCoverage() {
        ApiSpec spec = new ApiSpec();
        List<Endpoint> endpoints = Arrays.asList(
                createEndpoint("/api/v1", HttpMethod.GET, "Description 1"),
                createEndpoint("/api/v2", HttpMethod.POST, "Description 2")
        );
        spec.setEndpoints(endpoints);

        ApiMetrics metrics = calculator.calculate(spec);
        assertThat(metrics.getDocumentationCoverage()).isEqualTo(1.0);
    }

    private ApiSpec createApiSpec(int endpointCount, int deprecatedCount) {
        ApiSpec spec = new ApiSpec();
        List<Endpoint> endpoints = new ArrayList<>();

        for (int i = 0; i < endpointCount; i++) {
            Endpoint endpoint = new Endpoint();
            endpoint.setPath("/api/endpoint" + i);
            endpoint.setMethod(HttpMethod.GET);
            endpoint.setDescription("Endpoint " + i);
            endpoint.setDeprecated(i < deprecatedCount);
            endpoints.add(endpoint);
        }

        spec.setEndpoints(endpoints);
        return spec;
    }

    private Endpoint createEndpoint(String path, HttpMethod method, String description) {
        Endpoint endpoint = new Endpoint();
        endpoint.setPath(path);
        endpoint.setMethod(method);
        endpoint.setDescription(description);
        return endpoint;
    }
}
