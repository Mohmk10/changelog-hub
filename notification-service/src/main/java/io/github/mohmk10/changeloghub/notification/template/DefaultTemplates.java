package io.github.mohmk10.changeloghub.notification.template;

import io.github.mohmk10.changeloghub.notification.util.ChannelType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DefaultTemplates {

    private DefaultTemplates() {
        
    }

    public static final String BREAKING_CHANGE_TITLE =
        "⚠️ Breaking Changes Detected: ${apiName}";

    public static final String DANGEROUS_CHANGE_TITLE =
        "🟠 Dangerous Changes: ${apiName}";

    public static final String API_RELEASE_TITLE =
        "📦 API Version Released: ${apiName} ${toVersion}";

    public static final String DEPRECATION_TITLE =
        "🔔 Deprecation Notice: ${apiName}";

    public static final String BREAKING_CHANGE_BODY = """
        Version ${fromVersion} → ${toVersion}

        ${breakingCount} breaking change(s) detected.

        {{#if changesList}}
        Changes:
        ${changesList}
        {{/if}}

        Please review the changes and update your integration accordingly.
        """;

    public static final String SIMPLE_BODY = """
        ${message}

        API: ${apiName}
        Version: ${fromVersion} → ${toVersion}
        Total Changes: ${totalChanges}
        Breaking Changes: ${breakingCount}
        """;

    public static final String SLACK_BODY = """
        *Version:* `${fromVersion}` → `${toVersion}`

        *Changes:*
        • Total: ${totalChanges}
        • Breaking: ${breakingCount}

        {{#if changesList}}
        *Breaking Changes:*
        ${changesList}
        {{/if}}
        """;

    public static final String EMAIL_SUBJECT =
        "[ChangelogHub] ${eventType}: ${apiName}";

    public static final String EMAIL_BODY = """
        <h2>API Changes Detected</h2>
        <p><strong>API:</strong> ${apiName}</p>
        <p><strong>Version:</strong> ${fromVersion} → ${toVersion}</p>
        <p><strong>Total Changes:</strong> ${totalChanges}</p>
        <p><strong>Breaking Changes:</strong> ${breakingCount}</p>

        {{#if hasBreakingChanges}}
        <h3>Breaking Changes</h3>
        <ul>
        ${changesList}
        </ul>
        {{/if}}

        <p><a href="${reportUrl}">View Full Report</a></p>
        """;

    private static final Map<String, NotificationTemplate> TEMPLATES;

    static {
        Map<String, NotificationTemplate> templates = new HashMap<>();

        templates.put("breaking_change", NotificationTemplate.builder()
            .name("breaking_change")
            .titleTemplate(BREAKING_CHANGE_TITLE)
            .bodyTemplate(BREAKING_CHANGE_BODY)
            .isDefault(true)
            .build());

        templates.put("dangerous_change", NotificationTemplate.builder()
            .name("dangerous_change")
            .titleTemplate(DANGEROUS_CHANGE_TITLE)
            .bodyTemplate(SIMPLE_BODY)
            .isDefault(true)
            .build());

        templates.put("api_release", NotificationTemplate.builder()
            .name("api_release")
            .titleTemplate(API_RELEASE_TITLE)
            .bodyTemplate(SIMPLE_BODY)
            .isDefault(true)
            .build());

        templates.put("deprecation", NotificationTemplate.builder()
            .name("deprecation")
            .titleTemplate(DEPRECATION_TITLE)
            .bodyTemplate(SIMPLE_BODY)
            .isDefault(true)
            .build());

        templates.put("slack_breaking", NotificationTemplate.builder()
            .name("slack_breaking")
            .channelType(ChannelType.SLACK)
            .titleTemplate(BREAKING_CHANGE_TITLE)
            .bodyTemplate(SLACK_BODY)
            .build());

        templates.put("email_default", NotificationTemplate.builder()
            .name("email_default")
            .channelType(ChannelType.EMAIL)
            .titleTemplate(EMAIL_SUBJECT)
            .bodyTemplate(EMAIL_BODY)
            .build());

        TEMPLATES = Collections.unmodifiableMap(templates);
    }

    public static Map<String, NotificationTemplate> getAll() {
        return TEMPLATES;
    }

    public static NotificationTemplate get(String name) {
        return TEMPLATES.get(name);
    }

    public static NotificationTemplate getDefault(ChannelType channelType) {
        
        for (NotificationTemplate template : TEMPLATES.values()) {
            if (template.getChannelType() == channelType && template.isDefault()) {
                return template;
            }
        }

        return TEMPLATES.get("breaking_change");
    }

    public static NotificationTemplate breakingChange() {
        return TEMPLATES.get("breaking_change");
    }

    public static NotificationTemplate dangerousChange() {
        return TEMPLATES.get("dangerous_change");
    }

    public static NotificationTemplate apiRelease() {
        return TEMPLATES.get("api_release");
    }

    public static NotificationTemplate deprecation() {
        return TEMPLATES.get("deprecation");
    }
}
