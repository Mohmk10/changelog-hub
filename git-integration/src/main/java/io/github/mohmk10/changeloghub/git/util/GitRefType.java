package io.github.mohmk10.changeloghub.git.util;

/**
 * Enum representing the type of Git reference.
 */
public enum GitRefType {

    /**
     * A Git branch reference (e.g., main, feature/xyz).
     */
    BRANCH("branch", "refs/heads/"),

    /**
     * A Git tag reference (e.g., v1.0.0, release-1.2.3).
     */
    TAG("tag", "refs/tags/"),

    /**
     * A Git commit reference (SHA-1 hash).
     */
    COMMIT("commit", "");

    private final String displayName;
    private final String refPrefix;

    GitRefType(String displayName, String refPrefix) {
        this.displayName = displayName;
        this.refPrefix = refPrefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRefPrefix() {
        return refPrefix;
    }

    /**
     * Detect the type of reference from a ref string.
     */
    public static GitRefType fromRef(String ref) {
        if (ref == null || ref.isEmpty()) {
            return COMMIT;
        }

        if (ref.startsWith("refs/heads/") || ref.startsWith("refs/remotes/")) {
            return BRANCH;
        }

        if (ref.startsWith("refs/tags/")) {
            return TAG;
        }

        // Check if it looks like a commit SHA (40 hex chars or short form 7+)
        if (ref.matches("[0-9a-fA-F]{7,40}")) {
            return COMMIT;
        }

        // Could be a branch or tag name without prefix
        // Default to branch for named refs
        return BRANCH;
    }

    /**
     * Check if the given string could be a commit SHA.
     */
    public static boolean isCommitSha(String ref) {
        return ref != null && ref.matches("[0-9a-fA-F]{7,40}");
    }

    /**
     * Check if the given string is a full commit SHA (40 chars).
     */
    public static boolean isFullCommitSha(String ref) {
        return ref != null && ref.matches("[0-9a-fA-F]{40}");
    }

    /**
     * Check if the given string is a short commit SHA (7-39 chars).
     */
    public static boolean isShortCommitSha(String ref) {
        return ref != null && ref.matches("[0-9a-fA-F]{7,39}");
    }
}
