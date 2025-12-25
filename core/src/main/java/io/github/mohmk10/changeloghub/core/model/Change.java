package io.github.mohmk10.changeloghub.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Change {

    private String id;
    private ChangeType type;
    private ChangeCategory category;
    private Severity severity;
    private String path;
    private String description;
    private Object oldValue;
    private Object newValue;
    private LocalDateTime detectedAt;

    public Change() {
        this.id = UUID.randomUUID().toString();
        this.detectedAt = LocalDateTime.now();
    }

    public Change(String id, ChangeType type, ChangeCategory category, Severity severity,
                  String path, String description, Object oldValue, Object newValue,
                  LocalDateTime detectedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.type = type;
        this.category = category;
        this.severity = severity;
        this.path = path;
        this.description = description;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.detectedAt = detectedAt != null ? detectedAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChangeType getType() {
        return type;
    }

    public void setType(ChangeType type) {
        this.type = type;
    }

    public ChangeCategory getCategory() {
        return category;
    }

    public void setCategory(ChangeCategory category) {
        this.category = category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Change change = (Change) o;
        return Objects.equals(id, change.id) &&
                type == change.type &&
                category == change.category &&
                severity == change.severity &&
                Objects.equals(path, change.path) &&
                Objects.equals(description, change.description) &&
                Objects.equals(oldValue, change.oldValue) &&
                Objects.equals(newValue, change.newValue) &&
                Objects.equals(detectedAt, change.detectedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, category, severity, path, description, oldValue, newValue, detectedAt);
    }

    @Override
    public String toString() {
        return "Change{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", category=" + category +
                ", severity=" + severity +
                ", path='" + path + '\'' +
                ", description='" + description + '\'' +
                ", oldValue=" + oldValue +
                ", newValue=" + newValue +
                ", detectedAt=" + detectedAt +
                '}';
    }

    public static class Builder {
        private String id;
        private ChangeType type;
        private ChangeCategory category;
        private Severity severity;
        private String path;
        private String description;
        private Object oldValue;
        private Object newValue;
        private LocalDateTime detectedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder type(ChangeType type) {
            this.type = type;
            return this;
        }

        public Builder category(ChangeCategory category) {
            this.category = category;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder oldValue(Object oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public Builder newValue(Object newValue) {
            this.newValue = newValue;
            return this;
        }

        public Builder detectedAt(LocalDateTime detectedAt) {
            this.detectedAt = detectedAt;
            return this;
        }

        public Change build() {
            return new Change(id, type, category, severity, path, description, oldValue, newValue, detectedAt);
        }
    }
}
