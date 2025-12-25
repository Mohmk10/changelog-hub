package io.github.mohmk10.changeloghub.core.reporter.impl;

import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.RiskAssessment;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.core.reporter.Reporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MarkdownReporter implements Reporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String report(Changelog changelog) {
        if (changelog == null) {
            return "# No changelog data available\n";
        }

        StringBuilder sb = new StringBuilder();

        appendHeader(sb, changelog);
        appendSummary(sb, changelog);
        appendBreakingChanges(sb, changelog);
        appendDangerousChanges(sb, changelog);
        appendWarnings(sb, changelog);
        appendAdditions(sb, changelog);
        appendOtherChanges(sb, changelog);

        return sb.toString();
    }

    @Override
    public void reportToFile(Changelog changelog, Path outputPath) {
        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
            Files.writeString(outputPath, report(changelog));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report to file: " + outputPath, e);
        }
    }

    private void appendHeader(StringBuilder sb, Changelog changelog) {
        sb.append("# Changelog: ").append(nullSafe(changelog.getApiName())).append("\n\n");
        sb.append("**").append(nullSafe(changelog.getFromVersion()))
          .append(" → ").append(nullSafe(changelog.getToVersion())).append("**\n\n");

        if (changelog.getGeneratedAt() != null) {
            sb.append("*Generated: ").append(changelog.getGeneratedAt().format(DATE_FORMATTER)).append("*\n\n");
        }

        sb.append("---\n\n");
    }

    private void appendSummary(StringBuilder sb, Changelog changelog) {
        sb.append("## Summary\n\n");

        RiskAssessment risk = changelog.getRiskAssessment();
        int totalChanges = changelog.getChanges() != null ? changelog.getChanges().size() : 0;
        int breakingChanges = changelog.getBreakingChanges() != null ? changelog.getBreakingChanges().size() : 0;

        sb.append("| Metric | Value |\n");
        sb.append("|--------|-------|\n");
        sb.append("| Total changes | ").append(totalChanges).append(" |\n");
        sb.append("| Breaking changes | ").append(breakingChanges).append(" |\n");

        if (risk != null) {
            sb.append("| Risk level | ").append(risk.getLevel()).append(" |\n");
            sb.append("| Risk score | ").append(risk.getOverallScore()).append("/100 |\n");
            sb.append("| Recommended version bump | **").append(risk.getSemverRecommendation()).append("** |\n");
        }

        sb.append("\n");
    }

    private void appendBreakingChanges(StringBuilder sb, Changelog changelog) {
        List<BreakingChange> breakingChanges = changelog.getBreakingChanges();
        if (breakingChanges == null || breakingChanges.isEmpty()) {
            return;
        }

        sb.append("## 🔴 Breaking Changes\n\n");

        for (BreakingChange change : breakingChanges) {
            sb.append("### `").append(nullSafe(change.getPath())).append("`\n\n");
            sb.append("- **Type:** ").append(change.getType()).append("\n");
            sb.append("- **Category:** ").append(change.getCategory()).append("\n");
            sb.append("- **Description:** ").append(nullSafe(change.getDescription())).append("\n");
            sb.append("- **Impact Score:** ").append(change.getImpactScore()).append("/100\n");

            if (change.getMigrationSuggestion() != null) {
                sb.append("- **Migration:** ").append(change.getMigrationSuggestion()).append("\n");
            }

            sb.append("\n");
        }
    }

    private void appendDangerousChanges(StringBuilder sb, Changelog changelog) {
        List<Change> dangerous = filterBySeverity(changelog.getChanges(), Severity.DANGEROUS);
        if (dangerous.isEmpty()) {
            return;
        }

        sb.append("## 🟠 Dangerous Changes\n\n");
        appendChangeList(sb, dangerous);
    }

    private void appendWarnings(StringBuilder sb, Changelog changelog) {
        List<Change> warnings = filterBySeverity(changelog.getChanges(), Severity.WARNING);
        if (warnings.isEmpty()) {
            return;
        }

        sb.append("## 🟡 Warnings\n\n");
        appendChangeList(sb, warnings);
    }

    private void appendAdditions(StringBuilder sb, Changelog changelog) {
        List<Change> additions = changelog.getChanges() != null
                ? changelog.getChanges().stream()
                    .filter(c -> c.getSeverity() == Severity.INFO && c.getType() != null && c.getType().name().equals("ADDED"))
                    .collect(Collectors.toList())
                : List.of();

        if (additions.isEmpty()) {
            return;
        }

        sb.append("## 🟢 Additions\n\n");
        appendChangeList(sb, additions);
    }

    private void appendOtherChanges(StringBuilder sb, Changelog changelog) {
        List<Change> others = changelog.getChanges() != null
                ? changelog.getChanges().stream()
                    .filter(c -> c.getSeverity() == Severity.INFO && (c.getType() == null || !c.getType().name().equals("ADDED")))
                    .collect(Collectors.toList())
                : List.of();

        if (others.isEmpty()) {
            return;
        }

        sb.append("## ℹ️ Other Changes\n\n");
        appendChangeList(sb, others);
    }

    private void appendChangeList(StringBuilder sb, List<Change> changes) {
        for (Change change : changes) {
            sb.append("- **").append(nullSafe(change.getPath())).append("**: ");
            sb.append(nullSafe(change.getDescription()));
            sb.append(" *(").append(change.getType()).append(")*\n");
        }
        sb.append("\n");
    }

    private List<Change> filterBySeverity(List<Change> changes, Severity severity) {
        if (changes == null) {
            return List.of();
        }
        return changes.stream()
                .filter(c -> c.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    private String nullSafe(String value) {
        return value != null ? value : "N/A";
    }
}
