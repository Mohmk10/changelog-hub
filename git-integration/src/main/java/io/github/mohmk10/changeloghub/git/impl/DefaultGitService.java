package io.github.mohmk10.changeloghub.git.impl;

import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.git.GitService;
import io.github.mohmk10.changeloghub.git.analyzer.BranchAnalyzer;
import io.github.mohmk10.changeloghub.git.analyzer.CommitAnalyzer;
import io.github.mohmk10.changeloghub.git.analyzer.DiffAnalyzer;
import io.github.mohmk10.changeloghub.git.analyzer.TagAnalyzer;
import io.github.mohmk10.changeloghub.git.comparator.GitRefComparator;
import io.github.mohmk10.changeloghub.git.comparator.GitSpecComparator;
import io.github.mohmk10.changeloghub.git.config.GitConfig;
import io.github.mohmk10.changeloghub.git.exception.GitOperationException;
import io.github.mohmk10.changeloghub.git.extractor.ChangelogExtractor;
import io.github.mohmk10.changeloghub.git.extractor.FileExtractor;
import io.github.mohmk10.changeloghub.git.extractor.SpecFileDetector;
import io.github.mohmk10.changeloghub.git.model.*;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultGitService implements GitService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultGitService.class);

    private Repository repository;
    private GitConfig config;
    private Path repositoryPath;

    private FileExtractor fileExtractor;
    private SpecFileDetector specFileDetector;
    private ChangelogExtractor changelogExtractor;
    private CommitAnalyzer commitAnalyzer;
    private TagAnalyzer tagAnalyzer;
    private BranchAnalyzer branchAnalyzer;
    private DiffAnalyzer diffAnalyzer;
    private GitRefComparator refComparator;
    private GitSpecComparator specComparator;

    public DefaultGitService() {
        this.config = new GitConfig();
    }

    public DefaultGitService(GitConfig config) {
        this.config = config != null ? config : new GitConfig();
    }

    public static GitService create() {
        return new DefaultGitService();
    }

    public static GitService create(GitConfig config) {
        return new DefaultGitService(config);
    }

    public static GitService openRepository(Path path) {
        return new DefaultGitService().open(path);
    }

    public static GitService openRepository(String path) {
        return new DefaultGitService().open(path);
    }

    @Override
    public GitService open(Path path) {
        try {
            File gitDir = findGitDir(path.toFile());
            this.repository = new FileRepositoryBuilder()
                .setGitDir(gitDir)
                .readEnvironment()
                .findGitDir()
                .build();
            this.repositoryPath = path;
            this.config.setRepositoryPath(path);
            initializeComponents();
            logger.info("Opened repository at: {}", path);
            return this;
        } catch (IOException e) {
            throw GitOperationException.invalidRepository(path.toString());
        }
    }

    @Override
    public GitService open(String path) {
        return open(Path.of(path));
    }

    @Override
    public GitService clone(String url, Path localPath) {
        try {
            logger.info("Cloning repository from {} to {}", url, localPath);

            CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(url)
                .setDirectory(localPath.toFile());

            if (config.hasCredentials()) {
                GitConfig.CredentialsConfig creds = config.getCredentials();
                if (creds.isTokenAuth()) {
                    cloneCommand.setCredentialsProvider(
                        new org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider(
                            "token", creds.getAccessToken()
                        )
                    );
                } else if (creds.isBasicAuth()) {
                    cloneCommand.setCredentialsProvider(
                        new org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider(
                            creds.getUsername(), creds.getPassword()
                        )
                    );
                }
            }

            try (Git git = cloneCommand.call()) {
                this.repository = git.getRepository();
                this.repositoryPath = localPath;
                this.config.setRepositoryPath(localPath);
                initializeComponents();
                logger.info("Cloned repository to: {}", localPath);
            }

            return this;

        } catch (Exception e) {
            throw GitOperationException.cloneError(url, e);
        }
    }

    @Override
    public boolean isOpen() {
        return repository != null;
    }

    @Override
    public Path getRepositoryPath() {
        return repositoryPath;
    }

    @Override
    public Optional<GitFileContent> getFileContent(String filePath, String ref) {
        ensureOpen();
        return fileExtractor.extractFile(filePath, ref);
    }

    @Override
    public List<String> listFiles(String ref) {
        ensureOpen();
        return fileExtractor.listAllFiles(ref);
    }

    @Override
    public List<String> listFiles(String directory, String ref) {
        ensureOpen();
        return fileExtractor.listFiles(directory, ref);
    }

    @Override
    public boolean fileExists(String filePath, String ref) {
        ensureOpen();
        return fileExtractor.fileExists(filePath, ref);
    }

    @Override
    public List<GitBranch> getBranches() {
        ensureOpen();
        return branchAnalyzer.getAllBranches();
    }

    @Override
    public Optional<GitBranch> getBranch(String branchName) {
        ensureOpen();
        return branchAnalyzer.getBranch(branchName);
    }

    @Override
    public Optional<GitBranch> getCurrentBranch() {
        ensureOpen();
        return branchAnalyzer.getCurrentBranch();
    }

    @Override
    public Optional<GitBranch> getDefaultBranch() {
        ensureOpen();
        return branchAnalyzer.getDefaultBranch();
    }

    @Override
    public List<GitTag> getTags() {
        ensureOpen();
        return tagAnalyzer.getAllTags();
    }

    @Override
    public List<GitTag> getVersionTags() {
        ensureOpen();
        return tagAnalyzer.getTagsSortedByVersion();
    }

    @Override
    public Optional<GitTag> getTag(String tagName) {
        ensureOpen();
        return tagAnalyzer.getTag(tagName);
    }

    @Override
    public Optional<GitTag> getLatestTag() {
        ensureOpen();
        return tagAnalyzer.getLatestTag();
    }

    @Override
    public List<GitCommit> getCommits(String fromRef, String toRef) {
        ensureOpen();
        return commitAnalyzer.getCommitsBetween(fromRef, toRef);
    }

    @Override
    public List<GitCommit> getRecentCommits(int count) {
        ensureOpen();
        return commitAnalyzer.getRecentCommits(count);
    }

    @Override
    public Optional<GitCommit> getCommit(String ref) {
        ensureOpen();
        return commitAnalyzer.getCommit(ref);
    }

    @Override
    public GitDiff getDiff(String oldRef, String newRef) {
        ensureOpen();
        return diffAnalyzer.getDiff(oldRef, newRef);
    }

    @Override
    public List<String> getChangedFiles(String oldRef, String newRef) {
        ensureOpen();
        return diffAnalyzer.getChangedFiles(oldRef, newRef);
    }

    @Override
    public boolean hasChanges(String oldRef, String newRef) {
        ensureOpen();
        return diffAnalyzer.hasChanges(oldRef, newRef);
    }

    @Override
    public Map<SpecFileDetector.SpecType, List<String>> detectSpecs(String ref) {
        ensureOpen();
        return specFileDetector.detectSpecs(ref);
    }

    @Override
    public List<String> findOpenApiSpecs(String ref) {
        ensureOpen();
        return specFileDetector.findOpenApiSpecs(ref);
    }

    @Override
    public List<String> findGraphQLSchemas(String ref) {
        ensureOpen();
        return specFileDetector.findGraphQLSchemas(ref);
    }

    @Override
    public List<String> findProtobufFiles(String ref) {
        ensureOpen();
        return specFileDetector.findProtobufFiles(ref);
    }

    @Override
    public List<String> findAsyncApiSpecs(String ref) {
        ensureOpen();
        return specFileDetector.findAsyncApiSpecs(ref);
    }

    @Override
    public List<ChangelogExtractor.SpecChangelog> generateChangelogs(String oldRef, String newRef) {
        ensureOpen();
        return changelogExtractor.extractChangelogs(oldRef, newRef);
    }

    @Override
    public Optional<Changelog> generateChangelog(String specPath, String oldRef, String newRef) {
        ensureOpen();
        ChangelogExtractor.SpecChangelog specChangelog =
            changelogExtractor.extractChangelogForFile(specPath, oldRef, newRef);
        return specChangelog != null ? Optional.ofNullable(specChangelog.getChangelog()) : Optional.empty();
    }

    @Override
    public List<ChangelogExtractor.SpecChangelog> generateChangelogsForChangedFiles(String oldRef, String newRef) {
        ensureOpen();
        GitDiff diff = diffAnalyzer.getDiff(oldRef, newRef);
        return changelogExtractor.extractChangelogsForChangedFiles(diff, oldRef, newRef);
    }

    @Override
    public GitRefComparator.RefComparison compareRefs(String oldRef, String newRef) {
        ensureOpen();
        return refComparator.compare(oldRef, newRef);
    }

    @Override
    public GitSpecComparator.SpecComparison compareSpecs(String oldRef, String newRef) {
        ensureOpen();
        return specComparator.compare(oldRef, newRef);
    }

    @Override
    public List<GitRefComparator.RefComparison> compareConsecutiveTags() {
        ensureOpen();
        return refComparator.compareConsecutiveTags();
    }

    @Override
    public boolean hasBreakingChanges(String oldRef, String newRef) {
        ensureOpen();
        return specComparator.hasBreakingChanges(oldRef, newRef);
    }

    @Override
    public List<GitSpecComparator.BreakingChange> getBreakingChanges(String oldRef, String newRef) {
        ensureOpen();
        return specComparator.getBreakingChanges(oldRef, newRef);
    }

    @Override
    public GitRefComparator.ReleaseNotes generateReleaseNotes(String oldRef, String newRef) {
        ensureOpen();
        return refComparator.generateReleaseNotes(oldRef, newRef);
    }

    @Override
    public GitSpecComparator.MigrationGuide generateMigrationGuide(String oldRef, String newRef) {
        ensureOpen();
        return specComparator.generateMigrationGuide(oldRef, newRef);
    }

    @Override
    public void close() {
        if (repository != null) {
            repository.close();
            repository = null;
            logger.debug("Repository closed");
        }
    }

    private void ensureOpen() {
        if (repository == null) {
            throw new GitOperationException("Repository not open. Call open() first.");
        }
    }

    private void initializeComponents() {
        this.fileExtractor = new FileExtractor(repository, config);
        this.specFileDetector = new SpecFileDetector(repository, config);
        this.changelogExtractor = new ChangelogExtractor(repository, config);
        this.commitAnalyzer = new CommitAnalyzer(repository, config);
        this.tagAnalyzer = new TagAnalyzer(repository, config);
        this.branchAnalyzer = new BranchAnalyzer(repository, config);
        this.diffAnalyzer = new DiffAnalyzer(repository, config);
        this.refComparator = new GitRefComparator(repository, config);
        this.specComparator = new GitSpecComparator(repository, config);
    }

    private File findGitDir(File dir) {
        File gitDir = new File(dir, ".git");
        if (gitDir.exists()) {
            return gitDir;
        }
        
        File headFile = new File(dir, "HEAD");
        if (headFile.exists()) {
            return dir;
        }
        throw GitOperationException.invalidRepository(dir.toString());
    }
}
