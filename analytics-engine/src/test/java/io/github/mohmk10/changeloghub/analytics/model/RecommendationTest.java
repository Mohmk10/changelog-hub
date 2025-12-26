package io.github.mohmk10.changeloghub.analytics.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RecommendationTest {

    @Test
    void builder_shouldCreateRecommendation() {
        Recommendation rec = Recommendation.builder()
                .type(Recommendation.RecommendationType.STABILITY_IMPROVEMENT)
                .title("Reduce Breaking Changes")
                .description("High ratio of breaking changes detected")
                .action("Implement backwards compatibility policy")
                .priority(8)
                .effort(5)
                .impact(7)
                .build();

        assertThat(rec.getType()).isEqualTo(Recommendation.RecommendationType.STABILITY_IMPROVEMENT);
        assertThat(rec.getTitle()).isEqualTo("Reduce Breaking Changes");
        assertThat(rec.getDescription()).contains("breaking changes");
        assertThat(rec.getAction()).isEqualTo("Implement backwards compatibility policy");
        assertThat(rec.getPriority()).isEqualTo(8);
        assertThat(rec.getEffort()).isEqualTo(5);
        assertThat(rec.getImpact()).isEqualTo(7);
    }

    @Test
    void settersAndGetters_shouldWork() {
        Recommendation rec = new Recommendation();
        rec.setType(Recommendation.RecommendationType.DOCUMENTATION);
        rec.setTitle("Add API Documentation");
        rec.setDescription("50% of endpoints lack documentation");
        rec.setAction("Add OpenAPI descriptions");
        rec.setPriority(6);
        rec.setEffort(3);
        rec.setImpact(5);

        assertThat(rec.getType()).isEqualTo(Recommendation.RecommendationType.DOCUMENTATION);
        assertThat(rec.getTitle()).isEqualTo("Add API Documentation");
        assertThat(rec.getPriority()).isEqualTo(6);
        assertThat(rec.getEffort()).isEqualTo(3);
        assertThat(rec.getImpact()).isEqualTo(5);
    }

    @Test
    void recommendationType_shouldHaveAllTypes() {
        assertThat(Recommendation.RecommendationType.values()).containsExactlyInAnyOrder(
                Recommendation.RecommendationType.STABILITY_IMPROVEMENT,
                Recommendation.RecommendationType.DEPRECATION,
                Recommendation.RecommendationType.VERSIONING,
                Recommendation.RecommendationType.DOCUMENTATION,
                Recommendation.RecommendationType.PERFORMANCE,
                Recommendation.RecommendationType.COMPLIANCE,
                Recommendation.RecommendationType.DEBT_REDUCTION,
                Recommendation.RecommendationType.SECURITY
        );
    }

    @Test
    void getEfficiencyScore_shouldCalculateImpactOverEffort() {
        Recommendation rec = Recommendation.builder()
                .effort(2)
                .impact(8)
                .build();

        assertThat(rec.getEfficiencyScore()).isCloseTo(4.0, within(0.01));
    }

    @Test
    void getEfficiencyScore_withZeroEffort_shouldReturnImpact() {
        Recommendation rec = Recommendation.builder()
                .effort(0)
                .impact(8)
                .build();

        assertThat(rec.getEfficiencyScore()).isEqualTo(8.0);
    }

    @Test
    void isQuickWin_shouldReturnTrueForHighEfficiency() {
        Recommendation quickWin = Recommendation.builder()
                .effort(1)
                .impact(9)
                .build();

        Recommendation hardWork = Recommendation.builder()
                .effort(9)
                .impact(3)
                .build();

        assertThat(quickWin.isQuickWin()).isTrue();
        assertThat(hardWork.isQuickWin()).isFalse();
    }

    @Test
    void isHighPriority_shouldReturnTrueForHighPriority() {
        Recommendation highPriority = Recommendation.builder()
                .priority(9)
                .build();

        Recommendation lowPriority = Recommendation.builder()
                .priority(3)
                .build();

        assertThat(highPriority.isHighPriority()).isTrue();
        assertThat(lowPriority.isHighPriority()).isFalse();
    }
}
