package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.analytics.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TechnicalDebtReport {

    private String apiName;
    private TechnicalDebt debt;
    private List<TechnicalDebt.DebtItem> items;
    private List<Recommendation> recommendations;
    private int priorityScore;
    private LocalDateTime generatedAt;

    public TechnicalDebtReport() {
        this.items = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }

    public TechnicalDebt getDebt() { return debt; }
    public void setDebt(TechnicalDebt debt) { this.debt = debt; }

    public List<TechnicalDebt.DebtItem> getItems() { return items; }
    public void setItems(List<TechnicalDebt.DebtItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public List<Recommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
    }

    public int getPriorityScore() { return priorityScore; }
    public void setPriorityScore(int priorityScore) { this.priorityScore = priorityScore; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Technical Debt Report: ").append(apiName).append("\n\n");

        if (debt != null) {
            sb.append("## Summary\n\n");
            sb.append("- **Debt Score:** ").append(debt.getDebtScore()).append("/100\n");
            sb.append("- **Total Issues:** ").append(debt.getTotalIssues()).append("\n");
            sb.append("- **Deprecated Endpoints:** ").append(debt.getDeprecatedEndpointsCount()).append("\n");
            sb.append("- **Missing Documentation:** ").append(debt.getMissingDocumentationCount()).append("\n\n");
        }

        if (!items.isEmpty()) {
            sb.append("## Debt Items\n\n");
            for (TechnicalDebt.DebtItem item : items) {
                sb.append("- **").append(item.getType()).append(":** ")
                        .append(item.getDescription()).append("\n");
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
        private final TechnicalDebtReport report = new TechnicalDebtReport();

        public Builder apiName(String apiName) { report.apiName = apiName; return this; }
        public Builder debt(TechnicalDebt debt) { report.debt = debt; return this; }
        public Builder items(List<TechnicalDebt.DebtItem> items) { report.setItems(items); return this; }
        public Builder recommendations(List<Recommendation> recs) { report.setRecommendations(recs); return this; }
        public Builder priorityScore(int score) { report.priorityScore = score; return this; }
        public TechnicalDebtReport build() { return report; }
    }
}
