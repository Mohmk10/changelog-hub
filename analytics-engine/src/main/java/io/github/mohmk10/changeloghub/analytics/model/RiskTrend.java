package io.github.mohmk10.changeloghub.analytics.model;

import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;
import io.github.mohmk10.changeloghub.core.model.RiskLevel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RiskTrend {

    private String apiName;
    private TrendDirection direction;
    private int currentRiskScore;
    private int previousRiskScore;
    private double changePercentage;
    private int projectedNextScore;
    private RiskLevel currentRiskLevel;
    private RiskLevel projectedRiskLevel;
    private List<RiskDataPoint> dataPoints;
    private List<PeriodRisk> periodRisks;
    private double slope;
    private int periodsAnalyzed;
    private LocalDateTime analyzedAt;

    public RiskTrend() {
        this.dataPoints = new ArrayList<>();
        this.periodRisks = new ArrayList<>();
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

    public TrendDirection getDirection() {
        return direction;
    }

    public void setDirection(TrendDirection direction) {
        this.direction = direction;
    }

    public int getCurrentRiskScore() {
        return currentRiskScore;
    }

    public void setCurrentRiskScore(int currentRiskScore) {
        this.currentRiskScore = currentRiskScore;
    }

    public int getPreviousRiskScore() {
        return previousRiskScore;
    }

    public void setPreviousRiskScore(int previousRiskScore) {
        this.previousRiskScore = previousRiskScore;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    public void setChangePercentage(double changePercentage) {
        this.changePercentage = changePercentage;
    }

    public int getProjectedNextScore() {
        return projectedNextScore;
    }

    public void setProjectedNextScore(int projectedNextScore) {
        this.projectedNextScore = projectedNextScore;
    }

    public RiskLevel getCurrentRiskLevel() {
        return currentRiskLevel;
    }

    public void setCurrentRiskLevel(RiskLevel currentRiskLevel) {
        this.currentRiskLevel = currentRiskLevel;
    }

    public RiskLevel getProjectedRiskLevel() {
        return projectedRiskLevel;
    }

    public void setProjectedRiskLevel(RiskLevel projectedRiskLevel) {
        this.projectedRiskLevel = projectedRiskLevel;
    }

    public List<RiskDataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<RiskDataPoint> dataPoints) {
        this.dataPoints = dataPoints != null ? new ArrayList<>(dataPoints) : new ArrayList<>();
    }

    public void addDataPoint(RiskDataPoint dataPoint) {
        this.dataPoints.add(dataPoint);
    }

    public List<PeriodRisk> getPeriodRisks() {
        return periodRisks;
    }

    public void setPeriodRisks(List<PeriodRisk> periodRisks) {
        this.periodRisks = periodRisks != null ? new ArrayList<>(periodRisks) : new ArrayList<>();
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public int getRiskChange() {
        return currentRiskScore - previousRiskScore;
    }

    public int getPeriodsAnalyzed() {
        return periodsAnalyzed;
    }

    public void setPeriodsAnalyzed(int periodsAnalyzed) {
        this.periodsAnalyzed = periodsAnalyzed;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public boolean isImproving() {
        return direction == TrendDirection.IMPROVING;
    }

    public boolean isDegrading() {
        return direction == TrendDirection.DEGRADING;
    }

    public boolean isStable() {
        return direction == TrendDirection.STABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskTrend riskTrend = (RiskTrend) o;
        return currentRiskScore == riskTrend.currentRiskScore &&
                Objects.equals(apiName, riskTrend.apiName) &&
                direction == riskTrend.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiName, direction, currentRiskScore);
    }

    @Override
    public String toString() {
        return "RiskTrend{" +
                "apiName='" + apiName + '\'' +
                ", direction=" + direction +
                ", currentRiskScore=" + currentRiskScore +
                ", changePercentage=" + changePercentage +
                '}';
    }

    public static class RiskDataPoint {
        private LocalDateTime timestamp;
        private int riskScore;
        private String version;
        private int breakingChanges;

        public RiskDataPoint() {}

        public RiskDataPoint(LocalDateTime timestamp, int riskScore, String version, int breakingChanges) {
            this.timestamp = timestamp;
            this.riskScore = riskScore;
            this.version = version;
            this.breakingChanges = breakingChanges;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public int getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(int riskScore) {
            this.riskScore = riskScore;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public int getBreakingChanges() {
            return breakingChanges;
        }

        public void setBreakingChanges(int breakingChanges) {
            this.breakingChanges = breakingChanges;
        }
    }

    public static class PeriodRisk {
        private String periodLabel;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private int riskScore;
        private int breakingChangeCount;

        public PeriodRisk() {}

        public String getPeriodLabel() { return periodLabel; }
        public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }

        public java.time.LocalDate getStartDate() { return startDate; }
        public void setStartDate(java.time.LocalDate startDate) { this.startDate = startDate; }

        public java.time.LocalDate getEndDate() { return endDate; }
        public void setEndDate(java.time.LocalDate endDate) { this.endDate = endDate; }

        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

        public int getBreakingChangeCount() { return breakingChangeCount; }
        public void setBreakingChangeCount(int breakingChangeCount) { this.breakingChangeCount = breakingChangeCount; }
    }

    public static class Builder {
        private final RiskTrend trend = new RiskTrend();

        public Builder apiName(String apiName) {
            trend.apiName = apiName;
            return this;
        }

        public Builder direction(TrendDirection direction) {
            trend.direction = direction;
            return this;
        }

        public Builder currentRiskScore(int score) {
            trend.currentRiskScore = score;
            return this;
        }

        public Builder previousRiskScore(int score) {
            trend.previousRiskScore = score;
            return this;
        }

        public Builder changePercentage(double percentage) {
            trend.changePercentage = percentage;
            return this;
        }

        public Builder projectedNextScore(int score) {
            trend.projectedNextScore = score;
            return this;
        }

        public Builder currentRiskLevel(RiskLevel level) {
            trend.currentRiskLevel = level;
            return this;
        }

        public Builder projectedRiskLevel(RiskLevel level) {
            trend.projectedRiskLevel = level;
            return this;
        }

        public Builder dataPoints(List<RiskDataPoint> dataPoints) {
            trend.setDataPoints(dataPoints);
            return this;
        }

        public Builder addDataPoint(RiskDataPoint dataPoint) {
            trend.addDataPoint(dataPoint);
            return this;
        }

        public Builder periodsAnalyzed(int periods) {
            trend.periodsAnalyzed = periods;
            return this;
        }

        public Builder periodRisks(List<PeriodRisk> periodRisks) {
            trend.setPeriodRisks(periodRisks);
            return this;
        }

        public Builder slope(double slope) {
            trend.slope = slope;
            return this;
        }

        public Builder analyzedAt(LocalDateTime analyzedAt) {
            trend.analyzedAt = analyzedAt;
            return this;
        }

        public RiskTrend build() {
            return trend;
        }
    }
}
