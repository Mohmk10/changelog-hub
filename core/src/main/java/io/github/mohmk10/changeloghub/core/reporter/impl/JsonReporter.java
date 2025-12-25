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
import java.util.Map;

public class JsonReporter implements Reporter {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String report(Changelog changelog) {
        if (changelog == null) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        appendField(sb, "apiName", changelog.getApiName(), true);
        appendField(sb, "fromVersion", changelog.getFromVersion(), true);
        appendField(sb, "toVersion", changelog.getToVersion(), true);

        if (changelog.getGeneratedAt() != null) {
            appendField(sb, "generatedAt", changelog.getGeneratedAt().format(ISO_FORMATTER), true);
        }

        appendSummary(sb, changelog);
        appendBreakingChanges(sb, changelog);
        appendChanges(sb, changelog);
        appendRiskAssessment(sb, changelog);

        sb.append("}\n");
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

    private void appendSummary(StringBuilder sb, Changelog changelog) {
        RiskAssessment risk = changelog.getRiskAssessment();
        int totalChanges = changelog.getChanges() != null ? changelog.getChanges().size() : 0;
        int breakingChanges = changelog.getBreakingChanges() != null ? changelog.getBreakingChanges().size() : 0;

        sb.append("  \"summary\": {\n");
        sb.append("    \"totalChanges\": ").append(totalChanges).append(",\n");
        sb.append("    \"breakingChanges\": ").append(breakingChanges);

        if (risk != null) {
            sb.append(",\n");
            sb.append("    \"riskLevel\": \"").append(risk.getLevel()).append("\",\n");
            sb.append("    \"riskScore\": ").append(risk.getOverallScore()).append(",\n");
            sb.append("    \"semverRecommendation\": \"").append(risk.getSemverRecommendation()).append("\"");
        }

        sb.append("\n  },\n");
    }

    private void appendBreakingChanges(StringBuilder sb, Changelog changelog) {
        List<BreakingChange> breakingChanges = changelog.getBreakingChanges();
        sb.append("  \"breakingChanges\": [");

        if (breakingChanges != null && !breakingChanges.isEmpty()) {
            sb.append("\n");
            for (int i = 0; i < breakingChanges.size(); i++) {
                appendBreakingChange(sb, breakingChanges.get(i));
                if (i < breakingChanges.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("  ");
        }

        sb.append("],\n");
    }

    private void appendBreakingChange(StringBuilder sb, BreakingChange change) {
        sb.append("    {\n");
        sb.append("      \"id\": \"").append(escapeJson(change.getId())).append("\",\n");
        sb.append("      \"type\": \"").append(change.getType()).append("\",\n");
        sb.append("      \"category\": \"").append(change.getCategory()).append("\",\n");
        sb.append("      \"severity\": \"").append(change.getSeverity()).append("\",\n");
        sb.append("      \"path\": \"").append(escapeJson(change.getPath())).append("\",\n");
        sb.append("      \"description\": \"").append(escapeJson(change.getDescription())).append("\",\n");
        sb.append("      \"impactScore\": ").append(change.getImpactScore()).append(",\n");
        sb.append("      \"migrationSuggestion\": \"").append(escapeJson(change.getMigrationSuggestion())).append("\"\n");
        sb.append("    }");
    }

    private void appendChanges(StringBuilder sb, Changelog changelog) {
        List<Change> changes = changelog.getChanges();
        sb.append("  \"changes\": [");

        if (changes != null && !changes.isEmpty()) {
            sb.append("\n");
            for (int i = 0; i < changes.size(); i++) {
                appendChange(sb, changes.get(i));
                if (i < changes.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("  ");
        }

        sb.append("],\n");
    }

    private void appendChange(StringBuilder sb, Change change) {
        sb.append("    {\n");
        sb.append("      \"id\": \"").append(escapeJson(change.getId())).append("\",\n");
        sb.append("      \"type\": \"").append(change.getType()).append("\",\n");
        sb.append("      \"category\": \"").append(change.getCategory()).append("\",\n");
        sb.append("      \"severity\": \"").append(change.getSeverity()).append("\",\n");
        sb.append("      \"path\": \"").append(escapeJson(change.getPath())).append("\",\n");
        sb.append("      \"description\": \"").append(escapeJson(change.getDescription())).append("\"\n");
        sb.append("    }");
    }

    private void appendRiskAssessment(StringBuilder sb, Changelog changelog) {
        RiskAssessment risk = changelog.getRiskAssessment();
        sb.append("  \"riskAssessment\": ");

        if (risk == null) {
            sb.append("null\n");
            return;
        }

        sb.append("{\n");
        sb.append("    \"overallScore\": ").append(risk.getOverallScore()).append(",\n");
        sb.append("    \"level\": \"").append(risk.getLevel()).append("\",\n");
        sb.append("    \"breakingChangesCount\": ").append(risk.getBreakingChangesCount()).append(",\n");
        sb.append("    \"totalChangesCount\": ").append(risk.getTotalChangesCount()).append(",\n");
        sb.append("    \"semverRecommendation\": \"").append(risk.getSemverRecommendation()).append("\",\n");
        sb.append("    \"recommendation\": \"").append(escapeJson(risk.getRecommendation())).append("\",\n");

        appendChangesBySeverity(sb, risk.getChangesBySeverity());

        sb.append("  }\n");
    }

    private void appendChangesBySeverity(StringBuilder sb, Map<Severity, Integer> changesBySeverity) {
        sb.append("    \"changesBySeverity\": {");

        if (changesBySeverity != null && !changesBySeverity.isEmpty()) {
            sb.append("\n");
            int count = 0;
            for (Map.Entry<Severity, Integer> entry : changesBySeverity.entrySet()) {
                sb.append("      \"").append(entry.getKey()).append("\": ").append(entry.getValue());
                if (count < changesBySeverity.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
                count++;
            }
            sb.append("    ");
        }

        sb.append("}\n");
    }

    private void appendField(StringBuilder sb, String name, String value, boolean comma) {
        sb.append("  \"").append(name).append("\": ");
        if (value != null) {
            sb.append("\"").append(escapeJson(value)).append("\"");
        } else {
            sb.append("null");
        }
        if (comma) {
            sb.append(",");
        }
        sb.append("\n");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
