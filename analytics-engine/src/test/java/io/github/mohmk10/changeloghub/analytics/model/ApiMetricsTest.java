package io.github.mohmk10.changeloghub.analytics.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiMetricsTest {

    @Test
    void builder_shouldCreateApiMetrics() {
        ApiMetrics metrics = ApiMetrics.builder()
                .totalEndpoints(10)
                .totalChanges(50)
                .breakingChanges(5)
                .deprecatedEndpoints(2)
                .complexityScore(45)
                .documentationCoverage(0.85)
                .build();

        assertThat(metrics.getTotalEndpoints()).isEqualTo(10);
        assertThat(metrics.getTotalChanges()).isEqualTo(50);
        assertThat(metrics.getBreakingChanges()).isEqualTo(5);
        assertThat(metrics.getDeprecatedEndpoints()).isEqualTo(2);
        assertThat(metrics.getComplexityScore()).isEqualTo(45);
        assertThat(metrics.getDocumentationCoverage()).isEqualTo(0.85);
    }

    @Test
    void settersAndGetters_shouldWork() {
        ApiMetrics metrics = new ApiMetrics();
        metrics.setTotalEndpoints(15);
        metrics.setTotalChanges(100);
        metrics.setBreakingChanges(10);
        metrics.setDeprecatedEndpoints(3);
        metrics.setComplexityScore(60);
        metrics.setDocumentationCoverage(0.75);

        assertThat(metrics.getTotalEndpoints()).isEqualTo(15);
        assertThat(metrics.getTotalChanges()).isEqualTo(100);
        assertThat(metrics.getBreakingChanges()).isEqualTo(10);
        assertThat(metrics.getDeprecatedEndpoints()).isEqualTo(3);
        assertThat(metrics.getComplexityScore()).isEqualTo(60);
        assertThat(metrics.getDocumentationCoverage()).isEqualTo(0.75);
    }

    @Test
    void defaultValues_shouldBeZeroOrEmpty() {
        ApiMetrics metrics = new ApiMetrics();
        assertThat(metrics.getTotalEndpoints()).isZero();
        assertThat(metrics.getTotalChanges()).isZero();
        assertThat(metrics.getBreakingChanges()).isZero();
    }

    @Test
    void getBreakingChangeRatio_shouldCalculateCorrectly() {
        ApiMetrics metrics = ApiMetrics.builder()
                .totalChanges(100)
                .breakingChanges(25)
                .build();

        assertThat(metrics.getBreakingChangeRatio()).isEqualTo(0.25);
    }

    @Test
    void getBreakingChangeRatio_withZeroTotal_shouldReturnZero() {
        ApiMetrics metrics = ApiMetrics.builder()
                .totalChanges(0)
                .breakingChanges(0)
                .build();

        assertThat(metrics.getBreakingChangeRatio()).isEqualTo(0.0);
    }
}
