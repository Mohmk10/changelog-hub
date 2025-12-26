package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.StabilityScore;
import io.github.mohmk10.changeloghub.analytics.model.StabilityScore.StabilityFactor;
import io.github.mohmk10.changeloghub.analytics.util.AnalyticsConstants;
import io.github.mohmk10.changeloghub.analytics.util.StabilityGrade;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculates stability scores for APIs based on their changelog history.
 */
public class StabilityScorer {

    /**
     * Calculate stability score from changelog history.
     *
     * @param changelogs list of changelogs to analyze
     * @return calculated stability score
     */
    public StabilityScore calculate(List<Changelog> changelogs) {
        if (changelogs == null || changelogs.isEmpty()) {
            return createEmptyScore();
        }

        int totalChanges = 0;
        int breakingChanges = 0;

        for (Changelog changelog : changelogs) {
            totalChanges += changelog.getChanges().size();
            breakingChanges += changelog.getBreakingChanges().size();
        }

        // Calculate individual factor scores
        double breakingRatioScore = calculateBreakingRatioScore(totalChanges, breakingChanges);
        double timeBetweenScore = calculateTimeBetweenBreakingScore(changelogs);
        double deprecationScore = calculateDeprecationManagementScore(changelogs);
        double semverScore = calculateSemverComplianceScore(changelogs);

        // Calculate weighted final score
        double finalScore = (breakingRatioScore * AnalyticsConstants.WEIGHT_BREAKING_CHANGE_RATIO) +
                (timeBetweenScore * AnalyticsConstants.WEIGHT_TIME_BETWEEN_BREAKING) +
                (deprecationScore * AnalyticsConstants.WEIGHT_DEPRECATION_MANAGEMENT) +
                (semverScore * AnalyticsConstants.WEIGHT_SEMVER_COMPLIANCE);

        int score = (int) Math.round(finalScore);

        List<StabilityFactor> factors = new ArrayList<>();
        factors.add(new StabilityFactor("Breaking Change Ratio",
                AnalyticsConstants.WEIGHT_BREAKING_CHANGE_RATIO,
                breakingRatioScore,
                String.format("%.1f%% breaking changes", (totalChanges > 0 ? (double) breakingChanges / totalChanges * 100 : 0))));

        factors.add(new StabilityFactor("Time Between Breaking Changes",
                AnalyticsConstants.WEIGHT_TIME_BETWEEN_BREAKING,
                timeBetweenScore,
                "Average time between breaking changes"));

        factors.add(new StabilityFactor("Deprecation Management",
                AnalyticsConstants.WEIGHT_DEPRECATION_MANAGEMENT,
                deprecationScore,
                "How well deprecations are managed"));

        factors.add(new StabilityFactor("SemVer Compliance",
                AnalyticsConstants.WEIGHT_SEMVER_COMPLIANCE,
                semverScore,
                "Compliance with semantic versioning"));

        return StabilityScore.builder()
                .score(score)
                .breakingChangeRatio(totalChanges > 0 ? (double) breakingChanges / totalChanges : 0)
                .timeBetweenBreakingChangesScore(timeBetweenScore)
                .deprecationManagementScore(deprecationScore)
                .semverComplianceScore(semverScore)
                .totalChangesAnalyzed(totalChanges)
                .breakingChangesCount(breakingChanges)
                .periodsAnalyzed(changelogs.size())
                .factors(factors)
                .build();
    }

    /**
     * Calculate stability score for a specific API.
     *
     * @param apiName the API name
     * @param changelogs list of changelogs
     * @return stability score with API name set
     */
    public StabilityScore calculate(String apiName, List<Changelog> changelogs) {
        StabilityScore score = calculate(changelogs);
        score.setApiName(apiName);
        return score;
    }

    private double calculateBreakingRatioScore(int totalChanges, int breakingChanges) {
        if (totalChanges == 0) {
            return 100.0;
        }

        double ratio = (double) breakingChanges / totalChanges;

        // Score inversely proportional to breaking change ratio
        // 0% breaking = 100 score, 50%+ breaking = 0 score
        if (ratio >= 0.5) {
            return 0.0;
        }

        return 100.0 * (1.0 - (ratio * 2));
    }

