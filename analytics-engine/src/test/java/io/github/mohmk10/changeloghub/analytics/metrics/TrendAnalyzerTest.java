package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TrendAnalyzerTest {

    private TrendAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new TrendAnalyzer();
    }

    @Test
    void calculateSlope_withIncreasingValues_shouldReturnPositiveSlope() {
        List<Integer> values = Arrays.asList(1, 2, 3, 4, 5);
        double slope = analyzer.calculateSlope(values);
        assertThat(slope).isGreaterThan(0);
    }

    @Test
    void calculateSlope_withDecreasingValues_shouldReturnNegativeSlope() {
        List<Integer> values = Arrays.asList(5, 4, 3, 2, 1);
        double slope = analyzer.calculateSlope(values);
        assertThat(slope).isLessThan(0);
    }

    @Test
    void calculateSlope_withConstantValues_shouldReturnZeroSlope() {
        List<Integer> values = Arrays.asList(5, 5, 5, 5, 5);
        double slope = analyzer.calculateSlope(values);
        assertThat(slope).isCloseTo(0.0, within(0.001));
    }

    @Test
    void calculateSlope_withEmptyList_shouldReturnZero() {
        double slope = analyzer.calculateSlope(Collections.emptyList());
        assertThat(slope).isEqualTo(0.0);
    }

    @Test
    void calculateSlope_withSingleValue_shouldReturnZero() {
        double slope = analyzer.calculateSlope(Collections.singletonList(5));
        assertThat(slope).isEqualTo(0.0);
    }

    @Test
    void calculateSlope_withNull_shouldReturnZero() {
        double slope = analyzer.calculateSlope(null);
        assertThat(slope).isEqualTo(0.0);
    }

    @Test
    void determineTrendDirection_withPositiveSlope_shouldReturnImproving() {
        TrendDirection direction = analyzer.determineTrendDirection(0.5);
        assertThat(direction).isEqualTo(TrendDirection.IMPROVING);
    }

    @Test
    void determineTrendDirection_withNegativeSlope_shouldReturnDegrading() {
        TrendDirection direction = analyzer.determineTrendDirection(-0.5);
        assertThat(direction).isEqualTo(TrendDirection.DEGRADING);
    }

    @Test
    void determineTrendDirection_withZeroSlope_shouldReturnStable() {
        TrendDirection direction = analyzer.determineTrendDirection(0.0);
        assertThat(direction).isEqualTo(TrendDirection.STABLE);
    }

    @Test
    void calculateSlope_withLinearData_shouldReturnExactSlope() {
        // y = 2x, slope should be 2
        List<Integer> values = Arrays.asList(2, 4, 6, 8, 10);
        double slope = analyzer.calculateSlope(values);
        assertThat(slope).isCloseTo(2.0, within(0.01));
    }

    @Test
    void calculateSlope_withNoisyData_shouldReturnApproximateSlope() {
        // Roughly increasing with some noise
        List<Integer> values = Arrays.asList(1, 3, 2, 4, 6, 5, 7);
        double slope = analyzer.calculateSlope(values);
        assertThat(slope).isGreaterThan(0);
    }
}
