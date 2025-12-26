package io.github.mohmk10.changeloghub.analytics.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InsightTest {

    @Test
    void builder_shouldCreateInsight() {
        Insight insight = Insight.builder()
                .type(Insight.InsightType.TREND)
                .title("Breaking Changes Increasing")
                .description("The rate of breaking changes has increased 50% over the last quarter")
                .severity(Insight.Severity.WARNING)
                .confidence(0.85)
                .build();

        assertThat(insight.getType()).isEqualTo(Insight.InsightType.TREND);
        assertThat(insight.getTitle()).isEqualTo("Breaking Changes Increasing");
        assertThat(insight.getDescription()).contains("50%");
        assertThat(insight.getSeverity()).isEqualTo(Insight.Severity.WARNING);
        assertThat(insight.getConfidence()).isEqualTo(0.85);
    }

    @Test
    void settersAndGetters_shouldWork() {
        Insight insight = new Insight();
        insight.setType(Insight.InsightType.PATTERN);
        insight.setTitle("Weekly Release Pattern");
        insight.setDescription("Releases typically occur on Tuesdays");
        insight.setSeverity(Insight.Severity.INFO);
        insight.setConfidence(0.9);

        assertThat(insight.getType()).isEqualTo(Insight.InsightType.PATTERN);
        assertThat(insight.getTitle()).isEqualTo("Weekly Release Pattern");
        assertThat(insight.getDescription()).contains("Tuesdays");
        assertThat(insight.getSeverity()).isEqualTo(Insight.Severity.INFO);
        assertThat(insight.getConfidence()).isEqualTo(0.9);
    }

    @Test
    void insightType_shouldHaveAllTypes() {
        // Check that at least the main types exist
        assertThat(Insight.InsightType.values()).contains(
                Insight.InsightType.TREND,
                Insight.InsightType.PATTERN,
                Insight.InsightType.ANOMALY,
                Insight.InsightType.PREDICTION,
                Insight.InsightType.RECOMMENDATION
        );
    }

    @Test
    void severity_shouldHaveAllLevels() {
        assertThat(Insight.Severity.values()).containsExactlyInAnyOrder(
                Insight.Severity.INFO,
                Insight.Severity.WARNING,
                Insight.Severity.CRITICAL
        );
    }

    @Test
    void isHighConfidence_shouldReturnTrueForHighValues() {
        Insight highConfidence = Insight.builder()
                .confidence(0.9)
                .build();

        Insight lowConfidence = Insight.builder()
                .confidence(0.5)
                .build();

        assertThat(highConfidence.isHighConfidence()).isTrue();
        assertThat(lowConfidence.isHighConfidence()).isFalse();
    }

    @Test
    void isCritical_shouldReturnTrueForCriticalSeverity() {
        Insight critical = Insight.builder()
                .severity(Insight.Severity.CRITICAL)
                .build();

        Insight warning = Insight.builder()
                .severity(Insight.Severity.WARNING)
                .build();

        assertThat(critical.isCritical()).isTrue();
        assertThat(warning.isCritical()).isFalse();
    }
}
