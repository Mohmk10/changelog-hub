package io.github.mohmk10.changeloghub.notification;

import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.notification.model.*;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NotificationService {

    NotificationResult notify(Notification notification);

    NotificationResult notify(Changelog changelog);

    NotificationResult notify(Changelog changelog, List<ChannelType> channels);

    List<NotificationResult> notifyAll(Changelog changelog);

    CompletableFuture<List<NotificationResult>> notifyAsync(Notification notification);

    CompletableFuture<List<NotificationResult>> notifyAsync(Changelog changelog);

    void configure(NotificationConfig config);

    NotificationConfig getConfig();

    void addChannel(ChannelType type, ChannelConfig config);

    void removeChannel(ChannelType type);

    boolean isChannelConfigured(ChannelType type);

    java.util.Set<ChannelType> getConfiguredChannels();

    boolean testChannel(ChannelType type);

    java.util.Map<ChannelType, Boolean> testAllChannels();

    boolean isEnabled();

    void setEnabled(boolean enabled);
}
