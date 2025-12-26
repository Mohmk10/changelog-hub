package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.analytics.model.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TechnicalDebtReportTest {

    @Test
    void builder_shouldCreateReport() {
        TechnicalDebtReport report = TechnicalDebtReport.builder()
                .apiName("Test API")
                .priorityScore(75)
                .build();

        assertThat(report.getApiName()).isEqualTo("Test API");
        assertThat(report.getPriorityScore()).isEqualTo(75);
    }

    @Test
    void builder_shouldSetDebt() {
        TechnicalDebt debt = TechnicalDebt.builder()
                .debtScore(45)
                .deprecatedEndpointsCount(3)
                .missingDocumentationCount(5)
                .build();

        TechnicalDebtReport report = TechnicalDebtReport.builder()
                .apiName("Test API")
                .debt(debt)
                .build();

        assertThat(report.getDebt()).isEqualTo(debt);
        assertThat(report.getDebt().getDebtScore()).isEqualTo(45);
    }

    @Test
    void builder_shouldSetItems() {
        List<TechnicalDebt.DebtItem> items = Arrays.asList(
                new TechnicalDebt.DebtItem(TechnicalDebt.DebtItem.DebtType.DEPRECATED_ENDPOINT,
                        "/api/old", "Deprecated since v2", 7),
                new TechnicalDebt.DebtItem(TechnicalDebt.DebtItem.DebtType.MISSING_DOCUMENTATION,
                        "/api/users", "No description", 5)
        );

        TechnicalDebtReport report = TechnicalDebtReport.builder()
                .apiName("Test API")
                .items(items)
                .build();

        assertThat(report.getItems()).hasSize(2);
    }

    @Test
    void builder_shouldSetRecommendations() {
        List<Recommendation> recommendations = Arrays.asList(
                Recommendation.builder()
                        .title("Remove Deprecated Endpoints")
                        .action("Plan removal in next major version")
                        .build()
        );

        TechnicalDebtReport report = TechnicalDebtReport.builder()
                .apiName("Test API")
                .recommendations(recommendations)
                .build();

        assertThat(report.getRecommendations()).hasSize(1);
    }

    @Test
    void toMarkdown_shouldGenerateValidMarkdown() {
        TechnicalDebt debt = TechnicalDebt.builder()
                .debtScore(45)
                .deprecatedEndpointsCount(3)
                .missingDocumentationCount(5)
                .build();

        TechnicalDebtReport report = TechnicalDebtReport.builder()
                .apiName("Test API")
                .debt(debt)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("# Technical Debt Report: Test API");
        assertThat(markdown).contains("## Summary");
        assertThat(markdown).contains("Debt Score");
    }

    @Test
    void toMarkdown_shouldIncludeDebtItems() {
        List<TechnicalDebt.DebtItem> items = Arrays.asList(
                new TechnicalDebt.DebtItem(TechnicalDebt.DebtItem.DebtType.DEPRECATED_ENDPOINT,
                        "/api/old", "Deprecated since v2", 7)
        );

        TechnicalDebtReport report = TechnicalDebtReport.builder()
                .apiName("Test API")
                .items(items)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("## Debt Items");
        assertThat(markdown).contains("DEPRECATED_ENDPOINT");
    }

    @Test
    void toMarkdown_shouldIncludeRecommendations() {
        List<Recommendation> recommendations = Arrays.asList(
                Recommendation.builder()
                        .title("Remove Deprecated")
                        .action("Plan removal")
                        .build()
        );

        TechnicalDebtReport report = TechnicalDebtReport.builder()
                .apiName("Test API")
                .recommendations(recommendations)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("## Recommendations");
        assertThat(markdown).contains("Remove Deprecated");
    }

    @Test
    void settersAndGetters_shouldWork() {
        TechnicalDebtReport report = new TechnicalDebtReport();
        report.setApiName("Test API");
        report.setPriorityScore(80);

        assertThat(report.getApiName()).isEqualTo("Test API");
        assertThat(report.getPriorityScore()).isEqualTo(80);
    }

    @Test
    void generatedAt_shouldBeSetAutomatically() {
        TechnicalDebtReport report = new TechnicalDebtReport();
        assertThat(report.getGeneratedAt()).isNotNull();
    }
}
