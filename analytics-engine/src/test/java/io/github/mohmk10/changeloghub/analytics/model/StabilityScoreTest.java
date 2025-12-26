package io.github.mohmk10.changeloghub.analytics.model;

import io.github.mohmk10.changeloghub.analytics.util.StabilityGrade;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StabilityScoreTest {

    @Test
    void builder_shouldCreateStabilityScore() {
        StabilityScore score = StabilityScore.builder()
                .score(85)
                .grade(StabilityGrade.B)
                .breakingChangeRatio(0.15)
                .timeBetweenBreakingChangesScore(45)
                .deprecationManagementScore(0.9)
                .semverComplianceScore(1.0)
                .build();

        assertThat(score.getScore()).isEqualTo(85);
        assertThat(score.getGrade()).isEqualTo(StabilityGrade.B);
        assertThat(score.getBreakingChangeRatio()).isEqualTo(0.15);
        assertThat(score.getAvgDaysBetweenBreaking()).isEqualTo(45);
        assertThat(score.getDeprecationManagementScore()).isEqualTo(0.9);
        assertThat(score.getSemverComplianceScore()).isEqualTo(1.0);
    }

    @Test
    void settersAndGetters_shouldWork() {
        StabilityScore score = new StabilityScore();
        score.setScore(75);
        score.setGrade(StabilityGrade.C);
        score.setBreakingChangeRatio(0.25);

        assertThat(score.getScore()).isEqualTo(75);
        assertThat(score.getGrade()).isEqualTo(StabilityGrade.C);
        assertThat(score.getBreakingChangeRatio()).isEqualTo(0.25);
    }

    @Test
    void stabilityFactor_shouldBeCreatedCorrectly() {
        StabilityScore.StabilityFactor factor = new StabilityScore.StabilityFactor(
                "Breaking Change Ratio", 0.40, 85.0, "Low ratio of breaking changes"
        );

        assertThat(factor.getName()).isEqualTo("Breaking Change Ratio");
        assertThat(factor.getScore()).isEqualTo(85.0);
        assertThat(factor.getWeight()).isEqualTo(0.40);
        assertThat(factor.getDescription()).isEqualTo("Low ratio of breaking changes");
    }

    @Test
    void factors_shouldBeSetAndRetrieved() {
        List<StabilityScore.StabilityFactor> factors = Arrays.asList(
                new StabilityScore.StabilityFactor("Factor1", 0.5, 90.0, "Impact1"),
                new StabilityScore.StabilityFactor("Factor2", 0.5, 80.0, "Impact2")
        );

        StabilityScore score = StabilityScore.builder()
                .score(85)
                .factors(factors)
                .build();

        assertThat(score.getFactors()).hasSize(2);
        assertThat(score.getFactors().get(0).getName()).isEqualTo("Factor1");
    }

    @Test
    void isPoor_shouldReturnTrueForLowScores() {
        StabilityScore poorScore = StabilityScore.builder()
                .score(50)
                .grade(StabilityGrade.F)
                .build();

        StabilityScore goodScore = StabilityScore.builder()
                .score(85)
                .grade(StabilityGrade.B)
                .build();

        assertThat(poorScore.isPoor()).isTrue();
        assertThat(goodScore.isPoor()).isFalse();
    }

    @Test
    void isExcellent_shouldReturnTrueForHighScores() {
        StabilityScore excellentScore = StabilityScore.builder()
                .score(95)
                .grade(StabilityGrade.A)
                .build();

        StabilityScore averageScore = StabilityScore.builder()
                .score(75)
                .grade(StabilityGrade.C)
                .build();

        assertThat(excellentScore.isExcellent()).isTrue();
        assertThat(averageScore.isExcellent()).isFalse();
    }
}
