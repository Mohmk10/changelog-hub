package io.github.mohmk10.changeloghub.notification.template;

import io.github.mohmk10.changeloghub.notification.util.ChannelType;

import java.util.Objects;

/**
 * Represents a notification template.
 */
public class NotificationTemplate {

    private final String name;
    private final ChannelType channelType;
    private final String titleTemplate;
    private final String bodyTemplate;
    private final boolean isDefault;

    public NotificationTemplate(String name, ChannelType channelType,
                               String titleTemplate, String bodyTemplate) {
        this(name, channelType, titleTemplate, bodyTemplate, false);
    }

    public NotificationTemplate(String name, ChannelType channelType,
                               String titleTemplate, String bodyTemplate, boolean isDefault) {
        this.name = Objects.requireNonNull(name);
        this.channelType = channelType;
        this.titleTemplate = titleTemplate;
        this.bodyTemplate = bodyTemplate;
        this.isDefault = isDefault;
    }

    public String getName() {
        return name;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public String getTitleTemplate() {
        return titleTemplate;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Check if this template applies to a specific channel.
     */
    public boolean appliesTo(ChannelType channel) {
        return channelType == null || channelType == channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTemplate that = (NotificationTemplate) o;
        return Objects.equals(name, that.name) && channelType == that.channelType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, channelType);
    }

    @Override
    public String toString() {
        return "NotificationTemplate{" +
               "name='" + name + '\'' +
               ", channelType=" + channelType +
               ", isDefault=" + isDefault +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private ChannelType channelType;
        private String titleTemplate;
        private String bodyTemplate;
        private boolean isDefault;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder channelType(ChannelType channelType) {
            this.channelType = channelType;
            return this;
        }

        public Builder titleTemplate(String titleTemplate) {
            this.titleTemplate = titleTemplate;
            return this;
        }

        public Builder bodyTemplate(String bodyTemplate) {
            this.bodyTemplate = bodyTemplate;
            return this;
        }

        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public NotificationTemplate build() {
            return new NotificationTemplate(name, channelType, titleTemplate, bodyTemplate, isDefault);
        }
    }
}
