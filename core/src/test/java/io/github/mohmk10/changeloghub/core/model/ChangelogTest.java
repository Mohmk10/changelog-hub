package io.github.mohmk10.changeloghub.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ChangelogTest {

    @Test
    void shouldCreateChangelogWithDefaultConstructor() {
        Changelog changelog = new Changelog();

        assertThat(changelog.getId()).isNotNull();
        assertThat(changelog.getGeneratedAt()).isNotNull();
        assertThat(changelog.getChanges()).isNotNull().isEmpty();
        assertThat(changelog.getBreakingChanges()).isNotNull().isEmpty();
    }

    @Test
    void shouldCreateChangelogWithAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Change change = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .build();
        BreakingChange breakingChange = BreakingChange.breakingChangeBuilder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .impactScore(80)
                .build();
        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setLevel(RiskLevel.HIGH);

        Changelog changelog = new Changelog(
                "test-id",
                "Test API",
                "1.0.0",
                "2.0.0",
                Arrays.asList(change),
                Arrays.asList(breakingChange),
                riskAssessment,
                now
        );

        assertThat(changelog.getId()).isEqualTo("test-id");
        assertThat(changelog.getApiName()).isEqualTo("Test API");
        assertThat(changelog.getFromVersion()).isEqualTo("1.0.0");
        assertThat(changelog.getToVersion()).isEqualTo("2.0.0");
        assertThat(changelog.getChanges()).hasSize(1);
        assertThat(changelog.getBreakingChanges()).hasSize(1);
        assertThat(changelog.getRiskAssessment().getLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(changelog.getGeneratedAt()).isEqualTo(now);
    }

    @Test
    void shouldCreateChangelogWithBuilder() {
        Change change1 = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .path("/api/new")
                .build();
        Change change2 = Change.builder()
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.PARAMETER)
                .path("/api/users")
                .build();
        BreakingChange breakingChange = BreakingChange.breakingChangeBuilder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .path("/api/deprecated")
                .impactScore(90)
                .build();

        Changelog changelog = Changelog.builder()
                .apiName("Test API")
                .fromVersion("1.0.0")
                .toVersion("2.0.0")
                .addChange(change1)
                .addChange(change2)
                .addBreakingChange(breakingChange)
                .build();

        assertThat(changelog.getId()).isNotNull();
        assertThat(changelog.getApiName()).isEqualTo("Test API");
        assertThat(changelog.getChanges()).hasSize(2);
        assertThat(changelog.getBreakingChanges()).hasSize(1);
    }

    @Test
    void shouldCountChangesCorrectly() {
        Changelog changelog = Changelog.builder()
                .addChange(Change.builder().type(ChangeType.ADDED).build())
                .addChange(Change.builder().type(ChangeType.MODIFIED).build())
                .addChange(Change.builder().type(ChangeType.REMOVED).build())
                .addBreakingChange(BreakingChange.breakingChangeBuilder().type(ChangeType.REMOVED).build())
                .build();

        assertThat(changelog.getChanges()).hasSize(3);
        assertThat(changelog.getBreakingChanges()).hasSize(1);
    }

    @Test
    void shouldHaveCorrectEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();

        Changelog changelog1 = Changelog.builder()
                .id("test-id")
                .apiName("Test API")
                .fromVersion("1.0.0")
                .toVersion("2.0.0")
                .generatedAt(now)
                .build();

        Changelog changelog2 = Changelog.builder()
                .id("test-id")
                .apiName("Test API")
                .fromVersion("1.0.0")
                .toVersion("2.0.0")
                .generatedAt(now)
                .build();

        Changelog changelog3 = Changelog.builder()
                .id("different-id")
                .apiName("Test API")
                .fromVersion("1.0.0")
                .toVersion("2.0.0")
                .generatedAt(now)
                .build();

        assertThat(changelog1).isEqualTo(changelog2);
        assertThat(changelog1.hashCode()).isEqualTo(changelog2.hashCode());
        assertThat(changelog1).isNotEqualTo(changelog3);
    }

    @Test
    void shouldHaveCorrectToString() {
        Changelog changelog = Changelog.builder()
                .apiName("Test API")
                .fromVersion("1.0.0")
                .toVersion("2.0.0")
                .addChange(Change.builder().type(ChangeType.ADDED).build())
                .build();

        String toString = changelog.toString();

        assertThat(toString).contains("Test API");
        assertThat(toString).contains("1.0.0");
        assertThat(toString).contains("2.0.0");
    }
}
