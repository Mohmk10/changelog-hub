package io.github.mohmk10.changeloghub.analytics.insight;

import io.github.mohmk10.changeloghub.analytics.metrics.*;
import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.util.AnalyticsConstants;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generates insights from API metrics and history.
 */
public class InsightGenerator {

    private final StabilityScorer stabilityScorer;
    private final RiskCalculator riskCalculator;
    private final VelocityCalculator velocityCalculator;

    public InsightGenerator() {
        this.stabilityScorer = new StabilityScorer();
        this.riskCalculator = new RiskCalculator();
        this.velocityCalculator = new VelocityCalculator();
    }

    /**
     * Generate insights from API spec and changelog history.
     *
     * @param spec the API specification
     * @param history changelog history
     * @return list of insights
     */
    public List<Insight> generate(ApiSpec spec, List<Changelog> history) {
        List<Insight> insights = new ArrayList<>();

        if (history != null && !history.isEmpty()) {
            insights.addAll(generateBreakingChangeInsights(history));
            insights.addAll(generateStabilityInsights(history));
            insights.addAll(generateVelocityInsights(history));
            insights.addAll(generateRiskInsights(history));
        }

        if (spec != null) {
            insights.addAll(generateSpecInsights(spec));
        }

        // Sort by priority and limit
        insights.sort(Comparator.comparingInt(Insight::getPriority).reversed());
        if (insights.size() > AnalyticsConstants.MAX_INSIGHTS_PER_REPORT) {
            insights = new ArrayList<>(insights.subList(0, AnalyticsConstants.MAX_INSIGHTS_PER_REPORT));
        }

        return insights;
    }

    private List<Insight> generateBreakingChangeInsights(List<Changelog> history) {
        List<Insight> insights = new ArrayList<>();

        int totalBreaking = history.stream()
                .mapToInt(c -> c.getBreakingChanges().size())
                .sum();

        if (totalBreaking == 0) {
            insights.add(Insight.builder()
                    .type(Insight.InsightType.POSITIVE_TREND)
                    .title("No Breaking Changes")
                    .description("No breaking changes detected in the analyzed period")
                    .priority(3)
                    .confidence(1.0)
                    .build());
        } else {
            // Check recent trend
            int recentBreaking = 0;
            int olderBreaking = 0;
            int mid = history.size() / 2;

            for (int i = 0; i < history.size(); i++) {
                if (i >= mid) {
                    recentBreaking += history.get(i).getBreakingChanges().size();
                } else {
                    olderBreaking += history.get(i).getBreakingChanges().size();
                }
            }

            if (recentBreaking > olderBreaking * 1.5) {
                insights.add(Insight.builder()
                        .type(Insight.InsightType.BREAKING_CHANGE_TREND)
                        .title("Breaking Changes Increasing")
                        .description(String.format(
                                "Breaking changes increased from %d to %d in recent period",
                                olderBreaking, recentBreaking))
                        .priority(8)
                        .confidence(0.8)
                        .build());
            }
        }

        return insights;
    }

    private List<Insight> generateStabilityInsights(List<Changelog> history) {
        List<Insight> insights = new ArrayList<>();

        StabilityScore stability = stabilityScorer.calculate(history);

        if (stability.isPoor()) {
            insights.add(Insight.builder()
                    .type(Insight.InsightType.STABILITY_ALERT)
                    .title("Poor API Stability")
                    .description(String.format("Stability score is %d (%s). Consider reducing breaking changes.",
                            stability.getScore(), stability.getGrade()))
                    .priority(9)
                    .confidence(0.9)
                    .build());
        } else if (stability.isAcceptable() && stability.getScore() < 80) {
            insights.add(Insight.builder()
                    .type(Insight.InsightType.STABILITY_ALERT)
                    .title("Moderate Stability Concerns")
                    .description(String.format("Stability score is %d. Room for improvement.",
                            stability.getScore()))
                    .priority(5)
                    .confidence(0.85)
                    .build());
        }

        return insights;
    }

    private List<Insight> generateVelocityInsights(List<Changelog> history) {
        List<Insight> insights = new ArrayList<>();

        ChangeVelocity velocity = velocityCalculator.calculate(history);

        if (velocity.isAccelerating() && velocity.getAccelerationRate() > 0.5) {
            insights.add(Insight.builder()
                    .type(Insight.InsightType.VELOCITY_CHANGE)
                    .title("Rapid Change Velocity")
                    .description(String.format(
                            "Change velocity increased by %.0f%%. Consider slowing down.",
                            velocity.getAccelerationRate() * 100))
                    .priority(6)
                    .confidence(0.75)
                    .build());
        }

        if (velocity.getBreakingChangesPerRelease() > 2) {
            insights.add(Insight.builder()
                    .type(Insight.InsightType.BREAKING_CHANGE_TREND)
                    .title("High Breaking Changes Per Release")
                    .description(String.format(
                            "Average %.1f breaking changes per release. Consider batching changes.",
                            velocity.getBreakingChangesPerRelease()))
                    .priority(7)
                    .confidence(0.85)
                    .build());
        }

        return insights;
    }

    private List<Insight> generateRiskInsights(List<Changelog> history) {
        List<Insight> insights = new ArrayList<>();

        RiskTrend riskTrend = riskCalculator.analyzeTrend(history);

        if (riskTrend.isDegrading()) {
            insights.add(Insight.builder()
                    .type(Insight.InsightType.RISK_INCREASE)
                    .title("Increasing Risk Trend")
                    .description(String.format(
                            "Risk score increased by %.1f%% recently",
                            Math.abs(riskTrend.getChangePercentage())))
                    .priority(8)
                    .confidence(0.8)
                    .build());
        } else if (riskTrend.isImproving()) {
            insights.add(Insight.builder()
                    .type(Insight.InsightType.RISK_DECREASE)
                    .title("Decreasing Risk Trend")
                    .description(String.format(
                            "Risk score decreased by %.1f%% - good progress!",
                            Math.abs(riskTrend.getChangePercentage())))
                    .priority(4)
                    .confidence(0.8)
                    .build());
        }

        return insights;
    }

    private List<Insight> generateSpecInsights(ApiSpec spec) {
        List<Insight> insights = new ArrayList<>();

        if (spec.getEndpoints() != null) {
            long deprecated = spec.getEndpoints().stream()
                    .filter(e -> e.isDeprecated())
                    .count();

            if (deprecated > 0) {
                insights.add(Insight.builder()
                        .type(Insight.InsightType.DEPRECATION_REMINDER)
                        .title("Deprecated Endpoints Present")
                        .description(String.format(
                                "%d deprecated endpoint(s) should be reviewed for removal",
                                deprecated))
                        .priority(6)
                        .confidence(1.0)
                        .build());
            }
        }

        return insights;
    }
}
