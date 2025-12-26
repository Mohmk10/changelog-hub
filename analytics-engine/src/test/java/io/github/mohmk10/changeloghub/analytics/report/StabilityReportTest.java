package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.util.StabilityGrade;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StabilityReportTest {

    @Test
    void builder_shouldCreateReport() {
        StabilityReport report = StabilityReport.builder()
                .apiName("Test API")
                .versionsAnalyzed(10)
                .breakingChangesTotal(5)
                .build();

        assertThat(report.getApiName()).isEqualTo("Test API");
        assertThat(report.getVersionsAnalyzed()).isEqualTo(10);
        assertThat(report.getBreakingChangesTotal()).isEqualTo(5);
    }

    @Test
    void builder_shouldSetStabilityScores() {
        StabilityScore current = StabilityScore.builder()
                .score(85)
                .grade(StabilityGrade.B)
                .build();

        StabilityScore previous = StabilityScore.builder()
                .score(75)
                .grade(StabilityGrade.C)
                .build();

        StabilityReport report = StabilityReport.builder()
                .apiName("Test API")
                .currentStability(current)
                .previousStability(previous)
                .build();

        assertThat(report.getCurrentStability()).isEqualTo(current);
        assertThat(report.getPreviousStability()).isEqualTo(previous);
    }

    @Test
    void getStabilityTrend_withImproving_shouldReturnImproving() {
        StabilityReport report = StabilityReport.builder()
                .currentStability(StabilityScore.builder().score(85).build())
                .previousStability(StabilityScore.builder().score(70).build())
                .build();

        assertThat(report.getStabilityTrend()).isEqualTo("Improving");
    }

    @Test
    void getStabilityTrend_withDegrading_shouldReturnDegrading() {
        StabilityReport report = StabilityReport.builder()
                .currentStability(StabilityScore.builder().score(60).build())
                .previousStability(StabilityScore.builder().score(80).build())
                .build();

        assertThat(report.getStabilityTrend()).isEqualTo("Degrading");
    }

    @Test
    void getStabilityTrend_withStable_shouldReturnStable() {
        StabilityReport report = StabilityReport.builder()
                .currentStability(StabilityScore.builder().score(80).build())
                .previousStability(StabilityScore.builder().score(82).build())
                .build();

        assertThat(report.getStabilityTrend()).isEqualTo("Stable");
    }

    @Test
    void getStabilityTrend_withNoPrevious_shouldReturnNA() {
        StabilityReport report = StabilityReport.builder()
                .currentStability(StabilityScore.builder().score(80).build())
                .build();

        assertThat(report.getStabilityTrend()).isEqualTo("N/A");
    }

    @Test
    void builder_shouldSetFactors() {
        List<StabilityScore.StabilityFactor> factors = Arrays.asList(
                new StabilityScore.StabilityFactor("Factor1", 90.0, 0.4, "Impact1"),
                new StabilityScore.StabilityFactor("Factor2", 80.0, 0.6, "Impact2")
        );

        StabilityReport report = StabilityReport.builder()
                .apiName("Test API")
                .factors(factors)
                .build();

        assertThat(report.getFactors()).hasSize(2);
    }

    @Test
    void toMarkdown_shouldGenerateValidMarkdown() {
        StabilityReport report = StabilityReport.builder()
                .apiName("Test API")
                .currentStability(StabilityScore.builder()
                        .score(85)
                        .grade(StabilityGrade.B)
                        .build())
                .versionsAnalyzed(10)
                .breakingChangesTotal(5)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("# Stability Report: Test API");
        assertThat(markdown).contains("## Summary");
        assertThat(markdown).contains("Current Grade");
        assertThat(markdown).contains("Versions Analyzed");
    }

    @Test
    void toMarkdown_shouldIncludeFactors() {
        List<StabilityScore.StabilityFactor> factors = Arrays.asList(
                new StabilityScore.StabilityFactor("Breaking Ratio", 90.0, 0.4, "Low ratio")
        );

        StabilityReport report = StabilityReport.builder()
                .apiName("Test API")
                .factors(factors)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("## Stability Factors");
        assertThat(markdown).contains("Breaking Ratio");
    }

    @Test
    void toMarkdown_shouldIncludeRecommendations() {
        List<Recommendation> recommendations = Arrays.asList(
                Recommendation.builder()
                        .title("Improve Documentation")
                        .action("Add descriptions to all endpoints")
                        .build()
        );

        StabilityReport report = StabilityReport.builder()
                .apiName("Test API")
                .recommendations(recommendations)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("## Recommendations");
        assertThat(markdown).contains("Improve Documentation");
    }

    @Test
    void generatedAt_shouldBeSetAutomatically() {
        StabilityReport report = new StabilityReport();
        assertThat(report.getGeneratedAt()).isNotNull();
    }
}
