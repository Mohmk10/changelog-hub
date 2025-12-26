package io.github.mohmk10.changeloghub.analytics.util;

/**
 * Constants used throughout the analytics engine.
 */
public final class AnalyticsConstants {

    private AnalyticsConstants() {
        // Utility class
    }

    // Stability score weights
    public static final double WEIGHT_BREAKING_CHANGE_RATIO = 0.40;
    public static final double WEIGHT_TIME_BETWEEN_BREAKING = 0.30;
    public static final double WEIGHT_DEPRECATION_MANAGEMENT = 0.15;
    public static final double WEIGHT_SEMVER_COMPLIANCE = 0.15;

    // Score thresholds
    public static final int SCORE_MAX = 100;
    public static final int SCORE_MIN = 0;
    public static final int GRADE_A_THRESHOLD = 90;
    public static final int GRADE_B_THRESHOLD = 80;
    public static final int GRADE_C_THRESHOLD = 70;
    public static final int GRADE_D_THRESHOLD = 60;

    // Risk thresholds
    public static final int RISK_LOW_THRESHOLD = 25;
    public static final int RISK_MEDIUM_THRESHOLD = 50;
    public static final int RISK_HIGH_THRESHOLD = 75;

    // Trend analysis
    public static final double TREND_THRESHOLD = 0.05;
    public static final int MIN_DATA_POINTS_FOR_TREND = 3;

    // Velocity periods (in days)
    public static final int DAYS_PER_WEEK = 7;
    public static final int DAYS_PER_MONTH = 30;
    public static final int DAYS_PER_QUARTER = 90;

    // Complexity factors
    public static final int COMPLEXITY_ENDPOINT_WEIGHT = 1;
    public static final int COMPLEXITY_PARAMETER_WEIGHT = 2;
    public static final int COMPLEXITY_NESTED_SCHEMA_WEIGHT = 3;
    public static final int COMPLEXITY_RESPONSE_CODE_WEIGHT = 1;

    // Technical debt thresholds
    public static final int DEPRECATED_WARNING_DAYS = 90;
    public static final int DEPRECATED_CRITICAL_DAYS = 180;
    public static final int MAX_ACCEPTABLE_DEBT_SCORE = 30;

    // Ideal thresholds
    public static final int MIN_DAYS_BETWEEN_BREAKING_IDEAL = 30;
    public static final double LOW_DOCUMENTATION_THRESHOLD = 0.5;
    public static final int HIGH_COMPLEXITY_THRESHOLD = 50;

    // Report formatting
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Insight thresholds
    public static final double SIGNIFICANT_CHANGE_THRESHOLD = 0.20; // 20%
    public static final int MAX_INSIGHTS_PER_REPORT = 10;
    public static final int MAX_RECOMMENDATIONS_PER_REPORT = 5;
}
