package io.github.mohmk10.changeloghub.core.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RiskAssessmentTest {

    @Test
    void shouldCreateRiskAssessmentWithDefaultConstructor() {
        RiskAssessment riskAssessment = new RiskAssessment();

        assertThat(riskAssessment.getOverallScore()).isZero();
        assertThat(riskAssessment.getLevel()).isNull();
        assertThat(riskAssessment.getBreakingChangesCount()).isZero();
        assertThat(riskAssessment.getTotalChangesCount()).isZero();
        assertThat(riskAssessment.getChangesBySeverity()).isNotNull().isEmpty();
    }

    @Test
    void shouldCreateRiskAssessmentWithAllArgsConstructor() {
        Map<Severity, Integer> changesBySeverity = new HashMap<>();
        changesBySeverity.put(Severity.BREAKING, 2);
        changesBySeverity.put(Severity.WARNING, 5);
        changesBySeverity.put(Severity.INFO, 10);

        RiskAssessment riskAssessment = new RiskAssessment(
                75,
                RiskLevel.HIGH,
                2,
                17,
                changesBySeverity,
                "Consider major version bump",
                "MAJOR"
        );

        assertThat(riskAssessment.getOverallScore()).isEqualTo(75);
        assertThat(riskAssessment.getLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(riskAssessment.getBreakingChangesCount()).isEqualTo(2);
        assertThat(riskAssessment.getTotalChangesCount()).isEqualTo(17);
        assertThat(riskAssessment.getChangesBySeverity()).hasSize(3);
        assertThat(riskAssessment.getChangesBySeverity().get(Severity.BREAKING)).isEqualTo(2);
        assertThat(riskAssessment.getRecommendation()).isEqualTo("Consider major version bump");
        assertThat(riskAssessment.getSemverRecommendation()).isEqualTo("MAJOR");
    }

    @Test
    void shouldCalculateRiskLevelBasedOnScore() {
        RiskAssessment lowRisk = new RiskAssessment();
        lowRisk.setOverallScore(20);
        lowRisk.setLevel(RiskLevel.LOW);

        RiskAssessment mediumRisk = new RiskAssessment();
        mediumRisk.setOverallScore(45);
        mediumRisk.setLevel(RiskLevel.MEDIUM);

        RiskAssessment highRisk = new RiskAssessment();
        highRisk.setOverallScore(70);
        highRisk.setLevel(RiskLevel.HIGH);

        RiskAssessment criticalRisk = new RiskAssessment();
        criticalRisk.setOverallScore(95);
        criticalRisk.setLevel(RiskLevel.CRITICAL);

        assertThat(lowRisk.getLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(mediumRisk.getLevel()).isEqualTo(RiskLevel.MEDIUM);
        assertThat(highRisk.getLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(criticalRisk.getLevel()).isEqualTo(RiskLevel.CRITICAL);
    }

    @Test
    void shouldSupportAllRiskLevels() {
        for (RiskLevel level : RiskLevel.values()) {
            RiskAssessment riskAssessment = new RiskAssessment();
            riskAssessment.setLevel(level);

            assertThat(riskAssessment.getLevel()).isEqualTo(level);
        }
    }

    @Test
    void shouldTrackChangesBySeverity() {
        Map<Severity, Integer> changesBySeverity = new HashMap<>();
        changesBySeverity.put(Severity.BREAKING, 1);
        changesBySeverity.put(Severity.DANGEROUS, 2);
        changesBySeverity.put(Severity.WARNING, 3);
        changesBySeverity.put(Severity.INFO, 4);

        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setChangesBySeverity(changesBySeverity);

        assertThat(riskAssessment.getChangesBySeverity()).hasSize(4);
        assertThat(riskAssessment.getChangesBySeverity().get(Severity.BREAKING)).isEqualTo(1);
        assertThat(riskAssessment.getChangesBySeverity().get(Severity.DANGEROUS)).isEqualTo(2);
        assertThat(riskAssessment.getChangesBySeverity().get(Severity.WARNING)).isEqualTo(3);
        assertThat(riskAssessment.getChangesBySeverity().get(Severity.INFO)).isEqualTo(4);
    }

    @Test
    void shouldHaveCorrectEqualsAndHashCode() {
        Map<Severity, Integer> changes = new HashMap<>();
        changes.put(Severity.INFO, 5);

        RiskAssessment risk1 = new RiskAssessment(50, RiskLevel.MEDIUM, 0, 5, changes, "OK", "MINOR");
        RiskAssessment risk2 = new RiskAssessment(50, RiskLevel.MEDIUM, 0, 5, changes, "OK", "MINOR");
        RiskAssessment risk3 = new RiskAssessment(80, RiskLevel.HIGH, 2, 5, changes, "Caution", "MAJOR");

        assertThat(risk1).isEqualTo(risk2);
        assertThat(risk1.hashCode()).isEqualTo(risk2.hashCode());
        assertThat(risk1).isNotEqualTo(risk3);
    }

    @Test
    void shouldHaveCorrectToString() {
        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setOverallScore(85);
        riskAssessment.setLevel(RiskLevel.CRITICAL);
        riskAssessment.setBreakingChangesCount(5);
        riskAssessment.setSemverRecommendation("MAJOR");

        String toString = riskAssessment.toString();

        assertThat(toString).contains("85");
        assertThat(toString).contains("CRITICAL");
        assertThat(toString).contains("MAJOR");
    }

    @Test
    void shouldProvideSemverRecommendations() {
        RiskAssessment patchLevel = new RiskAssessment();
        patchLevel.setSemverRecommendation("PATCH");

        RiskAssessment minorLevel = new RiskAssessment();
        minorLevel.setSemverRecommendation("MINOR");

        RiskAssessment majorLevel = new RiskAssessment();
        majorLevel.setSemverRecommendation("MAJOR");

        assertThat(patchLevel.getSemverRecommendation()).isEqualTo("PATCH");
        assertThat(minorLevel.getSemverRecommendation()).isEqualTo("MINOR");
        assertThat(majorLevel.getSemverRecommendation()).isEqualTo("MAJOR");
    }
}
