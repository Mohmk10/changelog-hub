package io.github.mohmk10.changeloghub.analytics.aggregator;

import io.github.mohmk10.changeloghub.analytics.model.ApiMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComparisonAggregatorTest {

    private ComparisonAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new ComparisonAggregator();
    }

    @Test
    void compare_withNullMetrics_shouldReturnEmptyMetrics() {
        ApiMetrics result = aggregator.compare(null, null);
        assertThat(result).isNotNull();
    }

    @Test
    void compare_shouldCalculateDifference() {
        ApiMetrics oldMetrics = ApiMetrics.builder()
                .totalEndpoints(10)
                .totalChanges(50)
                .breakingChanges(5)
                .build();

        ApiMetrics newMetrics = ApiMetrics.builder()
                .totalEndpoints(15)
                .totalChanges(80)
                .breakingChanges(8)
                .build();

        ApiMetrics result = aggregator.compare(oldMetrics, newMetrics);

        // Result should show the difference or new values
        assertThat(result.getTotalEndpoints()).isEqualTo(5); // 15 - 10
        assertThat(result.getTotalChanges()).isEqualTo(30); // 80 - 50
        assertThat(result.getBreakingChanges()).isEqualTo(3); // 8 - 5
    }

    @Test
    void compare_withDecrease_shouldShowNegativeDifference() {
        ApiMetrics oldMetrics = ApiMetrics.builder()
                .totalEndpoints(20)
                .build();

        ApiMetrics newMetrics = ApiMetrics.builder()
                .totalEndpoints(15)
                .build();

        ApiMetrics result = aggregator.compare(oldMetrics, newMetrics);
        assertThat(result.getTotalEndpoints()).isEqualTo(-5);
    }

    @Test
    void compare_withSameValues_shouldShowZeroDifference() {
        ApiMetrics oldMetrics = ApiMetrics.builder()
                .totalEndpoints(10)
                .totalChanges(50)
                .build();

        ApiMetrics newMetrics = ApiMetrics.builder()
                .totalEndpoints(10)
                .totalChanges(50)
                .build();

        ApiMetrics result = aggregator.compare(oldMetrics, newMetrics);
        assertThat(result.getTotalEndpoints()).isZero();
        assertThat(result.getTotalChanges()).isZero();
    }

    @Test
    void compare_withOnlyOldMetrics_shouldShowNegativeDelta() {
        ApiMetrics oldMetrics = ApiMetrics.builder()
                .totalEndpoints(10)
                .build();

        ApiMetrics result = aggregator.compare(oldMetrics, null);
        assertThat(result.getTotalEndpoints()).isEqualTo(-10);
    }

    @Test
    void compare_withOnlyNewMetrics_shouldShowPositiveDelta() {
        ApiMetrics newMetrics = ApiMetrics.builder()
                .totalEndpoints(10)
                .build();

        ApiMetrics result = aggregator.compare(null, newMetrics);
        assertThat(result.getTotalEndpoints()).isEqualTo(10);
    }

    @Test
    void compare_shouldCalculateDocumentationCoverageDifference() {
        ApiMetrics oldMetrics = ApiMetrics.builder()
                .documentationCoverage(0.6)
                .build();

        ApiMetrics newMetrics = ApiMetrics.builder()
                .documentationCoverage(0.9)
                .build();

        ApiMetrics result = aggregator.compare(oldMetrics, newMetrics);
        assertThat(result.getDocumentationCoverage()).isEqualTo(0.3);
    }
}
