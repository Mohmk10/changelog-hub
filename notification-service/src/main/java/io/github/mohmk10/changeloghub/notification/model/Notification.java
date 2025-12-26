package io.github.mohmk10.changeloghub.notification.model;

import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;

import java.time.Instant;
import java.util.*;

/**
 * Represents a notification to be sent.
 */
public class Notification {

    private final String id;
    private final String title;
    private final String message;
    private final Severity severity;
    private final NotificationEvent event;
    private final Set<ChannelType> targetChannels;
    private final Map<String, Object> metadata;
    private final Instant createdAt;
    private final String reportUrl;
    private final List<String> mentions;

    private Notification(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.title = builder.title;
        this.message = builder.message;
        this.severity = builder.severity != null ? builder.severity : Severity.INFO;
        this.event = builder.event;
        this.targetChannels = builder.targetChannels != null
            ? EnumSet.copyOf(builder.targetChannels)
            : EnumSet.noneOf(ChannelType.class);
        this.metadata = builder.metadata != null
            ? new HashMap<>(builder.metadata)
            : new HashMap<>();
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.reportUrl = builder.reportUrl;
        this.mentions = builder.mentions != null
            ? new ArrayList<>(builder.mentions)
            : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public NotificationEvent getEvent() {
        return event;
    }

    public Set<ChannelType> getTargetChannels() {
        return Collections.unmodifiableSet(targetChannels);
    }

    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public List<String> getMentions() {
        return Collections.unmodifiableList(mentions);
    }

    public boolean hasEvent() {
        return event != null;
    }

    public boolean hasChangelog() {
        return event != null && event.hasChangelog();
    }

    public Changelog getChangelog() {
        return event != null ? event.getChangelog() : null;
    }

    public boolean isCritical() {
        return severity == Severity.BREAKING ||
               severity == Severity.DANGEROUS ||
               (event != null && event.isCritical());
    }

    public boolean isTargetedTo(ChannelType channel) {
        return targetChannels.isEmpty() || targetChannels.contains(channel);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Notification fromChangelog(Changelog changelog) {
        NotificationEvent event = NotificationEvent.fromChangelog(changelog).build();

        String title = String.format("API Changes Detected: %s",
            changelog.getApiName() != null ? changelog.getApiName() : "Unknown API");

        String message = buildMessageFromChangelog(changelog);

        return builder()
            .title(title)
            .message(message)
            .severity(event.getSeverity())
            .event(event)
            .build();
    }

    private static String buildMessageFromChangelog(Changelog changelog) {
        StringBuilder sb = new StringBuilder();

        if (changelog.getFromVersion() != null && changelog.getToVersion() != null) {
            sb.append(String.format("Version %s -> %s\n",
                changelog.getFromVersion(), changelog.getToVersion()));
        }

        int breakingCount = changelog.getBreakingChanges().size();
        int totalCount = changelog.getChanges().size();

        if (breakingCount > 0) {
            sb.append(String.format("Breaking changes: %d\n", breakingCount));
        }
        sb.append(String.format("Total changes: %d", totalCount));

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Notification{" +
               "id='" + id + '\'' +
               ", title='" + title + '\'' +
               ", severity=" + severity +
               ", channels=" + targetChannels +
               '}';
    }

    public static class Builder {
        private String id;
        private String title;
        private String message;
        private Severity severity;
        private NotificationEvent event;
        private Set<ChannelType> targetChannels;
        private Map<String, Object> metadata;
        private Instant createdAt;
        private String reportUrl;
        private List<String> mentions;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder event(NotificationEvent event) {
            this.event = event;
            return this;
        }

        public Builder targetChannels(Set<ChannelType> channels) {
            this.targetChannels = channels;
            return this;
        }

        public Builder targetChannel(ChannelType channel) {
            if (this.targetChannels == null) {
                this.targetChannels = EnumSet.noneOf(ChannelType.class);
            }
            this.targetChannels.add(channel);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder reportUrl(String reportUrl) {
            this.reportUrl = reportUrl;
            return this;
        }

        public Builder mentions(List<String> mentions) {
            this.mentions = mentions;
            return this;
        }

        public Builder addMention(String mention) {
            if (this.mentions == null) {
                this.mentions = new ArrayList<>();
            }
            this.mentions.add(mention);
            return this;
        }

        public Notification build() {
            return new Notification(this);
        }
    }
}
