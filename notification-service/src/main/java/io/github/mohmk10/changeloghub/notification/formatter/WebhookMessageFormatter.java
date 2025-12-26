package io.github.mohmk10.changeloghub.notification.formatter;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationEvent;
import io.github.mohmk10.changeloghub.notification.util.NotificationConstants;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WebhookMessageFormatter implements MessageFormatter {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_INSTANT;

    @Override
    public String format(Notification notification) {
        if (notification.hasChangelog()) {
            return format(notification.getChangelog());
        }

        StringBuilder json = new StringBuilder();
        json.append("{");

        json.append("\"id\": \"").append(escapeJson(notification.getId())).append("\",");
        json.append("\"type\": \"notification\",");
        json.append("\"timestamp\": \"").append(ISO_FORMAT.format(notification.getCreatedAt())).append("\",");
        json.append("\"title\": \"").append(escapeJson(notification.getTitle())).append("\",");
        json.append("\"message\": \"").append(escapeJson(notification.getMessage())).append("\",");
        json.append("\"severity\": \"").append(notification.getSeverity()).append("\",");

        NotificationEvent event = notification.getEvent();
        if (event != null) {
            json.append("\"event\": {");
            json.append("\"type\": \"").append(event.getEventType()).append("\",");
            if (event.getApiName() != null) {
                json.append("\"apiName\": \"").append(escapeJson(event.getApiName())).append("\",");
            }
            if (event.getFromVersion() != null) {
                json.append("\"fromVersion\": \"").append(escapeJson(event.getFromVersion())).append("\",");
            }
            if (event.getToVersion() != null) {
                json.append("\"toVersion\": \"").append(escapeJson(event.getToVersion())).append("\",");
            }
            json.append("\"critical\": ").append(event.isCritical());
            json.append("},");
        }

        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }

        json.append("}");

        return json.toString();
    }

    @Override
    public String format(Changelog changelog) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        json.append("\"type\": \"changelog\",");
        json.append("\"timestamp\": \"").append(ISO_FORMAT.format(Instant.now())).append("\",");

        if (changelog.getApiName() != null) {
            json.append("\"apiName\": \"").append(escapeJson(changelog.getApiName())).append("\",");
        }
        if (changelog.getFromVersion() != null) {
            json.append("\"fromVersion\": \"").append(escapeJson(changelog.getFromVersion())).append("\",");
        }
        if (changelog.getToVersion() != null) {
            json.append("\"toVersion\": \"").append(escapeJson(changelog.getToVersion())).append("\",");
        }

        json.append("\"summary\": {");
        json.append("\"totalChanges\": ").append(changelog.getChanges().size()).append(",");
        json.append("\"breakingChanges\": ").append(changelog.getBreakingChanges().size()).append(",");

        List<Change> changes = changelog.getChanges();
        long dangerousCount = changes.stream().filter(c -> c.getSeverity() == Severity.DANGEROUS).count();
        long warningCount = changes.stream().filter(c -> c.getSeverity() == Severity.WARNING).count();
        long infoCount = changes.stream().filter(c -> c.getSeverity() == Severity.INFO).count();

        json.append("\"dangerousChanges\": ").append(dangerousCount).append(",");
        json.append("\"warnings\": ").append(warningCount).append(",");
        json.append("\"info\": ").append(infoCount);
        json.append("},");

        json.append("\"breakingChanges\": [");
        List<BreakingChange> breakingChanges = changelog.getBreakingChanges();
        for (int i = 0; i < breakingChanges.size(); i++) {
            if (i > 0) json.append(",");
            BreakingChange bc = breakingChanges.get(i);
            json.append("{");
            json.append("\"description\": \"").append(escapeJson(bc.getDescription())).append("\"");
            if (bc.getCategory() != null) {
                json.append(",\"category\": \"").append(bc.getCategory()).append("\"");
            }
            if (bc.getPath() != null) {
                json.append(",\"path\": \"").append(escapeJson(bc.getPath())).append("\"");
            }
            if (bc.getMigrationSuggestion() != null) {
                json.append(",\"migrationSuggestion\": \"")
                    .append(escapeJson(bc.getMigrationSuggestion())).append("\"");
            }
            json.append("}");
        }
        json.append("],");

        json.append("\"changes\": [");
        for (int i = 0; i < changes.size(); i++) {
            if (i > 0) json.append(",");
            Change change = changes.get(i);
            json.append("{");
            json.append("\"description\": \"").append(escapeJson(change.getDescription())).append("\",");
            json.append("\"type\": \"").append(change.getType()).append("\",");
            json.append("\"severity\": \"").append(change.getSeverity()).append("\"");
            if (change.getPath() != null) {
                json.append(",\"path\": \"").append(escapeJson(change.getPath())).append("\"");
            }
            json.append("}");
        }
        json.append("]");

        json.append("}");

        return json.toString();
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
