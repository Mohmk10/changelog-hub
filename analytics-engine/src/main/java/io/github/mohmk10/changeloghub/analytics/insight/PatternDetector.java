package io.github.mohmk10.changeloghub.analytics.insight;

import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.time.DayOfWeek;
import java.time.Month;
import java.util.*;

public class PatternDetector {

    public static class Pattern {
        private PatternType type;
        private String description;
        private double confidence;
        private Map<String, Object> details;

        public enum PatternType {
            FREQUENT_BREAKING_CHANGES,
            SEASONAL_RELEASES,
            WEEKLY_PATTERN,
            MONTHLY_PATTERN,
            BURST_RELEASES,
            GRADUAL_DEPRECATION,
            VERSION_JUMPING
        }

        public Pattern(PatternType type, String description, double confidence) {
            this.type = type;
            this.description = description;
            this.confidence = confidence;
            this.details = new HashMap<>();
        }

        public PatternType getType() { return type; }
        public String getDescription() { return description; }
        public double getConfidence() { return confidence; }
        public Map<String, Object> getDetails() { return details; }
        public void addDetail(String key, Object value) { details.put(key, value); }
    }

    public List<Pattern> detectPatterns(List<Changelog> history) {
        List<Pattern> patterns = new ArrayList<>();

        if (history == null || history.size() < 3) {
            return patterns;
        }

        Pattern frequentBreaking = detectFrequentBreakingChanges(history);
        if (frequentBreaking != null) patterns.add(frequentBreaking);

        Pattern seasonal = detectSeasonalPattern(history);
        if (seasonal != null) patterns.add(seasonal);

        Pattern weekly = detectWeeklyPattern(history);
        if (weekly != null) patterns.add(weekly);

        Pattern burst = detectBurstReleases(history);
        if (burst != null) patterns.add(burst);

        return patterns;
    }

    public Pattern detectFrequentBreakingChanges(List<Changelog> history) {
        if (history == null || history.isEmpty()) return null;

        int withBreaking = 0;
        for (Changelog changelog : history) {
            if (!changelog.getBreakingChanges().isEmpty()) {
                withBreaking++;
            }
        }

        double ratio = (double) withBreaking / history.size();
        if (ratio > 0.5) {
            Pattern pattern = new Pattern(
                    Pattern.PatternType.FREQUENT_BREAKING_CHANGES,
                    String.format("%.0f%% of releases contain breaking changes", ratio * 100),
                    ratio
            );
            pattern.addDetail("releaseCount", history.size());
            pattern.addDetail("withBreakingCount", withBreaking);
            return pattern;
        }

        return null;
    }

    public Pattern detectSeasonalPattern(List<Changelog> history) {
        if (history == null || history.size() < 6) return null;

        Map<Month, Integer> monthCounts = new EnumMap<>(Month.class);
        for (Changelog changelog : history) {
            Month month = changelog.getGeneratedAt().getMonth();
            monthCounts.merge(month, 1, Integer::sum);
        }

        int maxCount = monthCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int avgCount = history.size() / 12;

        if (maxCount > avgCount * 2 && maxCount >= 3) {
            Month peakMonth = monthCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            Pattern pattern = new Pattern(
                    Pattern.PatternType.SEASONAL_RELEASES,
                    String.format("Peak releases in %s", peakMonth),
                    0.7
            );
            pattern.addDetail("peakMonth", peakMonth);
            pattern.addDetail("peakCount", maxCount);
            return pattern;
        }

        return null;
    }

    public Pattern detectWeeklyPattern(List<Changelog> history) {
        if (history == null || history.size() < 5) return null;

        Map<DayOfWeek, Integer> dayCounts = new EnumMap<>(DayOfWeek.class);
        for (Changelog changelog : history) {
            DayOfWeek day = changelog.getGeneratedAt().getDayOfWeek();
            dayCounts.merge(day, 1, Integer::sum);
        }

        int maxCount = dayCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int avgCount = history.size() / 7;

        if (maxCount > avgCount * 2 && maxCount >= 3) {
            DayOfWeek peakDay = dayCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            Pattern pattern = new Pattern(
                    Pattern.PatternType.WEEKLY_PATTERN,
                    String.format("Releases typically occur on %s", peakDay),
                    0.75
            );
            pattern.addDetail("peakDay", peakDay);
            pattern.addDetail("count", maxCount);
            return pattern;
        }

        return null;
    }

    public Pattern detectBurstReleases(List<Changelog> history) {
        if (history == null || history.size() < 4) return null;

        List<Changelog> sorted = new ArrayList<>(history);
        sorted.sort(Comparator.comparing(Changelog::getGeneratedAt));

        int burstCount = 0;
        for (int i = 1; i < sorted.size(); i++) {
            long hoursBetween = java.time.Duration.between(
                    sorted.get(i - 1).getGeneratedAt(),
                    sorted.get(i).getGeneratedAt()
            ).toHours();

            if (hoursBetween < 24) {
                burstCount++;
            }
        }

        double burstRatio = (double) burstCount / (sorted.size() - 1);
        if (burstRatio > 0.3 && burstCount >= 2) {
            Pattern pattern = new Pattern(
                    Pattern.PatternType.BURST_RELEASES,
                    String.format("%.0f%% of releases occur within 24 hours of each other", burstRatio * 100),
                    burstRatio
            );
            pattern.addDetail("burstCount", burstCount);
            return pattern;
        }

        return null;
    }
}
