package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.RiskTrend;
import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;
import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskCalculatorTest {

    private RiskCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RiskCalculator();
    }

    @Test
    void analyzeTrend_withNullHistory_shouldReturnStableTrend() {
        RiskTrend trend = calculator.analyzeTrend(null);
        assertThat(trend).isNotNull();
        assertThat(trend.getDirection()).isEqualTo(TrendDirection.STABLE);
    }

    @Test
    void analyzeTrend_withEmptyHistory_shouldReturnStableTrend() {
        RiskTrend trend = calculator.analyzeTrend(Collections.emptyList());
        assertThat(trend).isNotNull();
        assertThat(trend.getDirection()).isEqualTo(TrendDirection.STABLE);
    }

    @Test
    void analyzeTrend_withDecreasingRisk_shouldReturnImproving() {
        List<Changelog> history = Arrays.asList(
                createChangelog(5, LocalDateTime.now().minusDays(60)),
                createChangelog(3, LocalDateTime.now().minusDays(30)),
                createChangelog(1, LocalDateTime.now())
        );

        RiskTrend trend = calculator.analyzeTrend(history);
        assertThat(trend.getDirection()).isEqualTo(TrendDirection.IMPROVING);
    }

    @Test
    void analyzeTrend_withIncreasingRisk_shouldReturnDegrading() {
        List<Changelog> history = Arrays.asList(
                createChangelog(1, LocalDateTime.now().minusDays(60)),
                createChangelog(3, LocalDateTime.now().minusDays(30)),
                createChangelog(5, LocalDateTime.now())
        );

        RiskTrend trend = calculator.analyzeTrend(history);
        assertThat(trend.getDirection()).isEqualTo(TrendDirection.DEGRADING);
    }

    @Test
    void analyzeTrend_withConsistentRisk_shouldReturnStable() {
        List<Changelog> history = Arrays.asList(
                createChangelog(2, LocalDateTime.now().minusDays(60)),
                createChangelog(2, LocalDateTime.now().minusDays(30)),
                createChangelog(2, LocalDateTime.now())
        );

        RiskTrend trend = calculator.analyzeTrend(history);
        assertThat(trend.getDirection()).isEqualTo(TrendDirection.STABLE);
    }

    @Test
    void calculateRisk_shouldCalculateBasedOnBreakingChanges() {
        Changelog low = createChangelog(1, LocalDateTime.now());
        Changelog high = createChangelog(10, LocalDateTime.now());

        int lowScore = calculator.calculateRisk(low);
        int highScore = calculator.calculateRisk(high);

        assertThat(lowScore).isLessThan(highScore);
        assertThat(lowScore).isGreaterThanOrEqualTo(0);
        assertThat(highScore).isLessThanOrEqualTo(100);
    }

    @Test
    void calculateRisk_withNullChangelog_shouldReturnZero() {
        int score = calculator.calculateRisk(null);
        assertThat(score).isZero();
    }

    @Test
    void analyzeTrend_withSingleChangelog_shouldReturnStable() {
        List<Changelog> history = Collections.singletonList(
                createChangelog(3, LocalDateTime.now())
        );

        RiskTrend trend = calculator.analyzeTrend(history);
        assertThat(trend.getDirection()).isEqualTo(TrendDirection.STABLE);
    }

    private Changelog createChangelog(int breakingCount, LocalDateTime generatedAt) {
        Changelog changelog = new Changelog();
        changelog.setGeneratedAt(generatedAt);
        changelog.setToVersion("1.0.0");

        List<BreakingChange> breakingChanges = new ArrayList<>();
        for (int i = 0; i < breakingCount; i++) {
            BreakingChange bc = new BreakingChange();
            bc.setType(ChangeType.REMOVED);
            bc.setDescription("Breaking change " + i);
            breakingChanges.add(bc);
        }
        changelog.setBreakingChanges(breakingChanges);

        return changelog;
    }
}
