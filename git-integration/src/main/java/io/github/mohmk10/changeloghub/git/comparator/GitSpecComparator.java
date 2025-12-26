package io.github.mohmk10.changeloghub.git.comparator;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.git.analyzer.DiffAnalyzer;
import io.github.mohmk10.changeloghub.git.config.GitConfig;
import io.github.mohmk10.changeloghub.git.extractor.ChangelogExtractor;
import io.github.mohmk10.changeloghub.git.extractor.SpecFileDetector;
import io.github.mohmk10.changeloghub.git.model.GitDiff;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compares API specifications between Git references.
 */
public class GitSpecComparator {

    private static final Logger logger = LoggerFactory.getLogger(GitSpecComparator.class);

    private final Repository repository;
    private final GitConfig config;
    private final ChangelogExtractor changelogExtractor;
    private final DiffAnalyzer diffAnalyzer;
    private final SpecFileDetector specFileDetector;

    public GitSpecComparator(Repository repository) {
        this(repository, new GitConfig());
    }

    public GitSpecComparator(Repository repository, GitConfig config) {
        this.repository = repository;
        this.config = config;
        this.changelogExtractor = new ChangelogExtractor(repository, config);
        this.diffAnalyzer = new DiffAnalyzer(repository, config);
        this.specFileDetector = new SpecFileDetector(repository, config);
    }

    /**
     * Compare all API specs between two references.
     *
     * @param oldRef the old reference
     * @param newRef the new reference
     * @return comparison result
     */
    public SpecComparison compare(String oldRef, String newRef) {
        logger.info("Comparing specs between {} and {}", oldRef, newRef);

        // Get all changelogs
        List<ChangelogExtractor.SpecChangelog> changelogs =
            changelogExtractor.extractChangelogs(oldRef, newRef);

        // Get summary
        ChangelogExtractor.ChangelogSummary summary = changelogExtractor.summarize(changelogs);

        // Get diff for context
        GitDiff diff = diffAnalyzer.getDiff(oldRef, newRef);

        // Group by spec type
        Map<SpecFileDetector.SpecType, List<ChangelogExtractor.SpecChangelog>> byType =
            changelogs.stream()
                .collect(Collectors.groupingBy(ChangelogExtractor.SpecChangelog::getSpecType));

        return new SpecComparison(
            oldRef,
            newRef,
            changelogs,
            summary,
            diff,
            byType
        );
    }

    /**
     * Compare only changed spec files (more efficient for large repos).
     */
    public SpecComparison compareChangedOnly(String oldRef, String newRef) {
        logger.info("Comparing changed specs between {} and {}", oldRef, newRef);

        GitDiff diff = diffAnalyzer.getDiff(oldRef, newRef);
        List<ChangelogExtractor.SpecChangelog> changelogs =
            changelogExtractor.extractChangelogsForChangedFiles(diff, oldRef, newRef);

        ChangelogExtractor.ChangelogSummary summary = changelogExtractor.summarize(changelogs);

        Map<SpecFileDetector.SpecType, List<ChangelogExtractor.SpecChangelog>> byType =
            changelogs.stream()
                .collect(Collectors.groupingBy(ChangelogExtractor.SpecChangelog::getSpecType));

        return new SpecComparison(
            oldRef,
            newRef,
            changelogs,
            summary,
            diff,
            byType
        );
    }

    /**
     * Compare specs of a specific type only.
     */
    public SpecComparison compareByType(String oldRef, String newRef, SpecFileDetector.SpecType type) {
        SpecComparison full = compare(oldRef, newRef);

        List<ChangelogExtractor.SpecChangelog> filtered = full.getChangelogs().stream()
            .filter(c -> c.getSpecType() == type)
            .collect(Collectors.toList());

        ChangelogExtractor.ChangelogSummary summary = changelogExtractor.summarize(filtered);

        Map<SpecFileDetector.SpecType, List<ChangelogExtractor.SpecChangelog>> byType = new EnumMap<>(SpecFileDetector.SpecType.class);
        byType.put(type, filtered);

        return new SpecComparison(
            oldRef,
            newRef,
            filtered,
            summary,
            full.getDiff(),
            byType
        );
    }

    /**
     * Compare a single spec file between references.
     */
    public Optional<ChangelogExtractor.SpecChangelog> compareFile(
            String specPath, String oldRef, String newRef) {
        return Optional.ofNullable(
            changelogExtractor.extractChangelogForFile(specPath, oldRef, newRef)
        );
    }

    /**
     * Check if there are breaking changes between refs.
     */
    public boolean hasBreakingChanges(String oldRef, String newRef) {
        SpecComparison comparison = compareChangedOnly(oldRef, newRef);
        return comparison.hasBreakingChanges();
    }

