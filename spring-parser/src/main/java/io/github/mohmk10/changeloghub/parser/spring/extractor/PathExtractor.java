package io.github.mohmk10.changeloghub.parser.spring.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathExtractor {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");

    public String combinePaths(String basePath, String methodPath) {
        String normalizedBase = normalizePath(basePath);
        String normalizedMethod = normalizePath(methodPath);

        if (normalizedBase.isEmpty()) {
            return normalizedMethod.isEmpty() ? "/" : normalizedMethod;
        }

        if (normalizedMethod.isEmpty()) {
            return normalizedBase;
        }

        return normalizedBase + normalizedMethod;
    }

    public String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        String normalized = path.trim();

        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    public List<String> extractPathVariables(String path) {
        List<String> variables = new ArrayList<>();

        if (path == null || path.isEmpty()) {
            return variables;
        }

        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(path);
        while (matcher.find()) {
            String variable = matcher.group(1);
            
            int colonIndex = variable.indexOf(':');
            if (colonIndex > 0) {
                variable = variable.substring(0, colonIndex);
            }
            variables.add(variable);
        }

        return variables;
    }

    public boolean hasPathVariables(String path) {
        return path != null && PATH_VARIABLE_PATTERN.matcher(path).find();
    }

    public String toOpenApiPath(String springPath) {
        if (springPath == null) {
            return "/";
        }

        return springPath.replaceAll("\\{([^:}]+):[^}]+}", "{$1}");
    }
}
