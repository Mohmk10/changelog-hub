package io.github.mohmk10.changeloghub.analytics.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AnalyticsConstantsTest {

    @Test
    void stabilityWeights_shouldSumToOne() {
        double sum = AnalyticsConstants.WEIGHT_BREAKING_CHANGE_RATIO +
                AnalyticsConstants.WEIGHT_TIME_BETWEEN_BREAKING +
                AnalyticsConstants.WEIGHT_DEPRECATION_MANAGEMENT +
                AnalyticsConstants.WEIGHT_SEMVER_COMPLIANCE;

        assertThat(sum).isCloseTo(1.0, within(0.001));
    }

    @Test
    void breakingChangeRatioWeight_shouldBe40Percent() {
        assertThat(AnalyticsConstants.WEIGHT_BREAKING_CHANGE_RATIO).isEqualTo(0.40);
    }

    @Test
    void timeBetweenBreakingWeight_shouldBe30Percent() {
        assertThat(AnalyticsConstants.WEIGHT_TIME_BETWEEN_BREAKING).isEqualTo(0.30);
    }

    @Test
    void deprecationWeight_shouldBe15Percent() {
        assertThat(AnalyticsConstants.WEIGHT_DEPRECATION_MANAGEMENT).isEqualTo(0.15);
    }

    @Test
    void semverWeight_shouldBe15Percent() {
        assertThat(AnalyticsConstants.WEIGHT_SEMVER_COMPLIANCE).isEqualTo(0.15);
    }

    @Test
    void minDaysBetweenBreaking_shouldBePositive() {
        assertThat(AnalyticsConstants.MIN_DAYS_BETWEEN_BREAKING_IDEAL).isGreaterThan(0);
    }

    @Test
    void maxRecommendationsPerReport_shouldBeReasonable() {
        assertThat(AnalyticsConstants.MAX_RECOMMENDATIONS_PER_REPORT).isGreaterThan(0);
        assertThat(AnalyticsConstants.MAX_RECOMMENDATIONS_PER_REPORT).isLessThanOrEqualTo(20);
    }

    @Test
    void lowDocumentationThreshold_shouldBeLessThanOne() {
        assertThat(AnalyticsConstants.LOW_DOCUMENTATION_THRESHOLD).isGreaterThan(0);
        assertThat(AnalyticsConstants.LOW_DOCUMENTATION_THRESHOLD).isLessThan(1.0);
    }

    @Test
    void highComplexityThreshold_shouldBePositive() {
        assertThat(AnalyticsConstants.HIGH_COMPLEXITY_THRESHOLD).isGreaterThan(0);
    }
}
