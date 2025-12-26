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

public interface GitService extends AutoCloseable {

    GitService open(Path path);

    GitService open(String path);

    GitService clone(String url, Path localPath);

    boolean isOpen();

    Path getRepositoryPath();

    Optional<GitFileContent> getFileContent(String filePath, String ref);

    List<String> listFiles(String ref);

    List<String> listFiles(String directory, String ref);

    boolean fileExists(String filePath, String ref);

    List<GitBranch> getBranches();

    Optional<GitBranch> getBranch(String branchName);

    Optional<GitBranch> getCurrentBranch();

    Optional<GitBranch> getDefaultBranch();

    List<GitTag> getTags();

    List<GitTag> getVersionTags();

    Optional<GitTag> getTag(String tagName);

    Optional<GitTag> getLatestTag();

    List<GitCommit> getCommits(String fromRef, String toRef);

    List<GitCommit> getRecentCommits(int count);

    Optional<GitCommit> getCommit(String ref);

    GitDiff getDiff(String oldRef, String newRef);

    List<String> getChangedFiles(String oldRef, String newRef);

    boolean hasChanges(String oldRef, String newRef);

    Map<SpecFileDetector.SpecType, List<String>> detectSpecs(String ref);

    List<String> findOpenApiSpecs(String ref);

    List<String> findGraphQLSchemas(String ref);

    List<String> findProtobufFiles(String ref);

    List<String> findAsyncApiSpecs(String ref);

    List<ChangelogExtractor.SpecChangelog> generateChangelogs(String oldRef, String newRef);

    Optional<Changelog> generateChangelog(String specPath, String oldRef, String newRef);

    List<ChangelogExtractor.SpecChangelog> generateChangelogsForChangedFiles(String oldRef, String newRef);

    GitRefComparator.RefComparison compareRefs(String oldRef, String newRef);

    GitSpecComparator.SpecComparison compareSpecs(String oldRef, String newRef);

    List<GitRefComparator.RefComparison> compareConsecutiveTags();

    boolean hasBreakingChanges(String oldRef, String newRef);

    List<GitSpecComparator.BreakingChange> getBreakingChanges(String oldRef, String newRef);

    GitRefComparator.ReleaseNotes generateReleaseNotes(String oldRef, String newRef);

    GitSpecComparator.MigrationGuide generateMigrationGuide(String oldRef, String newRef);

    @Override
    void close();
}
