package io.github.mohmk10.changeloghub.analytics.util;

/**
 * Stability grades for API stability scoring.
 */
public enum StabilityGrade {
    A("Excellent", 90, 100, "Highly stable API with minimal breaking changes"),
    B("Good", 80, 89, "Stable API with occasional breaking changes"),
    C("Fair", 70, 79, "Moderately stable API with regular breaking changes"),
    D("Poor", 60, 69, "Unstable API with frequent breaking changes"),
    F("Failing", 0, 59, "Very unstable API with constant breaking changes");

    private final String label;
    private final int minScore;
    private final int maxScore;
    private final String description;

    StabilityGrade(String label, int minScore, int maxScore, String description) {
        this.label = label;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAcceptable() {
        return this == A || this == B || this == C;
    }

    public boolean isPoor() {
        return this == D || this == F;
    }

    public static StabilityGrade fromScore(int score) {
        if (score >= 90) return A;
        if (score >= 80) return B;
        if (score >= 70) return C;
        if (score >= 60) return D;
        return F;
    }

    public static StabilityGrade fromScore(double score) {
        return fromScore((int) Math.round(score));
    }
}
