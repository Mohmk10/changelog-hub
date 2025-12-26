package io.github.mohmk10.changeloghub.notification.filter;

import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.notification.model.Notification;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Filter notifications by severity level.
 */
public class SeverityFilter implements NotificationFilter {

    private final Severity minimumSeverity;
    private final Set<Severity> allowedSeverities;

    /**
     * Create a filter with minimum severity.
     */
    public SeverityFilter(Severity minimumSeverity) {
        this.minimumSeverity = Objects.requireNonNull(minimumSeverity);
        this.allowedSeverities = null;
    }

    /**
     * Create a filter with explicit allowed severities.
     */
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
            // Lower ordinal = higher severity (BREAKING=0, INFO=3)
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
        return 10; // Run early
    }

    /**
     * Create a filter that only allows breaking changes.
     */
    public static SeverityFilter breakingOnly() {
        return new SeverityFilter(EnumSet.of(Severity.BREAKING));
    }

    /**
     * Create a filter that allows breaking and dangerous changes.
     */
    public static SeverityFilter criticalOnly() {
        return new SeverityFilter(EnumSet.of(Severity.BREAKING, Severity.DANGEROUS));
    }

    /**
     * Create a filter with minimum severity.
     */
    public static SeverityFilter minimum(Severity minSeverity) {
        return new SeverityFilter(minSeverity);
    }

    /**
     * Create a filter that allows all severities.
     */
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
