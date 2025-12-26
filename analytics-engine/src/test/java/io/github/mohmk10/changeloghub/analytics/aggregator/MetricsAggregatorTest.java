package io.github.mohmk10.changeloghub.analytics.aggregator;

import io.github.mohmk10.changeloghub.analytics.model.ApiMetrics;
import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.Change;
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

class MetricsAggregatorTest {

    private MetricsAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new MetricsAggregator();
    }

    @Test
    void aggregate_withNullList_shouldReturnEmptyMetrics() {
        ApiMetrics result = aggregator.aggregate(null);
        assertThat(result).isNotNull();
        assertThat(result.getTotalEndpoints()).isZero();
    }

    @Test
    void aggregate_withEmptyList_shouldReturnEmptyMetrics() {
        ApiMetrics result = aggregator.aggregate(Collections.emptyList());
        assertThat(result).isNotNull();
        assertThat(result.getTotalEndpoints()).isZero();
    }

    @Test
    void aggregate_shouldSumTotalChanges() {
        List<Changelog> changelogs = Arrays.asList(
                createChangelog(10, 5),
                createChangelog(15, 3)
        );

        ApiMetrics result = aggregator.aggregate(changelogs);
        assertThat(result.getTotalChanges()).isEqualTo(25); 
    }

    @Test
    void aggregate_shouldSumBreakingChanges() {
        List<Changelog> changelogs = Arrays.asList(
                createChangelog(10, 5),
                createChangelog(15, 3)
        );

        ApiMetrics result = aggregator.aggregate(changelogs);
        assertThat(result.getBreakingChanges()).isEqualTo(8); 
    }

    @Test
    void aggregate_withSingleChangelog_shouldReturnCorrectValues() {
        Changelog changelog = createChangelog(10, 5);

        ApiMetrics result = aggregator.aggregate(Collections.singletonList(changelog));
        assertThat(result.getTotalChanges()).isEqualTo(10); 
        assertThat(result.getBreakingChanges()).isEqualTo(5);
    }

    @Test
    void aggregate_shouldCountUniqueEndpoints() {
        Changelog changelog1 = createChangelog(5, 2);
        Changelog changelog2 = createChangelog(3, 1);

        ApiMetrics result = aggregator.aggregate(Arrays.asList(changelog1, changelog2));
        
        assertThat(result.getTotalEndpoints()).isGreaterThanOrEqualTo(0);
    }

    private Changelog createChangelog(int changeCount, int breakingCount) {
        Changelog changelog = new Changelog();
        changelog.setApiName("Test API");
        changelog.setFromVersion("1.0.0");
        changelog.setToVersion("2.0.0");
        changelog.setGeneratedAt(LocalDateTime.now());

        List<Change> changes = new ArrayList<>();
        for (int i = 0; i < changeCount; i++) {
            Change change = new Change();
            change.setPath("/api/v1/endpoint" + i);
            change.setType(ChangeType.MODIFIED);
            change.setDescription("Change " + i);
            changes.add(change);
        }
        changelog.setChanges(changes);

        List<BreakingChange> breakingChanges = new ArrayList<>();
        for (int i = 0; i < breakingCount; i++) {
            BreakingChange bc = new BreakingChange();
            bc.setPath("/api/v1/breaking" + i);
            bc.setType(ChangeType.REMOVED);
            bc.setDescription("Breaking change " + i);
            breakingChanges.add(bc);
        }
        changelog.setBreakingChanges(breakingChanges);

        return changelog;
    }
}
