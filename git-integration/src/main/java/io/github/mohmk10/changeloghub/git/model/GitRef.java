package io.github.mohmk10.changeloghub.git.model;

import io.github.mohmk10.changeloghub.git.util.GitRefType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a Git reference (branch, tag, or commit).
 */
public class GitRef {

    private String name;
    private GitRefType type;
    private String commitId;
    private LocalDateTime date;

    public GitRef() {
    }

    public GitRef(String name, GitRefType type, String commitId, LocalDateTime date) {
        this.name = name;
        this.type = type;
        this.commitId = commitId;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GitRefType getType() {
        return type;
    }

    public void setType(GitRefType type) {
        this.type = type;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getShortCommitId() {
        return commitId != null && commitId.length() > 7 ? commitId.substring(0, 7) : commitId;
    }

    public boolean isBranch() {
        return type == GitRefType.BRANCH;
    }

    public boolean isTag() {
        return type == GitRefType.TAG;
    }

    public boolean isCommit() {
        return type == GitRefType.COMMIT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitRef gitRef = (GitRef) o;
        return Objects.equals(name, gitRef.name) &&
               type == gitRef.type &&
               Objects.equals(commitId, gitRef.commitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, commitId);
    }

    @Override
    public String toString() {
        return "GitRef{" +
               "name='" + name + '\'' +
               ", type=" + type +
               ", commitId='" + getShortCommitId() + '\'' +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GitRef ref = new GitRef();

        public Builder name(String name) {
            ref.setName(name);
            return this;
        }

        public Builder type(GitRefType type) {
            ref.setType(type);
            return this;
        }

        public Builder commitId(String commitId) {
            ref.setCommitId(commitId);
            return this;
        }

        public Builder date(LocalDateTime date) {
            ref.setDate(date);
            return this;
        }

        public GitRef build() {
            return ref;
        }
    }
}
