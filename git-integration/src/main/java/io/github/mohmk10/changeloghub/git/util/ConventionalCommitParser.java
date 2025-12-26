package io.github.mohmk10.changeloghub.git.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Conventional Commits format.
 *
 * Format: &lt;type&gt;[optional scope]: &lt;description&gt;
 *
 * Examples:
 * - feat: add new feature
 * - fix(auth): resolve login issue
 * - feat!: breaking change
 * - chore(deps): update dependencies
 *
 * @see <a href="https://www.conventionalcommits.org/">Conventional Commits</a>
 */
public class ConventionalCommitParser {

    // Pattern: type(scope)!: description
    private static final Pattern CONVENTIONAL_COMMIT_PATTERN =
        Pattern.compile("^(\\w+)(?:\\(([^)]+)\\))?(!)?: (.+)$", Pattern.MULTILINE);

    private ConventionalCommitParser() {
        // Utility class
    }

    /**
     * Parse a commit message into a ConventionalCommit object.
     *
     * @param message the commit message
     * @return parsed commit or null if not a conventional commit
     */
    public static ConventionalCommit parse(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        // Get only the first line (subject)
        String subject = message.split("\\R", 2)[0].trim();

        Matcher matcher = CONVENTIONAL_COMMIT_PATTERN.matcher(subject);
        if (!matcher.matches()) {
            return null;
        }

        String type = matcher.group(1);
        String scope = matcher.group(2);
        boolean breaking = matcher.group(3) != null;
        String description = matcher.group(4);

        // Check for BREAKING CHANGE in footer
        if (!breaking && message.contains("BREAKING CHANGE:")) {
            breaking = true;
        }
        if (!breaking && message.contains("BREAKING-CHANGE:")) {
            breaking = true;
        }

        // Extract body and footer if present
        String[] parts = message.split("\\R\\R", 2);
        String body = parts.length > 1 ? parts[1].trim() : null;

        return new ConventionalCommit(type, scope, description, body, breaking);
    }

    /**
     * Check if a commit message follows conventional commits format.
     */
    public static boolean isConventionalCommit(String message) {
        return parse(message) != null;
    }

    /**
     * Get the commit type from a message (or null if not conventional).
     */
    public static String getType(String message) {
        ConventionalCommit commit = parse(message);
        return commit != null ? commit.getType() : null;
    }

    /**
     * Check if a commit represents a feature.
     */
    public static boolean isFeature(String message) {
        String type = getType(message);
        return "feat".equals(type) || "feature".equals(type);
    }

    /**
     * Check if a commit represents a bug fix.
     */
    public static boolean isFix(String message) {
        String type = getType(message);
        return "fix".equals(type) || "bugfix".equals(type);
    }

    /**
     * Check if a commit represents a breaking change.
     */
    public static boolean isBreakingChange(String message) {
        ConventionalCommit commit = parse(message);
        return commit != null && commit.isBreaking();
    }

    /**
     * Represents a parsed conventional commit.
     */
    public static class ConventionalCommit {
        private final String type;
        private final String scope;
        private final String description;
        private final String body;
        private final boolean breaking;

        public ConventionalCommit(String type, String scope, String description,
                                   String body, boolean breaking) {
            this.type = type;
            this.scope = scope;
            this.description = description;
            this.body = body;
            this.breaking = breaking;
        }

        public String getType() {
            return type;
        }

        public String getScope() {
            return scope;
        }

        public String getDescription() {
            return description;
        }

        public String getBody() {
            return body;
        }

        public boolean isBreaking() {
            return breaking;
        }

        public boolean hasScope() {
            return scope != null && !scope.isEmpty();
        }

        /**
         * Check if this is a feature commit.
         */
        public boolean isFeature() {
            return "feat".equals(type) || "feature".equals(type);
        }

        /**
         * Check if this is a fix commit.
         */
        public boolean isFix() {
            return "fix".equals(type) || "bugfix".equals(type);
        }

        /**
         * Check if this is a chore/maintenance commit.
         */
        public boolean isChore() {
            return "chore".equals(type);
        }

        /**
         * Check if this is a documentation commit.
         */
        public boolean isDocs() {
            return "docs".equals(type);
        }

        /**
         * Check if this is a refactoring commit.
         */
        public boolean isRefactor() {
            return "refactor".equals(type);
        }

        /**
         * Check if this is a test commit.
         */
        public boolean isTest() {
            return "test".equals(type);
        }

        /**
         * Check if this is a style commit.
         */
        public boolean isStyle() {
            return "style".equals(type);
        }

        /**
         * Check if this is a performance commit.
         */
        public boolean isPerf() {
            return "perf".equals(type);
        }

        /**
         * Check if this is a CI commit.
         */
        public boolean isCi() {
            return "ci".equals(type);
        }

        /**
         * Check if this is a build commit.
         */
        public boolean isBuild() {
            return "build".equals(type);
        }

        /**
         * Get the formatted commit message.
         */
        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            if (scope != null) {
                sb.append("(").append(scope).append(")");
            }
            if (breaking) {
                sb.append("!");
            }
            sb.append(": ").append(description);
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConventionalCommit that = (ConventionalCommit) o;
            return breaking == that.breaking &&
                   Objects.equals(type, that.type) &&
                   Objects.equals(scope, that.scope) &&
                   Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, scope, description, breaking);
        }

        @Override
        public String toString() {
            return format();
        }
    }
}
