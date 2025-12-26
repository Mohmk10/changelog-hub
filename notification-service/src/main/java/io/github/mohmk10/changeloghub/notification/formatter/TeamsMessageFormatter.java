package io.github.mohmk10.changeloghub.notification.formatter;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.util.NotificationConstants;

import java.util.List;

/**
 * Formats messages for Microsoft Teams using Adaptive Cards.
 */
public class TeamsMessageFormatter implements MessageFormatter {

    @Override
    public String format(Notification notification) {
        if (notification.hasChangelog()) {
            return format(notification.getChangelog());
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@type\": \"MessageCard\",");
        json.append("\"@context\": \"http://schema.org/extensions\",");
        json.append("\"themeColor\": \"").append(getThemeColor(notification.getSeverity())).append("\",");
        json.append("\"summary\": \"").append(escapeJson(notification.getTitle())).append("\",");

        json.append("\"sections\": [{");
        json.append("\"activityTitle\": \"").append(escapeJson(notification.getTitle())).append("\",");
        json.append("\"text\": \"").append(escapeJson(notification.getMessage())).append("\"");
        json.append("}]");

        json.append("}");

        return json.toString();
    }

    @Override
    public String format(Changelog changelog) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@type\": \"MessageCard\",");
        json.append("\"@context\": \"http://schema.org/extensions\",");

        // Theme color based on severity
        int breakingCount = changelog.getBreakingChanges().size();
        String themeColor = breakingCount > 0
            ? NotificationConstants.COLOR_BREAKING.substring(1)
            : NotificationConstants.COLOR_INFO.substring(1);
        json.append("\"themeColor\": \"").append(themeColor).append("\",");

        // Summary
        String summary = "API Changes";
        if (changelog.getApiName() != null) {
            summary += ": " + changelog.getApiName();
        }
        json.append("\"summary\": \"").append(escapeJson(summary)).append("\",");

        json.append("\"sections\": [");

        // Header section
        json.append("{");
        json.append("\"activityTitle\": \"").append(escapeJson(summary)).append("\",");

        if (changelog.getFromVersion() != null && changelog.getToVersion() != null) {
            json.append("\"activitySubtitle\": \"Version ")
                .append(escapeJson(changelog.getFromVersion()))
                .append(" → ")
                .append(escapeJson(changelog.getToVersion()))
                .append("\",");
        }

        // Facts
        json.append("\"facts\": [");
        json.append("{\"name\": \"Total Changes\", \"value\": \"").append(changelog.getChanges().size()).append("\"},");
        json.append("{\"name\": \"Breaking Changes\", \"value\": \"").append(breakingCount).append("\"}");
        json.append("],");

        json.append("\"markdown\": true");
        json.append("}");

        // Breaking changes section
        List<BreakingChange> breakingChanges = changelog.getBreakingChanges();
        if (!breakingChanges.isEmpty()) {
            json.append(",{");
            json.append("\"activityTitle\": \"⚠️ Breaking Changes\",");
            json.append("\"facts\": [");

            int limit = Math.min(breakingChanges.size(), 10);
            for (int i = 0; i < limit; i++) {
                if (i > 0) json.append(",");
                BreakingChange bc = breakingChanges.get(i);
                json.append("{");
                json.append("\"name\": \"").append(i + 1).append("\",");
                json.append("\"value\": \"").append(escapeJson(bc.getDescription())).append("\"");
                json.append("}");
            }

            if (breakingChanges.size() > 10) {
                json.append(",{\"name\": \"...\", \"value\": \"and ")
                    .append(breakingChanges.size() - 10)
                    .append(" more\"}");
            }

            json.append("],");
            json.append("\"markdown\": true");
            json.append("}");
        }

        // Summary section
        List<Change> changes = changelog.getChanges();
        long dangerousCount = changes.stream().filter(c -> c.getSeverity() == Severity.DANGEROUS).count();
        long warningCount = changes.stream().filter(c -> c.getSeverity() == Severity.WARNING).count();

        if (dangerousCount > 0 || warningCount > 0) {
            json.append(",{");
            json.append("\"activityTitle\": \"Other Changes\",");
            json.append("\"facts\": [");

            boolean first = true;
            if (dangerousCount > 0) {
                json.append("{\"name\": \"Dangerous\", \"value\": \"").append(dangerousCount).append("\"}");
                first = false;
            }
            if (warningCount > 0) {
                if (!first) json.append(",");
                json.append("{\"name\": \"Warnings\", \"value\": \"").append(warningCount).append("\"}");
            }

            json.append("]");
            json.append("}");
        }

        json.append("],");

        // Potential actions
        json.append("\"potentialAction\": [");
        json.append("{");
        json.append("\"@type\": \"OpenUri\",");
        json.append("\"name\": \"View Details\",");
        json.append("\"targets\": [{\"os\": \"default\", \"uri\": \"https://example.com/changelog\"}]");
        json.append("}");
        json.append("]");

        json.append("}");

        return json.toString();
    }

    private String getThemeColor(Severity severity) {
        if (severity == null) {
            return NotificationConstants.COLOR_INFO.substring(1);
        }
        return switch (severity) {
            case BREAKING -> NotificationConstants.COLOR_BREAKING.substring(1);
            case DANGEROUS -> NotificationConstants.COLOR_DANGEROUS.substring(1);
            case WARNING -> NotificationConstants.COLOR_WARNING.substring(1);
            case INFO -> NotificationConstants.COLOR_INFO.substring(1);
        };
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    @Override
    public String getContentType() {
        return NotificationConstants.CONTENT_TYPE_JSON;
    }
}
