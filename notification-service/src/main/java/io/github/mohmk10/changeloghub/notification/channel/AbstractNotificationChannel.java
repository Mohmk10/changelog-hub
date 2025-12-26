package io.github.mohmk10.changeloghub.notification.channel;

import io.github.mohmk10.changeloghub.notification.exception.NotificationException;
import io.github.mohmk10.changeloghub.notification.formatter.MessageFormatter;
import io.github.mohmk10.changeloghub.notification.model.ChannelConfig;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationResult;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * Abstract base class for notification channels.
 */
public abstract class AbstractNotificationChannel implements NotificationChannel {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ChannelConfig config;
    protected MessageFormatter formatter;

    protected AbstractNotificationChannel() {
    }

    protected AbstractNotificationChannel(ChannelConfig config) {
        this.config = config;
    }

    @Override
    public NotificationResult send(Notification notification) {
        Instant start = Instant.now();

        try {
            validateConfiguration();
            validateNotification(notification);

            String formattedMessage = formatMessage(notification);
            NotificationResult result = doSend(notification, formattedMessage);

            // Add duration to result
            Duration duration = Duration.between(start, Instant.now());
            return NotificationResult.builder()
                .notificationId(result.getNotificationId())
                .channelType(result.getChannelType())
                .success(result.isSuccess())
                .messageId(result.getMessageId().orElse(null))
                .errorMessage(result.getErrorMessage().orElse(null))
                .duration(duration)
                .httpStatusCode(result.getHttpStatusCode())
                .retryCount(result.getRetryCount())
                .build();

        } catch (NotificationException e) {
            logger.error("Failed to send notification via {}: {}",
                getType().getDisplayName(), e.getMessage());
            return NotificationResult.failure(notification.getId(), getType(), e);

        } catch (Exception e) {
            logger.error("Unexpected error sending notification via {}: {}",
                getType().getDisplayName(), e.getMessage(), e);
            return NotificationResult.failure(notification.getId(), getType(), e);
        }
    }

    /**
     * Actually send the notification. Subclasses implement this.
     */
    protected abstract NotificationResult doSend(Notification notification, String formattedMessage);

    /**
     * Format the message for this channel.
     */
    protected String formatMessage(Notification notification) {
        if (formatter != null) {
            if (notification.hasChangelog()) {
                return formatter.format(notification.getChangelog());
            }
            return formatter.format(notification);
        }
        return buildDefaultMessage(notification);
    }

    /**
     * Build a default message if no formatter is set.
     */
    protected String buildDefaultMessage(Notification notification) {
        StringBuilder sb = new StringBuilder();
        if (notification.getTitle() != null) {
            sb.append(notification.getTitle()).append("\n\n");
        }
        if (notification.getMessage() != null) {
            sb.append(notification.getMessage());
        }
        return sb.toString();
    }

    /**
     * Validate the configuration before sending.
     */
    protected void validateConfiguration() {
        if (config == null) {
            throw NotificationException.channelNotConfigured(getType());
        }
        if (!config.isEnabled()) {
            throw NotificationException.invalidConfiguration(getType(), "Channel is disabled");
        }
        if (!config.isValid()) {
            throw NotificationException.invalidConfiguration(getType(), "Invalid configuration");
        }
    }

    /**
     * Validate the notification before sending.
     */
    protected void validateNotification(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
    }

    @Override
    public boolean isConfigured() {
        return config != null && config.isEnabled() && config.isValid();
    }

    @Override
    public void configure(ChannelConfig config) {
        if (config != null && config.getChannelType() != getType()) {
            throw new IllegalArgumentException(
                "Config type " + config.getChannelType() + " doesn't match channel type " + getType());
        }
        this.config = config;
    }

    @Override
    public ChannelConfig getConfig() {
        return config;
    }

    public void setFormatter(MessageFormatter formatter) {
        this.formatter = formatter;
    }

    public MessageFormatter getFormatter() {
        return formatter;
    }

    /**
     * Get timeout from config or default.
     */
    protected int getTimeout() {
        return config != null ? config.getTimeoutMs() : 30000;
    }

    /**
     * Get retry count from config or default.
     */
    protected int getRetryCount() {
        return config != null ? config.getRetryCount() : 3;
    }
}
