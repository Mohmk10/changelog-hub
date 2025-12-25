package io.github.mohmk10.changeloghub.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RiskAssessment {

    private int overallScore;
    private RiskLevel level;
    private int breakingChangesCount;
    private int totalChangesCount;
    private Map<Severity, Integer> changesBySeverity;
    private String recommendation;
    private String semverRecommendation;

    public RiskAssessment() {
        this.changesBySeverity = new HashMap<>();
    }

    public RiskAssessment(int overallScore, RiskLevel level, int breakingChangesCount,
                          int totalChangesCount, Map<Severity, Integer> changesBySeverity,
                          String recommendation, String semverRecommendation) {
        this.overallScore = overallScore;
        this.level = level;
        this.breakingChangesCount = breakingChangesCount;
        this.totalChangesCount = totalChangesCount;
        this.changesBySeverity = changesBySeverity != null ? new HashMap<>(changesBySeverity) : new HashMap<>();
        this.recommendation = recommendation;
        this.semverRecommendation = semverRecommendation;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public RiskLevel getLevel() {
        return level;
    }

    public void setLevel(RiskLevel level) {
        this.level = level;
    }

    public int getBreakingChangesCount() {
        return breakingChangesCount;
    }

    public void setBreakingChangesCount(int breakingChangesCount) {
        this.breakingChangesCount = breakingChangesCount;
    }

    public int getTotalChangesCount() {
        return totalChangesCount;
    }

    public void setTotalChangesCount(int totalChangesCount) {
        this.totalChangesCount = totalChangesCount;
    }

    public Map<Severity, Integer> getChangesBySeverity() {
        return changesBySeverity;
    }

    public void setChangesBySeverity(Map<Severity, Integer> changesBySeverity) {
        this.changesBySeverity = changesBySeverity != null ? new HashMap<>(changesBySeverity) : new HashMap<>();
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getSemverRecommendation() {
        return semverRecommendation;
    }

    public void setSemverRecommendation(String semverRecommendation) {
        this.semverRecommendation = semverRecommendation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskAssessment that = (RiskAssessment) o;
        return overallScore == that.overallScore &&
                breakingChangesCount == that.breakingChangesCount &&
                totalChangesCount == that.totalChangesCount &&
                level == that.level &&
                Objects.equals(changesBySeverity, that.changesBySeverity) &&
                Objects.equals(recommendation, that.recommendation) &&
                Objects.equals(semverRecommendation, that.semverRecommendation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overallScore, level, breakingChangesCount, totalChangesCount,
                changesBySeverity, recommendation, semverRecommendation);
    }

    @Override
    public String toString() {
        return "RiskAssessment{" +
                "overallScore=" + overallScore +
                ", level=" + level +
                ", breakingChangesCount=" + breakingChangesCount +
                ", totalChangesCount=" + totalChangesCount +
                ", changesBySeverity=" + changesBySeverity +
                ", recommendation='" + recommendation + '\'' +
                ", semverRecommendation='" + semverRecommendation + '\'' +
                '}';
    }
}
