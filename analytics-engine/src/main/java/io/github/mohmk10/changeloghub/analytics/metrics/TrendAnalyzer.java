package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.util.AnalyticsConstants;
import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;

import java.util.List;

/**
 * Analyzes trends in time-series data.
 */
public class TrendAnalyzer {

    /**
     * Analyze the trend direction of a series of values.
     *
     * @param values list of values in chronological order
     * @return the trend direction
     */
    public TrendDirection analyzeTrend(List<Integer> values) {
        if (values == null || values.size() < AnalyticsConstants.MIN_DATA_POINTS_FOR_TREND) {
            return TrendDirection.STABLE;
        }

        double slope = calculateSlope(values);
        return TrendDirection.fromSlope(slope, AnalyticsConstants.TREND_THRESHOLD);
    }

    /**
     * Calculate the slope of the trend line using linear regression.
     *
     * @param values list of values
     * @return slope of the trend line
     */
    public double calculateSlope(List<Integer> values) {
        if (values == null || values.size() < 2) {
            return 0.0;
        }

        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = values.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator = (n * sumX2) - (sumX * sumX);
        if (denominator == 0) {
            return 0.0;
        }

        return ((n * sumXY) - (sumX * sumY)) / denominator;
    }

    /**
     * Determine trend direction from slope value.
     *
     * @param slope the calculated slope
     * @return the trend direction
     */
    public TrendDirection determineTrendDirection(double slope) {
        return TrendDirection.fromSlope(slope, AnalyticsConstants.TREND_THRESHOLD);
    }

    /**
     * Check if the trend is improving (values decreasing for risk scores).
     *
     * @param riskScores list of risk scores
     * @return true if improving
     */
    public boolean isImproving(List<Integer> riskScores) {
        if (riskScores == null || riskScores.size() < 2) {
            return false;
        }

        double slope = calculateSlope(riskScores);
        // For risk scores, improving means decreasing (negative slope)
        return slope < -AnalyticsConstants.TREND_THRESHOLD;
    }

    /**
     * Check if the trend is degrading (values increasing for risk scores).
     *
     * @param riskScores list of risk scores
     * @return true if degrading
     */
    public boolean isDegrading(List<Integer> riskScores) {
        if (riskScores == null || riskScores.size() < 2) {
            return false;
        }

        double slope = calculateSlope(riskScores);
        // For risk scores, degrading means increasing (positive slope)
        return slope > AnalyticsConstants.TREND_THRESHOLD;
    }

    /**
     * Check if the trend is stable.
     *
     * @param values list of values
     * @return true if stable
     */
    public boolean isStable(List<Integer> values) {
        if (values == null || values.size() < 2) {
            return true;
        }

        double slope = calculateSlope(values);
        return Math.abs(slope) <= AnalyticsConstants.TREND_THRESHOLD;
    }

    /**
     * Calculate the average value.
     *
     * @param values list of values
     * @return average value
     */
    public double calculateAverage(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        return values.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculate the standard deviation.
     *
     * @param values list of values
     * @return standard deviation
     */
    public double calculateStandardDeviation(List<Integer> values) {
        if (values == null || values.size() < 2) {
            return 0.0;
        }

        double mean = calculateAverage(values);
        double sumSquaredDiff = 0;

        for (int value : values) {
            sumSquaredDiff += Math.pow(value - mean, 2);
        }

        return Math.sqrt(sumSquaredDiff / values.size());
    }

    /**
     * Detect if there's a significant change in the most recent period.
     *
     * @param values list of values
     * @return true if significant change detected
     */
    public boolean hasSignificantRecentChange(List<Integer> values) {
        if (values == null || values.size() < 2) {
            return false;
        }

        int lastIndex = values.size() - 1;
        int current = values.get(lastIndex);
        int previous = values.get(lastIndex - 1);

        if (previous == 0) {
            return current > 0;
        }

        double changeRatio = Math.abs((double) (current - previous) / previous);
        return changeRatio >= AnalyticsConstants.SIGNIFICANT_CHANGE_THRESHOLD;
    }
}
