package io.github.mohmk10.changeloghub.git;

import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.git.comparator.GitRefComparator;
import io.github.mohmk10.changeloghub.git.comparator.GitSpecComparator;
import io.github.mohmk10.changeloghub.git.extractor.ChangelogExtractor;
import io.github.mohmk10.changeloghub.git.extractor.SpecFileDetector;
import io.github.mohmk10.changeloghub.git.model.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main service interface for Git integration.
 * Provides high-level operations for comparing API specs between Git references.
 */
public interface GitService extends AutoCloseable {

    // ============================================================
    // Repository Management
    // ============================================================

    /**
     * Open a local Git repository.
     *
     * @param path path to the repository
     * @return the GitService instance
     */
    GitService open(Path path);

    /**
     * Open a local Git repository.
     *
     * @param path path to the repository
     * @return the GitService instance
     */
    GitService open(String path);

    /**
     * Clone a remote repository.
     *
     * @param url the repository URL
     * @param localPath local path to clone to
     * @return the GitService instance
     */
    GitService clone(String url, Path localPath);

    /**
     * Check if the repository is open.
     */
    boolean isOpen();

    /**
     * Get the repository path.
     */
    Path getRepositoryPath();

    // ============================================================
    // File Operations
    // ============================================================

    /**
     * Get file content at a specific reference.
     *
     * @param filePath the file path relative to repository root
     * @param ref the Git reference (branch, tag, or commit)
     * @return the file content, or empty if not found
     */
    Optional<GitFileContent> getFileContent(String filePath, String ref);

    /**
     * List files at a specific reference.
     *
     * @param ref the Git reference
     * @return list of file paths
     */
    List<String> listFiles(String ref);

    /**
     * List files in a directory at a specific reference.
     *
     * @param directory the directory path
     * @param ref the Git reference
     * @return list of file paths
     */
    List<String> listFiles(String directory, String ref);

    /**
     * Check if a file exists at a specific reference.
     */
    boolean fileExists(String filePath, String ref);

    // ============================================================
    // Branch Operations
    // ============================================================

    /**
     * Get all local branches.
     */
    List<GitBranch> getBranches();

    /**
     * Get a specific branch.
     */
    Optional<GitBranch> getBranch(String branchName);

    /**
     * Get the current branch.
     */
    Optional<GitBranch> getCurrentBranch();

    /**
     * Get the default branch (main or master).
     */
    Optional<GitBranch> getDefaultBranch();

    // ============================================================
    // Tag Operations
    // ============================================================

    /**
     * Get all tags.
     */
    List<GitTag> getTags();

    /**
     * Get semantic version tags sorted by version.
     */
    List<GitTag> getVersionTags();

    /**
     * Get a specific tag.
     */
    Optional<GitTag> getTag(String tagName);

    /**
     * Get the latest tag by version.
     */
    Optional<GitTag> getLatestTag();

    // ============================================================
    // Commit Operations
    // ============================================================

    /**
     * Get commits between two references.
     *
     * @param fromRef the starting reference (exclusive)
     * @param toRef the ending reference (inclusive)
     * @return list of commits
     */
    List<GitCommit> getCommits(String fromRef, String toRef);

    /**
     * Get recent commits from HEAD.
     *
     * @param count number of commits to retrieve
     * @return list of commits
     */
    List<GitCommit> getRecentCommits(int count);

    /**
     * Get a specific commit.
     */
    Optional<GitCommit> getCommit(String ref);

    // ============================================================
    // Diff Operations
    // ============================================================

    /**
     * Get the diff between two references.
     *
     * @param oldRef the old reference
     * @param newRef the new reference
     * @return the diff
     */
    GitDiff getDiff(String oldRef, String newRef);

    /**
     * Get changed files between two references.
     */
    List<String> getChangedFiles(String oldRef, String newRef);

    /**
     * Check if there are changes between two references.
     */
    boolean hasChanges(String oldRef, String newRef);

    // ============================================================
    // Spec Detection
    // ============================================================

    /**
     * Detect all API spec files at a reference.
     *
     * @param ref the Git reference
     * @return map of spec type to file paths
     */
    Map<SpecFileDetector.SpecType, List<String>> detectSpecs(String ref);

    /**
     * Find OpenAPI/Swagger spec files.
     */
    List<String> findOpenApiSpecs(String ref);

    /**
     * Find GraphQL schema files.
     */
    List<String> findGraphQLSchemas(String ref);

    /**
     * Find Protocol Buffers files.
     */
    List<String> findProtobufFiles(String ref);

    /**
     * Find AsyncAPI spec files.
     */
    List<String> findAsyncApiSpecs(String ref);

    // ============================================================
    // Changelog Generation
    // ============================================================

    /**
     * Generate changelogs for all specs between two references.
     *
     * @param oldRef the old reference
     * @param newRef the new reference
     * @return list of spec changelogs
     */
    List<ChangelogExtractor.SpecChangelog> generateChangelogs(String oldRef, String newRef);

    /**
     * Generate changelog for a specific spec file.
     *
     * @param specPath the spec file path
     * @param oldRef the old reference
     * @param newRef the new reference
     * @return the changelog, or empty if not found
     */
    Optional<Changelog> generateChangelog(String specPath, String oldRef, String newRef);

    /**
     * Generate changelogs for only changed files.
     */
    List<ChangelogExtractor.SpecChangelog> generateChangelogsForChangedFiles(String oldRef, String newRef);

    // ============================================================
    // Comparisons
    // ============================================================

    /**
     * Compare two Git references (branches, tags, or commits).
     *
     * @param oldRef the old reference
     * @param newRef the new reference
     * @return comparison result
     */
    GitRefComparator.RefComparison compareRefs(String oldRef, String newRef);

    /**
     * Compare API specs between two references.
     *
     * @param oldRef the old reference
     * @param newRef the new reference
     * @return spec comparison result
     */
    GitSpecComparator.SpecComparison compareSpecs(String oldRef, String newRef);

    /**
     * Compare consecutive version tags.
     *
     * @return list of comparisons between consecutive tags
     */
    List<GitRefComparator.RefComparison> compareConsecutiveTags();

    // ============================================================
    // Breaking Changes
    // ============================================================

    /**
     * Check if there are breaking changes between references.
     */
    boolean hasBreakingChanges(String oldRef, String newRef);

    /**
     * Get all breaking changes between references.
     */
    List<GitSpecComparator.BreakingChange> getBreakingChanges(String oldRef, String newRef);

    // ============================================================
    // Release Notes
    // ============================================================

    /**
     * Generate release notes between two references.
     *
     * @param oldRef the old reference
     * @param newRef the new reference
     * @return release notes
     */
    GitRefComparator.ReleaseNotes generateReleaseNotes(String oldRef, String newRef);

    /**
     * Generate a migration guide between two references.
     *
     * @param oldRef the old reference
     * @param newRef the new reference
     * @return migration guide
     */
    GitSpecComparator.MigrationGuide generateMigrationGuide(String oldRef, String newRef);

    // ============================================================
    // Lifecycle
    // ============================================================

    /**
     * Close the repository.
     */
    @Override
    void close();
}
