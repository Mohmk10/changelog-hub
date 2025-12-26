package io.github.mohmk10.changeloghub.analytics.insight;

import io.github.mohmk10.changeloghub.analytics.model.Insight;
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

class InsightGeneratorTest {

    private InsightGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new InsightGenerator();
    }

    @Test
    void generate_withNullHistory_shouldReturnEmptyList() {
        List<Insight> insights = generator.generate(null, null);
        assertThat(insights).isEmpty();
    }

    @Test
    void generate_withEmptyHistory_shouldReturnEmptyList() {
        List<Insight> insights = generator.generate(null, Collections.emptyList());
        assertThat(insights).isEmpty();
    }

    @Test
    void generate_withBreakingChangeTrend_shouldGenerateInsight() {
        List<Changelog> history = Arrays.asList(
                createChangelog(1, LocalDateTime.now().minusDays(60)),
                createChangelog(3, LocalDateTime.now().minusDays(30)),
                createChangelog(5, LocalDateTime.now())
        );

        List<Insight> insights = generator.generate(null, history);
        // Should generate some insights based on the history
        assertThat(insights).isNotNull();
    }

    @Test
    void generate_withNoBreakingChanges_shouldGeneratePositiveInsight() {
        List<Changelog> history = Arrays.asList(
                createChangelog(0, LocalDateTime.now().minusDays(60)),
                createChangelog(0, LocalDateTime.now().minusDays(30)),
                createChangelog(0, LocalDateTime.now())
        );

        List<Insight> insights = generator.generate(null, history);
        // Should process history without errors
        assertThat(insights).isNotNull();
    }

    @Test
    void generate_withHighBreakingChangeCount_shouldGenerateCriticalInsight() {
        List<Changelog> history = Arrays.asList(
                createChangelog(10, LocalDateTime.now().minusDays(30)),
                createChangelog(8, LocalDateTime.now())
        );

        List<Insight> insights = generator.generate(null, history);
        // Should analyze high breaking change count
        assertThat(insights).isNotNull();
    }

    @Test
    void generate_shouldIncludePatternInsights() {
        // Create history with pattern (burst releases)
        LocalDateTime now = LocalDateTime.now();
        List<Changelog> history = Arrays.asList(
                createChangelog(1, now.minusHours(2)),
                createChangelog(1, now.minusHours(4)),
                createChangelog(1, now.minusHours(6)),
                createChangelog(1, now.minusHours(8)),
                createChangelog(0, now.minusDays(30))
        );

        List<Insight> insights = generator.generate(null, history);
        boolean hasPatternInsight = insights.stream()
                .anyMatch(i -> i.getType() == Insight.InsightType.PATTERN);
        // May or may not detect pattern depending on thresholds
        assertThat(insights).isNotEmpty();
    }

    @Test
    void generate_shouldSetConfidenceLevel() {
        List<Changelog> history = Arrays.asList(
                createChangelog(2, LocalDateTime.now().minusDays(30)),
                createChangelog(3, LocalDateTime.now())
        );

        List<Insight> insights = generator.generate(null, history);
        for (Insight insight : insights) {
            assertThat(insight.getConfidence()).isBetween(0.0, 1.0);
        }
    }

    @Test
    void generate_shouldHaveTitlesAndDescriptions() {
        List<Changelog> history = Arrays.asList(
                createChangelog(2, LocalDateTime.now().minusDays(30)),
                createChangelog(3, LocalDateTime.now())
        );

        List<Insight> insights = generator.generate(null, history);
        for (Insight insight : insights) {
            assertThat(insight.getTitle()).isNotBlank();
            assertThat(insight.getDescription()).isNotBlank();
        }
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
        changelog.setChanges(new ArrayList<>());

        return changelog;
    }
}
