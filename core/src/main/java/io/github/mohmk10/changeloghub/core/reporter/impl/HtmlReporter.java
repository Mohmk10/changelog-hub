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

public class HtmlReporter implements Reporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String report(Changelog changelog) {
        if (changelog == null) {
            return createEmptyHtml();
        }

        StringBuilder sb = new StringBuilder();
        appendHtmlHeader(sb, changelog);
        appendBody(sb, changelog);
        appendHtmlFooter(sb);

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

    private String createEmptyHtml() {
        return "<!DOCTYPE html><html><head><title>No Data</title></head><body><h1>No changelog data available</h1></body></html>";
    }

    private void appendHtmlHeader(StringBuilder sb, Changelog changelog) {
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("  <title>Changelog: ").append(escapeHtml(changelog.getApiName())).append("</title>\n");
        appendStyles(sb);
        sb.append("</head>\n");
        sb.append("<body>\n");
    }

    private void appendStyles(StringBuilder sb) {
        sb.append("  <style>\n");
        sb.append("    * { box-sizing: border-box; margin: 0; padding: 0; }\n");
        sb.append("    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 1200px; margin: 0 auto; padding: 20px; background: #f5f5f5; }\n");
        sb.append("    .container { background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 30px; }\n");
        sb.append("    h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; margin-bottom: 20px; }\n");
        sb.append("    h2 { color: #34495e; margin: 25px 0 15px; cursor: pointer; }\n");
        sb.append("    h2:hover { color: #3498db; }\n");
        sb.append("    .version { color: #7f8c8d; font-size: 1.2em; margin-bottom: 10px; }\n");
        sb.append("    .date { color: #95a5a6; font-size: 0.9em; }\n");
        sb.append("    .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin: 20px 0; }\n");
        sb.append("    .summary-card { background: #ecf0f1; padding: 15px; border-radius: 6px; text-align: center; }\n");
        sb.append("    .summary-card .value { font-size: 2em; font-weight: bold; color: #2c3e50; }\n");
        sb.append("    .summary-card .label { color: #7f8c8d; font-size: 0.9em; }\n");
        sb.append("    .badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 0.85em; font-weight: 600; }\n");
        sb.append("    .badge-breaking { background: #e74c3c; color: white; }\n");
        sb.append("    .badge-dangerous { background: #e67e22; color: white; }\n");
        sb.append("    .badge-warning { background: #f1c40f; color: #333; }\n");
        sb.append("    .badge-info { background: #27ae60; color: white; }\n");
        sb.append("    .badge-major { background: #e74c3c; color: white; }\n");
        sb.append("    .badge-minor { background: #f39c12; color: white; }\n");
        sb.append("    .badge-patch { background: #27ae60; color: white; }\n");
        sb.append("    .section { margin: 20px 0; border-left: 4px solid #3498db; padding-left: 15px; }\n");
        sb.append("    .section.breaking { border-color: #e74c3c; }\n");
        sb.append("    .section.dangerous { border-color: #e67e22; }\n");
        sb.append("    .section.warning { border-color: #f1c40f; }\n");
        sb.append("    .section.info { border-color: #27ae60; }\n");
        sb.append("    .change-list { list-style: none; }\n");
        sb.append("    .change-item { background: #fafafa; margin: 10px 0; padding: 15px; border-radius: 6px; border: 1px solid #eee; }\n");
        sb.append("    .change-path { font-family: monospace; font-weight: bold; color: #2c3e50; }\n");
        sb.append("    .change-desc { margin: 8px 0; color: #555; }\n");
        sb.append("    .change-meta { font-size: 0.85em; color: #7f8c8d; }\n");
        sb.append("    .migration { background: #fff3cd; padding: 10px; border-radius: 4px; margin-top: 10px; border-left: 3px solid #ffc107; }\n");
        sb.append("    table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
        sb.append("    th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        sb.append("    th { background: #34495e; color: white; }\n");
        sb.append("    tr:hover { background: #f5f5f5; }\n");
        sb.append("    .collapsible { display: none; }\n");
        sb.append("    .collapsible.active { display: block; }\n");
        sb.append("  </style>\n");
    }

    private void appendBody(StringBuilder sb, Changelog changelog) {
        sb.append("<div class=\"container\">\n");

        appendTitle(sb, changelog);
        appendSummaryCards(sb, changelog);
        appendBreakingSection(sb, changelog);
        appendDangerousSection(sb, changelog);
        appendWarningSection(sb, changelog);
        appendInfoSection(sb, changelog);
        appendChangesTable(sb, changelog);

        sb.append("</div>\n");
        appendScript(sb);
    }

    private void appendTitle(StringBuilder sb, Changelog changelog) {
        sb.append("  <h1>Changelog: ").append(escapeHtml(changelog.getApiName())).append("</h1>\n");
        sb.append("  <p class=\"version\">").append(escapeHtml(changelog.getFromVersion()))
          .append(" → ").append(escapeHtml(changelog.getToVersion())).append("</p>\n");

        if (changelog.getGeneratedAt() != null) {
            sb.append("  <p class=\"date\">Generated: ").append(changelog.getGeneratedAt().format(DATE_FORMATTER)).append("</p>\n");
        }
    }

    private void appendSummaryCards(StringBuilder sb, Changelog changelog) {
        RiskAssessment risk = changelog.getRiskAssessment();
        int total = changelog.getChanges() != null ? changelog.getChanges().size() : 0;
        int breaking = changelog.getBreakingChanges() != null ? changelog.getBreakingChanges().size() : 0;

        sb.append("  <div class=\"summary\">\n");
        sb.append("    <div class=\"summary-card\"><div class=\"value\">").append(total).append("</div><div class=\"label\">Total Changes</div></div>\n");
        sb.append("    <div class=\"summary-card\"><div class=\"value\">").append(breaking).append("</div><div class=\"label\">Breaking Changes</div></div>\n");

        if (risk != null) {
            String levelClass = risk.getLevel().name().toLowerCase();
            sb.append("    <div class=\"summary-card\"><div class=\"value\"><span class=\"badge badge-").append(levelClass).append("\">").append(risk.getLevel()).append("</span></div><div class=\"label\">Risk Level</div></div>\n");

            String semverClass = risk.getSemverRecommendation().toLowerCase();
            sb.append("    <div class=\"summary-card\"><div class=\"value\"><span class=\"badge badge-").append(semverClass).append("\">").append(risk.getSemverRecommendation()).append("</span></div><div class=\"label\">Version Bump</div></div>\n");
        }

        sb.append("  </div>\n");
    }

    private void appendBreakingSection(StringBuilder sb, Changelog changelog) {
        List<BreakingChange> changes = changelog.getBreakingChanges();
        if (changes == null || changes.isEmpty()) return;

        sb.append("  <div class=\"section breaking\">\n");
        sb.append("    <h2 onclick=\"toggleSection(this)\">🔴 Breaking Changes (").append(changes.size()).append(")</h2>\n");
        sb.append("    <div class=\"collapsible active\">\n");
        sb.append("      <ul class=\"change-list\">\n");

        for (BreakingChange change : changes) {
            sb.append("        <li class=\"change-item\">\n");
            sb.append("          <div class=\"change-path\">").append(escapeHtml(change.getPath())).append("</div>\n");
            sb.append("          <div class=\"change-desc\">").append(escapeHtml(change.getDescription())).append("</div>\n");
            sb.append("          <div class=\"change-meta\">").append(change.getType()).append(" | Impact: ").append(change.getImpactScore()).append("/100</div>\n");
            if (change.getMigrationSuggestion() != null) {
                sb.append("          <div class=\"migration\"><strong>Migration:</strong> ").append(escapeHtml(change.getMigrationSuggestion())).append("</div>\n");
            }
            sb.append("        </li>\n");
        }

        sb.append("      </ul>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");
    }

    private void appendDangerousSection(StringBuilder sb, Changelog changelog) {
        appendSeveritySection(sb, changelog, Severity.DANGEROUS, "dangerous", "🟠 Dangerous Changes");
    }

    private void appendWarningSection(StringBuilder sb, Changelog changelog) {
        appendSeveritySection(sb, changelog, Severity.WARNING, "warning", "🟡 Warnings");
    }

    private void appendInfoSection(StringBuilder sb, Changelog changelog) {
        appendSeveritySection(sb, changelog, Severity.INFO, "info", "🟢 Additions & Info");
    }

    private void appendSeveritySection(StringBuilder sb, Changelog changelog, Severity severity, String cssClass, String title) {
        List<Change> changes = filterBySeverity(changelog.getChanges(), severity);
        if (changes.isEmpty()) return;

        sb.append("  <div class=\"section ").append(cssClass).append("\">\n");
        sb.append("    <h2 onclick=\"toggleSection(this)\">").append(title).append(" (").append(changes.size()).append(")</h2>\n");
        sb.append("    <div class=\"collapsible active\">\n");
        sb.append("      <ul class=\"change-list\">\n");

        for (Change change : changes) {
            sb.append("        <li class=\"change-item\">\n");
            sb.append("          <div class=\"change-path\">").append(escapeHtml(change.getPath())).append("</div>\n");
            sb.append("          <div class=\"change-desc\">").append(escapeHtml(change.getDescription())).append("</div>\n");
            sb.append("          <div class=\"change-meta\">").append(change.getType()).append(" | ").append(change.getCategory()).append("</div>\n");
            sb.append("        </li>\n");
        }

        sb.append("      </ul>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");
    }

    private void appendChangesTable(StringBuilder sb, Changelog changelog) {
        List<Change> changes = changelog.getChanges();
        if (changes == null || changes.isEmpty()) return;

        sb.append("  <h2>All Changes</h2>\n");
        sb.append("  <table>\n");
        sb.append("    <thead><tr><th>Path</th><th>Type</th><th>Category</th><th>Severity</th><th>Description</th></tr></thead>\n");
        sb.append("    <tbody>\n");

        for (Change change : changes) {
            String severityClass = change.getSeverity() != null ? change.getSeverity().name().toLowerCase() : "info";
            sb.append("      <tr>\n");
            sb.append("        <td><code>").append(escapeHtml(change.getPath())).append("</code></td>\n");
            sb.append("        <td>").append(change.getType()).append("</td>\n");
            sb.append("        <td>").append(change.getCategory()).append("</td>\n");
            sb.append("        <td><span class=\"badge badge-").append(severityClass).append("\">").append(change.getSeverity()).append("</span></td>\n");
            sb.append("        <td>").append(escapeHtml(change.getDescription())).append("</td>\n");
            sb.append("      </tr>\n");
        }

        sb.append("    </tbody>\n");
        sb.append("  </table>\n");
    }

    private void appendScript(StringBuilder sb) {
        sb.append("<script>\n");
        sb.append("function toggleSection(header) {\n");
        sb.append("  var content = header.nextElementSibling;\n");
        sb.append("  content.classList.toggle('active');\n");
        sb.append("}\n");
        sb.append("</script>\n");
    }

    private void appendHtmlFooter(StringBuilder sb) {
        sb.append("</body>\n");
        sb.append("</html>\n");
    }

    private List<Change> filterBySeverity(List<Change> changes, Severity severity) {
        if (changes == null) return List.of();
        return changes.stream()
                .filter(c -> c.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    private String escapeHtml(String value) {
        if (value == null) return "N/A";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
