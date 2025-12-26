package io.github.mohmk10.changeloghub.analytics.aggregator;

import io.github.mohmk10.changeloghub.analytics.model.ApiMetrics;
import io.github.mohmk10.changeloghub.analytics.model.StabilityScore;
import io.github.mohmk10.changeloghub.analytics.metrics.StabilityScorer;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.util.*;

/**
 * Compares metrics across multiple APIs.
 */
public class ComparisonAggregator {

    private final MetricsAggregator metricsAggregator;
    private final StabilityScorer stabilityScorer;

    public ComparisonAggregator() {
        this.metricsAggregator = new MetricsAggregator();
        this.stabilityScorer = new StabilityScorer();
    }

    /**
     * Compare two ApiMetrics and return their difference.
     *
     * @param oldMetrics the old metrics (can be null)
     * @param newMetrics the new metrics (can be null)
     * @return ApiMetrics representing the difference
     */
    public ApiMetrics compare(ApiMetrics oldMetrics, ApiMetrics newMetrics) {
        int oldEndpoints = oldMetrics != null ? oldMetrics.getTotalEndpoints() : 0;
        int newEndpoints = newMetrics != null ? newMetrics.getTotalEndpoints() : 0;
        int oldTotalChanges = oldMetrics != null ? oldMetrics.getTotalChanges() : 0;
        int newTotalChanges = newMetrics != null ? newMetrics.getTotalChanges() : 0;
        int oldBreaking = oldMetrics != null ? oldMetrics.getBreakingChanges() : 0;
        int newBreaking = newMetrics != null ? newMetrics.getBreakingChanges() : 0;
        double oldDocCoverage = oldMetrics != null ? oldMetrics.getDocumentationCoverage() : 0.0;
        double newDocCoverage = newMetrics != null ? newMetrics.getDocumentationCoverage() : 0.0;

        return ApiMetrics.builder()
                .totalEndpoints(newEndpoints - oldEndpoints)
                .totalChanges(newTotalChanges - oldTotalChanges)
                .breakingChanges(newBreaking - oldBreaking)
                .documentationCoverage(Math.round((newDocCoverage - oldDocCoverage) * 10.0) / 10.0)
                .build();
    }

    /**
     * Compare metrics across multiple APIs.
     *
     * @param apiChangelogs map of API name to changelogs
     * @return map of API name to aggregated metrics
     */
    public Map<String, ApiMetrics> compareMetrics(Map<String, List<Changelog>> apiChangelogs) {
        Map<String, ApiMetrics> result = new HashMap<>();

        for (Map.Entry<String, List<Changelog>> entry : apiChangelogs.entrySet()) {
            ApiMetrics metrics = metricsAggregator.aggregate(entry.getValue());
            metrics.setApiName(entry.getKey());
            result.put(entry.getKey(), metrics);
        }

        return result;
    }

    /**
     * Compare stability scores across multiple APIs.
     *
     * @param apiChangelogs map of API name to changelogs
     * @return map of API name to stability score
     */
    public Map<String, StabilityScore> compareStability(Map<String, List<Changelog>> apiChangelogs) {
        Map<String, StabilityScore> result = new HashMap<>();

        for (Map.Entry<String, List<Changelog>> entry : apiChangelogs.entrySet()) {
            StabilityScore score = stabilityScorer.calculate(entry.getKey(), entry.getValue());
            result.put(entry.getKey(), score);
        }

        return result;
    }

    /**
     * Rank APIs by stability.
     *
     * @param apiChangelogs map of API name to changelogs
     * @return list of API names sorted by stability (best first)
     */
    public List<String> rankByStability(Map<String, List<Changelog>> apiChangelogs) {
        Map<String, StabilityScore> scores = compareStability(apiChangelogs);

        return scores.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().getScore(), a.getValue().getScore()))
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Rank APIs by breaking change count.
     *
     * @param apiChangelogs map of API name to changelogs
     * @return list of API names sorted by breaking changes (least first)
     */
    public List<String> rankByBreakingChanges(Map<String, List<Changelog>> apiChangelogs) {
        Map<String, ApiMetrics> metrics = compareMetrics(apiChangelogs);

        return metrics.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().getBreakingChanges()))
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Find the most stable API.
     *
     * @param apiChangelogs map of API name to changelogs
     * @return name of the most stable API
     */
    public Optional<String> findMostStable(Map<String, List<Changelog>> apiChangelogs) {
        List<String> ranked = rankByStability(apiChangelogs);
        return ranked.isEmpty() ? Optional.empty() : Optional.of(ranked.get(0));
    }

    /**
     * Find the least stable API.
     *
     * @param apiChangelogs map of API name to changelogs
     * @return name of the least stable API
     */
    public Optional<String> findLeastStable(Map<String, List<Changelog>> apiChangelogs) {
        List<String> ranked = rankByStability(apiChangelogs);
        return ranked.isEmpty() ? Optional.empty() : Optional.of(ranked.get(ranked.size() - 1));
    }
}
