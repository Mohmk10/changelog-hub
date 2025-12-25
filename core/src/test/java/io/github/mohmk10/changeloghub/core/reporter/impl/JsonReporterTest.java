package io.github.mohmk10.changeloghub.core.reporter.impl;

import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.RiskAssessment;
import io.github.mohmk10.changeloghub.core.model.RiskLevel;
import io.github.mohmk10.changeloghub.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class JsonReporterTest {

    private JsonReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new JsonReporter();
    }

    @Test
    void testReportIsValidJson() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).startsWith("{");
        assertThat(report).endsWith("}\n");
        assertThat(countOccurrences(report, "{")).isEqualTo(countOccurrences(report, "}"));
        assertThat(countOccurrences(report, "[")).isEqualTo(countOccurrences(report, "]"));
    }

    @Test
    void testReportContainsAllFields() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).contains("\"apiName\"");
        assertThat(report).contains("\"fromVersion\"");
        assertThat(report).contains("\"toVersion\"");
        assertThat(report).contains("\"generatedAt\"");
        assertThat(report).contains("\"summary\"");
        assertThat(report).contains("\"breakingChanges\"");
        assertThat(report).contains("\"changes\"");
        assertThat(report).contains("\"riskAssessment\"");
    }

    @Test
    void testReportContainsChanges() {
        Change change = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.INFO)
                .path("/api/users")
                .description("New endpoint added")
                .build();

        Changelog changelog = Changelog.builder()
                .apiName("Test API")
                .fromVersion("1.0.0")
                .toVersion("1.1.0")
                .changes(Arrays.asList(change))
                .build();

        String report = reporter.report(changelog);

        assertThat(report).contains("\"path\": \"/api/users\"");
        assertThat(report).contains("\"type\": \"ADDED\"");
        assertThat(report).contains("\"category\": \"ENDPOINT\"");
        assertThat(report).contains("\"severity\": \"INFO\"");
    }

    @Test
    void testReportToFile(@TempDir Path tempDir) throws Exception {
        Changelog changelog = createTestChangelog();
        Path outputPath = tempDir.resolve("output/changelog.json");

        reporter.reportToFile(changelog, outputPath);

        assertThat(Files.exists(outputPath)).isTrue();
        String content = Files.readString(outputPath);
        assertThat(content).contains("\"apiName\": \"Test API\"");
    }

    @Test
    void testReportHandlesNull() {
        String report = reporter.report(null);

        assertThat(report).isEqualTo("{}");
    }

    @Test
    void testReportEscapesSpecialCharacters() {
        Change change = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.INFO)
                .path("/api/users")
                .description("Description with \"quotes\" and \\ backslash")
                .build();

        Changelog changelog = Changelog.builder()
                .apiName("Test API")
                .changes(Arrays.asList(change))
                .build();

        String report = reporter.report(changelog);

        assertThat(report).contains("\\\"quotes\\\"");
        assertThat(report).contains("\\\\");
    }

    @Test
    void testReportContainsSummary() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).contains("\"totalChanges\"");
        assertThat(report).contains("\"breakingChanges\"");
        assertThat(report).contains("\"riskLevel\"");
        assertThat(report).contains("\"semverRecommendation\"");
    }

    private Changelog createTestChangelog() {
        BreakingChange breakingChange = BreakingChange.breakingChangeBuilder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .path("/api/users")
                .description("Endpoint removed")
                .impactScore(100)
                .migrationSuggestion("Use /api/v2/users")
                .build();

        RiskAssessment risk = new RiskAssessment();
        risk.setLevel(RiskLevel.HIGH);
        risk.setOverallScore(75);
        risk.setSemverRecommendation("MAJOR");
        risk.setBreakingChangesCount(1);
        risk.setTotalChangesCount(1);

        return Changelog.builder()
                .apiName("Test API")
                .fromVersion("1.0.0")
                .toVersion("2.0.0")
                .addBreakingChange(breakingChange)
                .riskAssessment(risk)
                .build();
    }

    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
