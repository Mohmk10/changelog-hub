package io.github.mohmk10.changeloghub.git.extractor;

import io.github.mohmk10.changeloghub.git.config.GitConfig;
import io.github.mohmk10.changeloghub.git.exception.GitOperationException;
import io.github.mohmk10.changeloghub.git.model.GitFileContent;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Extracts file content from Git repository at specific references.
 */
public class FileExtractor {

    private static final Logger logger = LoggerFactory.getLogger(FileExtractor.class);

    private final Repository repository;
    private final GitConfig config;

    public FileExtractor(Repository repository) {
        this(repository, new GitConfig());
    }

    public FileExtractor(Repository repository, GitConfig config) {
        this.repository = repository;
        this.config = config;
    }

    /**
     * Extract file content at a specific Git reference.
     *
     * @param filePath the file path relative to repository root
     * @param ref the Git reference (branch, tag, or commit SHA)
     * @return the file content, or empty if not found
     */
    public Optional<GitFileContent> extractFile(String filePath, String ref) {
        try {
            ObjectId refId = resolveRef(ref);
            if (refId == null) {
                logger.debug("Reference not found: {}", ref);
                return Optional.of(GitFileContent.builder()
                    .path(filePath)
                    .ref(ref)
                    .exists(false)
                    .build());
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(refId);
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(filePath));

                    if (!treeWalk.next()) {
                        logger.debug("File not found: {} at {}", filePath, ref);
                        return Optional.of(GitFileContent.builder()
                            .path(filePath)
                            .ref(ref)
                            .commitId(commit.getName())
                            .exists(false)
                            .build());
                    }

                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);
                    byte[] bytes = loader.getBytes();

                    // Check file size limit
                    if (bytes.length > config.getMaxFileSizeBytes()) {
                        logger.warn("File too large: {} ({} bytes)", filePath, bytes.length);
                        return Optional.of(GitFileContent.builder()
                            .path(filePath)
                            .ref(ref)
                            .commitId(commit.getName())
                            .exists(true)
                            .bytes(new byte[0]) // Don't load content
                            .build());
                    }

                    return Optional.of(GitFileContent.builder()
                        .path(filePath)
                        .ref(ref)
                        .commitId(commit.getName())
                        .bytes(bytes)
                        .exists(true)
                        .build());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to extract file: {} at {}", filePath, ref, e);
            throw GitOperationException.fileReadError(filePath, ref, e);
        }
    }

    /**
     * Extract multiple files at a specific reference.
     */
    public List<GitFileContent> extractFiles(List<String> filePaths, String ref) {
        List<GitFileContent> results = new ArrayList<>();
        for (String filePath : filePaths) {
            extractFile(filePath, ref).ifPresent(results::add);
        }
        return results;
    }

    /**
     * List all files in a directory at a specific reference.
     */
    public List<String> listFiles(String directoryPath, String ref) {
        List<String> files = new ArrayList<>();

        try {
            ObjectId refId = resolveRef(ref);
            if (refId == null) {
                return files;
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(refId);
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);

                    if (directoryPath != null && !directoryPath.isEmpty()) {
                        treeWalk.setFilter(PathFilter.create(directoryPath));
                    }

                    while (treeWalk.next()) {
                        String path = treeWalk.getPathString();
                        if (!config.shouldIgnore(path)) {
                            files.add(path);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to list files at: {}", ref, e);
            throw GitOperationException.ioError("Failed to list files at: " + ref, e);
        }

        return files;
    }

    /**
     * List all files at repository root.
     */
    public List<String> listAllFiles(String ref) {
        return listFiles(null, ref);
    }

    /**
     * Check if a file exists at a specific reference.
     */
    public boolean fileExists(String filePath, String ref) {
        Optional<GitFileContent> content = extractFile(filePath, ref);
        return content.isPresent() && content.get().exists();
    }

    /**
     * Get file content as string (UTF-8).
     */
    public Optional<String> getFileContentAsString(String filePath, String ref) {
        return extractFile(filePath, ref)
            .filter(GitFileContent::exists)
            .map(GitFileContent::getContent);
    }

    /**
     * Find files matching a pattern at a specific reference.
     */
    public List<String> findFiles(String pattern, String ref) {
        List<String> allFiles = listAllFiles(ref);
        List<String> matched = new ArrayList<>();

        for (String file : allFiles) {
            if (matchesPattern(file, pattern)) {
                matched.add(file);
            }
        }

        return matched;
    }

    /**
     * Find files by extension at a specific reference.
     */
    public List<String> findFilesByExtension(String extension, String ref) {
        return findFiles("*." + extension, ref);
    }

    /**
     * Resolve a reference to an ObjectId.
     */
    private ObjectId resolveRef(String ref) throws IOException {
        ObjectId objectId = repository.resolve(ref);
        if (objectId == null) {
            // Try common prefixes
            objectId = repository.resolve("refs/heads/" + ref);
            if (objectId == null) {
                objectId = repository.resolve("refs/tags/" + ref);
            }
            if (objectId == null) {
                objectId = repository.resolve("refs/remotes/origin/" + ref);
            }
        }
        return objectId;
    }

    /**
     * Simple pattern matching (supports * wildcard).
     */
    private boolean matchesPattern(String path, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }

        String fileName = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;

        if (pattern.startsWith("*.")) {
            String extension = pattern.substring(2);
            return fileName.endsWith("." + extension);
        }

        if (pattern.contains("*")) {
            String regex = pattern.replace(".", "\\.").replace("*", ".*");
            return fileName.matches(regex);
        }

        return fileName.equals(pattern);
    }
}
