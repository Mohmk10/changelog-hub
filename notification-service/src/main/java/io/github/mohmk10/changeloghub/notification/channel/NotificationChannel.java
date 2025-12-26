package io.github.mohmk10.changeloghub.notification.channel;

import io.github.mohmk10.changeloghub.notification.model.ChannelConfig;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationResult;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;

public interface NotificationChannel {

    NotificationResult send(Notification notification);

    boolean isConfigured();

    ChannelType getType();

    void configure(ChannelConfig config);

    ChannelConfig getConfig();

    default boolean testConnection() {
        return isConfigured();
    }

    default String getDisplayName() {
        return getType().getDisplayName();
    }
}
