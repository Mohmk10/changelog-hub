package io.github.mohmk10.changeloghub.git.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the diff between two Git references.
 */
public class GitDiff {

    private String oldRef;
    private String newRef;
    private String oldCommitId;
    private String newCommitId;
    private List<String> addedFiles;
    private List<String> modifiedFiles;
    private List<String> deletedFiles;
    private List<String> renamedFiles;

    public GitDiff() {
        this.addedFiles = new ArrayList<>();
        this.modifiedFiles = new ArrayList<>();
        this.deletedFiles = new ArrayList<>();
        this.renamedFiles = new ArrayList<>();
    }

    public String getOldRef() {
        return oldRef;
    }

    public void setOldRef(String oldRef) {
        this.oldRef = oldRef;
    }

    public String getNewRef() {
        return newRef;
    }

    public void setNewRef(String newRef) {
        this.newRef = newRef;
    }

    public String getOldCommitId() {
        return oldCommitId;
    }

    public void setOldCommitId(String oldCommitId) {
        this.oldCommitId = oldCommitId;
    }

    public String getNewCommitId() {
        return newCommitId;
    }

    public void setNewCommitId(String newCommitId) {
        this.newCommitId = newCommitId;
    }

    public List<String> getAddedFiles() {
        return addedFiles;
    }

    public void setAddedFiles(List<String> addedFiles) {
        this.addedFiles = addedFiles != null ? new ArrayList<>(addedFiles) : new ArrayList<>();
    }

    public void addAddedFile(String file) {
        this.addedFiles.add(file);
    }

    public List<String> getModifiedFiles() {
        return modifiedFiles;
    }

    public void setModifiedFiles(List<String> modifiedFiles) {
        this.modifiedFiles = modifiedFiles != null ? new ArrayList<>(modifiedFiles) : new ArrayList<>();
    }

    public void addModifiedFile(String file) {
        this.modifiedFiles.add(file);
    }

    public List<String> getDeletedFiles() {
        return deletedFiles;
    }

    public void setDeletedFiles(List<String> deletedFiles) {
        this.deletedFiles = deletedFiles != null ? new ArrayList<>(deletedFiles) : new ArrayList<>();
    }

    public void addDeletedFile(String file) {
        this.deletedFiles.add(file);
    }

    public List<String> getRenamedFiles() {
        return renamedFiles;
    }

    public void setRenamedFiles(List<String> renamedFiles) {
        this.renamedFiles = renamedFiles != null ? new ArrayList<>(renamedFiles) : new ArrayList<>();
    }

    public void addRenamedFile(String file) {
        this.renamedFiles.add(file);
    }

    /**
     * Get total number of changes.
     */
    public int getTotalChanges() {
        return addedFiles.size() + modifiedFiles.size() + deletedFiles.size() + renamedFiles.size();
    }

    /**
     * Get all changed files (added, modified, deleted, renamed).
     */
    public List<String> getAllChangedFiles() {
        List<String> all = new ArrayList<>();
        all.addAll(addedFiles);
        all.addAll(modifiedFiles);
        all.addAll(deletedFiles);
        all.addAll(renamedFiles);
        return all;
    }

    /**
     * Check if there are any changes.
     */
    public boolean hasChanges() {
        return !addedFiles.isEmpty() || !modifiedFiles.isEmpty() ||
               !deletedFiles.isEmpty() || !renamedFiles.isEmpty();
    }

    /**
     * Check if a specific file was changed.
     */
    public boolean hasFileChanged(String filePath) {
        return addedFiles.contains(filePath) ||
               modifiedFiles.contains(filePath) ||
               deletedFiles.contains(filePath) ||
               renamedFiles.contains(filePath);
    }

    @Override
    public String toString() {
        return "GitDiff{" +
               "oldRef='" + oldRef + '\'' +
               ", newRef='" + newRef + '\'' +
               ", added=" + addedFiles.size() +
               ", modified=" + modifiedFiles.size() +
               ", deleted=" + deletedFiles.size() +
               ", renamed=" + renamedFiles.size() +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GitDiff diff = new GitDiff();

        public Builder oldRef(String oldRef) {
            diff.setOldRef(oldRef);
            return this;
        }

        public Builder newRef(String newRef) {
            diff.setNewRef(newRef);
            return this;
        }

        public Builder oldCommitId(String oldCommitId) {
            diff.setOldCommitId(oldCommitId);
            return this;
        }

        public Builder newCommitId(String newCommitId) {
            diff.setNewCommitId(newCommitId);
            return this;
        }

        public Builder addedFiles(List<String> files) {
            diff.setAddedFiles(files);
            return this;
        }

        public Builder modifiedFiles(List<String> files) {
            diff.setModifiedFiles(files);
            return this;
        }

        public Builder deletedFiles(List<String> files) {
            diff.setDeletedFiles(files);
            return this;
        }

        public Builder renamedFiles(List<String> files) {
            diff.setRenamedFiles(files);
            return this;
        }

        public GitDiff build() {
            return diff;
        }
    }
}
