package io.github.mohmk10.changeloghub.analytics.model;

import io.github.mohmk10.changeloghub.analytics.util.StabilityGrade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stability score for an API based on its change history.
 */
public class StabilityScore {

    private String apiName;
    private int score;
    private StabilityGrade grade;
    private double breakingChangeRatio;
    private double timeBetweenBreakingChangesScore;
    private double deprecationManagementScore;
    private double semverComplianceScore;
    private int totalChangesAnalyzed;
    private int breakingChangesCount;
    private int periodsAnalyzed;
    private List<StabilityFactor> factors;
    private LocalDateTime calculatedAt;

    public StabilityScore() {
        this.factors = new ArrayList<>();
        this.calculatedAt = LocalDateTime.now();
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));
        this.grade = StabilityGrade.fromScore(this.score);
    }

    public StabilityGrade getGrade() {
        return grade;
    }

    public void setGrade(StabilityGrade grade) {
        this.grade = grade;
    }

    public double getBreakingChangeRatio() {
        return breakingChangeRatio;
    }

    public void setBreakingChangeRatio(double breakingChangeRatio) {
        this.breakingChangeRatio = breakingChangeRatio;
    }

    public double getTimeBetweenBreakingChangesScore() {
        return timeBetweenBreakingChangesScore;
    }

    public void setTimeBetweenBreakingChangesScore(double timeBetweenBreakingChangesScore) {
        this.timeBetweenBreakingChangesScore = timeBetweenBreakingChangesScore;
    }

    public double getDeprecationManagementScore() {
        return deprecationManagementScore;
    }

    public void setDeprecationManagementScore(double deprecationManagementScore) {
        this.deprecationManagementScore = deprecationManagementScore;
    }

    public double getSemverComplianceScore() {
        return semverComplianceScore;
    }

    public void setSemverComplianceScore(double semverComplianceScore) {
        this.semverComplianceScore = semverComplianceScore;
    }

    public int getTotalChangesAnalyzed() {
        return totalChangesAnalyzed;
    }

    public void setTotalChangesAnalyzed(int totalChangesAnalyzed) {
        this.totalChangesAnalyzed = totalChangesAnalyzed;
    }

    public int getBreakingChangesCount() {
        return breakingChangesCount;
    }

    public void setBreakingChangesCount(int breakingChangesCount) {
        this.breakingChangesCount = breakingChangesCount;
    }

    public int getPeriodsAnalyzed() {
        return periodsAnalyzed;
    }

    public void setPeriodsAnalyzed(int periodsAnalyzed) {
        this.periodsAnalyzed = periodsAnalyzed;
    }

    public List<StabilityFactor> getFactors() {
        return factors;
    }

    public void setFactors(List<StabilityFactor> factors) {
        this.factors = factors != null ? new ArrayList<>(factors) : new ArrayList<>();
    }

    public void addFactor(StabilityFactor factor) {
        this.factors.add(factor);
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public boolean isAcceptable() {
        return grade != null && grade.isAcceptable();
    }

    public boolean isPoor() {
        return grade != null && grade.isPoor();
    }

    public boolean isExcellent() {
        return grade != null && grade == StabilityGrade.A;
    }

    public double getAvgDaysBetweenBreaking() {
        return timeBetweenBreakingChangesScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StabilityScore that = (StabilityScore) o;
        return score == that.score &&
                Objects.equals(apiName, that.apiName) &&
                grade == that.grade;
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiName, score, grade);
    }

    @Override
    public String toString() {
        return "StabilityScore{" +
                "apiName='" + apiName + '\'' +
                ", score=" + score +
                ", grade=" + grade +
                '}';
    }

    /**
     * Represents a factor contributing to the stability score.
     */
    public static class StabilityFactor {
        private String name;
        private double weight;
        private double score;
        private double contribution;
        private String description;

        public StabilityFactor() {}

        public StabilityFactor(String name, double weight, double score, String description) {
            this.name = name;
            this.weight = weight;
            this.score = score;
            this.contribution = weight * score;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public double getContribution() {
            return contribution;
        }

        public void setContribution(double contribution) {
            this.contribution = contribution;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Builder {
        private final StabilityScore stabilityScore = new StabilityScore();

        public Builder apiName(String apiName) {
            stabilityScore.apiName = apiName;
            return this;
        }

        public Builder score(int score) {
            stabilityScore.setScore(score);
            return this;
        }

        public Builder breakingChangeRatio(double ratio) {
            stabilityScore.breakingChangeRatio = ratio;
            return this;
        }

        public Builder timeBetweenBreakingChangesScore(double score) {
            stabilityScore.timeBetweenBreakingChangesScore = score;
            return this;
        }

        public Builder deprecationManagementScore(double score) {
            stabilityScore.deprecationManagementScore = score;
            return this;
        }

        public Builder semverComplianceScore(double score) {
            stabilityScore.semverComplianceScore = score;
            return this;
        }

        public Builder totalChangesAnalyzed(int total) {
            stabilityScore.totalChangesAnalyzed = total;
            return this;
        }

        public Builder breakingChangesCount(int count) {
            stabilityScore.breakingChangesCount = count;
            return this;
        }

        public Builder periodsAnalyzed(int periods) {
            stabilityScore.periodsAnalyzed = periods;
            return this;
        }

        public Builder grade(StabilityGrade grade) {
            stabilityScore.grade = grade;
            return this;
        }

        public Builder factors(List<StabilityFactor> factors) {
            stabilityScore.setFactors(factors);
            return this;
        }

        public Builder addFactor(StabilityFactor factor) {
            stabilityScore.addFactor(factor);
            return this;
        }

        public Builder calculatedAt(LocalDateTime calculatedAt) {
            stabilityScore.calculatedAt = calculatedAt;
            return this;
        }

        public StabilityScore build() {
            if (stabilityScore.grade == null && stabilityScore.score > 0) {
                stabilityScore.grade = StabilityGrade.fromScore(stabilityScore.score);
            }
            return stabilityScore;
        }
    }
}
