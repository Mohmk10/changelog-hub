package io.github.mohmk10.changeloghub.analytics.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.mohmk10.changeloghub.analytics.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApiEvolutionReport {

    private String apiName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ApiEvolution.VersionSummary> versions;
    private StabilityScore overallStability;
    private RiskTrend riskTrend;
    private ChangeVelocity velocity;
    private List<Insight> insights;
    private List<Recommendation> recommendations;
    private LocalDateTime generatedAt;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public ApiEvolutionReport() {
        this.versions = new ArrayList<>();
        this.insights = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<ApiEvolution.VersionSummary> getVersions() { return versions; }
    public void setVersions(List<ApiEvolution.VersionSummary> versions) {
        this.versions = versions != null ? new ArrayList<>(versions) : new ArrayList<>();
    }

    public StabilityScore getOverallStability() { return overallStability; }
    public void setOverallStability(StabilityScore overallStability) { this.overallStability = overallStability; }

    public RiskTrend getRiskTrend() { return riskTrend; }
    public void setRiskTrend(RiskTrend riskTrend) { this.riskTrend = riskTrend; }

    public ChangeVelocity getVelocity() { return velocity; }
    public void setVelocity(ChangeVelocity velocity) { this.velocity = velocity; }

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

    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();

        sb.append("# API Evolution Report: ").append(apiName).append("\n\n");
        sb.append("**Period:** ").append(startDate).append(" to ").append(endDate).append("\n\n");

        sb.append("## Summary\n\n");
        if (overallStability != null) {
            sb.append("- **Stability Grade:** ").append(overallStability.getGrade())
                    .append(" (").append(overallStability.getScore()).append("/100)\n");
        }
        if (riskTrend != null) {
            sb.append("- **Risk Trend:** ").append(riskTrend.getDirection()).append("\n");
        }
        if (velocity != null) {
            sb.append("- **Changes/Week:** ").append(String.format("%.1f", velocity.getChangesPerWeek())).append("\n");
        }
        sb.append("- **Versions Analyzed:** ").append(versions.size()).append("\n\n");

        if (!versions.isEmpty()) {
            sb.append("## Version History\n\n");
            sb.append("| Version | Date | Changes | Breaking |\n");
            sb.append("|---------|------|---------|----------|\n");
            for (ApiEvolution.VersionSummary v : versions) {
                sb.append("| ").append(v.getVersion())
                        .append(" | ").append(v.getReleaseDate())
                        .append(" | ").append(v.getTotalChanges())
                        .append(" | ").append(v.getBreakingChanges())
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
                        .append(rec.getDescription()).append("\n");
            }
        }

        return sb.toString();
    }

    public String toJson() {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public String toHtml() {
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>\n<html>\n<head>\n");
        sb.append("<title>API Evolution Report: ").append(escapeHtml(apiName)).append("</title>\n");
        sb.append("<style>\n");
        sb.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        sb.append("h1 { color: #333; }\n");
        sb.append("table { border-collapse: collapse; width: 100%; }\n");
        sb.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        sb.append("th { background-color: #4CAF50; color: white; }\n");
        sb.append(".insight { background-color: #fff3cd; padding: 10px; margin: 5px 0; }\n");
        sb.append(".recommendation { background-color: #d4edda; padding: 10px; margin: 5px 0; }\n");
        sb.append("</style>\n</head>\n<body>\n");

        sb.append("<h1>API Evolution Report: ").append(escapeHtml(apiName)).append("</h1>\n");
        sb.append("<p><strong>Period:</strong> ").append(startDate).append(" to ").append(endDate).append("</p>\n");

        sb.append("<h2>Summary</h2>\n<ul>\n");
        if (overallStability != null) {
            sb.append("<li><strong>Stability:</strong> Grade ").append(overallStability.getGrade())
                    .append(" (").append(overallStability.getScore()).append("/100)</li>\n");
        }
        if (riskTrend != null) {
            sb.append("<li><strong>Risk Trend:</strong> ").append(riskTrend.getDirection()).append("</li>\n");
        }
        sb.append("</ul>\n");

        if (!versions.isEmpty()) {
            sb.append("<h2>Version History</h2>\n<table>\n");
            sb.append("<tr><th>Version</th><th>Date</th><th>Changes</th><th>Breaking</th></tr>\n");
            for (ApiEvolution.VersionSummary v : versions) {
                sb.append("<tr><td>").append(v.getVersion())
                        .append("</td><td>").append(v.getReleaseDate())
                        .append("</td><td>").append(v.getTotalChanges())
                        .append("</td><td>").append(v.getBreakingChanges())
                        .append("</td></tr>\n");
            }
            sb.append("</table>\n");
        }

        sb.append("</body>\n</html>");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static class Builder {
        private final ApiEvolutionReport report = new ApiEvolutionReport();

        public Builder apiName(String apiName) { report.apiName = apiName; return this; }
        public Builder startDate(LocalDate date) { report.startDate = date; return this; }
        public Builder endDate(LocalDate date) { report.endDate = date; return this; }
        public Builder versions(List<ApiEvolution.VersionSummary> versions) { report.setVersions(versions); return this; }
        public Builder overallStability(StabilityScore score) { report.overallStability = score; return this; }
        public Builder riskTrend(RiskTrend trend) { report.riskTrend = trend; return this; }
        public Builder velocity(ChangeVelocity velocity) { report.velocity = velocity; return this; }
        public Builder insights(List<Insight> insights) { report.setInsights(insights); return this; }
        public Builder recommendations(List<Recommendation> recs) { report.setRecommendations(recs); return this; }
        public ApiEvolutionReport build() { return report; }
    }
}
