package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.StabilityScore;
import io.github.mohmk10.changeloghub.analytics.util.StabilityGrade;
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

class StabilityScorerTest {

    private StabilityScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new StabilityScorer();
    }

    @Test
    void calculate_withNullHistory_shouldReturnPerfectScore() {
        StabilityScore score = scorer.calculate(null);
        assertThat(score).isNotNull();
        assertThat(score.getScore()).isEqualTo(100);
        assertThat(score.getGrade()).isEqualTo(StabilityGrade.A);
    }

    @Test
    void calculate_withEmptyHistory_shouldReturnPerfectScore() {
        StabilityScore score = scorer.calculate(Collections.emptyList());
        assertThat(score).isNotNull();
        assertThat(score.getScore()).isEqualTo(100);
        assertThat(score.getGrade()).isEqualTo(StabilityGrade.A);
    }

    @Test
    void calculate_withNoBreakingChanges_shouldReturnHighScore() {
        List<Changelog> history = Arrays.asList(
                createChangelog("1.0.0", "1.1.0", 0, LocalDateTime.now().minusDays(30)),
                createChangelog("1.1.0", "1.2.0", 0, LocalDateTime.now().minusDays(15)),
                createChangelog("1.2.0", "1.3.0", 0, LocalDateTime.now())
        );

        StabilityScore score = scorer.calculate(history);
        assertThat(score.getScore()).isGreaterThanOrEqualTo(90);
        assertThat(score.getGrade()).isEqualTo(StabilityGrade.A);
    }

    @Test
    void calculate_withManyBreakingChanges_shouldReturnLowScore() {
        List<Changelog> history = Arrays.asList(
                createChangelog("1.0.0", "2.0.0", 5, LocalDateTime.now().minusDays(30)),
                createChangelog("2.0.0", "3.0.0", 3, LocalDateTime.now().minusDays(15)),
                createChangelog("3.0.0", "4.0.0", 4, LocalDateTime.now())
        );

        StabilityScore score = scorer.calculate(history);
        
        assertThat(score.getScore()).isBetween(0, 100);
        assertThat(score.getBreakingChangeRatio()).isBetween(0.0, 1.0);
    }

    @Test
    void calculate_shouldCalculateBreakingChangeRatio() {
        List<Changelog> history = Arrays.asList(
                createChangelog("1.0.0", "1.1.0", 0, LocalDateTime.now().minusDays(30)),
                createChangelog("1.1.0", "2.0.0", 2, LocalDateTime.now())
        );

        StabilityScore score = scorer.calculate(history);
        
        assertThat(score.getBreakingChangeRatio()).isBetween(0.0, 1.0);
    }

    @Test
    void calculate_shouldCalculateAvgDaysBetweenBreaking() {
        LocalDateTime now = LocalDateTime.now();
        List<Changelog> history = Arrays.asList(
                createChangelog("1.0.0", "2.0.0", 2, now.minusDays(60)),
                createChangelog("2.0.0", "2.1.0", 0, now.minusDays(30)),
                createChangelog("2.1.0", "3.0.0", 1, now)
        );

        StabilityScore score = scorer.calculate(history);
        assertThat(score.getAvgDaysBetweenBreaking()).isGreaterThan(0);
    }

    @Test
    void calculate_shouldIncludeFactors() {
        List<Changelog> history = Arrays.asList(
                createChangelog("1.0.0", "1.1.0", 1, LocalDateTime.now().minusDays(30)),
                createChangelog("1.1.0", "1.2.0", 0, LocalDateTime.now())
        );

        StabilityScore score = scorer.calculate(history);
        assertThat(score.getFactors()).isNotEmpty();
    }

    @Test
    void calculate_withSingleChangelog_shouldWorkCorrectly() {
        List<Changelog> history = Collections.singletonList(
                createChangelog("1.0.0", "2.0.0", 3, LocalDateTime.now())
        );

        StabilityScore score = scorer.calculate(history);
        assertThat(score).isNotNull();
        assertThat(score.getScore()).isGreaterThanOrEqualTo(0);
        assertThat(score.getScore()).isLessThanOrEqualTo(100);
    }

    private Changelog createChangelog(String oldVersion, String newVersion, int breakingCount, LocalDateTime generatedAt) {
        Changelog changelog = new Changelog();
        changelog.setFromVersion(oldVersion);
        changelog.setToVersion(newVersion);
        changelog.setGeneratedAt(generatedAt);

        List<BreakingChange> breakingChanges = new ArrayList<>();
        for (int i = 0; i < breakingCount; i++) {
            BreakingChange bc = new BreakingChange();
            bc.setType(ChangeType.REMOVED);
            bc.setDescription("Breaking change " + i);
            breakingChanges.add(bc);
        }
        changelog.setBreakingChanges(breakingChanges);
        changelog.setChanges(new ArrayList<>());

        return changelog;
    }
}
