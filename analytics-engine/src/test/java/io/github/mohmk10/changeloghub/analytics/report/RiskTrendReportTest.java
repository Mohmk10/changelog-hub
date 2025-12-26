package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskTrendReportTest {

    @Test
    void builder_shouldCreateReport() {
        RiskTrendReport report = RiskTrendReport.builder()
                .apiName("Test API")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .build();

        assertThat(report.getApiName()).isEqualTo("Test API");
        assertThat(report.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(report.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void builder_shouldSetOverallTrend() {
        RiskTrend trend = RiskTrend.builder()
                .direction(TrendDirection.IMPROVING)
                .currentRiskScore(30)
                .previousRiskScore(50)
                .build();

        RiskTrendReport report = RiskTrendReport.builder()
                .apiName("Test API")
                .overallTrend(trend)
                .build();

        assertThat(report.getOverallTrend()).isEqualTo(trend);
    }

    @Test
    void builder_shouldSetDataPoints() {
        List<RiskTrendReport.RiskDataPoint> dataPoints = Arrays.asList(
                new RiskTrendReport.RiskDataPoint(LocalDate.of(2024, 1, 1), 50, 3, "1.0.0"),
                new RiskTrendReport.RiskDataPoint(LocalDate.of(2024, 6, 1), 30, 1, "2.0.0")
        );

        RiskTrendReport report = RiskTrendReport.builder()
                .apiName("Test API")
                .dataPoints(dataPoints)
                .build();

        assertThat(report.getDataPoints()).hasSize(2);
    }

    @Test
    void riskDataPoint_shouldStoreAllProperties() {
        RiskTrendReport.RiskDataPoint point = new RiskTrendReport.RiskDataPoint();
        point.setDate(LocalDate.of(2024, 6, 15));
        point.setRiskScore(45);
        point.setBreakingChanges(3);
        point.setVersion("1.5.0");

        assertThat(point.getDate()).isEqualTo(LocalDate.of(2024, 6, 15));
        assertThat(point.getRiskScore()).isEqualTo(45);
        assertThat(point.getBreakingChanges()).isEqualTo(3);
        assertThat(point.getVersion()).isEqualTo("1.5.0");
    }

    @Test
    void riskDataPoint_constructorShouldWork() {
        RiskTrendReport.RiskDataPoint point = new RiskTrendReport.RiskDataPoint(
                LocalDate.of(2024, 6, 15), 45, 3, "1.5.0"
        );

        assertThat(point.getDate()).isEqualTo(LocalDate.of(2024, 6, 15));
        assertThat(point.getRiskScore()).isEqualTo(45);
        assertThat(point.getBreakingChanges()).isEqualTo(3);
        assertThat(point.getVersion()).isEqualTo("1.5.0");
    }

    @Test
    void builder_shouldSetPeriodRisks() {
        RiskTrend.PeriodRisk periodRisk = new RiskTrend.PeriodRisk();
        periodRisk.setPeriodLabel("Q1 2024");
        periodRisk.setRiskScore(40);
        periodRisk.setBreakingChangeCount(5);

        List<RiskTrend.PeriodRisk> periodRisks = Arrays.asList(periodRisk);

        RiskTrendReport report = RiskTrendReport.builder()
                .apiName("Test API")
                .periodRisks(periodRisks)
                .build();

        assertThat(report.getPeriodRisks()).hasSize(1);
    }

    @Test
    void toMarkdown_shouldGenerateValidMarkdown() {
        RiskTrend trend = RiskTrend.builder()
                .direction(TrendDirection.IMPROVING)
                .currentRiskScore(30)
                .previousRiskScore(50)
                .build();

        RiskTrendReport report = RiskTrendReport.builder()
                .apiName("Test API")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .overallTrend(trend)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("# Risk Trend Report: Test API");
        assertThat(markdown).contains("## Summary");
        assertThat(markdown).contains("Overall Trend");
    }

    @Test
    void toMarkdown_shouldIncludeDataPoints() {
        List<RiskTrendReport.RiskDataPoint> dataPoints = Arrays.asList(
                new RiskTrendReport.RiskDataPoint(LocalDate.of(2024, 1, 1), 50, 3, "1.0.0")
        );

        RiskTrendReport report = RiskTrendReport.builder()
                .apiName("Test API")
                .dataPoints(dataPoints)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("## Risk Timeline");
        assertThat(markdown).contains("1.0.0");
    }

    @Test
    void toMarkdown_shouldIncludePeriodAnalysis() {
        RiskTrend.PeriodRisk periodRisk = new RiskTrend.PeriodRisk();
        periodRisk.setPeriodLabel("Q1 2024");
        periodRisk.setRiskScore(40);
        periodRisk.setBreakingChangeCount(5);

        RiskTrendReport report = RiskTrendReport.builder()
                .apiName("Test API")
                .periodRisks(Arrays.asList(periodRisk))
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("## Period Analysis");
        assertThat(markdown).contains("Q1 2024");
    }

    @Test
    void toMarkdown_shouldIncludeInsightsAndRecommendations() {
        List<Insight> insights = Arrays.asList(
                Insight.builder().title("Test Insight").description("Description").build()
        );
        List<Recommendation> recommendations = Arrays.asList(
                Recommendation.builder().title("Test Rec").action("Action").build()
        );

        RiskTrendReport report = RiskTrendReport.builder()
                .apiName("Test API")
                .insights(insights)
                .recommendations(recommendations)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("## Insights");
        assertThat(markdown).contains("Test Insight");
        assertThat(markdown).contains("## Recommendations");
        assertThat(markdown).contains("Test Rec");
    }

    @Test
    void generatedAt_shouldBeSetAutomatically() {
        RiskTrendReport report = new RiskTrendReport();
        assertThat(report.getGeneratedAt()).isNotNull();
    }
}
