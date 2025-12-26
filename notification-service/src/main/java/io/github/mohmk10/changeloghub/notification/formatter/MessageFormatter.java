package io.github.mohmk10.changeloghub.notification.formatter;

import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.notification.model.Notification;

/**
 * Interface for formatting notification messages.
 */
public interface MessageFormatter {

    /**
     * Format a notification into a message string.
     *
     * @param notification the notification to format
     * @return the formatted message
     */
    String format(Notification notification);

    /**
     * Format a changelog into a message string.
     *
     * @param changelog the changelog to format
     * @return the formatted message
     */
    String format(Changelog changelog);

    /**
     * Get the content type of the formatted message.
     */
    default String getContentType() {
        return "text/plain";
    }
}
