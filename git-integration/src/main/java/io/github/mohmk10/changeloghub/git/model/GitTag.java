package io.github.mohmk10.changeloghub.git.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a Git tag (lightweight or annotated).
 */
public class GitTag {

    private String name;
    private String commitId;
    private String message;
    private LocalDateTime date;
    private String tagger;
    private String taggerEmail;
    private boolean annotated;

    public GitTag() {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getTagger() {
        return tagger;
    }

    public void setTagger(String tagger) {
        this.tagger = tagger;
    }

    public String getTaggerEmail() {
        return taggerEmail;
    }

    public void setTaggerEmail(String taggerEmail) {
        this.taggerEmail = taggerEmail;
    }

    public boolean isAnnotated() {
        return annotated;
    }

    public void setAnnotated(boolean annotated) {
        this.annotated = annotated;
    }

    public boolean isLightweight() {
        return !annotated;
    }

    /**
     * Check if this tag follows semantic versioning pattern.
     */
    public boolean isSemanticVersion() {
        if (name == null) return false;
        String version = name.startsWith("v") ? name.substring(1) : name;
        return version.matches("\\d+\\.\\d+\\.\\d+.*");
    }

    /**
     * Extract version string without 'v' prefix.
     */
    public String getVersion() {
        if (name == null) return null;
        return name.startsWith("v") ? name.substring(1) : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitTag gitTag = (GitTag) o;
        return Objects.equals(name, gitTag.name) &&
               Objects.equals(commitId, gitTag.commitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, commitId);
    }

    @Override
    public String toString() {
        return "GitTag{" +
               "name='" + name + '\'' +
               ", commitId='" + getShortCommitId() + '\'' +
               ", annotated=" + annotated +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GitTag tag = new GitTag();

        public Builder name(String name) {
            tag.setName(name);
            return this;
        }

        public Builder commitId(String commitId) {
            tag.setCommitId(commitId);
            return this;
        }

        public Builder message(String message) {
            tag.setMessage(message);
            return this;
        }

        public Builder date(LocalDateTime date) {
            tag.setDate(date);
            return this;
        }

        public Builder tagger(String tagger) {
            tag.setTagger(tagger);
            return this;
        }

        public Builder taggerEmail(String taggerEmail) {
            tag.setTaggerEmail(taggerEmail);
            return this;
        }

        public Builder annotated(boolean annotated) {
            tag.setAnnotated(annotated);
            return this;
        }

        public GitTag build() {
            return tag;
        }
    }
}
