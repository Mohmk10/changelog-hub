package io.github.mohmk10.changeloghub.notification;

import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.notification.model.*;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main service for sending notifications across multiple channels.
 */
public interface NotificationService {

    /**
     * Send a notification to all configured channels.
     *
     * @param notification the notification to send
     * @return the result of the send operation
     */
    NotificationResult notify(Notification notification);

    /**
     * Send a notification for a changelog to all configured channels.
     *
     * @param changelog the changelog to notify about
     * @return the result of the send operation
     */
    NotificationResult notify(Changelog changelog);

    /**
     * Send a notification for a changelog to specific channels.
     *
     * @param changelog the changelog to notify about
     * @param channels the channels to send to
     * @return the result of the send operation
     */
    NotificationResult notify(Changelog changelog, List<ChannelType> channels);

    /**
     * Send a notification to all configured channels and return all results.
     *
     * @param changelog the changelog to notify about
     * @return list of results from each channel
     */
    List<NotificationResult> notifyAll(Changelog changelog);

    /**
     * Send a notification asynchronously.
     *
     * @param notification the notification to send
     * @return future with results
     */
    CompletableFuture<List<NotificationResult>> notifyAsync(Notification notification);

    /**
     * Send a notification for a changelog asynchronously.
     *
     * @param changelog the changelog to notify about
     * @return future with results
     */
    CompletableFuture<List<NotificationResult>> notifyAsync(Changelog changelog);

    /**
     * Configure the notification service.
     *
     * @param config the configuration
     */
    void configure(NotificationConfig config);

    /**
     * Get the current configuration.
     *
     * @return the current configuration
     */
    NotificationConfig getConfig();

    /**
     * Add a channel configuration.
     *
     * @param type the channel type
     * @param config the channel configuration
     */
    void addChannel(ChannelType type, ChannelConfig config);

    /**
     * Remove a channel.
     *
     * @param type the channel type to remove
     */
    void removeChannel(ChannelType type);

    /**
     * Check if a channel is configured.
     *
     * @param type the channel type
     * @return true if configured and enabled
     */
    boolean isChannelConfigured(ChannelType type);

    /**
     * Get configured channel types.
     *
     * @return set of configured channel types
     */
    java.util.Set<ChannelType> getConfiguredChannels();

    /**
     * Test connection to a channel.
     *
     * @param type the channel type
     * @return true if connection test succeeds
     */
    boolean testChannel(ChannelType type);

    /**
     * Test connections to all configured channels.
     *
     * @return map of channel type to test result
     */
    java.util.Map<ChannelType, Boolean> testAllChannels();

    /**
     * Check if the service is enabled.
     *
     * @return true if notifications are enabled
     */
    boolean isEnabled();

    /**
     * Enable or disable the service.
     *
     * @param enabled true to enable, false to disable
     */
    void setEnabled(boolean enabled);
}
