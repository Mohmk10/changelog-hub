package io.github.mohmk10.changeloghub.analytics.impl;

import io.github.mohmk10.changeloghub.analytics.AnalyticsService;
import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.report.*;
import io.github.mohmk10.changeloghub.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAnalyticsServiceTest {

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new DefaultAnalyticsService();
    }

    @Test
    void calculateMetrics_shouldReturnMetrics() {
        ApiSpec spec = createApiSpec(10, 2);

        ApiMetrics metrics = service.calculateMetrics(spec);

        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalEndpoints()).isEqualTo(10);
        assertThat(metrics.getDeprecatedEndpoints()).isEqualTo(2);
    }

    @Test
    void calculateMetrics_withNullSpec_shouldReturnEmptyMetrics() {
        ApiMetrics metrics = service.calculateMetrics(null);
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalEndpoints()).isZero();
    }

    @Test
    void calculateStability_shouldReturnScore() {
        List<Changelog> history = createHistory(5, 2);

        StabilityScore stability = service.calculateStability(history);

        assertThat(stability).isNotNull();
        assertThat(stability.getScore()).isBetween(0, 100);
        assertThat(stability.getGrade()).isNotNull();
    }

    @Test
    void calculateStability_withEmptyHistory_shouldReturnPerfectScore() {
        StabilityScore stability = service.calculateStability(Collections.emptyList());

        assertThat(stability.getScore()).isEqualTo(100);
    }

    @Test
    void analyzeRiskTrend_shouldReturnTrend() {
        List<Changelog> history = createHistory(5, 3);

        RiskTrend trend = service.analyzeRiskTrend(history);

        assertThat(trend).isNotNull();
        assertThat(trend.getDirection()).isNotNull();
    }

    @Test
    void calculateVelocity_shouldReturnVelocity() {
        List<Changelog> history = createHistory(10, 0);

        ChangeVelocity velocity = service.calculateVelocity(history);

        assertThat(velocity).isNotNull();
    }

    @Test
    void analyzeTechnicalDebt_shouldReturnDebt() {
        ApiSpec spec = createApiSpec(10, 3);

        TechnicalDebt debt = service.analyzeTechnicalDebt(spec);

        assertThat(debt).isNotNull();
        assertThat(debt.getDeprecatedEndpointsCount()).isEqualTo(3);
    }

    @Test
    void analyzeEvolution_shouldReturnEvolution() {
        List<Changelog> history = createHistory(5, 2);

        ApiEvolution evolution = service.analyzeEvolution(history);

        assertThat(evolution).isNotNull();
        assertThat(evolution.getVersions()).hasSize(5);
    }

    @Test
    void generateInsights_shouldReturnInsights() {
        List<Changelog> history = createHistory(5, 3);

        List<Insight> insights = service.generateInsights(history);

        assertThat(insights).isNotNull();
    }

    @Test
    void generateRecommendations_shouldReturnRecommendations() {
        ApiMetrics metrics = ApiMetrics.builder()
                .documentationCoverage(0.5)
                .complexityScore(80)
                .build();

        StabilityScore stability = StabilityScore.builder()
                .score(60)
                .breakingChangeRatio(0.4)
                .build();

        List<Recommendation> recommendations = service.generateRecommendations(metrics, stability);

        assertThat(recommendations).isNotNull();
    }

    @Test
    void generateDebtRecommendations_shouldReturnRecommendations() {
        TechnicalDebt debt = TechnicalDebt.builder()
                .deprecatedEndpointsCount(5)
                .missingDocumentationCount(10)
                .build();

        List<Recommendation> recommendations = service.generateDebtRecommendations(debt);

        assertThat(recommendations).isNotNull();
        assertThat(recommendations).isNotEmpty();
    }

    @Test
    void generateEvolutionReport_shouldReturnReport() {
        List<Changelog> history = createHistory(5, 2);

        ApiEvolutionReport report = service.generateEvolutionReport("Test API", history);

        assertThat(report).isNotNull();
        assertThat(report.getApiName()).isEqualTo("Test API");
        assertThat(report.getVersions()).isNotEmpty();
    }

    @Test
    void generateEvolutionReport_shouldIncludeAllComponents() {
        List<Changelog> history = createHistory(5, 2);

        ApiEvolutionReport report = service.generateEvolutionReport("Test API", history);

        assertThat(report.getOverallStability()).isNotNull();
        assertThat(report.getRiskTrend()).isNotNull();
        assertThat(report.getVelocity()).isNotNull();
    }

    @Test
    void generateDebtReport_shouldReturnReport() {
        ApiSpec spec = createApiSpec(10, 3);

        TechnicalDebtReport report = service.generateDebtReport(spec);

        assertThat(report).isNotNull();
        assertThat(report.getDebt()).isNotNull();
    }

    @Test
    void generateStabilityReport_shouldReturnReport() {
        List<Changelog> history = createHistory(5, 2);

        StabilityReport report = service.generateStabilityReport("Test API", history);

        assertThat(report).isNotNull();
        assertThat(report.getApiName()).isEqualTo("Test API");
        assertThat(report.getCurrentStability()).isNotNull();
        assertThat(report.getVersionsAnalyzed()).isEqualTo(5);
    }

    @Test
    void generateRiskTrendReport_shouldReturnReport() {
        List<Changelog> history = createHistory(5, 2);

        RiskTrendReport report = service.generateRiskTrendReport("Test API", history);

        assertThat(report).isNotNull();
        assertThat(report.getApiName()).isEqualTo("Test API");
        assertThat(report.getOverallTrend()).isNotNull();
    }

    @Test
    void generateComplianceReport_shouldReturnReport() {
        ApiSpec spec = createApiSpec(10, 2);
        spec.setVersion("1.0.0");
        List<Changelog> history = createHistory(3, 1);

        ComplianceReport report = service.generateComplianceReport(spec, history);

        assertThat(report).isNotNull();
        assertThat(report.getOverallStatus()).isNotNull();
        assertThat(report.getChecks()).isNotEmpty();
    }

    @Test
    void aggregateMetrics_shouldCombineMetrics() {
        List<ApiSpec> specs = Arrays.asList(
                createApiSpec(10, 2),
                createApiSpec(15, 3)
        );

        ApiMetrics result = service.aggregateMetrics(specs);

        assertThat(result).isNotNull();
        assertThat(result.getTotalEndpoints()).isEqualTo(25);
    }

    @Test
    void compareSpecs_shouldShowDifference() {
        ApiSpec oldSpec = createApiSpec(10, 2);
        ApiSpec newSpec = createApiSpec(15, 3);

        ApiMetrics diff = service.compareSpecs(oldSpec, newSpec);

        assertThat(diff).isNotNull();
        assertThat(diff.getTotalEndpoints()).isEqualTo(5);
    }

    private ApiSpec createApiSpec(int endpointCount, int deprecatedCount) {
        ApiSpec spec = new ApiSpec();
        spec.setName("Test API");
        spec.setVersion("1.0.0");

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

    private List<Changelog> createHistory(int count, int withBreakingChanges) {
        List<Changelog> history = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < count; i++) {
            Changelog changelog = new Changelog();
            changelog.setFromVersion("1." + i + ".0");
            changelog.setToVersion("1." + (i + 1) + ".0");
            changelog.setGeneratedAt(now.minusDays((count - i) * 10));

            List<BreakingChange> breakingChanges = new ArrayList<>();
            if (i < withBreakingChanges) {
                BreakingChange bc = new BreakingChange();
                bc.setType(ChangeType.REMOVED);
                bc.setDescription("Removed endpoint");
                breakingChanges.add(bc);
            }
            changelog.setBreakingChanges(breakingChanges);
            changelog.setChanges(new ArrayList<>());

            history.add(changelog);
        }

        return history;
    }
}
