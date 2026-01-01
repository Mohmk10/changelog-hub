package io.github.mohmk10.changeloghub.api.dto;

import java.util.Map;

public class AnalyticsResponse {
    private Long totalComparisons;
    private Long totalBreakingChanges;
    private Double averageRiskScore;
    private Map<String, Long> changesBySeverity;
    private Map<String, Long> comparisonsByFormat;
    private Map<String, Long> comparisonsByMonth;

    public AnalyticsResponse() {}

    public Long getTotalComparisons() { return totalComparisons; }
    public void setTotalComparisons(Long totalComparisons) { this.totalComparisons = totalComparisons; }

    public Long getTotalBreakingChanges() { return totalBreakingChanges; }
    public void setTotalBreakingChanges(Long totalBreakingChanges) { this.totalBreakingChanges = totalBreakingChanges; }

    public Double getAverageRiskScore() { return averageRiskScore; }
    public void setAverageRiskScore(Double averageRiskScore) { this.averageRiskScore = averageRiskScore; }

    public Map<String, Long> getChangesBySeverity() { return changesBySeverity; }
    public void setChangesBySeverity(Map<String, Long> changesBySeverity) { this.changesBySeverity = changesBySeverity; }

    public Map<String, Long> getComparisonsByFormat() { return comparisonsByFormat; }
    public void setComparisonsByFormat(Map<String, Long> comparisonsByFormat) { this.comparisonsByFormat = comparisonsByFormat; }

    public Map<String, Long> getComparisonsByMonth() { return comparisonsByMonth; }
    public void setComparisonsByMonth(Map<String, Long> comparisonsByMonth) { this.comparisonsByMonth = comparisonsByMonth; }
}
