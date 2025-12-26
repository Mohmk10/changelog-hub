package io.github.mohmk10.changeloghub.analytics.util;

public enum TrendDirection {
    IMPROVING("Improving", "Metrics are getting better over time", 1),
    STABLE("Stable", "Metrics are remaining consistent", 0),
    DEGRADING("Degrading", "Metrics are getting worse over time", -1);

    private final String label;
    private final String description;
    private final int direction;

    TrendDirection(String label, String description, int direction) {
        this.label = label;
        this.description = description;
        this.direction = direction;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public int getDirection() {
        return direction;
    }

    public boolean isPositive() {
        return this == IMPROVING;
    }

    public boolean isNegative() {
        return this == DEGRADING;
    }

    public static TrendDirection fromSlope(double slope, double threshold) {
        if (slope > threshold) return IMPROVING;
        if (slope < -threshold) return DEGRADING;
        return STABLE;
    }

    public static TrendDirection fromChange(double changePercentage) {
        if (changePercentage > 5.0) return IMPROVING;
        if (changePercentage < -5.0) return DEGRADING;
        return STABLE;
    }
}
