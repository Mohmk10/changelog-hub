package io.github.mohmk10.changeloghub.notification.util;

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

    public boolean isWebhookBased() {
        return this == SLACK || this == DISCORD || this == TEAMS || this == WEBHOOK;
    }

    public boolean supportsRichFormatting() {
        return this != WEBHOOK;
    }
}
