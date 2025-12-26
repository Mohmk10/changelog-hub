package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Report on API risk trends over time.
 */
public class RiskTrendReport {

    private String apiName;
    private LocalDate startDate;
    private LocalDate endDate;
    private RiskTrend overallTrend;
    private List<RiskDataPoint> dataPoints;
    private List<RiskTrend.PeriodRisk> periodRisks;
    private List<Insight> insights;
    private List<Recommendation> recommendations;
    private LocalDateTime generatedAt;

    /**
     * Risk data point for trend visualization.
     */
    public static class RiskDataPoint {
        private LocalDate date;
        private int riskScore;
        private int breakingChanges;
        private String version;

        public RiskDataPoint() {}

        public RiskDataPoint(LocalDate date, int riskScore, int breakingChanges, String version) {
            this.date = date;
            this.riskScore = riskScore;
            this.breakingChanges = breakingChanges;
            this.version = version;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        public int getBreakingChanges() { return breakingChanges; }
        public void setBreakingChanges(int breakingChanges) { this.breakingChanges = breakingChanges; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }

    public RiskTrendReport() {
        this.dataPoints = new ArrayList<>();
        this.periodRisks = new ArrayList<>();
        this.insights = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public RiskTrend getOverallTrend() { return overallTrend; }
    public void setOverallTrend(RiskTrend overallTrend) { this.overallTrend = overallTrend; }

    public List<RiskDataPoint> getDataPoints() { return dataPoints; }
    public void setDataPoints(List<RiskDataPoint> dataPoints) {
        this.dataPoints = dataPoints != null ? new ArrayList<>(dataPoints) : new ArrayList<>();
    }

    public List<RiskTrend.PeriodRisk> getPeriodRisks() { return periodRisks; }
    public void setPeriodRisks(List<RiskTrend.PeriodRisk> periodRisks) {
        this.periodRisks = periodRisks != null ? new ArrayList<>(periodRisks) : new ArrayList<>();
    }

    public List<Insight> getInsights() { return insights; }
    public void setInsights(List<Insight> insights) {
        this.insights = insights != null ? new ArrayList<>(insights) : new ArrayList<>();
    }

    public List<Recommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
    }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    /**
     * Convert report to Markdown format.
     */
    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Risk Trend Report: ").append(apiName).append("\n\n");
        sb.append("**Period:** ").append(startDate).append(" to ").append(endDate).append("\n\n");

        // Summary
        sb.append("## Summary\n\n");
        if (overallTrend != null) {
            TrendDirection direction = overallTrend.getDirection();
            sb.append("- **Overall Trend:** ").append(direction != null ? direction.getLabel() : "N/A").append("\n");
            sb.append("- **Current Risk Score:** ").append(overallTrend.getCurrentRiskScore()).append("/100\n");
            sb.append("- **Previous Risk Score:** ").append(overallTrend.getPreviousRiskScore()).append("/100\n");
            sb.append("- **Risk Change:** ").append(overallTrend.getRiskChange() >= 0 ? "+" : "")
                    .append(overallTrend.getRiskChange()).append("\n\n");
        }

        // Risk Data Points
        if (!dataPoints.isEmpty()) {
            sb.append("## Risk Timeline\n\n");
            sb.append("| Date | Version | Risk Score | Breaking Changes |\n");
            sb.append("|------|---------|------------|------------------|\n");
            for (RiskDataPoint point : dataPoints) {
                sb.append("| ").append(point.getDate())
                        .append(" | ").append(point.getVersion())
                        .append(" | ").append(point.getRiskScore())
                        .append(" | ").append(point.getBreakingChanges())
                        .append(" |\n");
            }
            sb.append("\n");
        }

        // Period Analysis
        if (!periodRisks.isEmpty()) {
            sb.append("## Period Analysis\n\n");
            for (RiskTrend.PeriodRisk period : periodRisks) {
                sb.append("- **").append(period.getPeriodLabel()).append(":** ")
                        .append("Score ").append(period.getRiskScore())
                        .append(" (").append(period.getBreakingChangeCount()).append(" breaking changes)\n");
            }
            sb.append("\n");
        }

        // Insights
        if (!insights.isEmpty()) {
            sb.append("## Insights\n\n");
            for (Insight insight : insights) {
                sb.append("- **").append(insight.getTitle()).append(":** ")
                        .append(insight.getDescription()).append("\n");
            }
            sb.append("\n");
        }

        // Recommendations
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
        private final RiskTrendReport report = new RiskTrendReport();

        public Builder apiName(String apiName) { report.apiName = apiName; return this; }
        public Builder startDate(LocalDate date) { report.startDate = date; return this; }
        public Builder endDate(LocalDate date) { report.endDate = date; return this; }
        public Builder overallTrend(RiskTrend trend) { report.overallTrend = trend; return this; }
        public Builder dataPoints(List<RiskDataPoint> points) { report.setDataPoints(points); return this; }
        public Builder periodRisks(List<RiskTrend.PeriodRisk> risks) { report.setPeriodRisks(risks); return this; }
        public Builder insights(List<Insight> insights) { report.setInsights(insights); return this; }
        public Builder recommendations(List<Recommendation> recs) { report.setRecommendations(recs); return this; }
        public RiskTrendReport build() { return report; }
    }
}
