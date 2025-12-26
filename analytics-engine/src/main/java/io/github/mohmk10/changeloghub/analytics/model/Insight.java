package io.github.mohmk10.changeloghub.analytics.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Insight {

    private InsightType type;
    private Severity severity;
    private String title;
    private String description;
    private String context;
    private int priority;
    private double confidence;
    private LocalDateTime generatedAt;

    public enum Severity {
        CRITICAL,
        WARNING,
        INFO
    }

    public enum InsightType {
        BREAKING_CHANGE_TREND("Breaking Change Trend", "Analysis of breaking change patterns"),
        STABILITY_ALERT("Stability Alert", "API stability concerns"),
        DEPRECATION_REMINDER("Deprecation Reminder", "Deprecated features to address"),
        VELOCITY_CHANGE("Velocity Change", "Change in development pace"),
        RISK_INCREASE("Risk Increase", "Elevated risk detected"),
        RISK_DECREASE("Risk Decrease", "Risk reduction observed"),
        TECHNICAL_DEBT("Technical Debt", "Accumulated technical debt"),
        COMPLIANCE_ISSUE("Compliance Issue", "Standards compliance concerns"),
        POSITIVE_TREND("Positive Trend", "Positive development pattern"),
        SEASONAL_PATTERN("Seasonal Pattern", "Time-based pattern detected"),
        TREND("Trend", "General trend analysis"),
        PATTERN("Pattern", "Pattern detection"),
        ANOMALY("Anomaly", "Anomaly detection"),
        PREDICTION("Prediction", "Prediction analysis"),
        RECOMMENDATION("Recommendation", "Recommendation");

        private final String label;
        private final String description;

        InsightType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    public Insight() {
        this.generatedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public InsightType getType() {
        return type;
    }

    public void setType(InsightType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public boolean isHighPriority() {
        return priority >= 8;
    }

    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }

    public boolean isCritical() {
        return severity == Severity.CRITICAL;
    }

    public boolean isActionable() {
        return type == InsightType.DEPRECATION_REMINDER ||
                type == InsightType.TECHNICAL_DEBT ||
                type == InsightType.COMPLIANCE_ISSUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Insight insight = (Insight) o;
        return type == insight.type && Objects.equals(title, insight.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title);
    }

    @Override
    public String toString() {
        return "Insight{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                '}';
    }

    public static class Builder {
        private final Insight insight = new Insight();

        public Builder type(InsightType type) {
            insight.type = type;
            return this;
        }

        public Builder title(String title) {
            insight.title = title;
            return this;
        }

        public Builder description(String description) {
            insight.description = description;
            return this;
        }

        public Builder context(String context) {
            insight.context = context;
            return this;
        }

        public Builder priority(int priority) {
            insight.priority = priority;
            return this;
        }

        public Builder confidence(double confidence) {
            insight.confidence = confidence;
            return this;
        }

        public Builder severity(Severity severity) {
            insight.severity = severity;
            return this;
        }

        public Insight build() {
            return insight;
        }
    }
}
