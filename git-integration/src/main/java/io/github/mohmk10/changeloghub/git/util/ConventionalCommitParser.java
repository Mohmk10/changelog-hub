package io.github.mohmk10.changeloghub.git.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConventionalCommitParser {

    private static final Pattern CONVENTIONAL_COMMIT_PATTERN =
        Pattern.compile("^(\\w+)(?:\\(([^)]+)\\))?(!)?: (.+)$", Pattern.MULTILINE);

    private ConventionalCommitParser() {
        
    }

    public static ConventionalCommit parse(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        String subject = message.split("\\R", 2)[0].trim();

        Matcher matcher = CONVENTIONAL_COMMIT_PATTERN.matcher(subject);
        if (!matcher.matches()) {
            return null;
        }

        String type = matcher.group(1);
        String scope = matcher.group(2);
        boolean breaking = matcher.group(3) != null;
        String description = matcher.group(4);

        if (!breaking && message.contains("BREAKING CHANGE:")) {
            breaking = true;
        }
        if (!breaking && message.contains("BREAKING-CHANGE:")) {
            breaking = true;
        }

        String[] parts = message.split("\\R\\R", 2);
        String body = parts.length > 1 ? parts[1].trim() : null;

        return new ConventionalCommit(type, scope, description, body, breaking);
    }

    public static boolean isConventionalCommit(String message) {
        return parse(message) != null;
    }

    public static String getType(String message) {
        ConventionalCommit commit = parse(message);
        return commit != null ? commit.getType() : null;
    }

    public static boolean isFeature(String message) {
        String type = getType(message);
        return "feat".equals(type) || "feature".equals(type);
    }

    public static boolean isFix(String message) {
        String type = getType(message);
        return "fix".equals(type) || "bugfix".equals(type);
    }

    public static boolean isBreakingChange(String message) {
        ConventionalCommit commit = parse(message);
        return commit != null && commit.isBreaking();
    }

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

        public boolean isFeature() {
            return "feat".equals(type) || "feature".equals(type);
        }

        public boolean isFix() {
            return "fix".equals(type) || "bugfix".equals(type);
        }

        public boolean isChore() {
            return "chore".equals(type);
        }

        public boolean isDocs() {
            return "docs".equals(type);
        }

        public boolean isRefactor() {
            return "refactor".equals(type);
        }

        public boolean isTest() {
            return "test".equals(type);
        }

        public boolean isStyle() {
            return "style".equals(type);
        }

        public boolean isPerf() {
            return "perf".equals(type);
        }

        public boolean isCi() {
            return "ci".equals(type);
        }

        public boolean isBuild() {
            return "build".equals(type);
        }

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
