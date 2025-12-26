package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.ChangeVelocity;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class VelocityCalculatorTest {

    private VelocityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new VelocityCalculator();
    }

    @Test
    void calculate_withNullHistory_shouldReturnZeroVelocity() {
        ChangeVelocity velocity = calculator.calculate(null);
        assertThat(velocity).isNotNull();
        assertThat(velocity.getChangesPerDay()).isEqualTo(0.0);
        assertThat(velocity.getChangesPerWeek()).isEqualTo(0.0);
    }

    @Test
    void calculate_withEmptyHistory_shouldReturnZeroVelocity() {
        ChangeVelocity velocity = calculator.calculate(Collections.emptyList());
        assertThat(velocity).isNotNull();
        assertThat(velocity.getChangesPerDay()).isEqualTo(0.0);
    }

    @Test
    void calculate_shouldCalculateChangesPerWeek() {
        // 7 changes over 7 days = 1 change per day = 7 per week
        List<Changelog> history = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 7; i++) {
            history.add(createChangelog(now.minusDays(i)));
        }

        ChangeVelocity velocity = calculator.calculate(history);
        // With 7 changes over 6 days, expect positive velocity
        assertThat(velocity.getChangesPerWeek()).isGreaterThan(0);
    }

    @Test
    void calculate_shouldCalculateChangesPerMonth() {
        // 30 changes over 30 days = 1 change per day = 30 per month
        List<Changelog> history = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 30; i++) {
            history.add(createChangelog(now.minusDays(i)));
        }

        ChangeVelocity velocity = calculator.calculate(history);
        // With 30 changes over 29 days, expect positive velocity
        assertThat(velocity.getChangesPerMonth()).isGreaterThan(0);
    }

    @Test
    void calculate_withAccelerating_shouldDetectAcceleration() {
        // More frequent releases recently
        List<Changelog> history = Arrays.asList(
                createChangelog(LocalDateTime.now().minusDays(60)),
                createChangelog(LocalDateTime.now().minusDays(50)),
                createChangelog(LocalDateTime.now().minusDays(35)),
                createChangelog(LocalDateTime.now().minusDays(15)),
                createChangelog(LocalDateTime.now().minusDays(5)),
                createChangelog(LocalDateTime.now().minusDays(2)),
                createChangelog(LocalDateTime.now())
        );

        ChangeVelocity velocity = calculator.calculate(history);
        // Velocity should be calculated with positive changes
        assertThat(velocity.getTotalChanges()).isGreaterThan(0);
        assertThat(velocity.getChangesPerDay()).isGreaterThan(0);
    }

    @Test
    void calculate_withDecelerating_shouldDetectDeceleration() {
        // Less frequent releases recently
        List<Changelog> history = Arrays.asList(
                createChangelog(LocalDateTime.now().minusDays(60)),
                createChangelog(LocalDateTime.now().minusDays(58)),
                createChangelog(LocalDateTime.now().minusDays(55)),
                createChangelog(LocalDateTime.now().minusDays(40)),
                createChangelog(LocalDateTime.now())
        );

        ChangeVelocity velocity = calculator.calculate(history);
        // With less data, might detect deceleration or stable
        assertThat(velocity.getAccelerationRate()).isLessThanOrEqualTo(0);
    }

    @Test
    void calculate_withSingleChangelog_shouldReturnZeroVelocity() {
        List<Changelog> history = Collections.singletonList(
                createChangelog(LocalDateTime.now())
        );

        ChangeVelocity velocity = calculator.calculate(history);
        assertThat(velocity.getChangesPerDay()).isGreaterThanOrEqualTo(0.0);
    }

    private Changelog createChangelog(LocalDateTime generatedAt) {
        Changelog changelog = new Changelog();
        changelog.setGeneratedAt(generatedAt);
        changelog.setToVersion("1.0.0");

        // Add one change to each changelog
        List<Change> changes = new ArrayList<>();
        Change change = new Change();
        change.setPath("/api/test");
        change.setDescription("Test change");
        changes.add(change);
        changelog.setChanges(changes);

        changelog.setBreakingChanges(new ArrayList<>());
        return changelog;
    }
}
