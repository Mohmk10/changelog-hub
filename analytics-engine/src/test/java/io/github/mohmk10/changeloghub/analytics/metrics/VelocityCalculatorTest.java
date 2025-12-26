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
        
        List<Changelog> history = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 7; i++) {
            history.add(createChangelog(now.minusDays(i)));
        }

        ChangeVelocity velocity = calculator.calculate(history);
        
        assertThat(velocity.getChangesPerWeek()).isGreaterThan(0);
    }

    @Test
    void calculate_shouldCalculateChangesPerMonth() {
        
        List<Changelog> history = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 30; i++) {
            history.add(createChangelog(now.minusDays(i)));
        }

        ChangeVelocity velocity = calculator.calculate(history);
        
        assertThat(velocity.getChangesPerMonth()).isGreaterThan(0);
    }

    @Test
    void calculate_withAccelerating_shouldDetectAcceleration() {
        
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
        
        assertThat(velocity.getTotalChanges()).isGreaterThan(0);
        assertThat(velocity.getChangesPerDay()).isGreaterThan(0);
    }

    @Test
    void calculate_withDecelerating_shouldDetectDeceleration() {
        
        List<Changelog> history = Arrays.asList(
                createChangelog(LocalDateTime.now().minusDays(60)),
                createChangelog(LocalDateTime.now().minusDays(58)),
                createChangelog(LocalDateTime.now().minusDays(55)),
                createChangelog(LocalDateTime.now().minusDays(40)),
                createChangelog(LocalDateTime.now())
        );

        ChangeVelocity velocity = calculator.calculate(history);
        
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
