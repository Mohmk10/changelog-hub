package io.github.mohmk10.changeloghub.notification.model;

import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.notification.util.EventType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an event that triggers a notification.
 */
public class NotificationEvent {

    private final String id;
    private final EventType eventType;
    private final Severity severity;
    private final String apiName;
    private final String fromVersion;
    private final String toVersion;
    private final Changelog changelog;
    private final Instant timestamp;
    private final String source;

    private NotificationEvent(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.eventType = Objects.requireNonNull(builder.eventType, "eventType is required");
        this.severity = builder.severity != null ? builder.severity : builder.eventType.getDefaultSeverity();
        this.apiName = builder.apiName;
        this.fromVersion = builder.fromVersion;
        this.toVersion = builder.toVersion;
        this.changelog = builder.changelog;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.source = builder.source;
    }

    public String getId() {
        return id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getApiName() {
        return apiName;
    }

    public String getFromVersion() {
        return fromVersion;
    }

    public String getToVersion() {
        return toVersion;
    }

    public Changelog getChangelog() {
        return changelog;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    public boolean hasChangelog() {
        return changelog != null;
    }

    public boolean isCritical() {
        return eventType.isCritical() || severity == Severity.BREAKING;
    }

    public int getBreakingChangesCount() {
        return changelog != null ? changelog.getBreakingChanges().size() : 0;
    }

    public int getTotalChangesCount() {
        return changelog != null ? changelog.getChanges().size() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder fromChangelog(Changelog changelog) {
        Builder builder = new Builder()
            .changelog(changelog)
            .apiName(changelog.getApiName())
            .fromVersion(changelog.getFromVersion())
            .toVersion(changelog.getToVersion());

        if (!changelog.getBreakingChanges().isEmpty()) {
            builder.eventType(EventType.BREAKING_CHANGE_DETECTED)
                   .severity(Severity.BREAKING);
        } else if (changelog.getChanges().stream()
                .anyMatch(c -> c.getSeverity() == Severity.DANGEROUS)) {
            builder.eventType(EventType.DANGEROUS_CHANGE_DETECTED)
                   .severity(Severity.DANGEROUS);
        } else {
            builder.eventType(EventType.API_VERSION_RELEASED)
                   .severity(Severity.INFO);
        }

        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationEvent that = (NotificationEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NotificationEvent{" +
               "id='" + id + '\'' +
               ", eventType=" + eventType +
               ", severity=" + severity +
               ", apiName='" + apiName + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }

    public static class Builder {
        private String id;
        private EventType eventType;
        private Severity severity;
        private String apiName;
        private String fromVersion;
        private String toVersion;
        private Changelog changelog;
        private Instant timestamp;
        private String source;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder apiName(String apiName) {
            this.apiName = apiName;
            return this;
        }

        public Builder fromVersion(String fromVersion) {
            this.fromVersion = fromVersion;
            return this;
        }

        public Builder toVersion(String toVersion) {
            this.toVersion = toVersion;
            return this;
        }

        public Builder changelog(Changelog changelog) {
            this.changelog = changelog;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public NotificationEvent build() {
            return new NotificationEvent(this);
        }
    }
}
