package io.github.mohmk10.changeloghub.analytics.aggregator;

import io.github.mohmk10.changeloghub.analytics.model.ApiEvolution;
import io.github.mohmk10.changeloghub.analytics.model.ApiEvolution.VersionSummary;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.ChangeType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Aggregates API change history for evolution analysis.
 */
public class HistoryAggregator {

    /**
     * Aggregate changelog history into evolution data.
     *
     * @param changelogs list of changelogs
     * @return API evolution summary
     */
    public ApiEvolution aggregate(List<Changelog> changelogs) {
        if (changelogs == null || changelogs.isEmpty()) {
            return ApiEvolution.builder().build();
        }

        List<Changelog> sorted = new ArrayList<>(changelogs);
        sorted.sort(Comparator.comparing(Changelog::getGeneratedAt));

        String apiName = sorted.get(0).getApiName();
        LocalDate startDate = sorted.get(0).getGeneratedAt().toLocalDate();
        LocalDate endDate = sorted.get(sorted.size() - 1).getGeneratedAt().toLocalDate();

        int totalChanges = 0;
        int totalBreaking = 0;
        List<VersionSummary> versions = new ArrayList<>();

        for (Changelog changelog : sorted) {
            int changes = changelog.getChanges().size();
            int breaking = changelog.getBreakingChanges().size();
            totalChanges += changes + breaking;
            totalBreaking += breaking;

            int added = (int) changelog.getChanges().stream()
                    .filter(c -> c.getType() == ChangeType.ADDED)
                    .count();
            int removed = (int) changelog.getChanges().stream()
                    .filter(c -> c.getType() == ChangeType.REMOVED)
                    .count() + breaking;
            int modified = (int) changelog.getChanges().stream()
                    .filter(c -> c.getType() == ChangeType.MODIFIED)
                    .count();
            int deprecated = (int) changelog.getChanges().stream()
                    .filter(c -> c.getType() == ChangeType.DEPRECATED)
                    .count();

            VersionSummary summary = VersionSummary.builder()
                    .version(changelog.getToVersion())
                    .releaseDate(changelog.getGeneratedAt().toLocalDate())
                    .totalChanges(changes + breaking)
                    .breakingChanges(breaking)
                    .addedEndpoints(added)
                    .removedEndpoints(removed)
                    .modifiedEndpoints(modified)
                    .deprecatedEndpoints(deprecated)
                    .build();

            versions.add(summary);
        }

        double avgChanges = sorted.isEmpty() ? 0 : (double) totalChanges / sorted.size();
        double breakingRate = totalChanges == 0 ? 0 : (double) totalBreaking / totalChanges;

        return ApiEvolution.builder()
                .apiName(apiName)
                .startDate(startDate)
                .endDate(endDate)
                .versions(versions)
                .totalVersions(sorted.size())
                .totalChanges(totalChanges)
                .totalBreakingChanges(totalBreaking)
                .averageChangesPerVersion(avgChanges)
                .breakingChangeRate(breakingRate)
                .build();
    }

    /**
     * Get evolution between two dates.
     *
     * @param changelogs list of changelogs
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return filtered API evolution
     */
    public ApiEvolution aggregateBetween(List<Changelog> changelogs, LocalDate startDate, LocalDate endDate) {
        if (changelogs == null) {
            return ApiEvolution.builder().build();
        }

        List<Changelog> filtered = changelogs.stream()
                .filter(c -> {
                    LocalDate date = c.getGeneratedAt().toLocalDate();
                    return !date.isBefore(startDate) && !date.isAfter(endDate);
                })
                .toList();

        return aggregate(filtered);
    }
}
