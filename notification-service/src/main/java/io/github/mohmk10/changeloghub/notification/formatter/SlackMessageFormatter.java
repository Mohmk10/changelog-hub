package io.github.mohmk10.changeloghub.notification.formatter;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.util.NotificationConstants;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SlackMessageFormatter implements MessageFormatter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_INSTANT;

    @Override
    public String format(Notification notification) {
        StringBuilder json = new StringBuilder();

        if (notification.hasChangelog()) {
            return format(notification.getChangelog());
        }

        json.append("{");
        json.append("\"blocks\": [");

        json.append("{\"type\": \"header\", \"text\": {\"type\": \"plain_text\", \"text\": \"")
            .append(escapeJson(notification.getTitle() != null ? notification.getTitle() : "Notification"))
            .append("\"}},");

        json.append("{\"type\": \"section\", \"text\": {\"type\": \"mrkdwn\", \"text\": \"")
            .append(escapeJson(notification.getMessage() != null ? notification.getMessage() : ""))
            .append("\"}}");

        json.append("]");

        json.append(",\"text\": \"")
            .append(escapeJson(notification.getTitle()))
            .append("\"");

        json.append("}");

        return json.toString();
    }

    @Override
    public String format(Changelog changelog) {
        StringBuilder json = new StringBuilder();
        json.append("{\"blocks\": [");

        String emoji = getEmoji(changelog);
        String headerText = emoji + " API Changes Detected";
        if (changelog.getApiName() != null) {
            headerText += ": " + changelog.getApiName();
        }

        json.append("{\"type\": \"header\", \"text\": {\"type\": \"plain_text\", \"text\": \"")
            .append(escapeJson(headerText))
            .append("\", \"emoji\": true}},");

        if (changelog.getFromVersion() != null && changelog.getToVersion() != null) {
            json.append("{\"type\": \"section\", \"text\": {\"type\": \"mrkdwn\", \"text\": \"")
                .append("*Version:* `").append(escapeJson(changelog.getFromVersion()))
                .append("` → `").append(escapeJson(changelog.getToVersion())).append("`")
                .append("\"}},");
        }

        int breakingCount = changelog.getBreakingChanges().size();
        int totalCount = changelog.getChanges().size();

        json.append("{\"type\": \"section\", \"fields\": [");
        json.append("{\"type\": \"mrkdwn\", \"text\": \"*Total Changes:*\\n").append(totalCount).append("\"},");
        json.append("{\"type\": \"mrkdwn\", \"text\": \"*Breaking Changes:*\\n")
            .append(breakingCount > 0 ? ":warning: " : "").append(breakingCount).append("\"}");
        json.append("]},");

        json.append("{\"type\": \"divider\"},");

        List<BreakingChange> breakingChanges = changelog.getBreakingChanges();
        if (!breakingChanges.isEmpty()) {
            json.append("{\"type\": \"section\", \"text\": {\"type\": \"mrkdwn\", \"text\": \"")
                .append("*:rotating_light: Breaking Changes*\"}},");

            int limit = Math.min(breakingChanges.size(), 5);
            for (int i = 0; i < limit; i++) {
                BreakingChange bc = breakingChanges.get(i);
                json.append("{\"type\": \"section\", \"text\": {\"type\": \"mrkdwn\", \"text\": \"")
                    .append("• ").append(escapeJson(bc.getDescription()));
                if (bc.getMigrationSuggestion() != null) {
                    json.append("\\n  _").append(escapeJson(bc.getMigrationSuggestion())).append("_");
                }
                json.append("\"}},");
            }

            if (breakingChanges.size() > 5) {
                json.append("{\"type\": \"context\", \"elements\": [{\"type\": \"mrkdwn\", \"text\": \"")
                    .append("_...and ").append(breakingChanges.size() - 5).append(" more breaking changes_")
                    .append("\"}]},");
            }
        }

        List<Change> changes = changelog.getChanges();
        long dangerousCount = changes.stream()
            .filter(c -> c.getSeverity() == Severity.DANGEROUS)
            .count();
        long warningCount = changes.stream()
            .filter(c -> c.getSeverity() == Severity.WARNING)
            .count();
        long infoCount = changes.stream()
            .filter(c -> c.getSeverity() == Severity.INFO)
            .count();

        if (dangerousCount > 0 || warningCount > 0 || infoCount > 0) {
            json.append("{\"type\": \"context\", \"elements\": [{\"type\": \"mrkdwn\", \"text\": \"");
            if (dangerousCount > 0) {
                json.append(":large_orange_diamond: ").append(dangerousCount).append(" dangerous  ");
            }
            if (warningCount > 0) {
                json.append(":warning: ").append(warningCount).append(" warnings  ");
            }
            if (infoCount > 0) {
                json.append(":information_source: ").append(infoCount).append(" info");
            }
            json.append("\"}]},");
        }

        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }

        json.append("],");

        String color = getColor(changelog);
        json.append("\"attachments\": [{\"color\": \"").append(color).append("\", \"blocks\": []}],");

        json.append("\"text\": \"").append(escapeJson(headerText)).append("\"");

        json.append("}");

        return json.toString();
    }

    private String getEmoji(Changelog changelog) {
        if (!changelog.getBreakingChanges().isEmpty()) {
            return ":rotating_light:";
        }
        boolean hasDangerous = changelog.getChanges().stream()
            .anyMatch(c -> c.getSeverity() == Severity.DANGEROUS);
        if (hasDangerous) {
            return ":warning:";
        }
        return ":memo:";
    }

    private String getColor(Changelog changelog) {
        if (!changelog.getBreakingChanges().isEmpty()) {
            return NotificationConstants.COLOR_BREAKING;
        }
        boolean hasDangerous = changelog.getChanges().stream()
            .anyMatch(c -> c.getSeverity() == Severity.DANGEROUS);
        if (hasDangerous) {
            return NotificationConstants.COLOR_DANGEROUS;
        }
        return NotificationConstants.COLOR_INFO;
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
