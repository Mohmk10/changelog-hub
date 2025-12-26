package io.github.mohmk10.changeloghub.git.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Constants for Git integration module.
 */
public final class GitConstants {

    private GitConstants() {
        // Utility class
    }

    // ============================================================
    // Git References
    // ============================================================

    public static final String REFS_HEADS_PREFIX = "refs/heads/";
    public static final String REFS_TAGS_PREFIX = "refs/tags/";
    public static final String REFS_REMOTES_PREFIX = "refs/remotes/";

    public static final String HEAD = "HEAD";
    public static final String MAIN = "main";
    public static final String MASTER = "master";
    public static final String DEVELOP = "develop";
    public static final String ORIGIN = "origin";

    // ============================================================
    // File Extensions - OpenAPI
    // ============================================================

    public static final Set<String> OPENAPI_EXTENSIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("yaml", "yml", "json"))
    );

    public static final Set<String> OPENAPI_FILE_PATTERNS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "openapi.yaml", "openapi.yml", "openapi.json",
            "swagger.yaml", "swagger.yml", "swagger.json",
            "api.yaml", "api.yml", "api.json"
        ))
    );

    // ============================================================
    // File Extensions - GraphQL
    // ============================================================

    public static final Set<String> GRAPHQL_EXTENSIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("graphql", "gql", "graphqls"))
    );

    public static final Set<String> GRAPHQL_FILE_PATTERNS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "schema.graphql", "schema.gql", "schema.graphqls",
            "*.graphql", "*.gql", "*.graphqls"
        ))
    );

    // ============================================================
    // File Extensions - gRPC/Protobuf
    // ============================================================

    public static final Set<String> PROTOBUF_EXTENSIONS = Collections.unmodifiableSet(
        new HashSet<>(Collections.singletonList("proto"))
    );

    // ============================================================
    // File Extensions - AsyncAPI
    // ============================================================

    public static final Set<String> ASYNCAPI_EXTENSIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("yaml", "yml", "json"))
    );

    public static final Set<String> ASYNCAPI_FILE_PATTERNS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "asyncapi.yaml", "asyncapi.yml", "asyncapi.json"
        ))
    );

    // ============================================================
    // File Extensions - Spring Boot
    // ============================================================

    public static final Set<String> SPRING_EXTENSIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("java", "kt", "groovy"))
    );

    // ============================================================
    // All Spec Extensions
    // ============================================================

    public static final Set<String> ALL_SPEC_EXTENSIONS;
    static {
        Set<String> all = new HashSet<>();
        all.addAll(OPENAPI_EXTENSIONS);
        all.addAll(GRAPHQL_EXTENSIONS);
        all.addAll(PROTOBUF_EXTENSIONS);
        all.addAll(ASYNCAPI_EXTENSIONS);
        all.addAll(SPRING_EXTENSIONS);
        ALL_SPEC_EXTENSIONS = Collections.unmodifiableSet(all);
    }

    // ============================================================
    // Patterns
    // ============================================================

    public static final Pattern SEMANTIC_VERSION_PATTERN =
        Pattern.compile("v?(\\d+)\\.(\\d+)\\.(\\d+)(-[a-zA-Z0-9.]+)?(\\+[a-zA-Z0-9.]+)?");

    public static final Pattern COMMIT_SHA_PATTERN =
        Pattern.compile("[0-9a-fA-F]{7,40}");

    public static final Pattern FULL_COMMIT_SHA_PATTERN =
        Pattern.compile("[0-9a-fA-F]{40}");

    // ============================================================
    // Common Directories
    // ============================================================

    public static final Set<String> SPEC_DIRECTORIES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "api", "apis", "spec", "specs", "specification", "specifications",
            "openapi", "swagger", "graphql", "grpc", "proto", "protobuf",
            "asyncapi", "schema", "schemas", "contracts"
        ))
    );

    public static final Set<String> IGNORED_DIRECTORIES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            ".git", ".svn", ".hg", ".bzr",
            "node_modules", "vendor", "target", "build", "dist", "out",
            ".idea", ".vscode", ".settings", ".project",
            "__pycache__", ".pytest_cache", ".tox",
            "coverage", ".nyc_output"
        ))
    );

    // ============================================================
    // Conventional Commit Types
    // ============================================================

    public static final Set<String> CONVENTIONAL_COMMIT_TYPES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "feat", "feature", "fix", "bugfix", "docs", "style",
            "refactor", "perf", "test", "tests", "build", "ci", "chore", "revert"
        ))
    );

    public static final Set<String> BREAKING_CHANGE_KEYWORDS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "BREAKING CHANGE", "BREAKING-CHANGE", "BREAKING CHANGES"
        ))
    );

    // ============================================================
    // Default Values
    // ============================================================

    public static final int DEFAULT_COMMIT_LIMIT = 100;
    public static final int DEFAULT_SHORT_SHA_LENGTH = 7;
    public static final String DEFAULT_ENCODING = "UTF-8";

    // ============================================================
    // Utility Methods
    // ============================================================

    /**
     * Check if a file extension is a known spec extension.
     */
    public static boolean isSpecExtension(String extension) {
        return extension != null && ALL_SPEC_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Check if a directory should be ignored during spec search.
     */
    public static boolean isIgnoredDirectory(String dirName) {
        return dirName != null && IGNORED_DIRECTORIES.contains(dirName);
    }

    /**
     * Check if a directory is likely to contain API specs.
     */
    public static boolean isSpecDirectory(String dirName) {
        return dirName != null && SPEC_DIRECTORIES.contains(dirName.toLowerCase());
    }

    /**
     * Check if a string looks like a semantic version.
     */
    public static boolean isSemanticVersion(String version) {
        return version != null && SEMANTIC_VERSION_PATTERN.matcher(version).matches();
    }

    /**
     * Check if a string is a valid commit SHA.
     */
    public static boolean isCommitSha(String sha) {
        return sha != null && COMMIT_SHA_PATTERN.matcher(sha).matches();
    }

    /**
     * Check if a commit type is a conventional commit type.
     */
    public static boolean isConventionalCommitType(String type) {
        return type != null && CONVENTIONAL_COMMIT_TYPES.contains(type.toLowerCase());
    }
}
