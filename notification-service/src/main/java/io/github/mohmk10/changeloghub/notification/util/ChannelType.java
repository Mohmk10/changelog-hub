package io.github.mohmk10.changeloghub.notification.util;

/**
 * Types of notification channels supported.
 */
public enum ChannelType {
    SLACK("Slack", "slack"),
    DISCORD("Discord", "discord"),
    EMAIL("Email", "email"),
    TEAMS("Microsoft Teams", "teams"),
    WEBHOOK("Webhook", "webhook");

    private final String displayName;
    private final String configKey;

    ChannelType(String displayName, String configKey) {
        this.displayName = displayName;
        this.configKey = configKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getConfigKey() {
        return configKey;
    }

    /**
     * Get channel type from config key.
     */
    public static ChannelType fromConfigKey(String key) {
        if (key == null) {
            return null;
        }
        for (ChannelType type : values()) {
            if (type.configKey.equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Check if this channel supports webhooks.
     */
    public boolean isWebhookBased() {
        return this == SLACK || this == DISCORD || this == TEAMS || this == WEBHOOK;
    }

    /**
     * Check if this channel supports rich formatting.
     */
    public boolean supportsRichFormatting() {
        return this != WEBHOOK;
    }
}
