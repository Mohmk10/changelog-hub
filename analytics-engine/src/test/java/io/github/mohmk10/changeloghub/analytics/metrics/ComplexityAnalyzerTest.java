package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.TechnicalDebt;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.Parameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexityAnalyzerTest {

    private ComplexityAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new ComplexityAnalyzer();
    }

    @Test
    void analyzeComplexity_withNullSpec_shouldReturnZero() {
        int complexity = analyzer.analyzeComplexity(null);
        assertThat(complexity).isZero();
    }

    @Test
    void analyzeComplexity_withEmptySpec_shouldReturnZero() {
        ApiSpec spec = new ApiSpec();
        spec.setEndpoints(new ArrayList<>());

        int complexity = analyzer.analyzeComplexity(spec);
        assertThat(complexity).isZero();
    }

    @Test
    void analyzeComplexity_shouldIncreaseWithEndpoints() {
        ApiSpec smallSpec = createApiSpec(5);
        ApiSpec largeSpec = createApiSpec(20);

        int smallComplexity = analyzer.analyzeComplexity(smallSpec);
        int largeComplexity = analyzer.analyzeComplexity(largeSpec);

        assertThat(largeComplexity).isGreaterThan(smallComplexity);
    }

    @Test
    void analyzeComplexity_shouldIncreaseWithParameters() {
        ApiSpec simpleSpec = createApiSpecWithParameters(5, 1);
        ApiSpec complexSpec = createApiSpecWithParameters(5, 10);

        int simpleComplexity = analyzer.analyzeComplexity(simpleSpec);
        int complexComplexity = analyzer.analyzeComplexity(complexSpec);

        assertThat(complexComplexity).isGreaterThan(simpleComplexity);
    }

    @Test
    void analyzeTechnicalDebt_withNullSpec_shouldReturnEmptyDebt() {
        TechnicalDebt debt = analyzer.analyzeTechnicalDebt(null);
        assertThat(debt).isNotNull();
        assertThat(debt.getTotalIssues()).isZero();
    }

    @Test
    void analyzeTechnicalDebt_shouldDetectDeprecatedEndpoints() {
        ApiSpec spec = new ApiSpec();
        List<Endpoint> endpoints = Arrays.asList(
                createEndpoint("/api/v1", false, "Description"),
                createEndpoint("/api/old", true, "Description"),
                createEndpoint("/api/legacy", true, "Description")
        );
        spec.setEndpoints(endpoints);

        TechnicalDebt debt = analyzer.analyzeTechnicalDebt(spec);
        assertThat(debt.getDeprecatedEndpointsCount()).isEqualTo(2);
    }

    @Test
    void analyzeTechnicalDebt_shouldDetectMissingDocumentation() {
        ApiSpec spec = new ApiSpec();
        List<Endpoint> endpoints = Arrays.asList(
                createEndpoint("/api/v1", false, "Description"),
                createEndpoint("/api/v2", false, null),
                createEndpoint("/api/v3", false, "")
        );
        spec.setEndpoints(endpoints);

        TechnicalDebt debt = analyzer.analyzeTechnicalDebt(spec);
        assertThat(debt.getMissingDocumentationCount()).isEqualTo(2);
    }

    @Test
    void analyzeTechnicalDebt_shouldDetectNamingInconsistencies() {
        ApiSpec spec = new ApiSpec();
        List<Endpoint> endpoints = Arrays.asList(
                createEndpoint("/api/users", false, "Get users"),
                createEndpoint("/api/GetPosts", false, "Get posts"), // CamelCase
                createEndpoint("/API/comments", false, "Get comments") // Uppercase
        );
        spec.setEndpoints(endpoints);

        TechnicalDebt debt = analyzer.analyzeTechnicalDebt(spec);
        assertThat(debt.getInconsistentNamingCount()).isGreaterThan(0);
    }

    @Test
    void analyzeTechnicalDebt_shouldCalculateDebtScore() {
        ApiSpec spec = new ApiSpec();
        List<Endpoint> endpoints = Arrays.asList(
                createEndpoint("/api/v1", true, null), // deprecated + missing doc
                createEndpoint("/api/v2", false, "Description")
        );
        spec.setEndpoints(endpoints);

        TechnicalDebt debt = analyzer.analyzeTechnicalDebt(spec);
        assertThat(debt.getDebtScore()).isGreaterThan(0);
    }

    @Test
    void getComplexityLevel_shouldReturnCorrectLevel() {
        assertThat(analyzer.getComplexityLevel(10)).isEqualTo("Simple");
        assertThat(analyzer.getComplexityLevel(30)).isEqualTo("Low");
        assertThat(analyzer.getComplexityLevel(50)).isEqualTo("Moderate");
        assertThat(analyzer.getComplexityLevel(70)).isEqualTo("High");
        assertThat(analyzer.getComplexityLevel(90)).isEqualTo("Very High");
    }

    @Test
    void isComplex_shouldIdentifyHighComplexity() {
        assertThat(analyzer.isComplex(50)).isFalse();
        assertThat(analyzer.isComplex(70)).isTrue();
    }

    private ApiSpec createApiSpec(int endpointCount) {
        ApiSpec spec = new ApiSpec();
        List<Endpoint> endpoints = new ArrayList<>();
        for (int i = 0; i < endpointCount; i++) {
            Endpoint endpoint = new Endpoint();
            endpoint.setPath("/api/endpoint" + i);
            endpoint.setMethod(HttpMethod.GET);
            endpoint.setDescription("Description " + i);
            endpoints.add(endpoint);
        }
        spec.setEndpoints(endpoints);
        return spec;
    }

    private ApiSpec createApiSpecWithParameters(int endpointCount, int paramsPerEndpoint) {
        ApiSpec spec = new ApiSpec();
        List<Endpoint> endpoints = new ArrayList<>();
        for (int i = 0; i < endpointCount; i++) {
            Endpoint endpoint = new Endpoint();
            endpoint.setPath("/api/endpoint" + i);
            endpoint.setMethod(HttpMethod.GET);

            List<Parameter> params = new ArrayList<>();
            for (int j = 0; j < paramsPerEndpoint; j++) {
                Parameter param = new Parameter();
                param.setName("param" + j);
                param.setType("string");
                params.add(param);
            }
            endpoint.setParameters(params);
            endpoints.add(endpoint);
        }
        spec.setEndpoints(endpoints);
        return spec;
    }

    private Endpoint createEndpoint(String path, boolean deprecated, String description) {
        Endpoint endpoint = new Endpoint();
        endpoint.setPath(path);
        endpoint.setMethod(HttpMethod.GET);
        endpoint.setDeprecated(deprecated);
        endpoint.setDescription(description);
        return endpoint;
    }
}