    /**
     * Get only breaking changes between refs.
     */
    public List<BreakingChange> getBreakingChanges(String oldRef, String newRef) {
        SpecComparison comparison = compare(oldRef, newRef);
        List<BreakingChange> breaking = new ArrayList<>();

        for (ChangelogExtractor.SpecChangelog specChangelog : comparison.getChangelogs()) {
            if (specChangelog.isDeleted()) {
                breaking.add(new BreakingChange(
                    specChangelog.getSpecPath(),
                    specChangelog.getSpecType(),
                    "API_REMOVED",
                    "Entire API specification removed"
                ));
            } else if (specChangelog.getChangelog() != null) {
                for (io.github.mohmk10.changeloghub.core.model.BreakingChange change :
                        specChangelog.getChangelog().getBreakingChanges()) {
                    breaking.add(new BreakingChange(
                        specChangelog.getSpecPath(),
                        specChangelog.getSpecType(),
                        change.getCategory() != null ? change.getCategory().name() : "UNKNOWN",
                        change.getDescription()
                    ));
                }
            }
        }

        return breaking;
    }

    /**
     * Generate a migration guide between versions.
     */
    public MigrationGuide generateMigrationGuide(String oldRef, String newRef) {
        SpecComparison comparison = compare(oldRef, newRef);
        List<BreakingChange> breaking = getBreakingChanges(oldRef, newRef);

        List<MigrationStep> steps = new ArrayList<>();

        for (BreakingChange bc : breaking) {
            steps.add(new MigrationStep(
                bc.getSpecPath(),
                bc.getCategory(),
                bc.getDescription(),
                generateMigrationSuggestion(bc)
            ));
        }

        return new MigrationGuide(
            oldRef,
            newRef,
            steps,
            comparison.getSummary().getTotalChanges(),
            breaking.size()
        );
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private String generateMigrationSuggestion(BreakingChange change) {
        String category = change.getCategory();

        if (category.contains("REMOVED")) {
            return "This item has been removed. Check for replacement APIs or update your code to not depend on it.";
        }
        if (category.contains("PARAMETER") || category.contains("REQUIRED")) {
            return "Parameter requirements have changed. Update your API calls to include the required parameters.";
        }
        if (category.contains("TYPE")) {
            return "Type has changed. Update your code to handle the new type format.";
        }
        if (category.contains("PATH") || category.contains("ENDPOINT")) {
            return "Endpoint path has changed. Update your API client configuration.";
        }
        if (category.contains("RESPONSE")) {
            return "Response format has changed. Update your response handling code.";
        }

        return "Review the change and update your implementation accordingly.";
    }

    // ============================================================
    // Nested Types
    // ============================================================

    /**
     * Result of comparing specs between two references.
     */
    public static class SpecComparison {
        private final String oldRef;
        private final String newRef;
        private final List<ChangelogExtractor.SpecChangelog> changelogs;
        private final ChangelogExtractor.ChangelogSummary summary;
        private final GitDiff diff;
        private final Map<SpecFileDetector.SpecType, List<ChangelogExtractor.SpecChangelog>> byType;

        public SpecComparison(String oldRef, String newRef,
                             List<ChangelogExtractor.SpecChangelog> changelogs,
                             ChangelogExtractor.ChangelogSummary summary,
                             GitDiff diff,
                             Map<SpecFileDetector.SpecType, List<ChangelogExtractor.SpecChangelog>> byType) {
            this.oldRef = oldRef;
            this.newRef = newRef;
            this.changelogs = changelogs;
            this.summary = summary;
            this.diff = diff;
            this.byType = byType;
        }

        public String getOldRef() {
            return oldRef;
        }

        public String getNewRef() {
            return newRef;
        }

        public List<ChangelogExtractor.SpecChangelog> getChangelogs() {
            return Collections.unmodifiableList(changelogs);
        }

        public ChangelogExtractor.ChangelogSummary getSummary() {
            return summary;
        }

        public GitDiff getDiff() {
            return diff;
        }

        public Map<SpecFileDetector.SpecType, List<ChangelogExtractor.SpecChangelog>> getByType() {
            return Collections.unmodifiableMap(byType);
        }

        public boolean hasBreakingChanges() {
            return summary.hasBreakingChanges();
        }

        public boolean hasChanges() {
            return summary.getTotalChanges() > 0;
        }

        public List<ChangelogExtractor.SpecChangelog> getOpenApiChanges() {
            return byType.getOrDefault(SpecFileDetector.SpecType.OPENAPI, Collections.emptyList());
        }

        public List<ChangelogExtractor.SpecChangelog> getGraphQLChanges() {
            return byType.getOrDefault(SpecFileDetector.SpecType.GRAPHQL, Collections.emptyList());
        }

        public List<ChangelogExtractor.SpecChangelog> getProtobufChanges() {
            return byType.getOrDefault(SpecFileDetector.SpecType.PROTOBUF, Collections.emptyList());
        }

        public List<ChangelogExtractor.SpecChangelog> getAsyncApiChanges() {
            return byType.getOrDefault(SpecFileDetector.SpecType.ASYNCAPI, Collections.emptyList());
        }

        @Override
        public String toString() {
            return "SpecComparison{" +
                   "oldRef='" + oldRef + '\'' +
                   ", newRef='" + newRef + '\'' +
                   ", specs=" + changelogs.size() +
                   ", totalChanges=" + summary.getTotalChanges() +
                   ", breakingChanges=" + summary.getBreakingChanges() +
                   '}';
        }
    }

    /**
     * Represents a breaking change in an API spec.
     */
    public static class BreakingChange {
        private final String specPath;
        private final SpecFileDetector.SpecType specType;
        private final String category;
        private final String description;

        public BreakingChange(String specPath, SpecFileDetector.SpecType specType,
                             String category, String description) {
            this.specPath = specPath;
            this.specType = specType;
            this.category = category;
            this.description = description;
        }

        public String getSpecPath() {
            return specPath;
        }

        public SpecFileDetector.SpecType getSpecType() {
            return specType;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "BreakingChange{" +
                   "specPath='" + specPath + '\'' +
                   ", category='" + category + '\'' +
                   ", description='" + description + '\'' +
                   '}';
        }
    }

    /**
     * A step in the migration guide.
     */
    public static class MigrationStep {
        private final String specPath;
        private final String changeType;
        private final String description;
        private final String suggestion;

        public MigrationStep(String specPath, String changeType,
                            String description, String suggestion) {
            this.specPath = specPath;
            this.changeType = changeType;
            this.description = description;
            this.suggestion = suggestion;
        }

        public String getSpecPath() {
            return specPath;
        }

        public String getChangeType() {
            return changeType;
        }

        public String getDescription() {
            return description;
        }

        public String getSuggestion() {
            return suggestion;
        }

        @Override
        public String toString() {
            return "MigrationStep{" +
                   "specPath='" + specPath + '\'' +
                   ", changeType='" + changeType + '\'' +
                   '}';
        }
    }

    /**
     * Migration guide for upgrading between versions.
     */
    public static class MigrationGuide {
        private final String fromRef;
        private final String toRef;
        private final List<MigrationStep> steps;
        private final int totalChanges;
        private final int breakingChanges;

        public MigrationGuide(String fromRef, String toRef,
                             List<MigrationStep> steps,
                             int totalChanges, int breakingChanges) {
            this.fromRef = fromRef;
            this.toRef = toRef;
            this.steps = steps;
            this.totalChanges = totalChanges;
            this.breakingChanges = breakingChanges;
        }

        public String getFromRef() {
            return fromRef;
        }

        public String getToRef() {
            return toRef;
        }

        public List<MigrationStep> getSteps() {
            return Collections.unmodifiableList(steps);
        }

        public int getTotalChanges() {
            return totalChanges;
        }

        public int getBreakingChanges() {
            return breakingChanges;
        }

        public boolean hasBreakingChanges() {
            return breakingChanges > 0;
        }

        /**
         * Format as Markdown.
         */
        public String toMarkdown() {
            StringBuilder sb = new StringBuilder();
            sb.append("# Migration Guide\n\n");
            sb.append("**From:** ").append(fromRef).append(" **To:** ").append(toRef).append("\n\n");

            if (breakingChanges == 0) {
                sb.append("No breaking changes detected.\n\n");
                return sb.toString();
            }

            sb.append("## Breaking Changes (").append(breakingChanges).append(")\n\n");

            for (MigrationStep step : steps) {
                sb.append("### ").append(step.getSpecPath()).append("\n\n");
                sb.append("**Change:** ").append(step.getChangeType()).append("\n\n");
                sb.append(step.getDescription()).append("\n\n");
                sb.append("**Migration:** ").append(step.getSuggestion()).append("\n\n");
                sb.append("---\n\n");
            }

            return sb.toString();
        }

        @Override
        public String toString() {
            return "MigrationGuide{" +
                   "fromRef='" + fromRef + '\'' +
                   ", toRef='" + toRef + '\'' +
                   ", steps=" + steps.size() +
                   ", breakingChanges=" + breakingChanges +
                   '}';
        }
    }
}
