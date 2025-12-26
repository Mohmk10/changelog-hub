package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.analytics.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ComplianceReport {

    private String apiName;
    private ComplianceStatus overallStatus;
    private List<ComplianceCheck> checks;
    private List<ComplianceViolation> violations;
    private List<Recommendation> recommendations;
    private int passedChecks;
    private int failedChecks;
    private int warningChecks;
    private LocalDateTime generatedAt;

    public static class ComplianceCheck {
        private String name;
        private String category;
        private CheckStatus status;
        private String message;
        private String details;

        public enum CheckStatus {
            PASSED, FAILED, WARNING, SKIPPED
        }

        public ComplianceCheck() {}

        public ComplianceCheck(String name, String category, CheckStatus status, String message) {
            this.name = name;
            this.category = category;
            this.status = status;
            this.message = message;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public CheckStatus getStatus() { return status; }
        public void setStatus(CheckStatus status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }

    public static class ComplianceViolation {
        private String rule;
        private String severity;
        private String location;
        private String description;
        private String remediation;

        public ComplianceViolation() {}

        public ComplianceViolation(String rule, String severity, String location, String description) {
            this.rule = rule;
            this.severity = severity;
            this.location = location;
            this.description = description;
        }

        public String getRule() { return rule; }
        public void setRule(String rule) { this.rule = rule; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRemediation() { return remediation; }
        public void setRemediation(String remediation) { this.remediation = remediation; }
    }

    public ComplianceReport() {
        this.checks = new ArrayList<>();
        this.violations = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }

    public ComplianceStatus getOverallStatus() { return overallStatus; }
    public void setOverallStatus(ComplianceStatus overallStatus) { this.overallStatus = overallStatus; }

    public List<ComplianceCheck> getChecks() { return checks; }
    public void setChecks(List<ComplianceCheck> checks) {
        this.checks = checks != null ? new ArrayList<>(checks) : new ArrayList<>();
        updateCheckCounts();
    }

    public List<ComplianceViolation> getViolations() { return violations; }
    public void setViolations(List<ComplianceViolation> violations) {
        this.violations = violations != null ? new ArrayList<>(violations) : new ArrayList<>();
    }

    public List<Recommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
    }

    public int getPassedChecks() { return passedChecks; }
    public int getFailedChecks() { return failedChecks; }
    public int getWarningChecks() { return warningChecks; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public double getCompliancePercentage() {
        int total = passedChecks + failedChecks + warningChecks;
        if (total == 0) return 100.0;
        return (double) passedChecks / total * 100;
    }

    private void updateCheckCounts() {
        passedChecks = 0;
        failedChecks = 0;
        warningChecks = 0;
        for (ComplianceCheck check : checks) {
            switch (check.getStatus()) {
                case PASSED -> passedChecks++;
                case FAILED -> failedChecks++;
                case WARNING -> warningChecks++;
                default -> {  }
            }
        }
    }

    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Compliance Report: ").append(apiName).append("\n\n");

        sb.append("## Summary\n\n");
        if (overallStatus != null) {
            sb.append("- **Status:** ").append(overallStatus.getStatus()).append("\n");
            sb.append("- **Compliance Score:** ").append(String.format("%.1f%%", getCompliancePercentage())).append("\n");
        }
        sb.append("- **Passed Checks:** ").append(passedChecks).append("\n");
        sb.append("- **Failed Checks:** ").append(failedChecks).append("\n");
        sb.append("- **Warnings:** ").append(warningChecks).append("\n\n");

        if (!violations.isEmpty()) {
            sb.append("## Violations\n\n");
            for (ComplianceViolation violation : violations) {
                sb.append("### ").append(violation.getRule()).append(" [").append(violation.getSeverity()).append("]\n\n");
                sb.append("- **Location:** ").append(violation.getLocation()).append("\n");
                sb.append("- **Issue:** ").append(violation.getDescription()).append("\n");
                if (violation.getRemediation() != null) {
                    sb.append("- **Fix:** ").append(violation.getRemediation()).append("\n");
                }
                sb.append("\n");
            }
        }

        if (!checks.isEmpty()) {
            sb.append("## Check Results\n\n");
            sb.append("| Check | Category | Status | Message |\n");
            sb.append("|-------|----------|--------|--------|\n");
            for (ComplianceCheck check : checks) {
                String statusIcon = switch (check.getStatus()) {
                    case PASSED -> "PASS";
                    case FAILED -> "FAIL";
                    case WARNING -> "WARN";
                    case SKIPPED -> "SKIP";
                };
                sb.append("| ").append(check.getName())
                        .append(" | ").append(check.getCategory())
                        .append(" | ").append(statusIcon)
                        .append(" | ").append(check.getMessage() != null ? check.getMessage() : "")
                        .append(" |\n");
            }
            sb.append("\n");
        }

        if (!recommendations.isEmpty()) {
            sb.append("## Recommendations\n\n");
            for (Recommendation rec : recommendations) {
                sb.append("- **").append(rec.getTitle()).append(":** ")
                        .append(rec.getAction()).append("\n");
            }
        }

        return sb.toString();
    }

    public static class Builder {
        private final ComplianceReport report = new ComplianceReport();

        public Builder apiName(String apiName) { report.apiName = apiName; return this; }
        public Builder overallStatus(ComplianceStatus status) { report.overallStatus = status; return this; }
        public Builder checks(List<ComplianceCheck> checks) { report.setChecks(checks); return this; }
        public Builder violations(List<ComplianceViolation> violations) { report.setViolations(violations); return this; }
        public Builder recommendations(List<Recommendation> recs) { report.setRecommendations(recs); return this; }
        public ComplianceReport build() { return report; }
    }
}
