package io.github.mohmk10.changeloghub.analytics.insight;

import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.util.StabilityGrade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationEngineTest {

    private RecommendationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RecommendationEngine();
    }

    @Test
    void generateRecommendations_withNullInputs_shouldReturnEmptyList() {
        List<Recommendation> recommendations = engine.generateRecommendations(null, null);
        assertThat(recommendations).isEmpty();
    }

    @Test
    void generateRecommendations_withPoorStability_shouldGenerateRecommendations() {
        StabilityScore stability = StabilityScore.builder()
                .score(50)
                .grade(StabilityGrade.F)
                .breakingChangeRatio(0.8)
                .build();

        List<Recommendation> recommendations = engine.generateRecommendations(null, stability);
        assertThat(recommendations).isNotEmpty();
    }

    @Test
    void generateRecommendations_withHighBreakingRatio_shouldRecommendReduction() {
        StabilityScore stability = StabilityScore.builder()
                .score(70)
                .grade(StabilityGrade.C)
                .breakingChangeRatio(0.5)
                .build();

        List<Recommendation> recommendations = engine.generateRecommendations(null, stability);
        boolean hasStabilityRec = recommendations.stream()
                .anyMatch(r -> r.getType() == Recommendation.RecommendationType.STABILITY_IMPROVEMENT);
        assertThat(hasStabilityRec).isTrue();
    }

    @Test
    void generateRecommendations_withLowDocumentation_shouldRecommendImprovement() {
        ApiMetrics metrics = ApiMetrics.builder()
                .documentationCoverage(0.4)
                .complexityScore(30)
                .build();

        List<Recommendation> recommendations = engine.generateRecommendations(metrics, null);
        boolean hasDocRec = recommendations.stream()
                .anyMatch(r -> r.getType() == Recommendation.RecommendationType.DOCUMENTATION);
        assertThat(hasDocRec).isTrue();
    }

    @Test
    void generateRecommendations_withHighComplexity_shouldRecommendReduction() {
        ApiMetrics metrics = ApiMetrics.builder()
                .documentationCoverage(0.9)
                .complexityScore(85)
                .build();

        List<Recommendation> recommendations = engine.generateRecommendations(metrics, null);
        boolean hasComplexityRec = recommendations.stream()
                .anyMatch(r -> r.getType() == Recommendation.RecommendationType.PERFORMANCE);
        assertThat(hasComplexityRec).isTrue();
    }

    @Test
    void recommendForPoorStability_withGradeF_shouldGenerateMultipleRecommendations() {
        StabilityScore stability = StabilityScore.builder()
                .score(40)
                .grade(StabilityGrade.F)
                .build();

        List<Recommendation> recommendations = engine.recommendForPoorStability(stability);
        assertThat(recommendations).hasSizeGreaterThanOrEqualTo(2);

        boolean hasChangeFreezeRec = recommendations.stream()
                .anyMatch(r -> r.getTitle().contains("Change Freeze"));
        boolean hasVersioningRec = recommendations.stream()
                .anyMatch(r -> r.getType() == Recommendation.RecommendationType.VERSIONING);

        assertThat(hasChangeFreezeRec).isTrue();
        assertThat(hasVersioningRec).isTrue();
    }

    @Test
    void recommendForPoorStability_withGradeD_shouldIncludeDeprecationRec() {
        StabilityScore stability = StabilityScore.builder()
                .score(65)
                .grade(StabilityGrade.D)
                .build();

        List<Recommendation> recommendations = engine.recommendForPoorStability(stability);
        boolean hasDeprecationRec = recommendations.stream()
                .anyMatch(r -> r.getType() == Recommendation.RecommendationType.DEPRECATION);
        assertThat(hasDeprecationRec).isTrue();
    }

    @Test
    void recommendForHighDebt_withDeprecatedEndpoints_shouldRecommendRemoval() {
        TechnicalDebt debt = TechnicalDebt.builder()
                .deprecatedEndpointsCount(5)
                .build();

        List<Recommendation> recommendations = engine.recommendForHighDebt(debt);
        boolean hasDebtRec = recommendations.stream()
                .anyMatch(r -> r.getType() == Recommendation.RecommendationType.DEBT_REDUCTION);
        assertThat(hasDebtRec).isTrue();
    }

    @Test
    void recommendForHighDebt_withMissingDocumentation_shouldRecommendDocumentation() {
        TechnicalDebt debt = TechnicalDebt.builder()
                .missingDocumentationCount(10)
                .build();

        List<Recommendation> recommendations = engine.recommendForHighDebt(debt);
        boolean hasDocRec = recommendations.stream()
                .anyMatch(r -> r.getType() == Recommendation.RecommendationType.DOCUMENTATION);
        assertThat(hasDocRec).isTrue();
    }

    @Test
    void recommendForHighDebt_withNamingInconsistencies_shouldRecommendFix() {
        TechnicalDebt debt = TechnicalDebt.builder()
                .inconsistentNamingCount(8)
                .build();

        List<Recommendation> recommendations = engine.recommendForHighDebt(debt);
        boolean hasComplianceRec = recommendations.stream()
                .anyMatch(r -> r.getType() == Recommendation.RecommendationType.COMPLIANCE);
        assertThat(hasComplianceRec).isTrue();
    }

    @Test
    void generateRecommendations_shouldBeSortedByEfficiency() {
        ApiMetrics metrics = ApiMetrics.builder()
                .documentationCoverage(0.4)
                .complexityScore(85)
                .build();

        StabilityScore stability = StabilityScore.builder()
                .score(50)
                .grade(StabilityGrade.F)
                .breakingChangeRatio(0.6)
                .build();

        List<Recommendation> recommendations = engine.generateRecommendations(metrics, stability);

        // Verify sorted by efficiency (descending)
        for (int i = 0; i < recommendations.size() - 1; i++) {
            assertThat(recommendations.get(i).getEfficiencyScore())
                    .isGreaterThanOrEqualTo(recommendations.get(i + 1).getEfficiencyScore());
        }
    }

    @Test
    void recommendations_shouldHavePriorityAndEffort() {
        StabilityScore stability = StabilityScore.builder()
                .score(40)
                .grade(StabilityGrade.F)
                .build();

        List<Recommendation> recommendations = engine.recommendForPoorStability(stability);

        for (Recommendation rec : recommendations) {
            assertThat(rec.getPriority()).isBetween(1, 10);
            assertThat(rec.getEffort()).isBetween(1, 10);
            assertThat(rec.getImpact()).isBetween(1, 10);
        }
    }
}
