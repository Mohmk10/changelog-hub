package io.github.mohmk10.changeloghub.analytics.aggregator;

import io.github.mohmk10.changeloghub.analytics.model.ApiMetrics;
import io.github.mohmk10.changeloghub.analytics.model.StabilityScore;
import io.github.mohmk10.changeloghub.analytics.metrics.StabilityScorer;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.util.*;

public class ComparisonAggregator {

    private final MetricsAggregator metricsAggregator;
    private final StabilityScorer stabilityScorer;

    public ComparisonAggregator() {
        this.metricsAggregator = new MetricsAggregator();
        this.stabilityScorer = new StabilityScorer();
    }

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

    public Map<String, ApiMetrics> compareMetrics(Map<String, List<Changelog>> apiChangelogs) {
        Map<String, ApiMetrics> result = new HashMap<>();

        for (Map.Entry<String, List<Changelog>> entry : apiChangelogs.entrySet()) {
            ApiMetrics metrics = metricsAggregator.aggregate(entry.getValue());
            metrics.setApiName(entry.getKey());
            result.put(entry.getKey(), metrics);
        }

        return result;
    }

    public Map<String, StabilityScore> compareStability(Map<String, List<Changelog>> apiChangelogs) {
        Map<String, StabilityScore> result = new HashMap<>();

        for (Map.Entry<String, List<Changelog>> entry : apiChangelogs.entrySet()) {
            StabilityScore score = stabilityScorer.calculate(entry.getKey(), entry.getValue());
            result.put(entry.getKey(), score);
        }

        return result;
    }

    public List<String> rankByStability(Map<String, List<Changelog>> apiChangelogs) {
        Map<String, StabilityScore> scores = compareStability(apiChangelogs);

        return scores.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().getScore(), a.getValue().getScore()))
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> rankByBreakingChanges(Map<String, List<Changelog>> apiChangelogs) {
        Map<String, ApiMetrics> metrics = compareMetrics(apiChangelogs);

        return metrics.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().getBreakingChanges()))
                .map(Map.Entry::getKey)
                .toList();
    }

    public Optional<String> findMostStable(Map<String, List<Changelog>> apiChangelogs) {
        List<String> ranked = rankByStability(apiChangelogs);
        return ranked.isEmpty() ? Optional.empty() : Optional.of(ranked.get(0));
    }

    public Optional<String> findLeastStable(Map<String, List<Changelog>> apiChangelogs) {
        List<String> ranked = rankByStability(apiChangelogs);
        return ranked.isEmpty() ? Optional.empty() : Optional.of(ranked.get(ranked.size() - 1));
    }
}
