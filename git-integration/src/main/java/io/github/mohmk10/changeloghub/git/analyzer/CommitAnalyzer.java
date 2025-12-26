package io.github.mohmk10.changeloghub.git.analyzer;

import io.github.mohmk10.changeloghub.git.config.GitConfig;
import io.github.mohmk10.changeloghub.git.exception.GitOperationException;
import io.github.mohmk10.changeloghub.git.model.GitCommit;
import io.github.mohmk10.changeloghub.git.util.ConventionalCommitParser;
import io.github.mohmk10.changeloghub.git.util.ConventionalCommitParser.ConventionalCommit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes Git commits.
 */
public class CommitAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(CommitAnalyzer.class);

    private final Repository repository;
    private final GitConfig config;

    public CommitAnalyzer(Repository repository) {
        this(repository, new GitConfig());
    }

    public CommitAnalyzer(Repository repository, GitConfig config) {
        this.repository = repository;
        this.config = config;
    }

    /**
     * Get commits between two references.
     *
     * @param fromRef the starting reference (exclusive)
     * @param toRef the ending reference (inclusive)
     * @return list of commits
     */
    public List<GitCommit> getCommitsBetween(String fromRef, String toRef) {
        List<GitCommit> commits = new ArrayList<>();

        try (Git git = new Git(repository)) {
            ObjectId fromId = resolveRef(fromRef);
            ObjectId toId = resolveRef(toRef);

            if (toId == null) {
                throw GitOperationException.referenceNotFound(toRef);
            }

            LogCommand logCommand = git.log();
            logCommand.add(toId);

            if (fromId != null) {
                logCommand.not(fromId);
            }

            logCommand.setMaxCount(config.getMaxCommitDepth());

            for (RevCommit revCommit : logCommand.call()) {
                commits.add(convertToGitCommit(revCommit));
            }

        } catch (Exception e) {
            logger.error("Failed to get commits between {} and {}", fromRef, toRef, e);
            throw new GitOperationException("Failed to get commits", e);
        }

        return commits;
    }

    /**
     * Get a single commit by reference.
     */
    public Optional<GitCommit> getCommit(String ref) {
        try {
            ObjectId objectId = resolveRef(ref);
            if (objectId == null) {
                return Optional.empty();
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit revCommit = revWalk.parseCommit(objectId);
                return Optional.of(convertToGitCommit(revCommit));
            }
        } catch (Exception e) {
            logger.error("Failed to get commit: {}", ref, e);
            return Optional.empty();
        }
    }

    /**
     * Get recent commits.
     */
    public List<GitCommit> getRecentCommits(int count) {
        return getRecentCommits("HEAD", count);
    }

    /**
     * Get recent commits from a specific reference.
     */
    public List<GitCommit> getRecentCommits(String ref, int count) {
        List<GitCommit> commits = new ArrayList<>();

        try (Git git = new Git(repository)) {
            ObjectId objectId = resolveRef(ref);
            if (objectId == null) {
                return commits;
            }

            LogCommand logCommand = git.log()
                .add(objectId)
                .setMaxCount(count);

            for (RevCommit revCommit : logCommand.call()) {
                commits.add(convertToGitCommit(revCommit));
            }

        } catch (Exception e) {
            logger.error("Failed to get recent commits from: {}", ref, e);
        }

        return commits;
    }

    /**
     * Get files changed in a commit.
     */
    public List<String> getChangedFiles(String ref) {
        List<String> files = new ArrayList<>();

        try {
            ObjectId objectId = resolveRef(ref);
            if (objectId == null) {
                return files;
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(objectId);

                try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                    diffFormatter.setRepository(repository);
                    diffFormatter.setDetectRenames(config.isFollowRenames());

                    AbstractTreeIterator parentTree = getParentTree(commit);
                    AbstractTreeIterator commitTree = prepareTreeParser(commit.getTree().getId());

                    List<DiffEntry> diffs = diffFormatter.scan(parentTree, commitTree);
                    for (DiffEntry diff : diffs) {
                        String path = diff.getNewPath().equals("/dev/null")
                            ? diff.getOldPath()
                            : diff.getNewPath();
                        files.add(path);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get changed files for: {}", ref, e);
        }

        return files;
    }

    /**
     * Filter commits by conventional commit type.
     */
    public List<GitCommit> filterByType(List<GitCommit> commits, String type) {
        return commits.stream()
            .filter(c -> {
                ConventionalCommit cc = ConventionalCommitParser.parse(c.getMessage());
                return cc != null && type.equalsIgnoreCase(cc.getType());
            })
            .collect(Collectors.toList());
    }

    /**
     * Get all feature commits.
     */
    public List<GitCommit> getFeatureCommits(List<GitCommit> commits) {
        return commits.stream()
            .filter(c -> ConventionalCommitParser.isFeature(c.getMessage()))
            .collect(Collectors.toList());
    }

    /**
     * Get all fix commits.
     */
    public List<GitCommit> getFixCommits(List<GitCommit> commits) {
        return commits.stream()
            .filter(c -> ConventionalCommitParser.isFix(c.getMessage()))
            .collect(Collectors.toList());
    }

    /**
     * Get all breaking change commits.
     */
    public List<GitCommit> getBreakingChangeCommits(List<GitCommit> commits) {
        return commits.stream()
            .filter(c -> ConventionalCommitParser.isBreakingChange(c.getMessage()))
            .collect(Collectors.toList());
    }

    /**
     * Group commits by type.
     */
    public Map<String, List<GitCommit>> groupByType(List<GitCommit> commits) {
        Map<String, List<GitCommit>> grouped = new LinkedHashMap<>();

        for (GitCommit commit : commits) {
            ConventionalCommit cc = ConventionalCommitParser.parse(commit.getMessage());
            String type = cc != null ? cc.getType() : "other";
            grouped.computeIfAbsent(type, k -> new ArrayList<>()).add(commit);
        }

        return grouped;
    }

    /**
     * Group commits by author.
     */
    public Map<String, List<GitCommit>> groupByAuthor(List<GitCommit> commits) {
        return commits.stream()
            .collect(Collectors.groupingBy(
                GitCommit::getAuthor,
                LinkedHashMap::new,
                Collectors.toList()
            ));
    }

    /**
     * Get commit statistics.
     */
    public CommitStats getStats(List<GitCommit> commits) {
        Map<String, Integer> typeCount = new LinkedHashMap<>();
        Map<String, Integer> authorCount = new LinkedHashMap<>();
        int breakingCount = 0;
        int mergeCount = 0;

        for (GitCommit commit : commits) {
            // Count by type
            ConventionalCommit cc = ConventionalCommitParser.parse(commit.getMessage());
            String type = cc != null ? cc.getType() : "other";
            typeCount.merge(type, 1, Integer::sum);

            // Count by author
            authorCount.merge(commit.getAuthor(), 1, Integer::sum);

            // Count breaking changes
            if (cc != null && cc.isBreaking()) {
                breakingCount++;
            }

            // Count merge commits
            if (commit.isMergeCommit()) {
                mergeCount++;
            }
        }

        return new CommitStats(commits.size(), typeCount, authorCount, breakingCount, mergeCount);
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private ObjectId resolveRef(String ref) throws IOException {
        ObjectId objectId = repository.resolve(ref);
        if (objectId == null) {
            objectId = repository.resolve("refs/heads/" + ref);
            if (objectId == null) {
                objectId = repository.resolve("refs/tags/" + ref);
            }
        }
        return objectId;
    }

    private GitCommit convertToGitCommit(RevCommit revCommit) {
        PersonIdent author = revCommit.getAuthorIdent();
        LocalDateTime date = LocalDateTime.ofInstant(
            author.getWhen().toInstant(),
            ZoneId.systemDefault()
        );

        List<String> parentIds = new ArrayList<>();
        for (RevCommit parent : revCommit.getParents()) {
            parentIds.add(parent.getName());
        }

        return GitCommit.builder()
            .id(revCommit.getName())
            .message(revCommit.getFullMessage())
            .author(author.getName())
            .email(author.getEmailAddress())
            .date(date)
            .parentIds(parentIds)
            .build();
    }

    private AbstractTreeIterator getParentTree(RevCommit commit) throws IOException {
        if (commit.getParentCount() == 0) {
            return new EmptyTreeIterator();
        }

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit parent = revWalk.parseCommit(commit.getParent(0).getId());
            return prepareTreeParser(parent.getTree().getId());
        }
    }

    private AbstractTreeIterator prepareTreeParser(ObjectId treeId) throws IOException {
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        try (var reader = repository.newObjectReader()) {
            treeParser.reset(reader, treeId);
        }
        return treeParser;
    }

    // ============================================================
    // Nested Types
    // ============================================================

    /**
     * Commit statistics.
     */
    public static class CommitStats {
        private final int totalCommits;
        private final Map<String, Integer> commitsByType;
        private final Map<String, Integer> commitsByAuthor;
        private final int breakingChanges;
        private final int mergeCommits;

        public CommitStats(int totalCommits, Map<String, Integer> commitsByType,
                          Map<String, Integer> commitsByAuthor,
                          int breakingChanges, int mergeCommits) {
            this.totalCommits = totalCommits;
            this.commitsByType = commitsByType;
            this.commitsByAuthor = commitsByAuthor;
            this.breakingChanges = breakingChanges;
            this.mergeCommits = mergeCommits;
        }

        public int getTotalCommits() {
            return totalCommits;
        }

        public Map<String, Integer> getCommitsByType() {
            return Collections.unmodifiableMap(commitsByType);
        }

        public Map<String, Integer> getCommitsByAuthor() {
            return Collections.unmodifiableMap(commitsByAuthor);
        }

        public int getBreakingChanges() {
            return breakingChanges;
        }

        public int getMergeCommits() {
            return mergeCommits;
        }

        @Override
        public String toString() {
            return "CommitStats{" +
                   "totalCommits=" + totalCommits +
                   ", breakingChanges=" + breakingChanges +
                   ", mergeCommits=" + mergeCommits +
                   ", types=" + commitsByType.size() +
                   ", authors=" + commitsByAuthor.size() +
                   '}';
        }
    }
}
