package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.analytics.model.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ComplianceReportTest {

    @Test
    void builder_shouldCreateReport() {
        ComplianceReport report = ComplianceReport.builder()
                .apiName("Test API")
                .build();

        assertThat(report.getApiName()).isEqualTo("Test API");
    }

    @Test
    void builder_shouldSetOverallStatus() {
        ComplianceStatus status = ComplianceStatus.builder()
                .status(ComplianceStatus.Status.COMPLIANT)
                .complianceScore(95)
                .build();

        ComplianceReport report = ComplianceReport.builder()
                .apiName("Test API")
                .overallStatus(status)
                .build();

        assertThat(report.getOverallStatus()).isEqualTo(status);
    }

    @Test
    void builder_shouldSetChecks() {
        List<ComplianceReport.ComplianceCheck> checks = Arrays.asList(
                new ComplianceReport.ComplianceCheck("API Version", "Versioning",
                        ComplianceReport.ComplianceCheck.CheckStatus.PASSED, "Version specified"),
                new ComplianceReport.ComplianceCheck("Documentation", "Documentation",
                        ComplianceReport.ComplianceCheck.CheckStatus.FAILED, "Missing descriptions")
        );

        ComplianceReport report = ComplianceReport.builder()
                .apiName("Test API")
                .checks(checks)
                .build();

        assertThat(report.getChecks()).hasSize(2);
        assertThat(report.getPassedChecks()).isEqualTo(1);
        assertThat(report.getFailedChecks()).isEqualTo(1);
    }

    @Test
    void builder_shouldSetViolations() {
        List<ComplianceReport.ComplianceViolation> violations = Arrays.asList(
                new ComplianceReport.ComplianceViolation("VERSION_REQUIRED", "ERROR",
                        "API Specification", "Version must be specified")
        );

        ComplianceReport report = ComplianceReport.builder()
                .apiName("Test API")
                .violations(violations)
                .build();

        assertThat(report.getViolations()).hasSize(1);
    }

    @Test
    void getCompliancePercentage_shouldCalculateCorrectly() {
        List<ComplianceReport.ComplianceCheck> checks = Arrays.asList(
                new ComplianceReport.ComplianceCheck("Check1", "Cat1",
                        ComplianceReport.ComplianceCheck.CheckStatus.PASSED, "OK"),
                new ComplianceReport.ComplianceCheck("Check2", "Cat1",
                        ComplianceReport.ComplianceCheck.CheckStatus.PASSED, "OK"),
                new ComplianceReport.ComplianceCheck("Check3", "Cat1",
                        ComplianceReport.ComplianceCheck.CheckStatus.FAILED, "Failed"),
                new ComplianceReport.ComplianceCheck("Check4", "Cat1",
                        ComplianceReport.ComplianceCheck.CheckStatus.WARNING, "Warning")
        );

        ComplianceReport report = ComplianceReport.builder()
                .apiName("Test API")
                .checks(checks)
                .build();

        assertThat(report.getCompliancePercentage()).isCloseTo(50.0, within(0.1));
    }

    @Test
    void getCompliancePercentage_withNoChecks_shouldReturn100() {
        ComplianceReport report = ComplianceReport.builder()
                .apiName("Test API")
                .build();

        assertThat(report.getCompliancePercentage()).isEqualTo(100.0);
    }

    @Test
    void complianceCheck_shouldStoreAllProperties() {
        ComplianceReport.ComplianceCheck check = new ComplianceReport.ComplianceCheck();
        check.setName("API Version");
        check.setCategory("Versioning");
        check.setStatus(ComplianceReport.ComplianceCheck.CheckStatus.PASSED);
        check.setMessage("Version specified");
        check.setDetails("Version 1.0.0");

        assertThat(check.getName()).isEqualTo("API Version");
        assertThat(check.getCategory()).isEqualTo("Versioning");
        assertThat(check.getStatus()).isEqualTo(ComplianceReport.ComplianceCheck.CheckStatus.PASSED);
        assertThat(check.getMessage()).isEqualTo("Version specified");
        assertThat(check.getDetails()).isEqualTo("Version 1.0.0");
    }

    @Test
    void complianceViolation_shouldStoreAllProperties() {
        ComplianceReport.ComplianceViolation violation = new ComplianceReport.ComplianceViolation();
        violation.setRule("VERSION_REQUIRED");
        violation.setSeverity("ERROR");
        violation.setLocation("API Specification");
        violation.setDescription("Version must be specified");
        violation.setRemediation("Add version to OpenAPI spec");

        assertThat(violation.getRule()).isEqualTo("VERSION_REQUIRED");
        assertThat(violation.getSeverity()).isEqualTo("ERROR");
        assertThat(violation.getLocation()).isEqualTo("API Specification");
        assertThat(violation.getDescription()).isEqualTo("Version must be specified");
        assertThat(violation.getRemediation()).isEqualTo("Add version to OpenAPI spec");
    }

    @Test
    void toMarkdown_shouldGenerateValidMarkdown() {
        ComplianceStatus status = ComplianceStatus.builder()
                .status(ComplianceStatus.Status.PARTIAL)
                .build();

        ComplianceReport report = ComplianceReport.builder()
                .apiName("Test API")
                .overallStatus(status)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("# Compliance Report: Test API");
        assertThat(markdown).contains("## Summary");
    }

    @Test
    void toMarkdown_shouldIncludeViolations() {
        List<ComplianceReport.ComplianceViolation> violations = Arrays.asList(
                new ComplianceReport.ComplianceViolation("VERSION_REQUIRED", "ERROR",
                        "API Specification", "Version must be specified")
        );

        ComplianceReport report = ComplianceReport.builder()
                .apiName("Test API")
                .violations(violations)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("## Violations");
        assertThat(markdown).contains("VERSION_REQUIRED");
    }

    @Test
    void toMarkdown_shouldIncludeCheckResults() {
        List<ComplianceReport.ComplianceCheck> checks = Arrays.asList(
                new ComplianceReport.ComplianceCheck("API Version", "Versioning",
                        ComplianceReport.ComplianceCheck.CheckStatus.PASSED, "OK")
        );

        ComplianceReport report = ComplianceReport.builder()
                .apiName("Test API")
                .checks(checks)
                .build();

        String markdown = report.toMarkdown();

        assertThat(markdown).contains("## Check Results");
        assertThat(markdown).contains("API Version");
        assertThat(markdown).contains("PASS");
    }

    @Test
    void checkStatus_shouldHaveAllStatuses() {
        assertThat(ComplianceReport.ComplianceCheck.CheckStatus.values())
                .containsExactlyInAnyOrder(
                        ComplianceReport.ComplianceCheck.CheckStatus.PASSED,
                        ComplianceReport.ComplianceCheck.CheckStatus.FAILED,
                        ComplianceReport.ComplianceCheck.CheckStatus.WARNING,
                        ComplianceReport.ComplianceCheck.CheckStatus.SKIPPED
                );
    }

    @Test
    void generatedAt_shouldBeSetAutomatically() {
        ComplianceReport report = new ComplianceReport();
        assertThat(report.getGeneratedAt()).isNotNull();
    }
}
