package io.github.mohmk10.changeloghub.analytics.aggregator;

import io.github.mohmk10.changeloghub.analytics.model.ApiMetrics;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates API metrics over various time periods.
 */
public class MetricsAggregator {

    /**
     * Aggregate metrics from multiple changelogs.
     *
     * @param changelogs list of changelogs
     * @return aggregated metrics
     */
    public ApiMetrics aggregate(List<Changelog> changelogs) {
        if (changelogs == null || changelogs.isEmpty()) {
            return ApiMetrics.builder().build();
        }

        int totalChanges = 0;
        int breakingChanges = 0;
        Set<String> allPaths = new HashSet<>();

        for (Changelog changelog : changelogs) {
            totalChanges += changelog.getChanges().size();
            breakingChanges += changelog.getBreakingChanges().size();

            changelog.getChanges().forEach(c -> {
                if (c.getPath() != null) allPaths.add(c.getPath());
            });
            changelog.getBreakingChanges().forEach(c -> {
                if (c.getPath() != null) allPaths.add(c.getPath());
            });
        }

        String apiName = changelogs.get(0).getApiName();

        return ApiMetrics.builder()
                .apiName(apiName)
                .totalChanges(totalChanges)
                .breakingChanges(breakingChanges)
                .totalEndpoints(allPaths.size())
                .build();
    }

    /**
     * Aggregate metrics by time period.
     *
     * @param changelogs list of changelogs
     * @param periodDays days per period
     * @return map of period start date to aggregated metrics
     */
    public Map<LocalDate, ApiMetrics> aggregateByPeriod(List<Changelog> changelogs, int periodDays) {
        if (changelogs == null || changelogs.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<LocalDate, List<Changelog>> grouped = changelogs.stream()
                .collect(Collectors.groupingBy(
                        c -> getPeriodStart(c.getGeneratedAt(), periodDays)
                ));

        Map<LocalDate, ApiMetrics> result = new TreeMap<>();
        for (Map.Entry<LocalDate, List<Changelog>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), aggregate(entry.getValue()));
        }

        return result;
    }

    /**
     * Aggregate metrics by week.
     *
     * @param changelogs list of changelogs
     * @return map of week start date to aggregated metrics
     */
    public Map<LocalDate, ApiMetrics> aggregateByWeek(List<Changelog> changelogs) {
        return aggregateByPeriod(changelogs, 7);
    }

    /**
     * Aggregate metrics by month.
     *
     * @param changelogs list of changelogs
     * @return map of month start date to aggregated metrics
     */
    public Map<LocalDate, ApiMetrics> aggregateByMonth(List<Changelog> changelogs) {
        return aggregateByPeriod(changelogs, 30);
    }

    /**
     * Calculate averages from aggregated metrics.
     *
     * @param metricsMap map of period to metrics
     * @return average metrics across all periods
     */
    public ApiMetrics calculateAverages(Map<LocalDate, ApiMetrics> metricsMap) {
        if (metricsMap == null || metricsMap.isEmpty()) {
            return ApiMetrics.builder().build();
        }

        int totalChanges = 0;
        int breakingChanges = 0;
        int count = metricsMap.size();

        for (ApiMetrics metrics : metricsMap.values()) {
            totalChanges += metrics.getTotalChanges();
            breakingChanges += metrics.getBreakingChanges();
        }

        return ApiMetrics.builder()
                .totalChanges(totalChanges / count)
                .breakingChanges(breakingChanges / count)
                .build();
    }

    private LocalDate getPeriodStart(LocalDateTime dateTime, int periodDays) {
        LocalDate date = dateTime.toLocalDate();
        long daysSinceEpoch = date.toEpochDay();
        long periodStart = (daysSinceEpoch / periodDays) * periodDays;
        return LocalDate.ofEpochDay(periodStart);
    }
}
