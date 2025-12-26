package io.github.mohmk10.changeloghub.analytics.model;

import java.util.Objects;

/**
 * Represents a recommendation based on API analytics.
 */
public class Recommendation {

    private RecommendationType type;
    private String title;
    private String description;
    private String action;
    private int priority;
    private int effort;
    private int impact;
    private String targetPath;

    public enum RecommendationType {
        STABILITY_IMPROVEMENT("Stability Improvement", "Improve API stability"),
        DEBT_REDUCTION("Debt Reduction", "Reduce technical debt"),
        DOCUMENTATION("Documentation", "Improve documentation"),
        DEPRECATION("Deprecation", "Manage deprecations"),
        VERSIONING("Versioning", "Versioning best practices"),
        SECURITY("Security", "Security improvements"),
        PERFORMANCE("Performance", "Performance optimization"),
        COMPLIANCE("Compliance", "Compliance improvements");

        private final String label;
        private final String description;

        RecommendationType(String label, String description) {
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

    public Recommendation() {}

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public RecommendationType getType() {
        return type;
    }

    public void setType(RecommendationType type) {
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getEffort() {
        return effort;
    }

    public void setEffort(int effort) {
        this.effort = effort;
    }

    public int getImpact() {
        return impact;
    }

    public void setImpact(int impact) {
        this.impact = impact;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public double getEfficiencyScore() {
        if (effort == 0) return impact;
        return (double) impact / effort;
    }

    public boolean isQuickWin() {
        return effort <= 3 && impact >= 7;
    }

    public boolean isStrategic() {
        return effort >= 7 && impact >= 8;
    }

    public boolean isHighPriority() {
        return priority >= 7;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recommendation that = (Recommendation) o;
        return type == that.type && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title);
    }

    @Override
    public String toString() {
        return "Recommendation{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                '}';
    }

    public static class Builder {
        private final Recommendation rec = new Recommendation();

        public Builder type(RecommendationType type) {
            rec.type = type;
            return this;
        }

        public Builder title(String title) {
            rec.title = title;
            return this;
        }

        public Builder description(String description) {
            rec.description = description;
            return this;
        }

        public Builder action(String action) {
            rec.action = action;
            return this;
        }

        public Builder priority(int priority) {
            rec.priority = priority;
            return this;
        }

        public Builder effort(int effort) {
            rec.effort = effort;
            return this;
        }

        public Builder impact(int impact) {
            rec.impact = impact;
            return this;
        }

        public Builder targetPath(String targetPath) {
            rec.targetPath = targetPath;
            return this;
        }

        public Recommendation build() {
            return rec;
        }
    }
}
