package io.github.mohmk10.changeloghub.parser.openapi.integration;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.RiskAssessment;
import io.github.mohmk10.changeloghub.core.model.RiskLevel;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.core.reporter.ReportFormat;
import io.github.mohmk10.changeloghub.core.reporter.Reporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenAPI Integration Tests")
class OpenApiIntegrationTest {

    private IntegrationTestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new IntegrationTestHelper();
    }

    @Test
    @DisplayName("Should detect breaking changes between v1 and v2-breaking")
    void testDetectBreakingChangesBetweenVersions() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        ApiSpec v2 = helper.parseApiSpec("api-v2-breaking.yaml");

        assertThat(v1).isNotNull();
        assertThat(v2).isNotNull();
        assertThat(v1.getVersion()).isEqualTo("1.0.0");
        assertThat(v2.getVersion()).isEqualTo("2.0.0");

        Changelog changelog = helper.getComparator().compare(v1, v2);
        assertThat(changelog).isNotNull();
        assertThat(changelog.getChanges()).isNotEmpty();

        List<BreakingChange> breakingChanges = helper.getBreakingChangeDetector().detect(changelog.getChanges());
        assertThat(breakingChanges).isNotEmpty();

        boolean hasRemovedChange = changelog.getChanges().stream()
                .anyMatch(c -> c.getType() == ChangeType.REMOVED);
        assertThat(hasRemovedChange).isTrue();

        boolean hasAddedChange = changelog.getChanges().stream()
                .anyMatch(c -> c.getType() == ChangeType.ADDED);
        assertThat(hasAddedChange).isTrue();
    }

    @Test
    @DisplayName("Should detect no breaking changes for minor version")
    void testDetectNoBreakingChangesForMinorVersion() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        ApiSpec v2Minor = helper.parseApiSpec("api-v2-minor.yaml");

        assertThat(v1.getVersion()).isEqualTo("1.0.0");
        assertThat(v2Minor.getVersion()).isEqualTo("1.1.0");

        Changelog changelog = helper.getComparator().compare(v1, v2Minor);
        assertThat(changelog).isNotNull();
        assertThat(changelog.getChanges()).isNotEmpty();

        boolean hasDeprecation = changelog.getChanges().stream()
                .anyMatch(c -> c.getType() == ChangeType.DEPRECATED);
        assertThat(hasDeprecation).isTrue();

        boolean hasAddition = changelog.getChanges().stream()
                .anyMatch(c -> c.getType() == ChangeType.ADDED);
        assertThat(hasAddition).isTrue();

        boolean hasWarnings = changelog.getChanges().stream()
                .anyMatch(c -> c.getSeverity() == Severity.WARNING);
        assertThat(hasWarnings).isTrue();

        boolean hasInfoChanges = changelog.getChanges().stream()
                .anyMatch(c -> c.getSeverity() == Severity.INFO);
        assertThat(hasInfoChanges).isTrue();
    }

    @Test
    @DisplayName("Should assess high risk for breaking changes")
    void testRiskAssessmentForBreakingChanges() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        ApiSpec v2 = helper.parseApiSpec("api-v2-breaking.yaml");

        Changelog changelog = helper.getComparator().compare(v1, v2);
        RiskAssessment risk = helper.getAnalysisService().assessRisk(changelog);

        assertThat(risk).isNotNull();
        assertThat(risk.getLevel()).isIn(RiskLevel.HIGH, RiskLevel.CRITICAL);
        assertThat(risk.getSemverRecommendation()).isEqualTo("MAJOR");
        assertThat(risk.getBreakingChangesCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should assess low/medium risk for minor changes")
    void testRiskAssessmentForMinorChanges() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        ApiSpec v2Minor = helper.parseApiSpec("api-v2-minor.yaml");

        Changelog changelog = helper.getComparator().compare(v1, v2Minor);
        RiskAssessment risk = helper.getAnalysisService().assessRisk(changelog);

        assertThat(risk).isNotNull();
        assertThat(risk.getLevel()).isIn(RiskLevel.LOW, RiskLevel.MEDIUM);
        assertThat(risk.getSemverRecommendation()).isIn("MINOR", "PATCH");
    }

    @Test
    @DisplayName("Should generate valid Markdown report")
    void testGenerateMarkdownReport() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        ApiSpec v2 = helper.parseApiSpec("api-v2-breaking.yaml");

        Changelog changelog = helper.getChangelogGenerator().generate(v1, v2);
        assertThat(changelog).isNotNull();

        Reporter markdownReporter = helper.getReporter(ReportFormat.MARKDOWN);
        String report = markdownReporter.report(changelog);

        assertThat(report).isNotNull();
        assertThat(report).contains("# Changelog:");
        assertThat(report).contains("1.0.0");
        assertThat(report).contains("2.0.0");
        assertThat(report).contains("## Summary");

        if (!changelog.getBreakingChanges().isEmpty()) {
            assertThat(report).contains("Breaking Changes");
        }
    }

    @Test
    @DisplayName("Should generate valid JSON report")
    void testGenerateJsonReport() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        ApiSpec v2 = helper.parseApiSpec("api-v2-breaking.yaml");

        Changelog changelog = helper.getChangelogGenerator().generate(v1, v2);

        Reporter jsonReporter = helper.getReporter(ReportFormat.JSON);
        String report = jsonReporter.report(changelog);

        assertThat(report).isNotNull();
        assertThat(report).startsWith("{");
        assertThat(report).contains("\"apiName\"");
        assertThat(report).contains("\"fromVersion\"");
        assertThat(report).contains("\"toVersion\"");
        assertThat(report).contains("\"changes\"");
        assertThat(report).contains("\"riskAssessment\"");

        int openBraces = report.length() - report.replace("{", "").length();
        int closeBraces = report.length() - report.replace("}", "").length();
        assertThat(openBraces).isEqualTo(closeBraces);
    }

    @Test
    @DisplayName("Should generate valid HTML report")
    void testGenerateHtmlReport() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        ApiSpec v2 = helper.parseApiSpec("api-v2-breaking.yaml");

        Changelog changelog = helper.getChangelogGenerator().generate(v1, v2);

        Reporter htmlReporter = helper.getReporter(ReportFormat.HTML);
        String report = htmlReporter.report(changelog);

        assertThat(report).isNotNull();
        assertThat(report).contains("<!DOCTYPE html>");
        assertThat(report).contains("<html");
        assertThat(report).contains("</html>");
        assertThat(report).contains("User API");
    }

    @Test
    @DisplayName("Should generate valid Console report")
    void testGenerateConsoleReport() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        ApiSpec v2 = helper.parseApiSpec("api-v2-breaking.yaml");

        Changelog changelog = helper.getChangelogGenerator().generate(v1, v2);

        Reporter consoleReporter = helper.getReporter(ReportFormat.CONSOLE);
        String report = consoleReporter.report(changelog);

        assertThat(report).isNotNull();
        assertThat(report).contains("CHANGELOG");
        assertThat(report).contains("User API");
        assertThat(report).contains("SUMMARY");
    }

    @Test
    @DisplayName("Should complete end-to-end analysis flow")
    void testEndToEndAnalysis() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        assertThat(v1).isNotNull();
        assertThat(v1.getName()).isEqualTo("User API");
        assertThat(v1.getEndpoints()).isNotEmpty();

        ApiSpec v2 = helper.parseApiSpec("api-v2-breaking.yaml");
        assertThat(v2).isNotNull();
        assertThat(v2.getName()).isEqualTo("User API");
        assertThat(v2.getEndpoints()).isNotEmpty();

        Changelog changelog = helper.getComparator().compare(v1, v2);
        assertThat(changelog).isNotNull();
        assertThat(changelog.getChanges()).isNotEmpty();

        List<BreakingChange> breakingChanges = helper.getBreakingChangeDetector().detect(changelog.getChanges());
        assertThat(breakingChanges).isNotNull();

        assertThat(changelog.getChanges()).allMatch(c -> c.getSeverity() != null);

        RiskAssessment risk = helper.getAnalysisService().assessRisk(changelog);
        assertThat(risk).isNotNull();
        assertThat(risk.getLevel()).isNotNull();
        assertThat(risk.getSemverRecommendation()).isNotNull();

        Changelog fullChangelog = helper.getChangelogGenerator().generate(v1, v2);
        assertThat(fullChangelog).isNotNull();
        assertThat(fullChangelog.getApiName()).isEqualTo("User API");
        assertThat(fullChangelog.getFromVersion()).isEqualTo("1.0.0");
        assertThat(fullChangelog.getToVersion()).isEqualTo("2.0.0");
        assertThat(fullChangelog.getChanges()).isNotEmpty();
        assertThat(fullChangelog.getRiskAssessment()).isNotNull();

        for (ReportFormat format : ReportFormat.values()) {
            Reporter reporter = helper.getReporter(format);
            String report = reporter.report(fullChangelog);
            assertThat(report).isNotNull().isNotEmpty();
        }
    }

    @Test
    @DisplayName("Should count endpoints correctly")
    void testEndpointCounting() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        ApiSpec v2Breaking = helper.parseApiSpec("api-v2-breaking.yaml");
        ApiSpec v2Minor = helper.parseApiSpec("api-v2-minor.yaml");

        assertThat(v1.getEndpoints()).hasSize(3);
        assertThat(v2Breaking.getEndpoints()).hasSize(4);
        assertThat(v2Minor.getEndpoints()).hasSize(4);
    }

    @Test
    @DisplayName("Should parse deprecated endpoints correctly")
    void testParseDeprecatedEndpoints() throws Exception {
        ApiSpec v2Minor = helper.parseApiSpec("api-v2-minor.yaml");

        boolean hasDeprecatedEndpoint = v2Minor.getEndpoints().stream()
                .anyMatch(e -> e.isDeprecated());
        assertThat(hasDeprecatedEndpoint).isTrue();
    }

    @Test
    @DisplayName("Should print sample console report")
    void testPrintSampleReport() throws Exception {
        ApiSpec v1 = helper.parseApiSpec("api-v1.yaml");
        ApiSpec v2 = helper.parseApiSpec("api-v2-breaking.yaml");

        Changelog changelog = helper.getChangelogGenerator().generate(v1, v2);
        Reporter consoleReporter = helper.getReporter(ReportFormat.CONSOLE);
        String report = consoleReporter.report(changelog);

        System.out.println("\n========== SAMPLE CONSOLE REPORT ==========");
        System.out.println(report);
        System.out.println("============================================\n");

        assertThat(report).isNotEmpty();
    }
}
