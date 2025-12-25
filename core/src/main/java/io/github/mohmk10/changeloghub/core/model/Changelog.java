package io.github.mohmk10.changeloghub.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Changelog {

    private String id;
    private String apiName;
    private String fromVersion;
    private String toVersion;
    private List<Change> changes;
    private List<BreakingChange> breakingChanges;
    private RiskAssessment riskAssessment;
    private LocalDateTime generatedAt;

    public Changelog() {
        this.id = UUID.randomUUID().toString();
        this.changes = new ArrayList<>();
        this.breakingChanges = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    public Changelog(String id, String apiName, String fromVersion, String toVersion,
                     List<Change> changes, List<BreakingChange> breakingChanges,
                     RiskAssessment riskAssessment, LocalDateTime generatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.apiName = apiName;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.changes = changes != null ? new ArrayList<>(changes) : new ArrayList<>();
        this.breakingChanges = breakingChanges != null ? new ArrayList<>(breakingChanges) : new ArrayList<>();
        this.riskAssessment = riskAssessment;
        this.generatedAt = generatedAt != null ? generatedAt : LocalDateTime.now();
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

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(String fromVersion) {
        this.fromVersion = fromVersion;
    }

    public String getToVersion() {
        return toVersion;
    }

    public void setToVersion(String toVersion) {
        this.toVersion = toVersion;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes != null ? new ArrayList<>(changes) : new ArrayList<>();
    }

    public List<BreakingChange> getBreakingChanges() {
        return breakingChanges;
    }

    public void setBreakingChanges(List<BreakingChange> breakingChanges) {
        this.breakingChanges = breakingChanges != null ? new ArrayList<>(breakingChanges) : new ArrayList<>();
    }

    public RiskAssessment getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(RiskAssessment riskAssessment) {
        this.riskAssessment = riskAssessment;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Changelog changelog = (Changelog) o;
        return Objects.equals(id, changelog.id) &&
                Objects.equals(apiName, changelog.apiName) &&
                Objects.equals(fromVersion, changelog.fromVersion) &&
                Objects.equals(toVersion, changelog.toVersion) &&
                Objects.equals(changes, changelog.changes) &&
                Objects.equals(breakingChanges, changelog.breakingChanges) &&
                Objects.equals(riskAssessment, changelog.riskAssessment) &&
                Objects.equals(generatedAt, changelog.generatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, apiName, fromVersion, toVersion, changes, breakingChanges, riskAssessment, generatedAt);
    }

    @Override
    public String toString() {
        return "Changelog{" +
                "id='" + id + '\'' +
                ", apiName='" + apiName + '\'' +
                ", fromVersion='" + fromVersion + '\'' +
                ", toVersion='" + toVersion + '\'' +
                ", changes=" + changes.size() +
                ", breakingChanges=" + breakingChanges.size() +
                ", riskAssessment=" + riskAssessment +
                ", generatedAt=" + generatedAt +
                '}';
    }

    public static class Builder {
        private String id;
        private String apiName;
        private String fromVersion;
        private String toVersion;
        private List<Change> changes = new ArrayList<>();
        private List<BreakingChange> breakingChanges = new ArrayList<>();
        private RiskAssessment riskAssessment;
        private LocalDateTime generatedAt;

        public Builder id(String id) {
            this.id = id;
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

        public Builder changes(List<Change> changes) {
            this.changes = changes != null ? new ArrayList<>(changes) : new ArrayList<>();
            return this;
        }

        public Builder addChange(Change change) {
            this.changes.add(change);
            return this;
        }

        public Builder breakingChanges(List<BreakingChange> breakingChanges) {
            this.breakingChanges = breakingChanges != null ? new ArrayList<>(breakingChanges) : new ArrayList<>();
            return this;
        }

        public Builder addBreakingChange(BreakingChange breakingChange) {
            this.breakingChanges.add(breakingChange);
            return this;
        }

        public Builder riskAssessment(RiskAssessment riskAssessment) {
            this.riskAssessment = riskAssessment;
            return this;
        }

        public Builder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public Changelog build() {
            return new Changelog(id, apiName, fromVersion, toVersion, changes, breakingChanges, riskAssessment, generatedAt);
        }
    }
}
