package io.github.mohmk10.changeloghub.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BreakingChange extends Change {

    private String migrationSuggestion;
    private List<String> affectedConsumers;
    private int impactScore;

    public BreakingChange() {
        super();
        this.affectedConsumers = new ArrayList<>();
        setSeverity(Severity.BREAKING);
    }

    public BreakingChange(String id, ChangeType type, ChangeCategory category, Severity severity,
                          String path, String description, Object oldValue, Object newValue,
                          LocalDateTime detectedAt, String migrationSuggestion,
                          List<String> affectedConsumers, int impactScore) {
        super(id, type, category, severity, path, description, oldValue, newValue, detectedAt);
        this.migrationSuggestion = migrationSuggestion;
        this.affectedConsumers = affectedConsumers != null ? new ArrayList<>(affectedConsumers) : new ArrayList<>();
        this.impactScore = impactScore;
    }

    public static BreakingChangeBuilder breakingChangeBuilder() {
        return new BreakingChangeBuilder();
    }

    public String getMigrationSuggestion() {
        return migrationSuggestion;
    }

    public void setMigrationSuggestion(String migrationSuggestion) {
        this.migrationSuggestion = migrationSuggestion;
    }

    public List<String> getAffectedConsumers() {
        return affectedConsumers;
    }

    public void setAffectedConsumers(List<String> affectedConsumers) {
        this.affectedConsumers = affectedConsumers != null ? new ArrayList<>(affectedConsumers) : new ArrayList<>();
    }

    public int getImpactScore() {
        return impactScore;
    }

    public void setImpactScore(int impactScore) {
        this.impactScore = Math.max(0, Math.min(100, impactScore));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BreakingChange that = (BreakingChange) o;
        return impactScore == that.impactScore &&
                Objects.equals(migrationSuggestion, that.migrationSuggestion) &&
                Objects.equals(affectedConsumers, that.affectedConsumers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), migrationSuggestion, affectedConsumers, impactScore);
    }

    @Override
    public String toString() {
        return "BreakingChange{" +
                "id='" + getId() + '\'' +
                ", type=" + getType() +
                ", category=" + getCategory() +
                ", severity=" + getSeverity() +
                ", path='" + getPath() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", migrationSuggestion='" + migrationSuggestion + '\'' +
                ", affectedConsumers=" + affectedConsumers +
                ", impactScore=" + impactScore +
                '}';
    }

    public static class BreakingChangeBuilder {
        private String id;
        private ChangeType type;
        private ChangeCategory category;
        private Severity severity = Severity.BREAKING;
        private String path;
        private String description;
        private Object oldValue;
        private Object newValue;
        private LocalDateTime detectedAt;
        private String migrationSuggestion;
        private List<String> affectedConsumers = new ArrayList<>();
        private int impactScore;

        public BreakingChangeBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BreakingChangeBuilder type(ChangeType type) {
            this.type = type;
            return this;
        }

        public BreakingChangeBuilder category(ChangeCategory category) {
            this.category = category;
            return this;
        }

        public BreakingChangeBuilder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public BreakingChangeBuilder path(String path) {
            this.path = path;
            return this;
        }

        public BreakingChangeBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BreakingChangeBuilder oldValue(Object oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public BreakingChangeBuilder newValue(Object newValue) {
            this.newValue = newValue;
            return this;
        }

        public BreakingChangeBuilder detectedAt(LocalDateTime detectedAt) {
            this.detectedAt = detectedAt;
            return this;
        }

        public BreakingChangeBuilder migrationSuggestion(String migrationSuggestion) {
            this.migrationSuggestion = migrationSuggestion;
            return this;
        }

        public BreakingChangeBuilder affectedConsumers(List<String> affectedConsumers) {
            this.affectedConsumers = affectedConsumers != null ? new ArrayList<>(affectedConsumers) : new ArrayList<>();
            return this;
        }

        public BreakingChangeBuilder addAffectedConsumer(String consumer) {
            this.affectedConsumers.add(consumer);
            return this;
        }

        public BreakingChangeBuilder impactScore(int impactScore) {
            this.impactScore = impactScore;
            return this;
        }

        public BreakingChange build() {
            return new BreakingChange(id, type, category, severity, path, description, oldValue, newValue,
                    detectedAt, migrationSuggestion, affectedConsumers, impactScore);
        }
    }
}
