package io.github.mohmk10.changeloghub.git.analyzer;

import io.github.mohmk10.changeloghub.git.config.GitConfig;
import io.github.mohmk10.changeloghub.git.model.GitBranch;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes Git branches.
 */
public class BranchAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(BranchAnalyzer.class);

    private final Repository repository;
    private final GitConfig config;

    public BranchAnalyzer(Repository repository) {
        this(repository, new GitConfig());
    }

    public BranchAnalyzer(Repository repository, GitConfig config) {
        this.repository = repository;
        this.config = config;
    }

    /**
     * Get all local branches.
     */
    public List<GitBranch> getLocalBranches() {
        return getBranches(ListBranchCommand.ListMode.ALL, false);
    }

    /**
     * Get all remote branches.
     */
    public List<GitBranch> getRemoteBranches() {
        return getBranches(ListBranchCommand.ListMode.REMOTE, true);
    }

    /**
     * Get all branches (local and remote).
     */
    public List<GitBranch> getAllBranches() {
        List<GitBranch> branches = new ArrayList<>();
        branches.addAll(getLocalBranches());
        if (config.isIncludeRemoteBranches()) {
            branches.addAll(getRemoteBranches());
        }
        return branches;
    }

    /**
     * Get a specific branch.
     */
    public Optional<GitBranch> getBranch(String branchName) {
        try {
            String fullName = branchName.startsWith("refs/")
                ? branchName
                : "refs/heads/" + branchName;

            Ref ref = repository.findRef(fullName);
            if (ref == null) {
                // Try remote
                ref = repository.findRef("refs/remotes/origin/" + branchName);
            }

            if (ref == null) {
                return Optional.empty();
            }

            return Optional.of(parseBranch(ref));

        } catch (Exception e) {
            logger.error("Failed to get branch: {}", branchName, e);
            return Optional.empty();
        }
    }

    /**
     * Get the current branch.
     */
    public Optional<GitBranch> getCurrentBranch() {
        try {
            String fullBranch = repository.getFullBranch();
            if (fullBranch == null) {
                return Optional.empty();
            }

            Ref ref = repository.findRef(fullBranch);
            if (ref == null) {
                return Optional.empty();
            }

            GitBranch branch = parseBranch(ref);
            return Optional.of(branch);

        } catch (Exception e) {
            logger.error("Failed to get current branch", e);
            return Optional.empty();
        }
    }

    /**
     * Get the default branch (main or master).
     */
    public Optional<GitBranch> getDefaultBranch() {
        // Try configured default branch
        Optional<GitBranch> branch = getBranch(config.getDefaultBranch());
        if (branch.isPresent()) {
            return branch;
        }

        // Try common defaults
        branch = getBranch("main");
        if (branch.isPresent()) {
            return branch;
        }

        branch = getBranch("master");
        return branch;
    }

    /**
     * Get feature branches.
     */
    public List<GitBranch> getFeatureBranches() {
        return getAllBranches().stream()
            .filter(GitBranch::isFeatureBranch)
            .collect(Collectors.toList());
    }

    /**
     * Get release branches.
     */
    public List<GitBranch> getReleaseBranches() {
        return getAllBranches().stream()
            .filter(GitBranch::isReleaseBranch)
            .collect(Collectors.toList());
    }

    /**
     * Get branches sorted by last commit date (newest first).
     */
    public List<GitBranch> getBranchesSortedByDate() {
        return getAllBranches().stream()
            .filter(b -> b.getLastCommitDate() != null)
            .sorted(Comparator.comparing(GitBranch::getLastCommitDate).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Get branches that contain a specific commit.
     */
    public List<GitBranch> getBranchesContainingCommit(String commitId) {
        List<GitBranch> result = new ArrayList<>();

        try {
            ObjectId targetId = ObjectId.fromString(commitId);

            for (GitBranch branch : getAllBranches()) {
                if (branchContainsCommit(branch, targetId)) {
                    result.add(branch);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get branches containing commit: {}", commitId, e);
        }

        return result;
    }

    /**
     * Check if a branch exists.
     */
    public boolean branchExists(String branchName) {
        return getBranch(branchName).isPresent();
    }

    /**
     * Get the merge base (common ancestor) of two branches.
     */
    public Optional<String> getMergeBase(String branch1, String branch2) {
        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId id1 = resolveRef(branch1);
            ObjectId id2 = resolveRef(branch2);

            if (id1 == null || id2 == null) {
                return Optional.empty();
            }

            RevCommit commit1 = revWalk.parseCommit(id1);
            RevCommit commit2 = revWalk.parseCommit(id2);

            revWalk.setRevFilter(org.eclipse.jgit.revwalk.filter.RevFilter.MERGE_BASE);
            revWalk.markStart(commit1);
            revWalk.markStart(commit2);

            RevCommit mergeBase = revWalk.next();
            return mergeBase != null ? Optional.of(mergeBase.getName()) : Optional.empty();

        } catch (Exception e) {
            logger.error("Failed to get merge base for {} and {}", branch1, branch2, e);
            return Optional.empty();
        }
    }

    /**
     * Check if branch1 is ahead of branch2.
     */
    public boolean isAhead(String branch1, String branch2) {
        int[] aheadBehind = getAheadBehind(branch1, branch2);
        return aheadBehind[0] > 0;
    }

    /**
     * Check if branch1 is behind branch2.
     */
    public boolean isBehind(String branch1, String branch2) {
        int[] aheadBehind = getAheadBehind(branch1, branch2);
        return aheadBehind[1] > 0;
    }

    /**
     * Get ahead/behind counts for branch1 relative to branch2.
     * Returns [ahead, behind].
     */
    public int[] getAheadBehind(String branch1, String branch2) {
        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId id1 = resolveRef(branch1);
            ObjectId id2 = resolveRef(branch2);

            if (id1 == null || id2 == null) {
                return new int[]{0, 0};
            }

            Optional<String> mergeBase = getMergeBase(branch1, branch2);
            if (mergeBase.isEmpty()) {
                return new int[]{0, 0};
            }

            int ahead = countCommitsBetween(mergeBase.get(), branch1);
            int behind = countCommitsBetween(mergeBase.get(), branch2);

            return new int[]{ahead, behind};

        } catch (Exception e) {
            logger.error("Failed to get ahead/behind for {} and {}", branch1, branch2, e);
            return new int[]{0, 0};
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private List<GitBranch> getBranches(ListBranchCommand.ListMode mode, boolean remote) {
        List<GitBranch> branches = new ArrayList<>();

        try (Git git = new Git(repository)) {
            ListBranchCommand command = git.branchList();
            if (mode != null) {
                command.setListMode(mode);
            }

            List<Ref> refs = command.call();
            for (Ref ref : refs) {
                boolean isRemote = ref.getName().startsWith("refs/remotes/");
                if (remote == isRemote || mode == ListBranchCommand.ListMode.ALL) {
                    GitBranch branch = parseBranch(ref);
                    branches.add(branch);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to get branches", e);
        }

        return branches;
    }

    private GitBranch parseBranch(Ref ref) {
        String name = ref.getName();
        boolean remote = name.startsWith("refs/remotes/");
        String remoteName = null;

        if (remote) {
            String withoutPrefix = name.replace("refs/remotes/", "");
            int slashIndex = withoutPrefix.indexOf('/');
            if (slashIndex > 0) {
                remoteName = withoutPrefix.substring(0, slashIndex);
            }
        }

        ObjectId objectId = ref.getObjectId();
        LocalDateTime lastCommitDate = null;

        if (objectId != null) {
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(objectId);
                lastCommitDate = LocalDateTime.ofInstant(
                    commit.getAuthorIdent().getWhen().toInstant(),
                    ZoneId.systemDefault()
                );
            } catch (Exception e) {
                logger.debug("Could not get commit date for branch: {}", name);
            }
        }

        String shortName = name;
        if (name.startsWith("refs/heads/")) {
            shortName = name.substring("refs/heads/".length());
        } else if (name.startsWith("refs/remotes/")) {
            shortName = name.substring("refs/remotes/".length());
        }

        boolean isDefault = shortName.equals("main") || shortName.equals("master") ||
                           shortName.equals(config.getDefaultBranch());

        return GitBranch.builder()
            .name(name)
            .commitId(objectId != null ? objectId.getName() : null)
            .remote(remote)
            .remoteName(remoteName)
            .isDefault(isDefault)
            .lastCommitDate(lastCommitDate)
            .build();
    }

    private ObjectId resolveRef(String ref) throws IOException {
        ObjectId objectId = repository.resolve(ref);
        if (objectId == null) {
            objectId = repository.resolve("refs/heads/" + ref);
            if (objectId == null) {
                objectId = repository.resolve("refs/remotes/origin/" + ref);
            }
        }
        return objectId;
    }

    private boolean branchContainsCommit(GitBranch branch, ObjectId targetId) {
        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId branchId = ObjectId.fromString(branch.getCommitId());
            revWalk.markStart(revWalk.parseCommit(branchId));

            for (RevCommit commit : revWalk) {
                if (commit.getId().equals(targetId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.debug("Error checking if branch {} contains commit", branch.getName());
        }
        return false;
    }

    private int countCommitsBetween(String fromRef, String toRef) {
        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId fromId = resolveRef(fromRef);
            ObjectId toId = resolveRef(toRef);

            if (fromId == null || toId == null) {
                return 0;
            }

            revWalk.markStart(revWalk.parseCommit(toId));
            revWalk.markUninteresting(revWalk.parseCommit(fromId));

            int count = 0;
            for (RevCommit commit : revWalk) {
                count++;
            }
            return count;

        } catch (Exception e) {
            logger.debug("Error counting commits between {} and {}", fromRef, toRef);
            return 0;
        }
    }
}
