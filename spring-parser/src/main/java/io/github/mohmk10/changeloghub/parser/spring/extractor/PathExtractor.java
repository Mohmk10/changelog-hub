package io.github.mohmk10.changeloghub.parser.spring.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to extract and manipulate API paths.
 */
public class PathExtractor {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");

    /**
     * Combine base path and method path.
     * Ensures proper handling of slashes.
     */
    public String combinePaths(String basePath, String methodPath) {
        String normalizedBase = normalizePath(basePath);
        String normalizedMethod = normalizePath(methodPath);

        if (normalizedBase.isEmpty()) {
            return normalizedMethod.isEmpty() ? "/" : normalizedMethod;
        }

        if (normalizedMethod.isEmpty()) {
            return normalizedBase;
        }

        // Remove trailing slash from base and leading slash from method already handled by normalize
        return normalizedBase + normalizedMethod;
    }

    /**
     * Normalize a path.
     * - Ensures path starts with /
     * - Removes trailing slash (unless root)
     * - Handles empty and null paths
     */
    public String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        String normalized = path.trim();

        // Ensure starts with /
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        // Remove trailing slash (unless root path)
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    /**
     * Extract path variables from a path template.
     * Example: /users/{id}/posts/{postId} -> ["id", "postId"]
     */
    public List<String> extractPathVariables(String path) {
        List<String> variables = new ArrayList<>();

        if (path == null || path.isEmpty()) {
            return variables;
        }

        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(path);
        while (matcher.find()) {
            String variable = matcher.group(1);
            // Handle regex patterns in path variables like {id:\\d+}
            int colonIndex = variable.indexOf(':');
            if (colonIndex > 0) {
                variable = variable.substring(0, colonIndex);
            }
            variables.add(variable);
        }

        return variables;
    }

    /**
     * Check if a path contains path variables.
     */
    public boolean hasPathVariables(String path) {
        return path != null && PATH_VARIABLE_PATTERN.matcher(path).find();
    }

    /**
     * Convert Spring path format to OpenAPI format.
     * Spring: /users/{id:\\d+}
     * OpenAPI: /users/{id}
     */
    public String toOpenApiPath(String springPath) {
        if (springPath == null) {
            return "/";
        }

        // Remove regex patterns from path variables
        return springPath.replaceAll("\\{([^:}]+):[^}]+}", "{$1}");
    }
}
