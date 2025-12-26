package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.RiskTrend;
import io.github.mohmk10.changeloghub.analytics.model.RiskTrend.RiskDataPoint;
import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.RiskLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RiskCalculator {

    private final TrendAnalyzer trendAnalyzer;

    public RiskCalculator() {
        this.trendAnalyzer = new TrendAnalyzer();
    }

    public int calculateRisk(Changelog changelog) {
        if (changelog == null) {
            return 0;
        }

        int breakingChanges = changelog.getBreakingChanges().size();
        int totalChanges = changelog.getChanges().size() + breakingChanges;

        if (totalChanges == 0) {
            return 0;
        }

        int baseRisk = Math.min(100, breakingChanges * 20);

        double ratio = (double) breakingChanges / totalChanges;
        int ratioModifier = (int) (ratio * 30);

        int assessmentRisk = 0;
        if (changelog.getRiskAssessment() != null) {
            assessmentRisk = changelog.getRiskAssessment().getOverallScore();
        }

        int finalRisk = (baseRisk + ratioModifier + assessmentRisk) / 2;
        return Math.min(100, Math.max(0, finalRisk));
    }

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

        int recentWeight = 0;
        int recentCount = Math.min(3, history.size());
        List<Changelog> sorted = new ArrayList<>(history);
        sorted.sort(Comparator.comparing(Changelog::getGeneratedAt).reversed());

        for (int i = 0; i < recentCount; i++) {
            recentWeight += calculateRisk(sorted.get(i));
        }

        double avgRisk = (double) totalRisk / count;
        double recentAvg = recentCount > 0 ? (double) recentWeight / recentCount : 0;

        return (int) Math.round((recentAvg * 0.6) + (avgRisk * 0.4));
    }

    public RiskTrend analyzeTrend(List<Changelog> history, int periods) {
        if (history == null || history.isEmpty()) {
            return RiskTrend.builder()
                    .direction(TrendDirection.STABLE)
                    .currentRiskScore(0)
                    .previousRiskScore(0)
                    .changePercentage(0)
                    .build();
        }

        List<Changelog> sorted = new ArrayList<>(history);
        sorted.sort(Comparator.comparing(Changelog::getGeneratedAt));

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

        TrendDirection rawDirection = trendAnalyzer.analyzeTrend(riskScores);
        TrendDirection direction = invertForRisk(rawDirection);

        int currentScore = riskScores.isEmpty() ? 0 : riskScores.get(riskScores.size() - 1);
        int previousScore = riskScores.size() < 2 ? currentScore : riskScores.get(riskScores.size() - 2);

        double changePercentage = previousScore == 0 ? 0 :
                ((double) (currentScore - previousScore) / previousScore) * 100;

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

    private TrendDirection invertForRisk(TrendDirection direction) {
        switch (direction) {
            case IMPROVING:
                return TrendDirection.DEGRADING; 
            case DEGRADING:
                return TrendDirection.IMPROVING; 
            default:
                return TrendDirection.STABLE;
        }
    }
}
