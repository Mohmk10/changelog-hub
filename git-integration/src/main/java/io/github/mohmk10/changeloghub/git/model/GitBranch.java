package io.github.mohmk10.changeloghub.git.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class GitBranch {

    private String name;
    private String commitId;
    private boolean remote;
    private boolean isDefault;
    private String remoteName;
    private LocalDateTime lastCommitDate;

    public GitBranch() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getShortCommitId() {
        return commitId != null && commitId.length() > 7 ? commitId.substring(0, 7) : commitId;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public boolean isLocal() {
        return !remote;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    public LocalDateTime getLastCommitDate() {
        return lastCommitDate;
    }

    public void setLastCommitDate(LocalDateTime lastCommitDate) {
        this.lastCommitDate = lastCommitDate;
    }

    public String getShortName() {
        if (name == null) return null;
        if (name.startsWith("refs/heads/")) {
            return name.substring("refs/heads/".length());
        }
        if (name.startsWith("refs/remotes/")) {
            return name.substring("refs/remotes/".length());
        }
        return name;
    }

    public boolean isMainBranch() {
        String shortName = getShortName();
        return "main".equals(shortName) || "master".equals(shortName);
    }

    public boolean isFeatureBranch() {
        String shortName = getShortName();
        return shortName != null &&
               (shortName.startsWith("feature/") || shortName.startsWith("feat/"));
    }

    public boolean isReleaseBranch() {
        String shortName = getShortName();
        return shortName != null && shortName.startsWith("release/");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitBranch gitBranch = (GitBranch) o;
        return Objects.equals(name, gitBranch.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "GitBranch{" +
               "name='" + getShortName() + '\'' +
               ", remote=" + remote +
               ", default=" + isDefault +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GitBranch branch = new GitBranch();

        public Builder name(String name) {
            branch.setName(name);
            return this;
        }

        public Builder commitId(String commitId) {
            branch.setCommitId(commitId);
            return this;
        }

        public Builder remote(boolean remote) {
            branch.setRemote(remote);
            return this;
        }

        public Builder isDefault(boolean isDefault) {
            branch.setDefault(isDefault);
            return this;
        }

        public Builder remoteName(String remoteName) {
            branch.setRemoteName(remoteName);
            return this;
        }

        public Builder lastCommitDate(LocalDateTime lastCommitDate) {
            branch.setLastCommitDate(lastCommitDate);
            return this;
        }

        public GitBranch build() {
            return branch;
        }
    }
}
