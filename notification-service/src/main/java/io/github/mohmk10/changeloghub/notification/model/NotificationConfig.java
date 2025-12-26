package io.github.mohmk10.changeloghub.notification.model;

import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.notification.filter.NotificationFilter;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;
import io.github.mohmk10.changeloghub.notification.util.EventType;

import java.util.*;

public class NotificationConfig {

    private boolean enabled;
    private final Map<ChannelType, ChannelConfig> channels;
    private final List<NotificationFilter> filters;
    private final Set<EventType> enabledEvents;
    private Severity minimumSeverity;
    private boolean asyncEnabled;
    private int asyncThreadPoolSize;
    private boolean failFast;

    public NotificationConfig() {
        this.enabled = true;
        this.channels = new EnumMap<>(ChannelType.class);
        this.filters = new ArrayList<>();
        this.enabledEvents = EnumSet.allOf(EventType.class);
        this.minimumSeverity = Severity.INFO;
        this.asyncEnabled = true;
        this.asyncThreadPoolSize = 4;
        this.failFast = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<ChannelType, ChannelConfig> getChannels() {
        return Collections.unmodifiableMap(channels);
    }

    public ChannelConfig getChannel(ChannelType type) {
        return channels.get(type);
    }

    public void addChannel(ChannelType type, ChannelConfig config) {
        channels.put(type, config);
    }

    public void removeChannel(ChannelType type) {
        channels.remove(type);
    }

    public boolean hasChannel(ChannelType type) {
        ChannelConfig config = channels.get(type);
        return config != null && config.isEnabled();
    }

    public Set<ChannelType> getConfiguredChannels() {
        Set<ChannelType> configured = EnumSet.noneOf(ChannelType.class);
        for (Map.Entry<ChannelType, ChannelConfig> entry : channels.entrySet()) {
            if (entry.getValue().isEnabled()) {
                configured.add(entry.getKey());
            }
        }
        return configured;
    }

    public List<NotificationFilter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    public void addFilter(NotificationFilter filter) {
        filters.add(filter);
    }

    public void removeFilter(NotificationFilter filter) {
        filters.remove(filter);
    }

    public void clearFilters() {
        filters.clear();
    }

    public Set<EventType> getEnabledEvents() {
        return Collections.unmodifiableSet(enabledEvents);
    }

    public void setEnabledEvents(Set<EventType> events) {
        enabledEvents.clear();
        enabledEvents.addAll(events);
    }

    public void enableEvent(EventType event) {
        enabledEvents.add(event);
    }

    public void disableEvent(EventType event) {
        enabledEvents.remove(event);
    }

    public boolean isEventEnabled(EventType event) {
        return enabledEvents.contains(event);
    }

    public Severity getMinimumSeverity() {
        return minimumSeverity;
    }

    public void setMinimumSeverity(Severity minimumSeverity) {
        this.minimumSeverity = minimumSeverity;
    }

    public boolean isAsyncEnabled() {
        return asyncEnabled;
    }

    public void setAsyncEnabled(boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
    }

    public int getAsyncThreadPoolSize() {
        return asyncThreadPoolSize;
    }

    public void setAsyncThreadPoolSize(int asyncThreadPoolSize) {
        this.asyncThreadPoolSize = asyncThreadPoolSize;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public boolean meetsSeverityThreshold(Severity severity) {
        if (minimumSeverity == null || severity == null) {
            return true;
        }
        return severity.ordinal() <= minimumSeverity.ordinal();
    }

    public boolean isValid() {
        if (!enabled) return true;

        if (channels.isEmpty()) return false;

        if (enabledEvents.isEmpty()) return false;

        for (ChannelConfig config : channels.values()) {
            if (config.isEnabled() && !config.isValid()) {
                return false;
            }
        }

        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NotificationConfig config;

        public Builder() {
            this.config = new NotificationConfig();
        }

        public Builder enabled(boolean enabled) {
            config.setEnabled(enabled);
            return this;
        }

        public Builder slack(String webhookUrl) {
            config.addChannel(ChannelType.SLACK,
                ChannelConfig.slack(webhookUrl).build());
            return this;
        }

        public Builder discord(String webhookUrl) {
            config.addChannel(ChannelType.DISCORD,
                ChannelConfig.discord(webhookUrl).build());
            return this;
        }

        public Builder teams(String webhookUrl) {
            config.addChannel(ChannelType.TEAMS,
                ChannelConfig.teams(webhookUrl).build());
            return this;
        }

        public Builder webhook(String webhookUrl) {
            config.addChannel(ChannelType.WEBHOOK,
                ChannelConfig.webhook(webhookUrl).build());
            return this;
        }

        public Builder email(ChannelConfig.SmtpConfig smtpConfig) {
            config.addChannel(ChannelType.EMAIL,
                ChannelConfig.email(smtpConfig).build());
            return this;
        }

        public Builder channel(ChannelType type, ChannelConfig channelConfig) {
            config.addChannel(type, channelConfig);
            return this;
        }

        public Builder filter(NotificationFilter filter) {
            config.addFilter(filter);
            return this;
        }

        public Builder enabledEvents(EventType... events) {
            config.setEnabledEvents(EnumSet.copyOf(Arrays.asList(events)));
            return this;
        }

        public Builder minimumSeverity(Severity severity) {
            config.setMinimumSeverity(severity);
            return this;
        }

        public Builder async(boolean enabled) {
            config.setAsyncEnabled(enabled);
            return this;
        }

        public Builder asyncThreadPoolSize(int size) {
            config.setAsyncThreadPoolSize(size);
            return this;
        }

        public Builder failFast(boolean failFast) {
            config.setFailFast(failFast);
            return this;
        }

        public NotificationConfig build() {
            return config;
        }
    }
}
