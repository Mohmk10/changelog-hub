package io.github.mohmk10.changeloghub.analytics.insight;

import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.util.AnalyticsConstants;
import io.github.mohmk10.changeloghub.analytics.util.StabilityGrade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generates recommendations based on API metrics and analysis.
 */
public class RecommendationEngine {

    /**
     * Generate recommendations based on metrics and stability.
     *
     * @param metrics API metrics
     * @param stability stability score
     * @return list of recommendations
     */
    public List<Recommendation> generateRecommendations(ApiMetrics metrics, StabilityScore stability) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (stability != null) {
            recommendations.addAll(generateStabilityRecommendations(stability));
        }

        if (metrics != null) {
            recommendations.addAll(generateMetricsRecommendations(metrics));
        }

        // Sort by efficiency (impact/effort) and limit
        recommendations.sort(Comparator.comparingDouble(Recommendation::getEfficiencyScore).reversed());
        if (recommendations.size() > AnalyticsConstants.MAX_RECOMMENDATIONS_PER_REPORT) {
            recommendations = new ArrayList<>(
                    recommendations.subList(0, AnalyticsConstants.MAX_RECOMMENDATIONS_PER_REPORT));
        }

        return recommendations;
    }

    /**
     * Generate recommendations for poor stability.
     *
     * @param stability stability score
     * @return list of recommendations
     */
    public List<Recommendation> recommendForPoorStability(StabilityScore stability) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (stability == null || stability.getGrade() == null) {
            return recommendations;
        }

        if (stability.getGrade() == StabilityGrade.F) {
            recommendations.add(Recommendation.builder()
                    .type(Recommendation.RecommendationType.STABILITY_IMPROVEMENT)
                    .title("Implement Change Freeze")
                    .description("Consider a temporary change freeze to stabilize the API")
                    .action("Halt non-critical changes and focus on stability")
                    .priority(10)
                    .effort(3)
                    .impact(9)
                    .build());

            recommendations.add(Recommendation.builder()
                    .type(Recommendation.RecommendationType.VERSIONING)
                    .title("Major Version Planning")
                    .description("Plan a major version release to batch breaking changes")
                    .action("Create a roadmap for the next major version")
                    .priority(9)
                    .effort(5)
                    .impact(8)
                    .build());
        }

        if (stability.getGrade() == StabilityGrade.D || stability.getGrade() == StabilityGrade.F) {
            recommendations.add(Recommendation.builder()
                    .type(Recommendation.RecommendationType.DEPRECATION)
                    .title("Improve Deprecation Policy")
                    .description("Announce deprecations before breaking changes")
                    .action("Implement a minimum 2-version deprecation period")
                    .priority(8)
                    .effort(4)
                    .impact(7)
                    .build());
        }

        return recommendations;
    }

    /**
     * Generate recommendations for high technical debt.
     *
     * @param debt technical debt analysis
     * @return list of recommendations
     */
    public List<Recommendation> recommendForHighDebt(TechnicalDebt debt) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (debt == null) {
            return recommendations;
        }

        if (debt.getDeprecatedEndpointsCount() > 0) {
            recommendations.add(Recommendation.builder()
                    .type(Recommendation.RecommendationType.DEBT_REDUCTION)
                    .title("Remove Deprecated Endpoints")
                    .description(String.format("Remove %d deprecated endpoint(s)",
                            debt.getDeprecatedEndpointsCount()))
                    .action("Plan removal in next major version")
                    .priority(7)
                    .effort(4)
                    .impact(6)
                    .build());
        }

        if (debt.getMissingDocumentationCount() > 0) {
            recommendations.add(Recommendation.builder()
                    .type(Recommendation.RecommendationType.DOCUMENTATION)
                    .title("Improve Documentation Coverage")
                    .description(String.format("%d endpoint(s) lack documentation",
                            debt.getMissingDocumentationCount()))
                    .action("Add OpenAPI descriptions and examples")
                    .priority(5)
                    .effort(3)
                    .impact(5)
                    .build());
        }

        if (debt.getInconsistentNamingCount() > 0) {
            recommendations.add(Recommendation.builder()
                    .type(Recommendation.RecommendationType.COMPLIANCE)
                    .title("Fix Naming Inconsistencies")
                    .description(String.format("%d naming inconsistencies detected",
                            debt.getInconsistentNamingCount()))
                    .action("Standardize naming conventions")
                    .priority(4)
                    .effort(5)
                    .impact(4)
                    .build());
        }

        return recommendations;
    }

    private List<Recommendation> generateStabilityRecommendations(StabilityScore stability) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (stability.isPoor()) {
            recommendations.addAll(recommendForPoorStability(stability));
        }

        if (stability.getBreakingChangeRatio() > 0.3) {
            recommendations.add(Recommendation.builder()
                    .type(Recommendation.RecommendationType.STABILITY_IMPROVEMENT)
                    .title("Reduce Breaking Change Ratio")
                    .description(String.format("%.0f%% of changes are breaking",
                            stability.getBreakingChangeRatio() * 100))
                    .action("Review changes for backwards compatibility before release")
                    .priority(8)
                    .effort(2)
                    .impact(7)
                    .build());
        }

        return recommendations;
    }

    private List<Recommendation> generateMetricsRecommendations(ApiMetrics metrics) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (metrics.getComplexityScore() > 70) {
            recommendations.add(Recommendation.builder()
                    .type(Recommendation.RecommendationType.PERFORMANCE)
                    .title("Reduce API Complexity")
                    .description(String.format("Complexity score is %d (high)",
                            metrics.getComplexityScore()))
                    .action("Consider splitting into smaller, focused APIs")
                    .priority(6)
                    .effort(8)
                    .impact(7)
                    .build());
        }

        if (metrics.getDocumentationCoverage() < 0.8) {
            recommendations.add(Recommendation.builder()
                    .type(Recommendation.RecommendationType.DOCUMENTATION)
                    .title("Increase Documentation Coverage")
                    .description(String.format("Only %.0f%% documentation coverage",
                            metrics.getDocumentationCoverage() * 100))
                    .action("Add descriptions to undocumented endpoints")
                    .priority(5)
                    .effort(3)
                    .impact(5)
                    .build());
        }

        return recommendations;
    }
}
