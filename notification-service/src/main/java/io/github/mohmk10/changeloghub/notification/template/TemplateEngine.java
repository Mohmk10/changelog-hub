package io.github.mohmk10.changeloghub.notification.template;

import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationEvent;
import io.github.mohmk10.changeloghub.notification.util.NotificationConstants;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateEngine {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern CONDITIONAL_PATTERN =
        Pattern.compile("\\{\\{#if\\s+(\\w+)}}(.+?)\\{\\{/if}}", Pattern.DOTALL);

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public String process(String template, Map<String, Object> variables) {
        if (template == null) return "";

        String result = template;

        result = processConditionals(result, variables);

        result = replaceVariables(result, variables);

        return result;
    }

    public String process(String template, Notification notification) {
        Map<String, Object> variables = buildVariables(notification);
        return process(template, variables);
    }

    public String process(String template, Changelog changelog) {
        Map<String, Object> variables = buildVariables(changelog);
        return process(template, variables);
    }

    public Map<String, Object> buildVariables(Notification notification) {
        Map<String, Object> vars = new HashMap<>();

        vars.put("title", notification.getTitle());
        vars.put("message", notification.getMessage());
        vars.put("severity", notification.getSeverity() != null ? notification.getSeverity().name() : "INFO");
        vars.put("timestamp", DATE_FORMAT.format(notification.getCreatedAt()));

        NotificationEvent event = notification.getEvent();
        if (event != null) {
            vars.put("eventType", event.getEventType().getDescription());
            vars.put("apiName", event.getApiName());
            vars.put("fromVersion", event.getFromVersion());
            vars.put("toVersion", event.getToVersion());
            vars.put("breakingCount", event.getBreakingChangesCount());
            vars.put("totalChanges", event.getTotalChangesCount());
            vars.put("critical", event.isCritical());
        }

        if (notification.hasChangelog()) {
            Changelog changelog = notification.getChangelog();
            addChangelogVariables(vars, changelog);
        }

        if (notification.getReportUrl() != null) {
            vars.put("reportUrl", notification.getReportUrl());
        }

        return vars;
    }

    public Map<String, Object> buildVariables(Changelog changelog) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("timestamp", DATE_FORMAT.format(Instant.now()));
        addChangelogVariables(vars, changelog);
        return vars;
    }

    private void addChangelogVariables(Map<String, Object> vars, Changelog changelog) {
        vars.put("apiName", changelog.getApiName());
        vars.put("fromVersion", changelog.getFromVersion());
        vars.put("toVersion", changelog.getToVersion());
        vars.put("breakingCount", changelog.getBreakingChanges().size());
        vars.put("totalChanges", changelog.getChanges().size());
        vars.put("hasBreakingChanges", !changelog.getBreakingChanges().isEmpty());

        StringBuilder changesList = new StringBuilder();
        int limit = Math.min(changelog.getBreakingChanges().size(), 5);
        for (int i = 0; i < limit; i++) {
            changesList.append("- ").append(changelog.getBreakingChanges().get(i).getDescription()).append("\n");
        }
        if (changelog.getBreakingChanges().size() > 5) {
            changesList.append("... and ").append(changelog.getBreakingChanges().size() - 5).append(" more\n");
        }
        vars.put("changesList", changesList.toString());
    }

    private String processConditionals(String template, Map<String, Object> variables) {
        Matcher matcher = CONDITIONAL_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String variable = matcher.group(1);
            String content = matcher.group(2);

            Object value = variables.get(variable);
            boolean show = value != null &&
                           !(value instanceof Boolean && !(Boolean) value) &&
                           !(value instanceof Number && ((Number) value).intValue() == 0) &&
                           !(value instanceof String && ((String) value).isEmpty());

            matcher.appendReplacement(result, show ? Matcher.quoteReplacement(content) : "");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String replaceVariables(String template, Map<String, Object> variables) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String variable = matcher.group(1);
            Object value = variables.get(variable);
            String replacement = value != null ? String.valueOf(value) : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
