package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.ChangeVelocity;
import io.github.mohmk10.changeloghub.analytics.util.AnalyticsConstants;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calculates the velocity of API changes over time.
 */
public class VelocityCalculator {

    /**
     * Calculate change velocity from changelog history.
     *
     * @param history list of changelogs
     * @return calculated velocity metrics
     */
    public ChangeVelocity calculate(List<Changelog> history) {
        if (history == null || history.isEmpty()) {
            return createEmptyVelocity();
        }

        // Sort by date
        List<Changelog> sorted = new ArrayList<>(history);
        sorted.sort(Comparator.comparing(Changelog::getGeneratedAt));

        // Calculate totals
        int totalChanges = 0;
        int totalBreakingChanges = 0;

        for (Changelog changelog : sorted) {
            totalChanges += changelog.getChanges().size() + changelog.getBreakingChanges().size();
            totalBreakingChanges += changelog.getBreakingChanges().size();
        }

        // Calculate time span
        LocalDateTime firstDate = sorted.get(0).getGeneratedAt();
        LocalDateTime lastDate = sorted.get(sorted.size() - 1).getGeneratedAt();
        Duration timeSpan = Duration.between(firstDate, lastDate);
        long totalDays = Math.max(1, timeSpan.toDays());

        // Calculate velocity metrics
        double changesPerDay = (double) totalChanges / totalDays;
        double changesPerWeek = changesPerDay * AnalyticsConstants.DAYS_PER_WEEK;
        double changesPerMonth = changesPerDay * AnalyticsConstants.DAYS_PER_MONTH;

        // Calculate breaking changes per release
        double breakingPerRelease = sorted.size() > 0 ?
                (double) totalBreakingChanges / sorted.size() : 0;

        // Calculate average time between releases
        Duration avgTimeBetweenReleases = calculateAverageTimeBetweenReleases(sorted);

        // Determine if accelerating
        boolean accelerating = isAccelerating(sorted);
        double accelerationRate = calculateAccelerationRate(sorted);

        return ChangeVelocity.builder()
                .changesPerDay(changesPerDay)
                .changesPerWeek(changesPerWeek)
                .changesPerMonth(changesPerMonth)
                .breakingChangesPerRelease(breakingPerRelease)
                .averageTimeBetweenReleases(avgTimeBetweenReleases)
                .accelerating(accelerating)
                .accelerationRate(accelerationRate)
                .totalReleases(sorted.size())
                .totalChanges(totalChanges)
                .totalBreakingChanges(totalBreakingChanges)
                .periodStart(firstDate)
                .periodEnd(lastDate)
                .build();
    }

    /**
     * Calculate changes per period.
     *
     * @param history list of changelogs
     * @param periodDays number of days in period
     * @return average changes per period
     */
    public double changesPerPeriod(List<Changelog> history, int periodDays) {
        if (history == null || history.isEmpty()) {
            return 0.0;
        }

        ChangeVelocity velocity = calculate(history);
        return velocity.getChangesPerDay() * periodDays;
    }

    /**
     * Check if the change velocity is accelerating.
     *
     * @param history list of changelogs
     * @return true if accelerating
     */
    public boolean isAccelerating(List<Changelog> history) {
        if (history == null || history.size() < 4) {
            return false;
        }

        List<Changelog> sorted = new ArrayList<>(history);
        sorted.sort(Comparator.comparing(Changelog::getGeneratedAt));

        // Compare first half vs second half
        int midpoint = sorted.size() / 2;

        int firstHalfChanges = 0;
        int secondHalfChanges = 0;

        for (int i = 0; i < midpoint; i++) {
            firstHalfChanges += sorted.get(i).getChanges().size() +
                    sorted.get(i).getBreakingChanges().size();
        }

        for (int i = midpoint; i < sorted.size(); i++) {
            secondHalfChanges += sorted.get(i).getChanges().size() +
                    sorted.get(i).getBreakingChanges().size();
        }

        // Accelerating if second half has significantly more changes
        double ratio = firstHalfChanges > 0 ?
                (double) secondHalfChanges / firstHalfChanges : 1.0;

        return ratio > 1.2; // 20% increase threshold
    }

    private Duration calculateAverageTimeBetweenReleases(List<Changelog> sorted) {
        if (sorted.size() < 2) {
            return Duration.ZERO;
        }

        long totalSeconds = 0;
        for (int i = 1; i < sorted.size(); i++) {
            Duration between = Duration.between(
                    sorted.get(i - 1).getGeneratedAt(),
                    sorted.get(i).getGeneratedAt()
            );
            totalSeconds += between.getSeconds();
        }

        return Duration.ofSeconds(totalSeconds / (sorted.size() - 1));
    }

    private double calculateAccelerationRate(List<Changelog> sorted) {
        if (sorted.size() < 4) {
            return 0.0;
        }

        int midpoint = sorted.size() / 2;

        // Calculate average changes per release for each half
        double firstHalfAvg = 0;
        double secondHalfAvg = 0;

        for (int i = 0; i < midpoint; i++) {
            firstHalfAvg += sorted.get(i).getChanges().size() +
                    sorted.get(i).getBreakingChanges().size();
        }
        firstHalfAvg /= midpoint;

        for (int i = midpoint; i < sorted.size(); i++) {
            secondHalfAvg += sorted.get(i).getChanges().size() +
                    sorted.get(i).getBreakingChanges().size();
        }
        secondHalfAvg /= (sorted.size() - midpoint);

        if (firstHalfAvg == 0) {
            return secondHalfAvg > 0 ? 1.0 : 0.0;
        }

        return (secondHalfAvg - firstHalfAvg) / firstHalfAvg;
    }

    private ChangeVelocity createEmptyVelocity() {
        return ChangeVelocity.builder()
                .changesPerDay(0)
                .changesPerWeek(0)
                .changesPerMonth(0)
                .breakingChangesPerRelease(0)
                .averageTimeBetweenReleases(Duration.ZERO)
                .accelerating(false)
                .accelerationRate(0)
                .totalReleases(0)
                .totalChanges(0)
                .totalBreakingChanges(0)
                .build();
    }
}
