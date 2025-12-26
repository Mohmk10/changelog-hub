package io.github.mohmk10.changeloghub.notification.formatter;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.util.NotificationConstants;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Formats messages for Discord using embeds.
 */
public class DiscordMessageFormatter implements MessageFormatter {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_INSTANT;

    @Override
    public String format(Notification notification) {
        if (notification.hasChangelog()) {
            return format(notification.getChangelog());
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"embeds\": [{");

        // Title
        json.append("\"title\": \"")
            .append(escapeJson(notification.getTitle() != null ? notification.getTitle() : "Notification"))
            .append("\",");

        // Description
        if (notification.getMessage() != null) {
            json.append("\"description\": \"")
                .append(escapeJson(notification.getMessage()))
                .append("\",");
        }

        // Color based on severity
        int color = getColorCode(notification.getSeverity());
        json.append("\"color\": ").append(color).append(",");

        // Timestamp
        json.append("\"timestamp\": \"")
            .append(ISO_FORMAT.format(notification.getCreatedAt()))
            .append("\",");

        // Footer
        json.append("\"footer\": {\"text\": \"ChangelogHub\"}");

        json.append("}]}");

        return json.toString();
    }

    @Override
    public String format(Changelog changelog) {
        StringBuilder json = new StringBuilder();
        json.append("{\"embeds\": [{");

        // Title
        String title = "API Changes Detected";
        if (changelog.getApiName() != null) {
            title += ": " + changelog.getApiName();
        }
        json.append("\"title\": \"").append(escapeJson(title)).append("\",");

        // Description with version
        StringBuilder description = new StringBuilder();
        if (changelog.getFromVersion() != null && changelog.getToVersion() != null) {
            description.append("**Version:** `")
                .append(changelog.getFromVersion())
                .append("` → `")
                .append(changelog.getToVersion())
                .append("`\\n\\n");
        }

        // Summary
        int breakingCount = changelog.getBreakingChanges().size();
        int totalCount = changelog.getChanges().size();

        if (breakingCount > 0) {
            description.append("⚠️ **").append(breakingCount).append(" Breaking Changes**\\n");
        }
        description.append("📝 **").append(totalCount).append(" Total Changes**");

        json.append("\"description\": \"").append(escapeJson(description.toString())).append("\",");

        // Color
        int color = breakingCount > 0
            ? NotificationConstants.DISCORD_COLOR_BREAKING
            : NotificationConstants.DISCORD_COLOR_INFO;
        json.append("\"color\": ").append(color).append(",");

        // Fields for breaking changes
        json.append("\"fields\": [");

        List<BreakingChange> breakingChanges = changelog.getBreakingChanges();
        int limit = Math.min(breakingChanges.size(), NotificationConstants.DISCORD_MAX_EMBED_FIELDS - 1);

        for (int i = 0; i < limit; i++) {
            BreakingChange bc = breakingChanges.get(i);
            if (i > 0) json.append(",");

            json.append("{");
            json.append("\"name\": \"🔴 Breaking Change\",");

            String value = bc.getDescription();
            if (value.length() > NotificationConstants.DISCORD_MAX_FIELD_VALUE_LENGTH) {
                value = value.substring(0, NotificationConstants.DISCORD_MAX_FIELD_VALUE_LENGTH - 3) + "...";
            }
            json.append("\"value\": \"").append(escapeJson(value)).append("\",");
            json.append("\"inline\": false");
            json.append("}");
        }

        // Add summary of other changes
        long dangerousCount = changelog.getChanges().stream()
            .filter(c -> c.getSeverity() == Severity.DANGEROUS)
            .count();
        long warningCount = changelog.getChanges().stream()
            .filter(c -> c.getSeverity() == Severity.WARNING)
            .count();

        if (dangerousCount > 0 || warningCount > 0 || breakingChanges.size() > limit) {
            if (limit > 0) json.append(",");
            json.append("{");
            json.append("\"name\": \"Summary\",");

            StringBuilder summary = new StringBuilder();
            if (breakingChanges.size() > limit) {
                summary.append("...and ").append(breakingChanges.size() - limit).append(" more breaking changes\\n");
            }
            if (dangerousCount > 0) {
                summary.append("🟠 ").append(dangerousCount).append(" dangerous changes\\n");
            }
            if (warningCount > 0) {
                summary.append("🟡 ").append(warningCount).append(" warnings");
            }

            json.append("\"value\": \"").append(escapeJson(summary.toString())).append("\",");
            json.append("\"inline\": false");
            json.append("}");
        }

        json.append("],");

        // Timestamp
        json.append("\"timestamp\": \"").append(ISO_FORMAT.format(Instant.now())).append("\",");

        // Footer
        json.append("\"footer\": {\"text\": \"ChangelogHub\"}");

        json.append("}]}");

        return json.toString();
    }

    private int getColorCode(Severity severity) {
        if (severity == null) {
            return NotificationConstants.DISCORD_COLOR_INFO;
        }
        return switch (severity) {
            case BREAKING -> NotificationConstants.DISCORD_COLOR_BREAKING;
            case DANGEROUS -> NotificationConstants.DISCORD_COLOR_DANGEROUS;
            case WARNING -> NotificationConstants.DISCORD_COLOR_WARNING;
            case INFO -> NotificationConstants.DISCORD_COLOR_INFO;
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
