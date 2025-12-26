package io.github.mohmk10.changeloghub.notification.filter;

import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.notification.model.Notification;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class SeverityFilter implements NotificationFilter {

    private final Severity minimumSeverity;
    private final Set<Severity> allowedSeverities;

    public SeverityFilter(Severity minimumSeverity) {
        this.minimumSeverity = Objects.requireNonNull(minimumSeverity);
        this.allowedSeverities = null;
    }

    public SeverityFilter(Set<Severity> allowedSeverities) {
        this.minimumSeverity = null;
        this.allowedSeverities = EnumSet.copyOf(Objects.requireNonNull(allowedSeverities));
    }

    @Override
    public boolean shouldSend(Notification notification) {
        Severity severity = notification.getSeverity();

        if (allowedSeverities != null) {
            return allowedSeverities.contains(severity);
        }

        if (minimumSeverity != null && severity != null) {
            
            return severity.ordinal() <= minimumSeverity.ordinal();
        }

        return true;
    }

    @Override
    public String getFilterReason(Notification notification) {
        if (allowedSeverities != null) {
            return String.format("Severity %s not in allowed set %s",
                notification.getSeverity(), allowedSeverities);
        }
        return String.format("Severity %s below minimum %s",
            notification.getSeverity(), minimumSeverity);
    }

    @Override
    public int getPriority() {
        return 10; 
    }

    public static SeverityFilter breakingOnly() {
        return new SeverityFilter(EnumSet.of(Severity.BREAKING));
    }

    public static SeverityFilter criticalOnly() {
        return new SeverityFilter(EnumSet.of(Severity.BREAKING, Severity.DANGEROUS));
    }

    public static SeverityFilter minimum(Severity minSeverity) {
        return new SeverityFilter(minSeverity);
    }

    public static SeverityFilter allowAll() {
        return new SeverityFilter(EnumSet.allOf(Severity.class));
    }

    public Severity getMinimumSeverity() {
        return minimumSeverity;
    }

    public Set<Severity> getAllowedSeverities() {
        return allowedSeverities != null
            ? EnumSet.copyOf(allowedSeverities)
            : null;
    }
}
