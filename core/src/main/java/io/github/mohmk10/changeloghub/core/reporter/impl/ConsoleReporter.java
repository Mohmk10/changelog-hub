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
import java.util.List;
import java.util.stream.Collectors;

public class ConsoleReporter implements Reporter {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String ORANGE = "\u001B[38;5;208m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";
    private static final String CYAN = "\u001B[36m";

    private static final String SYMBOL_BREAKING = "✗";
    private static final String SYMBOL_DANGEROUS = "⚠";
    private static final String SYMBOL_WARNING = "⚡";
    private static final String SYMBOL_INFO = "✓";

    @Override
    public String report(Changelog changelog) {
        if (changelog == null) {
            return "No changelog data available\n";
        }

        StringBuilder sb = new StringBuilder();

        appendHeader(sb, changelog);
        appendSummary(sb, changelog);
        appendBreakingChanges(sb, changelog);
        appendDangerousChanges(sb, changelog);
        appendWarnings(sb, changelog);
        appendInfoChanges(sb, changelog);
        appendFooter(sb);

        return sb.toString();
    }

    @Override
    public void reportToFile(Changelog changelog, Path outputPath) {
        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
            String reportWithoutColors = stripAnsiCodes(report(changelog));
            Files.writeString(outputPath, reportWithoutColors);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report to file: " + outputPath, e);
        }
    }

    private void appendHeader(StringBuilder sb, Changelog changelog) {
        sb.append("\n");
        sb.append(BOLD).append(CYAN).append("═══════════════════════════════════════════════════════════════").append(RESET).append("\n");
        sb.append(BOLD).append("  CHANGELOG: ").append(nullSafe(changelog.getApiName())).append(RESET).append("\n");
        sb.append(DIM).append("  ").append(nullSafe(changelog.getFromVersion()))
          .append(" → ").append(nullSafe(changelog.getToVersion())).append(RESET).append("\n");
        sb.append(BOLD).append(CYAN).append("═══════════════════════════════════════════════════════════════").append(RESET).append("\n\n");
    }

    private void appendSummary(StringBuilder sb, Changelog changelog) {
        RiskAssessment risk = changelog.getRiskAssessment();
        int total = changelog.getChanges() != null ? changelog.getChanges().size() : 0;
        int breaking = changelog.getBreakingChanges() != null ? changelog.getBreakingChanges().size() : 0;

        sb.append(BOLD).append("  SUMMARY").append(RESET).append("\n");
        sb.append("  ─────────────────────────────────────────────────────────────\n");
        sb.append("  Total changes:    ").append(total).append("\n");
        sb.append("  Breaking changes: ").append(breaking > 0 ? RED + breaking + RESET : breaking).append("\n");

        if (risk != null) {
            String levelColor = getLevelColor(risk.getLevel().name());
            sb.append("  Risk level:       ").append(levelColor).append(risk.getLevel()).append(RESET)
              .append(" (").append(risk.getOverallScore()).append("/100)\n");

            String semverColor = getSemverColor(risk.getSemverRecommendation());
            sb.append("  Version bump:     ").append(semverColor).append(risk.getSemverRecommendation()).append(RESET).append("\n");
        }

        sb.append("\n");
    }

    private void appendBreakingChanges(StringBuilder sb, Changelog changelog) {
        List<BreakingChange> changes = changelog.getBreakingChanges();
        if (changes == null || changes.isEmpty()) return;

        sb.append(BOLD).append(RED).append("  ").append(SYMBOL_BREAKING).append(" BREAKING CHANGES (").append(changes.size()).append(")").append(RESET).append("\n");
        sb.append("  ─────────────────────────────────────────────────────────────\n");

        for (BreakingChange change : changes) {
            sb.append(RED).append("  ").append(SYMBOL_BREAKING).append(" ").append(RESET);
            sb.append(BOLD).append(nullSafe(change.getPath())).append(RESET).append("\n");
            sb.append("    ").append(DIM).append(change.getType()).append(" | Impact: ").append(change.getImpactScore()).append("/100").append(RESET).append("\n");
            sb.append("    ").append(nullSafe(change.getDescription())).append("\n");
            if (change.getMigrationSuggestion() != null) {
                sb.append("    ").append(YELLOW).append("→ ").append(change.getMigrationSuggestion()).append(RESET).append("\n");
            }
            sb.append("\n");
        }
    }

    private void appendDangerousChanges(StringBuilder sb, Changelog changelog) {
        List<Change> changes = filterBySeverity(changelog.getChanges(), Severity.DANGEROUS);
        if (changes.isEmpty()) return;

        sb.append(BOLD).append(ORANGE).append("  ").append(SYMBOL_DANGEROUS).append(" DANGEROUS CHANGES (").append(changes.size()).append(")").append(RESET).append("\n");
        sb.append("  ─────────────────────────────────────────────────────────────\n");

        for (Change change : changes) {
            sb.append(ORANGE).append("  ").append(SYMBOL_DANGEROUS).append(" ").append(RESET);
            sb.append(nullSafe(change.getPath())).append("\n");
            sb.append("    ").append(nullSafe(change.getDescription())).append("\n\n");
        }
    }

    private void appendWarnings(StringBuilder sb, Changelog changelog) {
        List<Change> changes = filterBySeverity(changelog.getChanges(), Severity.WARNING);
        if (changes.isEmpty()) return;

        sb.append(BOLD).append(YELLOW).append("  ").append(SYMBOL_WARNING).append(" WARNINGS (").append(changes.size()).append(")").append(RESET).append("\n");
        sb.append("  ─────────────────────────────────────────────────────────────\n");

        for (Change change : changes) {
            sb.append(YELLOW).append("  ").append(SYMBOL_WARNING).append(" ").append(RESET);
            sb.append(nullSafe(change.getPath())).append("\n");
            sb.append("    ").append(nullSafe(change.getDescription())).append("\n\n");
        }
    }

    private void appendInfoChanges(StringBuilder sb, Changelog changelog) {
        List<Change> changes = filterBySeverity(changelog.getChanges(), Severity.INFO);
        if (changes.isEmpty()) return;

        sb.append(BOLD).append(GREEN).append("  ").append(SYMBOL_INFO).append(" ADDITIONS & INFO (").append(changes.size()).append(")").append(RESET).append("\n");
        sb.append("  ─────────────────────────────────────────────────────────────\n");

        for (Change change : changes) {
            sb.append(GREEN).append("  ").append(SYMBOL_INFO).append(" ").append(RESET);
            sb.append(nullSafe(change.getPath())).append("\n");
            sb.append("    ").append(nullSafe(change.getDescription())).append("\n\n");
        }
    }

    private void appendFooter(StringBuilder sb) {
        sb.append(DIM).append("  ─────────────────────────────────────────────────────────────").append(RESET).append("\n");
        sb.append(DIM).append("  Generated by Changelog Hub").append(RESET).append("\n\n");
    }

    private List<Change> filterBySeverity(List<Change> changes, Severity severity) {
        if (changes == null) return List.of();
        return changes.stream()
                .filter(c -> c.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    private String getLevelColor(String level) {
        switch (level) {
            case "CRITICAL": return RED + BOLD;
            case "HIGH": return RED;
            case "MEDIUM": return YELLOW;
            case "LOW": return GREEN;
            default: return "";
        }
    }

    private String getSemverColor(String semver) {
        switch (semver) {
            case "MAJOR": return RED + BOLD;
            case "MINOR": return YELLOW;
            case "PATCH": return GREEN;
            default: return "";
        }
    }

    private String nullSafe(String value) {
        return value != null ? value : "N/A";
    }

    private String stripAnsiCodes(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}
