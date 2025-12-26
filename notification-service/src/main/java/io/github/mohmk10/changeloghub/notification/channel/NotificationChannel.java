package io.github.mohmk10.changeloghub.notification.channel;

import io.github.mohmk10.changeloghub.notification.model.ChannelConfig;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationResult;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;

/**
 * Interface for notification channels.
 */
public interface NotificationChannel {

    /**
     * Send a notification through this channel.
     *
     * @param notification the notification to send
     * @return the result of the send operation
     */
    NotificationResult send(Notification notification);

    /**
     * Check if this channel is properly configured.
     *
     * @return true if configured and ready to send
     */
    boolean isConfigured();

    /**
     * Get the type of this channel.
     *
     * @return the channel type
     */
    ChannelType getType();

    /**
     * Configure this channel.
     *
     * @param config the channel configuration
     */
    void configure(ChannelConfig config);

    /**
     * Get the current configuration.
     *
     * @return the current configuration, or null if not configured
     */
    ChannelConfig getConfig();

    /**
     * Test the channel connection.
     *
     * @return true if the connection test succeeds
     */
    default boolean testConnection() {
        return isConfigured();
    }

    /**
     * Get the display name of this channel.
     */
    default String getDisplayName() {
        return getType().getDisplayName();
    }
}
