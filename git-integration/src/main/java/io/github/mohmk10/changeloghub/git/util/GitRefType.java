package io.github.mohmk10.changeloghub.git.util;

public enum GitRefType {

    BRANCH("branch", "refs/heads/"),

    TAG("tag", "refs/tags/"),

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

        if (ref.matches("[0-9a-fA-F]{7,40}")) {
            return COMMIT;
        }

        return BRANCH;
    }

    public static boolean isCommitSha(String ref) {
        return ref != null && ref.matches("[0-9a-fA-F]{7,40}");
    }

    public static boolean isFullCommitSha(String ref) {
        return ref != null && ref.matches("[0-9a-fA-F]{40}");
    }

    public static boolean isShortCommitSha(String ref) {
        return ref != null && ref.matches("[0-9a-fA-F]{7,39}");
    }
}
