package io.github.mohmk10.changeloghub.analytics.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrendDirectionTest {

    @Test
    void improving_shouldHaveCorrectProperties() {
        TrendDirection direction = TrendDirection.IMPROVING;
        assertThat(direction.getLabel()).isEqualTo("Improving");
        assertThat(direction.getDescription()).contains("better");
        assertThat(direction.getDirection()).isEqualTo(1);
    }

    @Test
    void stable_shouldHaveCorrectProperties() {
        TrendDirection direction = TrendDirection.STABLE;
        assertThat(direction.getLabel()).isEqualTo("Stable");
        assertThat(direction.getDescription()).contains("consistent");
        assertThat(direction.getDirection()).isEqualTo(0);
    }

    @Test
    void degrading_shouldHaveCorrectProperties() {
        TrendDirection direction = TrendDirection.DEGRADING;
        assertThat(direction.getLabel()).isEqualTo("Degrading");
        assertThat(direction.getDescription()).contains("worse");
        assertThat(direction.getDirection()).isEqualTo(-1);
    }

    @Test
    void fromSlope_shouldReturnCorrectDirection() {
        assertThat(TrendDirection.fromSlope(0.5, 0.1)).isEqualTo(TrendDirection.IMPROVING);
        assertThat(TrendDirection.fromSlope(-0.5, 0.1)).isEqualTo(TrendDirection.DEGRADING);
        assertThat(TrendDirection.fromSlope(0.0, 0.1)).isEqualTo(TrendDirection.STABLE);
    }

    @Test
    void fromChange_shouldReturnCorrectDirection() {
        assertThat(TrendDirection.fromChange(10.0)).isEqualTo(TrendDirection.IMPROVING);
        assertThat(TrendDirection.fromChange(-10.0)).isEqualTo(TrendDirection.DEGRADING);
        assertThat(TrendDirection.fromChange(2.0)).isEqualTo(TrendDirection.STABLE);
    }

    @Test
    void isPositive_shouldReturnTrueForImproving() {
        assertThat(TrendDirection.IMPROVING.isPositive()).isTrue();
        assertThat(TrendDirection.STABLE.isPositive()).isFalse();
        assertThat(TrendDirection.DEGRADING.isPositive()).isFalse();
    }

    @Test
    void isNegative_shouldReturnTrueForDegrading() {
        assertThat(TrendDirection.DEGRADING.isNegative()).isTrue();
        assertThat(TrendDirection.STABLE.isNegative()).isFalse();
        assertThat(TrendDirection.IMPROVING.isNegative()).isFalse();
    }
}
