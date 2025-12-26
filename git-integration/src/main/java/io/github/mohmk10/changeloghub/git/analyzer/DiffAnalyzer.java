package io.github.mohmk10.changeloghub.git.analyzer;

import io.github.mohmk10.changeloghub.git.config.GitConfig;
import io.github.mohmk10.changeloghub.git.exception.GitOperationException;
import io.github.mohmk10.changeloghub.git.model.GitDiff;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DiffAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(DiffAnalyzer.class);

    private final Repository repository;
    private final GitConfig config;

    public DiffAnalyzer(Repository repository) {
        this(repository, new GitConfig());
    }

    public DiffAnalyzer(Repository repository, GitConfig config) {
        this.repository = repository;
        this.config = config;
    }

    public GitDiff getDiff(String oldRef, String newRef) {
        try {
            ObjectId oldId = resolveRef(oldRef);
            ObjectId newId = resolveRef(newRef);

            if (newId == null) {
                throw GitOperationException.referenceNotFound(newRef);
            }

            AbstractTreeIterator oldTree;
            String oldCommitId = null;

            if (oldId == null) {
                oldTree = new EmptyTreeIterator();
            } else {
                try (RevWalk revWalk = new RevWalk(repository)) {
                    RevCommit commit = revWalk.parseCommit(oldId);
                    oldTree = prepareTreeParser(commit.getTree());
                    oldCommitId = commit.getName();
                }
            }

            AbstractTreeIterator newTree;
            String newCommitId;

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(newId);
                newTree = prepareTreeParser(commit.getTree());
                newCommitId = commit.getName();
            }

            try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                diffFormatter.setRepository(repository);
                diffFormatter.setDetectRenames(config.isFollowRenames());

                List<DiffEntry> diffs = diffFormatter.scan(oldTree, newTree);

                GitDiff.Builder builder = GitDiff.builder()
                    .oldRef(oldRef)
                    .newRef(newRef)
                    .oldCommitId(oldCommitId)
                    .newCommitId(newCommitId);

                for (DiffEntry diff : diffs) {
                    switch (diff.getChangeType()) {
                        case ADD:
                            builder.addedFiles(Collections.singletonList(diff.getNewPath()));
                            break;
                        case DELETE:
                            builder.deletedFiles(Collections.singletonList(diff.getOldPath()));
                            break;
                        case MODIFY:
                            builder.modifiedFiles(Collections.singletonList(diff.getNewPath()));
                            break;
                        case RENAME:
                            builder.renamedFiles(Collections.singletonList(
                                diff.getOldPath() + " -> " + diff.getNewPath()));
                            break;
                        case COPY:
                            builder.addedFiles(Collections.singletonList(diff.getNewPath()));
                            break;
                    }
                }

                return buildDiffFromEntries(diffs, oldRef, newRef, oldCommitId, newCommitId);
            }

        } catch (Exception e) {
            logger.error("Failed to get diff between {} and {}", oldRef, newRef, e);
            throw GitOperationException.diffError(oldRef, newRef, e);
        }
    }

    public List<String> getChangedFiles(String oldRef, String newRef) {
        GitDiff diff = getDiff(oldRef, newRef);
        return diff.getAllChangedFiles();
    }

    public List<String> getAddedFiles(String oldRef, String newRef) {
        GitDiff diff = getDiff(oldRef, newRef);
        return diff.getAddedFiles();
    }

    public List<String> getModifiedFiles(String oldRef, String newRef) {
        GitDiff diff = getDiff(oldRef, newRef);
        return diff.getModifiedFiles();
    }

    public List<String> getDeletedFiles(String oldRef, String newRef) {
        GitDiff diff = getDiff(oldRef, newRef);
        return diff.getDeletedFiles();
    }

    public List<String> getChangedFilesWithExtension(String oldRef, String newRef, String extension) {
        return getChangedFiles(oldRef, newRef).stream()
            .filter(file -> file.endsWith("." + extension))
            .collect(Collectors.toList());
    }

    public List<String> getChangedFilesInDirectory(String oldRef, String newRef, String directory) {
        String dir = directory.endsWith("/") ? directory : directory + "/";
        return getChangedFiles(oldRef, newRef).stream()
            .filter(file -> file.startsWith(dir))
            .collect(Collectors.toList());
    }

    public boolean hasChanges(String oldRef, String newRef) {
        GitDiff diff = getDiff(oldRef, newRef);
        return diff.hasChanges();
    }

    public boolean fileChanged(String oldRef, String newRef, String filePath) {
        GitDiff diff = getDiff(oldRef, newRef);
        return diff.hasFileChanged(filePath);
    }

    public DiffStats getStats(String oldRef, String newRef) {
        GitDiff diff = getDiff(oldRef, newRef);

        Map<String, Integer> changesByExtension = new HashMap<>();
        Map<String, Integer> changesByDirectory = new HashMap<>();

        for (String file : diff.getAllChangedFiles()) {
            
            int lastDot = file.lastIndexOf('.');
            String ext = lastDot > 0 ? file.substring(lastDot + 1) : "no-ext";
            changesByExtension.merge(ext, 1, Integer::sum);

            int lastSlash = file.lastIndexOf('/');
            String dir = lastSlash > 0 ? file.substring(0, lastSlash) : "/";
            changesByDirectory.merge(dir, 1, Integer::sum);
        }

        return new DiffStats(
            diff.getAddedFiles().size(),
            diff.getModifiedFiles().size(),
            diff.getDeletedFiles().size(),
            diff.getRenamedFiles().size(),
            changesByExtension,
            changesByDirectory
        );
    }

    public List<String> getChangedSpecFiles(String oldRef, String newRef) {
        return getChangedFiles(oldRef, newRef).stream()
            .filter(config::matchesSpecPattern)
            .filter(file -> !config.shouldIgnore(file))
            .collect(Collectors.toList());
    }

    private ObjectId resolveRef(String ref) throws IOException {
        if (ref == null) return null;

        ObjectId objectId = repository.resolve(ref);
        if (objectId == null) {
            objectId = repository.resolve("refs/heads/" + ref);
            if (objectId == null) {
                objectId = repository.resolve("refs/tags/" + ref);
            }
        }
        return objectId;
    }

    private AbstractTreeIterator prepareTreeParser(RevTree tree) throws IOException {
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        try (ObjectReader reader = repository.newObjectReader()) {
            treeParser.reset(reader, tree.getId());
        }
        return treeParser;
    }

    private GitDiff buildDiffFromEntries(List<DiffEntry> entries,
                                         String oldRef, String newRef,
                                         String oldCommitId, String newCommitId) {
        List<String> added = new ArrayList<>();
        List<String> modified = new ArrayList<>();
        List<String> deleted = new ArrayList<>();
        List<String> renamed = new ArrayList<>();

        for (DiffEntry entry : entries) {
            switch (entry.getChangeType()) {
                case ADD:
                    added.add(entry.getNewPath());
                    break;
                case DELETE:
                    deleted.add(entry.getOldPath());
                    break;
                case MODIFY:
                    modified.add(entry.getNewPath());
                    break;
                case RENAME:
                    renamed.add(entry.getOldPath() + " -> " + entry.getNewPath());
                    break;
                case COPY:
                    added.add(entry.getNewPath());
                    break;
            }
        }

        return GitDiff.builder()
            .oldRef(oldRef)
            .newRef(newRef)
            .oldCommitId(oldCommitId)
            .newCommitId(newCommitId)
            .addedFiles(added)
            .modifiedFiles(modified)
            .deletedFiles(deleted)
            .renamedFiles(renamed)
            .build();
    }

    public static class DiffStats {
        private final int added;
        private final int modified;
        private final int deleted;
        private final int renamed;
        private final Map<String, Integer> changesByExtension;
        private final Map<String, Integer> changesByDirectory;

        public DiffStats(int added, int modified, int deleted, int renamed,
                        Map<String, Integer> changesByExtension,
                        Map<String, Integer> changesByDirectory) {
            this.added = added;
            this.modified = modified;
            this.deleted = deleted;
            this.renamed = renamed;
            this.changesByExtension = changesByExtension;
            this.changesByDirectory = changesByDirectory;
        }

        public int getAdded() {
            return added;
        }

        public int getModified() {
            return modified;
        }

        public int getDeleted() {
            return deleted;
        }

        public int getRenamed() {
            return renamed;
        }

        public int getTotal() {
            return added + modified + deleted + renamed;
        }

        public Map<String, Integer> getChangesByExtension() {
            return Collections.unmodifiableMap(changesByExtension);
        }

        public Map<String, Integer> getChangesByDirectory() {
            return Collections.unmodifiableMap(changesByDirectory);
        }

        @Override
        public String toString() {
            return "DiffStats{" +
                   "added=" + added +
                   ", modified=" + modified +
                   ", deleted=" + deleted +
                   ", renamed=" + renamed +
                   ", total=" + getTotal() +
                   '}';
        }
    }
}
