package io.github.mohmk10.changeloghub.notification.filter;

import io.github.mohmk10.changeloghub.notification.model.Notification;

public interface NotificationFilter {

    boolean shouldSend(Notification notification);

    default String getName() {
        return getClass().getSimpleName();
    }

    default String getFilterReason(Notification notification) {
        return "Filtered by " + getName();
    }

    default int getPriority() {
        return 100;
    }
}
