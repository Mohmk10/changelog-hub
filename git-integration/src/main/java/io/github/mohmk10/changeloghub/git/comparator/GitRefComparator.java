package io.github.mohmk10.changeloghub.git.comparator;

import io.github.mohmk10.changeloghub.git.analyzer.BranchAnalyzer;
import io.github.mohmk10.changeloghub.git.analyzer.CommitAnalyzer;
import io.github.mohmk10.changeloghub.git.analyzer.DiffAnalyzer;
import io.github.mohmk10.changeloghub.git.analyzer.TagAnalyzer;
import io.github.mohmk10.changeloghub.git.config.GitConfig;
import io.github.mohmk10.changeloghub.git.model.GitCommit;
import io.github.mohmk10.changeloghub.git.model.GitDiff;
import io.github.mohmk10.changeloghub.git.model.GitRef;
import io.github.mohmk10.changeloghub.git.model.GitTag;
import io.github.mohmk10.changeloghub.git.util.ConventionalCommitParser;
import io.github.mohmk10.changeloghub.git.util.GitRefType;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class GitRefComparator {

    private static final Logger logger = LoggerFactory.getLogger(GitRefComparator.class);

    private final Repository repository;
    private final GitConfig config;
    private final CommitAnalyzer commitAnalyzer;
    private final TagAnalyzer tagAnalyzer;
    private final BranchAnalyzer branchAnalyzer;
    private final DiffAnalyzer diffAnalyzer;

    public GitRefComparator(Repository repository) {
        this(repository, new GitConfig());
    }

    public GitRefComparator(Repository repository, GitConfig config) {
        this.repository = repository;
        this.config = config;
        this.commitAnalyzer = new CommitAnalyzer(repository, config);
        this.tagAnalyzer = new TagAnalyzer(repository, config);
        this.branchAnalyzer = new BranchAnalyzer(repository, config);
        this.diffAnalyzer = new DiffAnalyzer(repository, config);
    }

    public RefComparison compare(String oldRef, String newRef) {
        logger.info("Comparing {} to {}", oldRef, newRef);

        List<GitCommit> commits = commitAnalyzer.getCommitsBetween(oldRef, newRef);

        GitDiff diff = diffAnalyzer.getDiff(oldRef, newRef);

        CommitAnalyzer.CommitStats commitStats = commitAnalyzer.getStats(commits);

        DiffAnalyzer.DiffStats diffStats = diffAnalyzer.getStats(oldRef, newRef);

        List<GitCommit> breakingCommits = commitAnalyzer.getBreakingChangeCommits(commits);

        GitRefType oldType = GitRefType.fromRef(oldRef);
        GitRefType newType = GitRefType.fromRef(newRef);

        return new RefComparison(
            oldRef,
            newRef,
            oldType,
            newType,
            commits,
            diff,
            commitStats,
            diffStats,
            breakingCommits
        );
    }

    public RefComparison compareTags(String oldTag, String newTag) {
        String oldRef = oldTag.startsWith("refs/tags/") ? oldTag : "refs/tags/" + oldTag;
        String newRef = newTag.startsWith("refs/tags/") ? newTag : "refs/tags/" + newTag;
        return compare(oldRef, newRef);
    }

    public RefComparison compareBranches(String oldBranch, String newBranch) {
        String oldRef = oldBranch.startsWith("refs/") ? oldBranch : "refs/heads/" + oldBranch;
        String newRef = newBranch.startsWith("refs/") ? newBranch : "refs/heads/" + newBranch;
        return compare(oldRef, newRef);
    }

    public RefComparison compareAgainstDefault(String branch) {
        return branchAnalyzer.getDefaultBranch()
            .map(defaultBranch -> compare(defaultBranch.getName(), branch))
            .orElse(null);
    }

    public List<RefComparison> compareConsecutiveTags() {
        List<GitTag> tags = tagAnalyzer.getTagsSortedByVersion();
        List<RefComparison> comparisons = new ArrayList<>();

        for (int i = 0; i < tags.size() - 1; i++) {
            RefComparison comparison = compareTags(
                tags.get(i + 1).getName(),  
                tags.get(i).getName()       
            );
            comparisons.add(comparison);
        }

        return comparisons;
    }

    public ReleaseNotes generateReleaseNotes(String oldRef, String newRef) {
        RefComparison comparison = compare(oldRef, newRef);
        return generateReleaseNotes(comparison);
    }

    public ReleaseNotes generateReleaseNotes(RefComparison comparison) {
        List<GitCommit> commits = comparison.getCommits();

        Map<String, List<GitCommit>> grouped = commitAnalyzer.groupByType(commits);

        List<String> features = grouped.getOrDefault("feat", Collections.emptyList())
            .stream()
            .map(this::formatCommitForReleaseNotes)
            .collect(Collectors.toList());

        List<String> fixes = grouped.getOrDefault("fix", Collections.emptyList())
            .stream()
            .map(this::formatCommitForReleaseNotes)
            .collect(Collectors.toList());

        List<String> breakingChanges = comparison.getBreakingCommits()
            .stream()
            .map(this::formatCommitForReleaseNotes)
            .collect(Collectors.toList());

        List<String> other = new ArrayList<>();
        for (Map.Entry<String, List<GitCommit>> entry : grouped.entrySet()) {
            if (!"feat".equals(entry.getKey()) && !"fix".equals(entry.getKey())) {
                for (GitCommit commit : entry.getValue()) {
                    if (!comparison.getBreakingCommits().contains(commit)) {
                        other.add(formatCommitForReleaseNotes(commit));
                    }
                }
            }
        }

        return new ReleaseNotes(
            comparison.getOldRef(),
            comparison.getNewRef(),
            features,
            fixes,
            breakingChanges,
            other,
            comparison.getCommitStats().getTotalCommits(),
            comparison.getDiffStats().getTotal()
        );
    }

    private String formatCommitForReleaseNotes(GitCommit commit) {
        ConventionalCommitParser.ConventionalCommit cc =
            ConventionalCommitParser.parse(commit.getMessage());

        if (cc != null) {
            String scope = cc.hasScope() ? "**" + cc.getScope() + "**: " : "";
            return scope + cc.getDescription() + " (" + commit.getShortId() + ")";
        }

        return commit.getMessageFirstLine() + " (" + commit.getShortId() + ")";
    }

    public static class RefComparison {
        private final String oldRef;
        private final String newRef;
        private final GitRefType oldType;
        private final GitRefType newType;
        private final List<GitCommit> commits;
        private final GitDiff diff;
        private final CommitAnalyzer.CommitStats commitStats;
        private final DiffAnalyzer.DiffStats diffStats;
        private final List<GitCommit> breakingCommits;

        public RefComparison(String oldRef, String newRef,
                            GitRefType oldType, GitRefType newType,
                            List<GitCommit> commits, GitDiff diff,
                            CommitAnalyzer.CommitStats commitStats,
                            DiffAnalyzer.DiffStats diffStats,
                            List<GitCommit> breakingCommits) {
            this.oldRef = oldRef;
            this.newRef = newRef;
            this.oldType = oldType;
            this.newType = newType;
            this.commits = commits;
            this.diff = diff;
            this.commitStats = commitStats;
            this.diffStats = diffStats;
            this.breakingCommits = breakingCommits;
        }

        public String getOldRef() {
            return oldRef;
        }

        public String getNewRef() {
            return newRef;
        }

        public GitRefType getOldType() {
            return oldType;
        }

        public GitRefType getNewType() {
            return newType;
        }

        public List<GitCommit> getCommits() {
            return Collections.unmodifiableList(commits);
        }

        public GitDiff getDiff() {
            return diff;
        }

        public CommitAnalyzer.CommitStats getCommitStats() {
            return commitStats;
        }

        public DiffAnalyzer.DiffStats getDiffStats() {
            return diffStats;
        }

        public List<GitCommit> getBreakingCommits() {
            return Collections.unmodifiableList(breakingCommits);
        }

        public boolean hasBreakingChanges() {
            return !breakingCommits.isEmpty();
        }

        public boolean hasChanges() {
            return !commits.isEmpty() || diff.hasChanges();
        }

        @Override
        public String toString() {
            return "RefComparison{" +
                   "oldRef='" + oldRef + '\'' +
                   ", newRef='" + newRef + '\'' +
                   ", commits=" + commits.size() +
                   ", changedFiles=" + diff.getTotalChanges() +
                   ", breakingChanges=" + breakingCommits.size() +
                   '}';
        }
    }

    public static class ReleaseNotes {
        private final String fromRef;
        private final String toRef;
        private final List<String> features;
        private final List<String> fixes;
        private final List<String> breakingChanges;
        private final List<String> other;
        private final int totalCommits;
        private final int totalFilesChanged;

        public ReleaseNotes(String fromRef, String toRef,
                           List<String> features, List<String> fixes,
                           List<String> breakingChanges, List<String> other,
                           int totalCommits, int totalFilesChanged) {
            this.fromRef = fromRef;
            this.toRef = toRef;
            this.features = features;
            this.fixes = fixes;
            this.breakingChanges = breakingChanges;
            this.other = other;
            this.totalCommits = totalCommits;
            this.totalFilesChanged = totalFilesChanged;
        }

        public String getFromRef() {
            return fromRef;
        }

        public String getToRef() {
            return toRef;
        }

        public List<String> getFeatures() {
            return Collections.unmodifiableList(features);
        }

        public List<String> getFixes() {
            return Collections.unmodifiableList(fixes);
        }

        public List<String> getBreakingChanges() {
            return Collections.unmodifiableList(breakingChanges);
        }

        public List<String> getOther() {
            return Collections.unmodifiableList(other);
        }

        public int getTotalCommits() {
            return totalCommits;
        }

        public int getTotalFilesChanged() {
            return totalFilesChanged;
        }

        public boolean hasBreakingChanges() {
            return !breakingChanges.isEmpty();
        }

        public String toMarkdown() {
            StringBuilder sb = new StringBuilder();
            sb.append("# Release Notes\n\n");
            sb.append("**From:** ").append(fromRef).append(" **To:** ").append(toRef).append("\n\n");

            if (!breakingChanges.isEmpty()) {
                sb.append("## ⚠️ Breaking Changes\n\n");
                for (String change : breakingChanges) {
                    sb.append("- ").append(change).append("\n");
                }
                sb.append("\n");
            }

            if (!features.isEmpty()) {
                sb.append("## ✨ Features\n\n");
                for (String feature : features) {
                    sb.append("- ").append(feature).append("\n");
                }
                sb.append("\n");
            }

            if (!fixes.isEmpty()) {
                sb.append("## 🐛 Bug Fixes\n\n");
                for (String fix : fixes) {
                    sb.append("- ").append(fix).append("\n");
                }
                sb.append("\n");
            }

            if (!other.isEmpty()) {
                sb.append("## 📝 Other Changes\n\n");
                for (String item : other) {
                    sb.append("- ").append(item).append("\n");
                }
                sb.append("\n");
            }

            sb.append("---\n\n");
            sb.append("*").append(totalCommits).append(" commits, ")
              .append(totalFilesChanged).append(" files changed*\n");

            return sb.toString();
        }

        @Override
        public String toString() {
            return "ReleaseNotes{" +
                   "fromRef='" + fromRef + '\'' +
                   ", toRef='" + toRef + '\'' +
                   ", features=" + features.size() +
                   ", fixes=" + fixes.size() +
                   ", breakingChanges=" + breakingChanges.size() +
                   '}';
        }
    }
}