    private double calculateTimeBetweenBreakingScore(List<Changelog> changelogs) {
        List<LocalDateTime> breakingDates = new ArrayList<>();

        for (Changelog changelog : changelogs) {
            if (!changelog.getBreakingChanges().isEmpty()) {
                breakingDates.add(changelog.getGeneratedAt());
            }
        }

        if (breakingDates.size() < 2) {
            return 100.0; // Not enough data, assume good
        }

        // Sort dates
        breakingDates.sort(LocalDateTime::compareTo);

        // Calculate average days between breaking changes
        long totalDays = 0;
        for (int i = 1; i < breakingDates.size(); i++) {
            Duration duration = Duration.between(breakingDates.get(i - 1), breakingDates.get(i));
            totalDays += duration.toDays();
        }

        double avgDays = (double) totalDays / (breakingDates.size() - 1);

        // Score based on average days between breaking changes
        // 90+ days = 100, 0 days = 0
        if (avgDays >= 90) {
            return 100.0;
        }

        return (avgDays / 90.0) * 100.0;
    }

    private double calculateDeprecationManagementScore(List<Changelog> changelogs) {
        int deprecations = 0;
        int properDeprecations = 0;

        for (Changelog changelog : changelogs) {
            changelog.getChanges().forEach(change -> {
                if (change.getDescription() != null &&
                        change.getDescription().toLowerCase().contains("deprecat")) {
                    // Count as deprecation
                }
            });
        }

        // For now, assume good deprecation management if no breaking changes
        // without deprecation warnings
        int breakingWithoutWarning = 0;
        for (Changelog changelog : changelogs) {
            if (!changelog.getBreakingChanges().isEmpty()) {
                boolean hasDeprecationWarning = changelog.getChanges().stream()
                        .anyMatch(c -> c.getDescription() != null &&
                                c.getDescription().toLowerCase().contains("deprecat"));
                if (!hasDeprecationWarning) {
                    breakingWithoutWarning++;
                }
            }
        }

        if (changelogs.isEmpty()) {
            return 100.0;
        }

        double ratio = (double) breakingWithoutWarning / changelogs.size();
        return 100.0 * (1.0 - ratio);
    }

    private double calculateSemverComplianceScore(List<Changelog> changelogs) {
        int compliant = 0;
        int nonCompliant = 0;

        for (Changelog changelog : changelogs) {
            String fromVersion = changelog.getFromVersion();
            String toVersion = changelog.getToVersion();

            if (fromVersion == null || toVersion == null) {
                continue;
            }

            boolean hasBreaking = !changelog.getBreakingChanges().isEmpty();
            boolean isMajorBump = isMajorVersionBump(fromVersion, toVersion);

            if (hasBreaking && isMajorBump) {
                compliant++;
            } else if (hasBreaking && !isMajorBump) {
                nonCompliant++;
            } else {
                compliant++;
            }
        }

        int total = compliant + nonCompliant;
        if (total == 0) {
            return 100.0;
        }

        return (double) compliant / total * 100.0;
    }

    private boolean isMajorVersionBump(String fromVersion, String toVersion) {
        try {
            int fromMajor = extractMajorVersion(fromVersion);
            int toMajor = extractMajorVersion(toVersion);
            return toMajor > fromMajor;
        } catch (Exception e) {
            return false;
        }
    }

    private int extractMajorVersion(String version) {
        if (version == null) return 0;
        String cleaned = version.replaceAll("^[vV]", "");
        String[] parts = cleaned.split("\\.");
        if (parts.length > 0) {
            return Integer.parseInt(parts[0]);
        }
        return 0;
    }

    private StabilityScore createEmptyScore() {
        return StabilityScore.builder()
                .score(100)
                .breakingChangeRatio(0)
                .totalChangesAnalyzed(0)
                .breakingChangesCount(0)
                .periodsAnalyzed(0)
                .build();
    }

    /**
     * Get the grade for a given score.
     *
     * @param score the numeric score
     * @return the stability grade
     */
    public StabilityGrade getGrade(int score) {
        return StabilityGrade.fromScore(score);
    }
}
