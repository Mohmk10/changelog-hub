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

class HtmlReporterTest {

    private HtmlReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new HtmlReporter();
    }

    @Test
    void testReportIsValidHtml() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).contains("<!DOCTYPE html>");
        assertThat(report).contains("<html");
        assertThat(report).contains("</html>");
        assertThat(report).contains("<head>");
        assertThat(report).contains("</head>");
        assertThat(report).contains("<body>");
        assertThat(report).contains("</body>");
    }

    @Test
    void testReportContainsStyles() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).contains("<style>");
        assertThat(report).contains("</style>");
        assertThat(report).contains(".container");
        assertThat(report).contains(".breaking");
    }

    @Test
    void testReportContainsHeader() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).contains("Test API");
        assertThat(report).contains("1.0.0");
        assertThat(report).contains("2.0.0");
    }

    @Test
    void testReportContainsSummary() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).contains("class=\"summary\"");
        assertThat(report).contains("Total Changes");
        assertThat(report).contains("Breaking Changes");
        assertThat(report).contains("Risk Level");
    }

    @Test
    void testReportContainsBreakingChanges() {
        Changelog changelog = createTestChangelog();

        String report = reporter.report(changelog);

        assertThat(report).contains("Breaking Changes");
        assertThat(report).contains("/api/users");
        assertThat(report).contains("Endpoint removed");
    }

    @Test
    void testReportContainsAllSeveritySections() {
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
        assertThat(report).contains("🟢 Additions & Info");
    }

    @Test
    void testReportToFile(@TempDir Path tempDir) throws Exception {
        Changelog changelog = createTestChangelog();
        Path outputPath = tempDir.resolve("output/changelog.html");

        reporter.reportToFile(changelog, outputPath);

        assertThat(Files.exists(outputPath)).isTrue();
        String content = Files.readString(outputPath);
        assertThat(content).contains("<!DOCTYPE html>");
        assertThat(content).contains("Test API");
    }

    @Test
    void testReportHandlesNull() {
        String report = reporter.report(null);

        assertThat(report).contains("<!DOCTYPE html>");
        assertThat(report).contains("No changelog data available");
    }

    @Test
    void testReportEscapesHtmlCharacters() {
        Change change = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.INFO)
                .path("/api/users")
                .description("Description with <script>alert('xss')</script>")
                .build();

        Changelog changelog = Changelog.builder()
                .apiName("Test API")
                .changes(Arrays.asList(change))
                .build();

        String report = reporter.report(changelog);

        // The malicious script tag should be escaped
        assertThat(report).contains("&lt;script&gt;alert");
        assertThat(report).contains("&lt;/script&gt;");
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
}
