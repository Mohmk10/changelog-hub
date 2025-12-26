package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.util.StabilityGrade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StabilityReport {

    private String apiName;
    private StabilityScore currentStability;
    private StabilityScore previousStability;
    private List<StabilityScore.StabilityFactor> factors;
    private List<Insight> insights;
    private List<Recommendation> recommendations;
    private int versionsAnalyzed;
    private int breakingChangesTotal;
    private LocalDateTime generatedAt;

    public StabilityReport() {
        this.factors = new ArrayList<>();
        this.insights = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }

    public StabilityScore getCurrentStability() { return currentStability; }
    public void setCurrentStability(StabilityScore currentStability) { this.currentStability = currentStability; }

    public StabilityScore getPreviousStability() { return previousStability; }
    public void setPreviousStability(StabilityScore previousStability) { this.previousStability = previousStability; }

    public List<StabilityScore.StabilityFactor> getFactors() { return factors; }
    public void setFactors(List<StabilityScore.StabilityFactor> factors) {
        this.factors = factors != null ? new ArrayList<>(factors) : new ArrayList<>();
    }

    public List<Insight> getInsights() { return insights; }
    public void setInsights(List<Insight> insights) {
        this.insights = insights != null ? new ArrayList<>(insights) : new ArrayList<>();
    }

    public List<Recommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
    }

    public int getVersionsAnalyzed() { return versionsAnalyzed; }
    public void setVersionsAnalyzed(int versionsAnalyzed) { this.versionsAnalyzed = versionsAnalyzed; }

    public int getBreakingChangesTotal() { return breakingChangesTotal; }
    public void setBreakingChangesTotal(int breakingChangesTotal) { this.breakingChangesTotal = breakingChangesTotal; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getStabilityTrend() {
        if (currentStability == null || previousStability == null) {
            return "N/A";
        }
        int diff = currentStability.getScore() - previousStability.getScore();
        if (diff > 5) return "Improving";
        if (diff < -5) return "Degrading";
        return "Stable";
    }

    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Stability Report: ").append(apiName).append("\n\n");

        sb.append("## Summary\n\n");
        if (currentStability != null) {
            StabilityGrade grade = currentStability.getGrade();
            sb.append("- **Current Grade:** ").append(grade != null ? grade.name() : "N/A")
                    .append(" (").append(currentStability.getScore()).append("/100)\n");
            if (grade != null) {
                sb.append("- **Status:** ").append(grade.getLabel()).append("\n");
            }
        }
        sb.append("- **Versions Analyzed:** ").append(versionsAnalyzed).append("\n");
        sb.append("- **Total Breaking Changes:** ").append(breakingChangesTotal).append("\n");
        sb.append("- **Trend:** ").append(getStabilityTrend()).append("\n\n");

        if (!factors.isEmpty()) {
            sb.append("## Stability Factors\n\n");
            sb.append("| Factor | Score | Weight | Impact |\n");
            sb.append("|--------|-------|--------|--------|\n");
            for (StabilityScore.StabilityFactor factor : factors) {
                sb.append("| ").append(factor.getName())
                        .append(" | ").append(String.format("%.1f", factor.getScore()))
                        .append(" | ").append(String.format("%.0f%%", factor.getWeight() * 100))
                        .append(" | ").append(String.format("%.1f", factor.getContribution()))
                        .append(" |\n");
            }
            sb.append("\n");
        }

        if (!insights.isEmpty()) {
            sb.append("## Insights\n\n");
            for (Insight insight : insights) {
                sb.append("- **").append(insight.getTitle()).append(":** ")
                        .append(insight.getDescription()).append("\n");
            }
            sb.append("\n");
        }

        if (!recommendations.isEmpty()) {
            sb.append("## Recommendations\n\n");
            for (Recommendation rec : recommendations) {
                sb.append("- **").append(rec.getTitle()).append(":** ")
                        .append(rec.getAction()).append("\n");
            }
        }

        return sb.toString();
    }

    public static class Builder {
        private final StabilityReport report = new StabilityReport();

        public Builder apiName(String apiName) { report.apiName = apiName; return this; }
        public Builder currentStability(StabilityScore score) { report.currentStability = score; return this; }
        public Builder previousStability(StabilityScore score) { report.previousStability = score; return this; }
        public Builder factors(List<StabilityScore.StabilityFactor> factors) { report.setFactors(factors); return this; }
        public Builder insights(List<Insight> insights) { report.setInsights(insights); return this; }
        public Builder recommendations(List<Recommendation> recs) { report.setRecommendations(recs); return this; }
        public Builder versionsAnalyzed(int count) { report.versionsAnalyzed = count; return this; }
        public Builder breakingChangesTotal(int count) { report.breakingChangesTotal = count; return this; }
        public StabilityReport build() { return report; }
    }
}
