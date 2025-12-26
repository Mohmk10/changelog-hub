package io.github.mohmk10.changeloghub.notification.filter;

import io.github.mohmk10.changeloghub.notification.model.Notification;

/**
 * Interface for filtering notifications before sending.
 */
public interface NotificationFilter {

    /**
     * Check if the notification should be allowed.
     *
     * @param notification the notification to check
     * @return true if the notification should be sent, false to filter it out
     */
    boolean shouldSend(Notification notification);

    /**
     * Get the name of this filter for logging.
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Get the reason for filtering (if applicable).
     */
    default String getFilterReason(Notification notification) {
        return "Filtered by " + getName();
    }

    /**
     * Get the priority of this filter (lower runs first).
     */
    default int getPriority() {
        return 100;
    }
}
