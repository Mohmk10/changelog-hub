package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.RiskTrend;
import io.github.mohmk10.changeloghub.analytics.model.RiskTrend.RiskDataPoint;
import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.RiskLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calculates risk scores and trends for APIs.
 */
public class RiskCalculator {

    private final TrendAnalyzer trendAnalyzer;

    public RiskCalculator() {
        this.trendAnalyzer = new TrendAnalyzer();
    }

    /**
     * Calculate risk score for a single changelog.
     *
     * @param changelog the changelog to analyze
     * @return risk score (0-100)
     */
    public int calculateRisk(Changelog changelog) {
        if (changelog == null) {
            return 0;
        }

        int breakingChanges = changelog.getBreakingChanges().size();
        int totalChanges = changelog.getChanges().size() + breakingChanges;

        if (totalChanges == 0) {
            return 0;
        }

        // Base risk from breaking changes
        int baseRisk = Math.min(100, breakingChanges * 20);

        // Modifier based on ratio
        double ratio = (double) breakingChanges / totalChanges;
        int ratioModifier = (int) (ratio * 30);

        // Risk from risk assessment if available
        int assessmentRisk = 0;
        if (changelog.getRiskAssessment() != null) {
            assessmentRisk = changelog.getRiskAssessment().getOverallScore();
        }

        // Combine scores
        int finalRisk = (baseRisk + ratioModifier + assessmentRisk) / 2;
        return Math.min(100, Math.max(0, finalRisk));
    }

    /**
     * Calculate cumulative risk from multiple changelogs.
     *
     * @param history list of changelogs
     * @return cumulative risk score
     */
    public int calculateCumulativeRisk(List<Changelog> history) {
        if (history == null || history.isEmpty()) {
            return 0;
        }

        int totalRisk = 0;
        int count = 0;

        for (Changelog changelog : history) {
            totalRisk += calculateRisk(changelog);
            count++;
        }

        // Weight recent changelogs more heavily
        int recentWeight = 0;
        int recentCount = Math.min(3, history.size());
        List<Changelog> sorted = new ArrayList<>(history);
        sorted.sort(Comparator.comparing(Changelog::getGeneratedAt).reversed());

        for (int i = 0; i < recentCount; i++) {
            recentWeight += calculateRisk(sorted.get(i));
        }

        double avgRisk = (double) totalRisk / count;
        double recentAvg = recentCount > 0 ? (double) recentWeight / recentCount : 0;

        // Weighted average: 60% recent, 40% overall
        return (int) Math.round((recentAvg * 0.6) + (avgRisk * 0.4));
    }

    /**
     * Analyze risk trend over multiple periods.
     *
     * @param history list of changelogs
     * @param periods number of periods to analyze
     * @return risk trend analysis
     */
    public RiskTrend analyzeTrend(List<Changelog> history, int periods) {
        if (history == null || history.isEmpty()) {
            return RiskTrend.builder()
                    .direction(TrendDirection.STABLE)
                    .currentRiskScore(0)
                    .previousRiskScore(0)
                    .changePercentage(0)
                    .build();
        }

        // Sort by date
        List<Changelog> sorted = new ArrayList<>(history);
        sorted.sort(Comparator.comparing(Changelog::getGeneratedAt));

        // Calculate risk for each changelog
        List<Integer> riskScores = new ArrayList<>();
        List<RiskDataPoint> dataPoints = new ArrayList<>();

        for (Changelog changelog : sorted) {
            int risk = calculateRisk(changelog);
            riskScores.add(risk);

            dataPoints.add(new RiskDataPoint(
                    changelog.getGeneratedAt(),
                    risk,
                    changelog.getToVersion(),
                    changelog.getBreakingChanges().size()
            ));
        }

        // Determine trend - for risk scores, we need to INVERT the direction
        // because lower risk is better (IMPROVING), higher risk is worse (DEGRADING)
        TrendDirection rawDirection = trendAnalyzer.analyzeTrend(riskScores);
        TrendDirection direction = invertForRisk(rawDirection);

        // Current and previous scores
        int currentScore = riskScores.isEmpty() ? 0 : riskScores.get(riskScores.size() - 1);
        int previousScore = riskScores.size() < 2 ? currentScore : riskScores.get(riskScores.size() - 2);

        // Change percentage
        double changePercentage = previousScore == 0 ? 0 :
                ((double) (currentScore - previousScore) / previousScore) * 100;

        // Project next score based on trend
        int projectedNext = projectNextScore(riskScores, direction);

        return RiskTrend.builder()
                .direction(direction)
                .currentRiskScore(currentScore)
                .previousRiskScore(previousScore)
                .changePercentage(changePercentage)
                .projectedNextScore(projectedNext)
                .currentRiskLevel(getRiskLevel(currentScore))
                .projectedRiskLevel(getRiskLevel(projectedNext))
                .dataPoints(dataPoints)
                .periodsAnalyzed(sorted.size())
                .build();
    }

    /**
     * Analyze risk trend with default period count.
     *
     * @param history list of changelogs
     * @return risk trend analysis
     */
    public RiskTrend analyzeTrend(List<Changelog> history) {
        return analyzeTrend(history, history != null ? history.size() : 0);
    }

    private int projectNextScore(List<Integer> scores, TrendDirection direction) {
        if (scores.isEmpty()) {
            return 0;
        }

        int current = scores.get(scores.size() - 1);

        switch (direction) {
            case IMPROVING:
                return Math.max(0, current - 10);
            case DEGRADING:
                return Math.min(100, current + 10);
            default:
                return current;
        }
    }

    private RiskLevel getRiskLevel(int score) {
        if (score >= 75) return RiskLevel.CRITICAL;
        if (score >= 50) return RiskLevel.HIGH;
        if (score >= 25) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    /**
     * Invert trend direction for risk scores.
     * For risk: decreasing values (negative slope) = IMPROVING
     *           increasing values (positive slope) = DEGRADING
     */
    private TrendDirection invertForRisk(TrendDirection direction) {
        switch (direction) {
            case IMPROVING:
                return TrendDirection.DEGRADING; // Values going up means risk increasing = bad
            case DEGRADING:
                return TrendDirection.IMPROVING; // Values going down means risk decreasing = good
            default:
                return TrendDirection.STABLE;
        }
    }
}
