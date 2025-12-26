package io.github.mohmk10.changeloghub.notification.formatter;

import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.notification.model.Notification;

public interface MessageFormatter {

    String format(Notification notification);

    String format(Changelog changelog);

    default String getContentType() {
        return "text/plain";
    }
}
