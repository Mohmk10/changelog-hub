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

class MarkdownReporterTest {

    private MarkdownReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new MarkdownReporter();
    }

    @Test
    void testReportContainsHeader() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).contains("# Changelog: Test API");
        assertThat(report).contains("1.0.0 → 2.0.0");
    }

    @Test
    void testReportContainsSummary() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).contains("## Summary");
        assertThat(report).contains("Total changes");
        assertThat(report).contains("Breaking changes");
        assertThat(report).contains("Risk level");
    }

    @Test
    void testReportContainsBreakingChanges() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).contains("## 🔴 Breaking Changes");
        assertThat(report).contains("/api/users");
        assertThat(report).contains("Migration:");
    }

    @Test
    void testReportContainsAllSections() {
        Change breakingChange = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .path("/api/users")
                .description("Endpoint removed")
                .build();

        Change dangerousChange = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.PARAMETER)
                .severity(Severity.DANGEROUS)
                .path("parameter:filter")
                .description("Parameter removed")
                .build();

        Change warningChange = Change.builder()
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.WARNING)
                .path("/api/legacy")
                .description("Endpoint deprecated")
                .build();

        Change infoChange = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.INFO)
                .path("/api/new")
                .description("New endpoint")
                .build();

        Changelog changelog = Changelog.builder()
                .apiName("Test API")
                .fromVersion("1.0.0")
                .toVersion("2.0.0")
                .changes(Arrays.asList(breakingChange, dangerousChange, warningChange, infoChange))
                .build();

        String report = reporter.report(changelog);

        assertThat(report).contains("🟠 Dangerous Changes");
        assertThat(report).contains("🟡 Warnings");
        assertThat(report).contains("🟢 Additions");
    }

    @Test
    void testReportToFile(@TempDir Path tempDir) throws Exception {
        Changelog changelog = createTestChangelog();
        Path outputPath = tempDir.resolve("subdir/changelog.md");

        reporter.reportToFile(changelog, outputPath);

        assertThat(Files.exists(outputPath)).isTrue();
        String content = Files.readString(outputPath);
        assertThat(content).contains("# Changelog: Test API");
    }

    @Test
    void testReportHandlesNull() {
        String report = reporter.report(null);

        assertThat(report).contains("No changelog data available");
    }

    private Changelog createTestChangelog() {
        BreakingChange breakingChange = BreakingChange.breakingChangeBuilder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .path("/api/users")
                .description("Endpoint removed")
                .impactScore(100)
                .migrationSuggestion("Use /api/v2/users instead")
                .build();

        RiskAssessment risk = new RiskAssessment();
        risk.setLevel(RiskLevel.HIGH);
        risk.setOverallScore(75);
        risk.setSemverRecommendation("MAJOR");

        return Changelog.builder()
                .apiName("Test API")
                .fromVersion("1.0.0")
                .toVersion("2.0.0")
                .addBreakingChange(breakingChange)
                .riskAssessment(risk)
                .build();
    }
}
