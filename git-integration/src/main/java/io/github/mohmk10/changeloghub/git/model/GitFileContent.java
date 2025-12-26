package io.github.mohmk10.changeloghub.git.model;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class GitFileContent {

    private String path;
    private String ref;
    private String content;
    private byte[] bytes;
    private boolean exists;
    private long size;
    private String commitId;

    public GitFileContent() {
        this.exists = false;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getContent() {
        if (content == null && bytes != null) {
            content = new String(bytes, StandardCharsets.UTF_8);
        }
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        if (content != null) {
            this.bytes = content.getBytes(StandardCharsets.UTF_8);
            this.size = bytes.length;
        }
    }

    public byte[] getBytes() {
        if (bytes == null && content != null) {
            bytes = content.getBytes(StandardCharsets.UTF_8);
        }
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        if (bytes != null) {
            this.size = bytes.length;
        }
    }

    public boolean exists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getFileName() {
        if (path == null) return null;
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    public String getExtension() {
        String fileName = getFileName();
        if (fileName == null) return null;
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : null;
    }

    public boolean isEmpty() {
        return !exists || size == 0 ||
               (content == null && bytes == null) ||
               (content != null && content.isEmpty());
    }

    public boolean isTextFile() {
        String ext = getExtension();
        if (ext == null) return true;
        return ext.matches("(?i)yaml|yml|json|xml|txt|md|graphql|proto|java|kt|scala|groovy|properties");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitFileContent that = (GitFileContent) o;
        return Objects.equals(path, that.path) &&
               Objects.equals(ref, that.ref) &&
               Objects.equals(commitId, that.commitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, ref, commitId);
    }

    @Override
    public String toString() {
        return "GitFileContent{" +
               "path='" + path + '\'' +
               ", ref='" + ref + '\'' +
               ", exists=" + exists +
               ", size=" + size +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GitFileContent file = new GitFileContent();

        public Builder path(String path) {
            file.setPath(path);
            return this;
        }

        public Builder ref(String ref) {
            file.setRef(ref);
            return this;
        }

        public Builder content(String content) {
            file.setContent(content);
            file.setExists(content != null);
            return this;
        }

        public Builder bytes(byte[] bytes) {
            file.setBytes(bytes);
            file.setExists(bytes != null);
            return this;
        }

        public Builder exists(boolean exists) {
            file.setExists(exists);
            return this;
        }

        public Builder commitId(String commitId) {
            file.setCommitId(commitId);
            return this;
        }

        public GitFileContent build() {
            return file;
        }
    }
}
