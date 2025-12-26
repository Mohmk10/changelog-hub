package io.github.mohmk10.changeloghub.analytics.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApiEvolution {

    private String apiName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<VersionSummary> versions;
    private int totalVersions;
    private int totalChanges;
    private int totalBreakingChanges;
    private double averageChangesPerVersion;
    private double breakingChangeRate;
    private LocalDateTime analyzedAt;

    public ApiEvolution() {
        this.versions = new ArrayList<>();
        this.analyzedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<VersionSummary> getVersions() {
        return versions;
    }

    public void setVersions(List<VersionSummary> versions) {
        this.versions = versions != null ? new ArrayList<>(versions) : new ArrayList<>();
    }

    public void addVersion(VersionSummary version) {
        this.versions.add(version);
    }

    public int getTotalVersions() {
        return totalVersions;
    }

    public void setTotalVersions(int totalVersions) {
        this.totalVersions = totalVersions;
    }

    public int getTotalChanges() {
        return totalChanges;
    }

    public void setTotalChanges(int totalChanges) {
        this.totalChanges = totalChanges;
    }

    public int getTotalBreakingChanges() {
        return totalBreakingChanges;
    }

    public void setTotalBreakingChanges(int totalBreakingChanges) {
        this.totalBreakingChanges = totalBreakingChanges;
    }

    public double getAverageChangesPerVersion() {
        return averageChangesPerVersion;
    }

    public void setAverageChangesPerVersion(double averageChangesPerVersion) {
        this.averageChangesPerVersion = averageChangesPerVersion;
    }

    public double getBreakingChangeRate() {
        return breakingChangeRate;
    }

    public void setBreakingChangeRate(double breakingChangeRate) {
        this.breakingChangeRate = breakingChangeRate;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiEvolution that = (ApiEvolution) o;
        return Objects.equals(apiName, that.apiName) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiName, startDate, endDate);
    }

    public static class VersionSummary {
        private String version;
        private LocalDate releaseDate;
        private int totalChanges;
        private int breakingChanges;
        private int addedEndpoints;
        private int removedEndpoints;
        private int modifiedEndpoints;
        private int deprecatedEndpoints;

        public VersionSummary() {}

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public LocalDate getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(LocalDate releaseDate) {
            this.releaseDate = releaseDate;
        }

        public int getTotalChanges() {
            return totalChanges;
        }

        public void setTotalChanges(int totalChanges) {
            this.totalChanges = totalChanges;
        }

        public int getBreakingChanges() {
            return breakingChanges;
        }

        public void setBreakingChanges(int breakingChanges) {
            this.breakingChanges = breakingChanges;
        }

        public int getAddedEndpoints() {
            return addedEndpoints;
        }

        public void setAddedEndpoints(int addedEndpoints) {
            this.addedEndpoints = addedEndpoints;
        }

        public int getRemovedEndpoints() {
            return removedEndpoints;
        }

        public void setRemovedEndpoints(int removedEndpoints) {
            this.removedEndpoints = removedEndpoints;
        }

        public int getModifiedEndpoints() {
            return modifiedEndpoints;
        }

        public void setModifiedEndpoints(int modifiedEndpoints) {
            this.modifiedEndpoints = modifiedEndpoints;
        }

        public int getDeprecatedEndpoints() {
            return deprecatedEndpoints;
        }

        public void setDeprecatedEndpoints(int deprecatedEndpoints) {
            this.deprecatedEndpoints = deprecatedEndpoints;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final VersionSummary summary = new VersionSummary();

            public Builder version(String version) {
                summary.version = version;
                return this;
            }

            public Builder releaseDate(LocalDate date) {
                summary.releaseDate = date;
                return this;
            }

            public Builder totalChanges(int count) {
                summary.totalChanges = count;
                return this;
            }

            public Builder breakingChanges(int count) {
                summary.breakingChanges = count;
                return this;
            }

            public Builder addedEndpoints(int count) {
                summary.addedEndpoints = count;
                return this;
            }

            public Builder removedEndpoints(int count) {
                summary.removedEndpoints = count;
                return this;
            }

            public Builder modifiedEndpoints(int count) {
                summary.modifiedEndpoints = count;
                return this;
            }

            public Builder deprecatedEndpoints(int count) {
                summary.deprecatedEndpoints = count;
                return this;
            }

            public VersionSummary build() {
                return summary;
            }
        }
    }

    public static class Builder {
        private final ApiEvolution evolution = new ApiEvolution();

        public Builder apiName(String apiName) {
            evolution.apiName = apiName;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            evolution.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            evolution.endDate = endDate;
            return this;
        }

        public Builder versions(List<VersionSummary> versions) {
            evolution.setVersions(versions);
            return this;
        }

        public Builder addVersion(VersionSummary version) {
            evolution.addVersion(version);
            return this;
        }

        public Builder totalVersions(int total) {
            evolution.totalVersions = total;
            return this;
        }

        public Builder totalChanges(int total) {
            evolution.totalChanges = total;
            return this;
        }

        public Builder totalBreakingChanges(int total) {
            evolution.totalBreakingChanges = total;
            return this;
        }

        public Builder averageChangesPerVersion(double avg) {
            evolution.averageChangesPerVersion = avg;
            return this;
        }

        public Builder breakingChangeRate(double rate) {
            evolution.breakingChangeRate = rate;
            return this;
        }

        public ApiEvolution build() {
            return evolution;
        }
    }
}
