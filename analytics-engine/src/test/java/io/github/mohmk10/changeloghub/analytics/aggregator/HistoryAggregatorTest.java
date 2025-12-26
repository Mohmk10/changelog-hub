package io.github.mohmk10.changeloghub.analytics.aggregator;

import io.github.mohmk10.changeloghub.analytics.model.ApiEvolution;
import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryAggregatorTest {

    private HistoryAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new HistoryAggregator();
    }

    @Test
    void aggregate_withNullHistory_shouldReturnEmptyEvolution() {
        ApiEvolution evolution = aggregator.aggregate(null);
        assertThat(evolution).isNotNull();
        assertThat(evolution.getVersions()).isEmpty();
    }

    @Test
    void aggregate_withEmptyHistory_shouldReturnEmptyEvolution() {
        ApiEvolution evolution = aggregator.aggregate(Collections.emptyList());
        assertThat(evolution).isNotNull();
        assertThat(evolution.getVersions()).isEmpty();
    }

    @Test
    void aggregate_shouldCreateVersionSummaries() {
        List<Changelog> history = Arrays.asList(
                createChangelog("1.0.0", "1.1.0", 0, LocalDateTime.now().minusDays(30)),
                createChangelog("1.1.0", "2.0.0", 3, LocalDateTime.now())
        );

        ApiEvolution evolution = aggregator.aggregate(history);
        assertThat(evolution.getVersions()).hasSize(2);
    }

    @Test
    void aggregate_shouldTrackBreakingChanges() {
        List<Changelog> history = Arrays.asList(
                createChangelog("1.0.0", "2.0.0", 5, LocalDateTime.now())
        );

        ApiEvolution evolution = aggregator.aggregate(history);
        assertThat(evolution.getVersions().get(0).getBreakingChanges()).isEqualTo(5);
    }

    @Test
    void aggregate_shouldSetTotalChanges() {
        List<Changelog> history = Arrays.asList(
                createChangelog("1.0.0", "1.1.0", 0, LocalDateTime.now().minusDays(30)),
                createChangelog("1.1.0", "2.0.0", 2, LocalDateTime.now())
        );

        ApiEvolution evolution = aggregator.aggregate(history);
        assertThat(evolution.getTotalVersions()).isEqualTo(2);
    }

    @Test
    void aggregate_shouldSortByDate() {
        LocalDateTime older = LocalDateTime.now().minusDays(60);
        LocalDateTime newer = LocalDateTime.now();

        List<Changelog> history = Arrays.asList(
                createChangelog("2.0.0", "2.1.0", 0, newer),
                createChangelog("1.0.0", "2.0.0", 1, older)
        );

        ApiEvolution evolution = aggregator.aggregate(history);
        List<ApiEvolution.VersionSummary> versions = evolution.getVersions();

        assertThat(versions.get(0).getReleaseDate()).isBefore(versions.get(1).getReleaseDate());
    }

    @Test
    void versionSummary_shouldContainCorrectData() {
        LocalDateTime date = LocalDateTime.of(2024, 1, 15, 10, 30);
        List<Changelog> history = Collections.singletonList(
                createChangelog("1.0.0", "1.1.0", 2, date)
        );

        ApiEvolution evolution = aggregator.aggregate(history);
        ApiEvolution.VersionSummary summary = evolution.getVersions().get(0);

        assertThat(summary.getVersion()).isEqualTo("1.1.0");
        assertThat(summary.getReleaseDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(summary.getBreakingChanges()).isEqualTo(2);
    }

    @Test
    void aggregate_shouldCalculateTotalBreakingChanges() {
        List<Changelog> history = Arrays.asList(
                createChangelog("1.0.0", "2.0.0", 3, LocalDateTime.now().minusDays(30)),
                createChangelog("2.0.0", "3.0.0", 2, LocalDateTime.now())
        );

        ApiEvolution evolution = aggregator.aggregate(history);
        assertThat(evolution.getTotalBreakingChanges()).isEqualTo(5);
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

        return changelog;
    }
}
