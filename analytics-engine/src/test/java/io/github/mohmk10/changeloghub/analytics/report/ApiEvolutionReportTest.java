package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.util.StabilityGrade;
import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiEvolutionReportTest {

    @Test
    void builder_shouldCreateReport() {
        ApiEvolutionReport report = ApiEvolutionReport.builder()
                .apiName("Test API")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .build();

        assertThat(report.getApiName()).isEqualTo("Test API");
        assertThat(report.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(report.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void builder_shouldSetAllFields() {
        StabilityScore stability = StabilityScore.builder()
                .score(85)
                .grade(StabilityGrade.B)
                .build();

        RiskTrend trend = RiskTrend.builder()
                .direction(TrendDirection.STABLE)
                .build();

        ChangeVelocity velocity = ChangeVelocity.builder()
                .changesPerWeek(5.0)
                .build();

        List<Insight> insights = Arrays.asList(
                Insight.builder().title("Test Insight").build()
        );

        List<Recommendation> recommendations = Arrays.asList(
                Recommendation.builder().title("Test Recommendation").build()
        );

        ApiEvolutionReport report = ApiEvolutionReport.builder()
                .apiName("Test API")
                .overallStability(stability)
                .riskTrend(trend)
                .velocity(velocity)
                .insights(insights)
                .recommendations(recommendations)
                .build();

        assertThat(report.getOverallStability()).isEqualTo(stability);
        assertThat(report.getRiskTrend()).isEqualTo(trend);
        assertThat(report.getVelocity()).isEqualTo(velocity);
        assertThat(report.getInsights()).hasSize(1);
        assertThat(report.getRecommendations()).hasSize(1);
    }

    @Test
    void toMarkdown_shouldGenerateValidMarkdown() {
        ApiEvolutionReport report = ApiEvolutionReport.builder()
                .apiName("Test API")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .overallStability(StabilityScore.builder()
                        .score(85)
                        .grade(StabilityGrade.B)
                        .build())
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("# API Evolution Report: Test API");
        assertThat(markdown).contains("## Summary");
        assertThat(markdown).contains("Stability Grade");
    }

    @Test
    void toMarkdown_shouldIncludeVersionHistory() {
        List<ApiEvolution.VersionSummary> versions = Arrays.asList(
                createVersionSummary("1.0.0", LocalDate.of(2024, 1, 1), 10, 2),
                createVersionSummary("2.0.0", LocalDate.of(2024, 6, 1), 15, 5)
        );

        ApiEvolutionReport report = ApiEvolutionReport.builder()
                .apiName("Test API")
                .versions(versions)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("## Version History");
        assertThat(markdown).contains("1.0.0");
        assertThat(markdown).contains("2.0.0");
    }

    @Test
    void toJson_shouldGenerateValidJson() {
        ApiEvolutionReport report = ApiEvolutionReport.builder()
                .apiName("Test API")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .build();

        String json = report.toJson();

        assertThat(json).contains("\"apiName\"");
        assertThat(json).contains("Test API");
    }

    @Test
    void toHtml_shouldGenerateValidHtml() {
        ApiEvolutionReport report = ApiEvolutionReport.builder()
                .apiName("Test API")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .build();

        String html = report.toHtml();

        assertThat(html).contains("<!DOCTYPE html>");
        assertThat(html).contains("<title>API Evolution Report: Test API</title>");
        assertThat(html).contains("</html>");
    }

    @Test
    void toHtml_shouldEscapeSpecialCharacters() {
        ApiEvolutionReport report = ApiEvolutionReport.builder()
                .apiName("Test <API> & More")
                .build();

        String html = report.toHtml();

        assertThat(html).contains("&lt;API&gt;");
        assertThat(html).contains("&amp;");
    }

    @Test
    void generatedAt_shouldBeSetAutomatically() {
        ApiEvolutionReport report = new ApiEvolutionReport();
        assertThat(report.getGeneratedAt()).isNotNull();
    }

    private ApiEvolution.VersionSummary createVersionSummary(String version, LocalDate date,
                                                              int totalChanges, int breaking) {
        ApiEvolution.VersionSummary summary = new ApiEvolution.VersionSummary();
        summary.setVersion(version);
        summary.setReleaseDate(date);
        summary.setTotalChanges(totalChanges);
        summary.setBreakingChanges(breaking);
        return summary;
    }
}
