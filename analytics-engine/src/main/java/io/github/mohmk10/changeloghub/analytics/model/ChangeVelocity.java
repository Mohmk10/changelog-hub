package io.github.mohmk10.changeloghub.analytics.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the velocity of API changes over time.
 */
public class ChangeVelocity {

    private String apiName;
    private double changesPerDay;
    private double changesPerWeek;
    private double changesPerMonth;
    private double breakingChangesPerRelease;
    private Duration averageTimeBetweenReleases;
    private boolean accelerating;
    private double accelerationRate;
    private int totalReleases;
    private int totalChanges;
    private int totalBreakingChanges;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime analyzedAt;

    public ChangeVelocity() {
        this.analyzedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public double getChangesPerDay() {
        return changesPerDay;
    }

    public void setChangesPerDay(double changesPerDay) {
        this.changesPerDay = changesPerDay;
    }

    public double getChangesPerWeek() {
        return changesPerWeek;
    }

    public void setChangesPerWeek(double changesPerWeek) {
        this.changesPerWeek = changesPerWeek;
    }

    public double getChangesPerMonth() {
        return changesPerMonth;
    }

    public void setChangesPerMonth(double changesPerMonth) {
        this.changesPerMonth = changesPerMonth;
    }

    public double getBreakingChangesPerRelease() {
        return breakingChangesPerRelease;
    }

    public void setBreakingChangesPerRelease(double breakingChangesPerRelease) {
        this.breakingChangesPerRelease = breakingChangesPerRelease;
    }

    public Duration getAverageTimeBetweenReleases() {
        return averageTimeBetweenReleases;
    }

    public void setAverageTimeBetweenReleases(Duration averageTimeBetweenReleases) {
        this.averageTimeBetweenReleases = averageTimeBetweenReleases;
    }

    public boolean isAccelerating() {
        return accelerating;
    }

    public void setAccelerating(boolean accelerating) {
        this.accelerating = accelerating;
    }

    public double getAccelerationRate() {
        return accelerationRate;
    }

    public void setAccelerationRate(double accelerationRate) {
        this.accelerationRate = accelerationRate;
    }

    public int getTotalReleases() {
        return totalReleases;
    }

    public void setTotalReleases(int totalReleases) {
        this.totalReleases = totalReleases;
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

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public boolean isDecelerating() {
        return !accelerating && accelerationRate < 0;
    }

    public boolean isStable() {
        return Math.abs(accelerationRate) < 0.1;
    }

    public long getAverageTimeBetweenReleasesDays() {
        return averageTimeBetweenReleases != null ? averageTimeBetweenReleases.toDays() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeVelocity that = (ChangeVelocity) o;
        return Double.compare(that.changesPerDay, changesPerDay) == 0 &&
                Objects.equals(apiName, that.apiName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiName, changesPerDay);
    }

    @Override
    public String toString() {
        return "ChangeVelocity{" +
                "apiName='" + apiName + '\'' +
                ", changesPerWeek=" + changesPerWeek +
                ", accelerating=" + accelerating +
                '}';
    }

    public static class Builder {
        private final ChangeVelocity velocity = new ChangeVelocity();

        public Builder apiName(String apiName) {
            velocity.apiName = apiName;
            return this;
        }

        public Builder changesPerDay(double value) {
            velocity.changesPerDay = value;
            return this;
        }

        public Builder changesPerWeek(double value) {
            velocity.changesPerWeek = value;
            return this;
        }

        public Builder changesPerMonth(double value) {
            velocity.changesPerMonth = value;
            return this;
        }

        public Builder breakingChangesPerRelease(double value) {
            velocity.breakingChangesPerRelease = value;
            return this;
        }

        public Builder averageTimeBetweenReleases(Duration duration) {
            velocity.averageTimeBetweenReleases = duration;
            return this;
        }

        public Builder accelerating(boolean accelerating) {
            velocity.accelerating = accelerating;
            return this;
        }

        public Builder accelerationRate(double rate) {
            velocity.accelerationRate = rate;
            return this;
        }

        public Builder totalReleases(int count) {
            velocity.totalReleases = count;
            return this;
        }

        public Builder totalChanges(int count) {
            velocity.totalChanges = count;
            return this;
        }

        public Builder totalBreakingChanges(int count) {
            velocity.totalBreakingChanges = count;
            return this;
        }

        public Builder periodStart(LocalDateTime start) {
            velocity.periodStart = start;
            return this;
        }

        public Builder periodEnd(LocalDateTime end) {
            velocity.periodEnd = end;
            return this;
        }

        public ChangeVelocity build() {
            return velocity;
        }
    }
}
