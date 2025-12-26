package io.github.mohmk10.changeloghub.git.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GitCommit {

    private String id;
    private String shortId;
    private String message;
    private String author;
    private String email;
    private LocalDateTime date;
    private List<String> changedFiles;
    private List<String> parentIds;

    public GitCommit() {
        this.changedFiles = new ArrayList<>();
        this.parentIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (id != null && id.length() > 7) {
            this.shortId = id.substring(0, 7);
        } else {
            this.shortId = id;
        }
    }

    public String getShortId() {
        return shortId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(List<String> changedFiles) {
        this.changedFiles = changedFiles != null ? new ArrayList<>(changedFiles) : new ArrayList<>();
    }

    public void addChangedFile(String file) {
        this.changedFiles.add(file);
    }

    public List<String> getParentIds() {
        return parentIds;
    }

    public void setParentIds(List<String> parentIds) {
        this.parentIds = parentIds != null ? new ArrayList<>(parentIds) : new ArrayList<>();
    }

    public void addParentId(String parentId) {
        this.parentIds.add(parentId);
    }

    public boolean isMergeCommit() {
        return parentIds.size() > 1;
    }

    public String getMessageFirstLine() {
        if (message == null) {
            return null;
        }
        int newlineIndex = message.indexOf('\n');
        return newlineIndex > 0 ? message.substring(0, newlineIndex) : message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitCommit gitCommit = (GitCommit) o;
        return Objects.equals(id, gitCommit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GitCommit{" +
               "id='" + shortId + '\'' +
               ", author='" + author + '\'' +
               ", message='" + getMessageFirstLine() + '\'' +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GitCommit commit = new GitCommit();

        public Builder id(String id) {
            commit.setId(id);
            return this;
        }

        public Builder message(String message) {
            commit.setMessage(message);
            return this;
        }

        public Builder author(String author) {
            commit.setAuthor(author);
            return this;
        }

        public Builder email(String email) {
            commit.setEmail(email);
            return this;
        }

        public Builder date(LocalDateTime date) {
            commit.setDate(date);
            return this;
        }

        public Builder changedFiles(List<String> files) {
            commit.setChangedFiles(files);
            return this;
        }

        public Builder parentIds(List<String> parentIds) {
            commit.setParentIds(parentIds);
            return this;
        }

        public GitCommit build() {
            return commit;
        }
    }
}
