package io.github.mohmk10.changeloghub.git.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GitConfig {

    private Path repositoryPath;
    private String defaultBranch;
    private int maxCommitDepth;
    private int maxFileSizeBytes;
    private boolean followRenames;
    private boolean includeRemoteBranches;
    private Set<String> specFilePatterns;
    private Set<String> ignoredPaths;
    private List<String> specDirectories;
    private CredentialsConfig credentials;
    private ProxyConfig proxy;

    public GitConfig() {
        this.defaultBranch = "main";
        this.maxCommitDepth = 100;
        this.maxFileSizeBytes = 10 * 1024 * 1024; 
        this.followRenames = true;
        this.includeRemoteBranches = false;
        this.specFilePatterns = new HashSet<>(Arrays.asList(
            "*.yaml", "*.yml", "*.json",
            "*.graphql", "*.gql", "*.graphqls",
            "*.proto"
        ));
        this.ignoredPaths = new HashSet<>(Arrays.asList(
            "node_modules", "vendor", "target", "build", ".git"
        ));
        this.specDirectories = new ArrayList<>(Arrays.asList(
            "api", "specs", "openapi", "graphql", "proto", "asyncapi"
        ));
    }

    public Path getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(Path repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public int getMaxCommitDepth() {
        return maxCommitDepth;
    }

    public void setMaxCommitDepth(int maxCommitDepth) {
        this.maxCommitDepth = maxCommitDepth;
    }

    public int getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(int maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public boolean isFollowRenames() {
        return followRenames;
    }

    public void setFollowRenames(boolean followRenames) {
        this.followRenames = followRenames;
    }

    public boolean isIncludeRemoteBranches() {
        return includeRemoteBranches;
    }

    public void setIncludeRemoteBranches(boolean includeRemoteBranches) {
        this.includeRemoteBranches = includeRemoteBranches;
    }

    public Set<String> getSpecFilePatterns() {
        return Collections.unmodifiableSet(specFilePatterns);
    }

    public void setSpecFilePatterns(Set<String> specFilePatterns) {
        this.specFilePatterns = specFilePatterns != null ? new HashSet<>(specFilePatterns) : new HashSet<>();
    }

    public void addSpecFilePattern(String pattern) {
        this.specFilePatterns.add(pattern);
    }

    public Set<String> getIgnoredPaths() {
        return Collections.unmodifiableSet(ignoredPaths);
    }

    public void setIgnoredPaths(Set<String> ignoredPaths) {
        this.ignoredPaths = ignoredPaths != null ? new HashSet<>(ignoredPaths) : new HashSet<>();
    }

    public void addIgnoredPath(String path) {
        this.ignoredPaths.add(path);
    }

    public List<String> getSpecDirectories() {
        return Collections.unmodifiableList(specDirectories);
    }

    public void setSpecDirectories(List<String> specDirectories) {
        this.specDirectories = specDirectories != null ? new ArrayList<>(specDirectories) : new ArrayList<>();
    }

    public void addSpecDirectory(String directory) {
        this.specDirectories.add(directory);
    }

    public CredentialsConfig getCredentials() {
        return credentials;
    }

    public void setCredentials(CredentialsConfig credentials) {
        this.credentials = credentials;
    }

    public ProxyConfig getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfig proxy) {
        this.proxy = proxy;
    }

    public boolean hasCredentials() {
        return credentials != null && credentials.isValid();
    }

    public boolean hasProxy() {
        return proxy != null && proxy.isValid();
    }

    public boolean shouldIgnore(String path) {
        if (path == null) return false;
        return ignoredPaths.stream().anyMatch(ignored ->
            path.contains("/" + ignored + "/") ||
            path.startsWith(ignored + "/") ||
            path.equals(ignored)
        );
    }

    public boolean matchesSpecPattern(String fileName) {
        if (fileName == null) return false;
        String lowerName = fileName.toLowerCase();
        return specFilePatterns.stream().anyMatch(pattern -> {
            String lowerPattern = pattern.toLowerCase();
            if (lowerPattern.startsWith("*.")) {
                String ext = lowerPattern.substring(2);
                return lowerName.endsWith("." + ext);
            }
            return lowerName.equals(lowerPattern);
        });
    }

    public static class CredentialsConfig {
        private String username;
        private String password;
        private String sshKeyPath;
        private String sshPassphrase;
        private String accessToken;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSshKeyPath() {
            return sshKeyPath;
        }

        public void setSshKeyPath(String sshKeyPath) {
            this.sshKeyPath = sshKeyPath;
        }

        public String getSshPassphrase() {
            return sshPassphrase;
        }

        public void setSshPassphrase(String sshPassphrase) {
            this.sshPassphrase = sshPassphrase;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public boolean isValid() {
            return (username != null && password != null) ||
                   sshKeyPath != null ||
                   accessToken != null;
        }

        public boolean isTokenAuth() {
            return accessToken != null;
        }

        public boolean isSshAuth() {
            return sshKeyPath != null;
        }

        public boolean isBasicAuth() {
            return username != null && password != null;
        }
    }

    public static class ProxyConfig {
        private String host;
        private int port;
        private String username;
        private String password;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isValid() {
            return host != null && !host.isEmpty() && port > 0;
        }

        public boolean hasAuthentication() {
            return username != null && password != null;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GitConfig config = new GitConfig();

        public Builder repositoryPath(Path path) {
            config.setRepositoryPath(path);
            return this;
        }

        public Builder repositoryPath(String path) {
            config.setRepositoryPath(Path.of(path));
            return this;
        }

        public Builder defaultBranch(String branch) {
            config.setDefaultBranch(branch);
            return this;
        }

        public Builder maxCommitDepth(int depth) {
            config.setMaxCommitDepth(depth);
            return this;
        }

        public Builder maxFileSizeBytes(int size) {
            config.setMaxFileSizeBytes(size);
            return this;
        }

        public Builder followRenames(boolean follow) {
            config.setFollowRenames(follow);
            return this;
        }

        public Builder includeRemoteBranches(boolean include) {
            config.setIncludeRemoteBranches(include);
            return this;
        }

        public Builder specFilePatterns(Set<String> patterns) {
            config.setSpecFilePatterns(patterns);
            return this;
        }

        public Builder ignoredPaths(Set<String> paths) {
            config.setIgnoredPaths(paths);
            return this;
        }

        public Builder specDirectories(List<String> directories) {
            config.setSpecDirectories(directories);
            return this;
        }

        public Builder credentials(CredentialsConfig credentials) {
            config.setCredentials(credentials);
            return this;
        }

        public Builder proxy(ProxyConfig proxy) {
            config.setProxy(proxy);
            return this;
        }

        public GitConfig build() {
            return config;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitConfig gitConfig = (GitConfig) o;
        return Objects.equals(repositoryPath, gitConfig.repositoryPath) &&
               Objects.equals(defaultBranch, gitConfig.defaultBranch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryPath, defaultBranch);
    }

    @Override
    public String toString() {
        return "GitConfig{" +
               "repositoryPath=" + repositoryPath +
               ", defaultBranch='" + defaultBranch + '\'' +
               ", maxCommitDepth=" + maxCommitDepth +
               ", followRenames=" + followRenames +
               '}';
    }
}
