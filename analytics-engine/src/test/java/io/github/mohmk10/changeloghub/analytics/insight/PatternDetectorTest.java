package io.github.mohmk10.changeloghub.analytics.insight;

import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatternDetectorTest {

    private PatternDetector detector;

    @BeforeEach
    void setUp() {
        detector = new PatternDetector();
    }

    @Test
    void detectPatterns_withNullHistory_shouldReturnEmptyList() {
        List<PatternDetector.Pattern> patterns = detector.detectPatterns(null);
        assertThat(patterns).isEmpty();
    }

    @Test
    void detectPatterns_withEmptyHistory_shouldReturnEmptyList() {
        List<PatternDetector.Pattern> patterns = detector.detectPatterns(Collections.emptyList());
        assertThat(patterns).isEmpty();
    }

    @Test
    void detectPatterns_withSmallHistory_shouldReturnEmptyList() {
        List<Changelog> history = Collections.singletonList(
                createChangelog(1, LocalDateTime.now())
        );
        List<PatternDetector.Pattern> patterns = detector.detectPatterns(history);
        assertThat(patterns).isEmpty();
    }

    @Test
    void detectFrequentBreakingChanges_shouldDetectHighRatio() {
        List<Changelog> history = new ArrayList<>();
        // All releases have breaking changes
        for (int i = 0; i < 10; i++) {
            history.add(createChangelog(2, LocalDateTime.now().minusDays(i * 10)));
        }

        PatternDetector.Pattern pattern = detector.detectFrequentBreakingChanges(history);
        assertThat(pattern).isNotNull();
        assertThat(pattern.getType()).isEqualTo(PatternDetector.Pattern.PatternType.FREQUENT_BREAKING_CHANGES);
        assertThat(pattern.getConfidence()).isGreaterThan(0.5);
    }

    @Test
    void detectFrequentBreakingChanges_withNoBreaking_shouldReturnNull() {
        List<Changelog> history = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            history.add(createChangelog(0, LocalDateTime.now().minusDays(i * 10)));
        }

        PatternDetector.Pattern pattern = detector.detectFrequentBreakingChanges(history);
        assertThat(pattern).isNull();
    }

    @Test
    void detectSeasonalPattern_shouldDetectPeakMonth() {
        List<Changelog> history = new ArrayList<>();
        // Most releases in January
        for (int year = 2020; year <= 2023; year++) {
            history.add(createChangelog(0, LocalDateTime.of(year, Month.JANUARY, 15, 10, 0)));
            history.add(createChangelog(0, LocalDateTime.of(year, Month.JANUARY, 20, 10, 0)));
            history.add(createChangelog(0, LocalDateTime.of(year, Month.JUNE, 15, 10, 0)));
        }

        PatternDetector.Pattern pattern = detector.detectSeasonalPattern(history);
        assertThat(pattern).isNotNull();
        assertThat(pattern.getType()).isEqualTo(PatternDetector.Pattern.PatternType.SEASONAL_RELEASES);
        assertThat(pattern.getDetails().get("peakMonth")).isEqualTo(Month.JANUARY);
    }

    @Test
    void detectWeeklyPattern_shouldDetectPeakDay() {
        List<Changelog> history = new ArrayList<>();
        // Most releases on Tuesday
        LocalDateTime tuesday = LocalDateTime.now().with(DayOfWeek.TUESDAY);
        for (int i = 0; i < 10; i++) {
            history.add(createChangelog(0, tuesday.minusWeeks(i)));
        }
        // Few releases on other days
        history.add(createChangelog(0, tuesday.with(DayOfWeek.FRIDAY)));
        history.add(createChangelog(0, tuesday.with(DayOfWeek.MONDAY)));

        PatternDetector.Pattern pattern = detector.detectWeeklyPattern(history);
        assertThat(pattern).isNotNull();
        assertThat(pattern.getType()).isEqualTo(PatternDetector.Pattern.PatternType.WEEKLY_PATTERN);
    }

    @Test
    void detectBurstReleases_shouldDetectCloseReleases() {
        List<Changelog> history = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        // Burst of releases within hours
        for (int i = 0; i < 5; i++) {
            history.add(createChangelog(0, now.minusHours(i * 3)));
        }

        PatternDetector.Pattern pattern = detector.detectBurstReleases(history);
        assertThat(pattern).isNotNull();
        assertThat(pattern.getType()).isEqualTo(PatternDetector.Pattern.PatternType.BURST_RELEASES);
    }

    @Test
    void detectBurstReleases_withSpreadReleases_shouldReturnNull() {
        List<Changelog> history = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        // Releases spread over weeks
        for (int i = 0; i < 5; i++) {
            history.add(createChangelog(0, now.minusDays(i * 7)));
        }

        PatternDetector.Pattern pattern = detector.detectBurstReleases(history);
        assertThat(pattern).isNull();
    }

    @Test
    void pattern_shouldHaveDescription() {
        PatternDetector.Pattern pattern = new PatternDetector.Pattern(
                PatternDetector.Pattern.PatternType.FREQUENT_BREAKING_CHANGES,
                "Test description",
                0.8
        );

        assertThat(pattern.getDescription()).isEqualTo("Test description");
        assertThat(pattern.getConfidence()).isEqualTo(0.8);
    }

    @Test
    void pattern_shouldStoreDetails() {
        PatternDetector.Pattern pattern = new PatternDetector.Pattern(
                PatternDetector.Pattern.PatternType.WEEKLY_PATTERN,
                "Test",
                0.7
        );
        pattern.addDetail("key1", "value1");
        pattern.addDetail("key2", 42);

        assertThat(pattern.getDetails()).containsEntry("key1", "value1");
        assertThat(pattern.getDetails()).containsEntry("key2", 42);
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
