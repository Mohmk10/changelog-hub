package io.github.mohmk10.changeloghub.notification.impl;

import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.notification.NotificationService;
import io.github.mohmk10.changeloghub.notification.channel.*;
import io.github.mohmk10.changeloghub.notification.exception.NotificationException;
import io.github.mohmk10.changeloghub.notification.filter.NotificationFilter;
import io.github.mohmk10.changeloghub.notification.model.*;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Default implementation of the notification service.
 */
public class DefaultNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNotificationService.class);

    private NotificationConfig config;
    private final Map<ChannelType, NotificationChannel> channels;
    private ExecutorService executor;
    private boolean initialized = false;

    public DefaultNotificationService() {
        this.config = new NotificationConfig();
        this.channels = new EnumMap<>(ChannelType.class);
    }

    public DefaultNotificationService(NotificationConfig config) {
        this.config = config;
        this.channels = new EnumMap<>(ChannelType.class);
        initialize();
    }

    private void initialize() {
        if (initialized) return;

        // Initialize channels from config
        for (Map.Entry<ChannelType, ChannelConfig> entry : config.getChannels().entrySet()) {
            ChannelType type = entry.getKey();
            ChannelConfig channelConfig = entry.getValue();

            if (channelConfig.isEnabled()) {
                NotificationChannel channel = createChannel(type);
                channel.configure(channelConfig);
                channels.put(type, channel);
            }
        }

        // Initialize executor for async operations
        if (config.isAsyncEnabled()) {
            executor = Executors.newFixedThreadPool(config.getAsyncThreadPoolSize());
        }

        initialized = true;
        logger.info("NotificationService initialized with {} channel(s)", channels.size());
    }

    private NotificationChannel createChannel(ChannelType type) {
        return switch (type) {
            case SLACK -> new SlackNotifier();
            case DISCORD -> new DiscordNotifier();
            case EMAIL -> new EmailNotifier();
            case TEAMS -> new TeamsNotifier();
            case WEBHOOK -> new WebhookNotifier();
        };
    }

    @Override
    public NotificationResult notify(Notification notification) {
        if (!config.isEnabled()) {
            logger.debug("Notifications are disabled");
            return NotificationResult.failure(notification.getId(), null, "Notifications disabled");
        }

        // Apply filters
        for (NotificationFilter filter : config.getFilters()) {
            if (!filter.shouldSend(notification)) {
                String reason = filter.getFilterReason(notification);
                logger.debug("Notification filtered: {}", reason);
                throw NotificationException.filtered(reason);
            }
        }

        // Determine target channels
        Set<ChannelType> targets = notification.getTargetChannels();
        if (targets.isEmpty()) {
            targets = config.getConfiguredChannels();
        }

        // Send to first successful channel
        for (ChannelType type : targets) {
            NotificationChannel channel = channels.get(type);
            if (channel != null && channel.isConfigured()) {
                try {
                    NotificationResult result = channel.send(notification);
                    if (result.isSuccess()) {
                        return result;
                    }
                    if (config.isFailFast()) {
                        return result;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to send via {}: {}", type, e.getMessage());
                    if (config.isFailFast()) {
                        throw e;
                    }
                }
            }
        }

        return NotificationResult.failure(notification.getId(), null, "All channels failed");
    }

    @Override
    public NotificationResult notify(Changelog changelog) {
        Notification notification = Notification.fromChangelog(changelog);
        return notify(notification);
    }

    @Override
    public NotificationResult notify(Changelog changelog, List<ChannelType> channelTypes) {
        Notification notification = Notification.builder()
            .title("API Changes: " + changelog.getApiName())
            .message(buildChangelogMessage(changelog))
            .event(NotificationEvent.fromChangelog(changelog).build())
            .targetChannels(EnumSet.copyOf(channelTypes))
            .build();

        return notify(notification);
    }

    @Override
    public List<NotificationResult> notifyAll(Changelog changelog) {
        Notification notification = Notification.fromChangelog(changelog);
        return notifyAllChannels(notification);
    }

    private List<NotificationResult> notifyAllChannels(Notification notification) {
        if (!config.isEnabled()) {
            return Collections.singletonList(
                NotificationResult.failure(notification.getId(), null, "Notifications disabled"));
        }

        // Apply filters
        for (NotificationFilter filter : config.getFilters()) {
            if (!filter.shouldSend(notification)) {
                logger.debug("Notification filtered: {}", filter.getFilterReason(notification));
                return Collections.emptyList();
            }
        }

        List<NotificationResult> results = new ArrayList<>();

        Set<ChannelType> targets = notification.getTargetChannels();
        if (targets.isEmpty()) {
            targets = config.getConfiguredChannels();
        }

        for (ChannelType type : targets) {
            NotificationChannel channel = channels.get(type);
            if (channel != null && channel.isConfigured()) {
                try {
                    NotificationResult result = channel.send(notification);
                    results.add(result);
                } catch (Exception e) {
                    logger.warn("Failed to send via {}: {}", type, e.getMessage());
                    results.add(NotificationResult.failure(notification.getId(), type, e));
                }
            }
        }

        return results;
    }

    @Override
    public CompletableFuture<List<NotificationResult>> notifyAsync(Notification notification) {
        if (executor == null) {
            return CompletableFuture.completedFuture(
                Collections.singletonList(notify(notification)));
        }

        return CompletableFuture.supplyAsync(() -> notifyAllChannels(notification), executor);
    }

    @Override
    public CompletableFuture<List<NotificationResult>> notifyAsync(Changelog changelog) {
        Notification notification = Notification.fromChangelog(changelog);
        return notifyAsync(notification);
    }

    @Override
    public void configure(NotificationConfig config) {
        this.config = config;
        this.initialized = false;
        channels.clear();

        if (executor != null) {
            executor.shutdown();
            executor = null;
        }

        initialize();
    }

    @Override
    public NotificationConfig getConfig() {
        return config;
    }

    @Override
    public void addChannel(ChannelType type, ChannelConfig channelConfig) {
        config.addChannel(type, channelConfig);

        NotificationChannel channel = createChannel(type);
        channel.configure(channelConfig);
        channels.put(type, channel);

        logger.info("Added channel: {}", type);
    }

    @Override
    public void removeChannel(ChannelType type) {
        config.removeChannel(type);
        channels.remove(type);
        logger.info("Removed channel: {}", type);
    }

    @Override
    public boolean isChannelConfigured(ChannelType type) {
        NotificationChannel channel = channels.get(type);
        return channel != null && channel.isConfigured();
    }

    @Override
    public Set<ChannelType> getConfiguredChannels() {
        Set<ChannelType> configured = EnumSet.noneOf(ChannelType.class);
        for (Map.Entry<ChannelType, NotificationChannel> entry : channels.entrySet()) {
            if (entry.getValue().isConfigured()) {
                configured.add(entry.getKey());
            }
        }
        return configured;
    }

    @Override
    public boolean testChannel(ChannelType type) {
        NotificationChannel channel = channels.get(type);
        if (channel == null) {
            return false;
        }
        return channel.testConnection();
    }

    @Override
    public Map<ChannelType, Boolean> testAllChannels() {
        Map<ChannelType, Boolean> results = new EnumMap<>(ChannelType.class);
        for (Map.Entry<ChannelType, NotificationChannel> entry : channels.entrySet()) {
            results.put(entry.getKey(), entry.getValue().testConnection());
        }
        return results;
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setEnabled(enabled);
    }

    private String buildChangelogMessage(Changelog changelog) {
        StringBuilder sb = new StringBuilder();
        if (changelog.getFromVersion() != null && changelog.getToVersion() != null) {
            sb.append("Version ").append(changelog.getFromVersion())
              .append(" -> ").append(changelog.getToVersion()).append("\n");
        }
        sb.append("Total changes: ").append(changelog.getChanges().size()).append("\n");
        sb.append("Breaking changes: ").append(changelog.getBreakingChanges().size());
        return sb.toString();
    }

    /**
     * Shutdown the executor service.
     */
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Get a channel by type (for testing).
     */
    NotificationChannel getChannel(ChannelType type) {
        return channels.get(type);
    }

    /**
     * Create with builder pattern.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NotificationConfig config = new NotificationConfig();

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

        public Builder channel(ChannelType type, ChannelConfig channelConfig) {
            config.addChannel(type, channelConfig);
            return this;
        }

        public Builder filter(NotificationFilter filter) {
            config.addFilter(filter);
            return this;
        }

        public Builder async(boolean async) {
            config.setAsyncEnabled(async);
            return this;
        }

        public DefaultNotificationService build() {
            return new DefaultNotificationService(config);
        }
    }
}
