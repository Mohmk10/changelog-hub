package io.github.mohmk10.changeloghub.git.exception;

public class GitOperationException extends RuntimeException {

    private final GitErrorCode errorCode;
    private final String repositoryPath;
    private final String reference;

    public GitOperationException(String message) {
        super(message);
        this.errorCode = GitErrorCode.UNKNOWN;
        this.repositoryPath = null;
        this.reference = null;
    }

    public GitOperationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = GitErrorCode.UNKNOWN;
        this.repositoryPath = null;
        this.reference = null;
    }

    public GitOperationException(GitErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.repositoryPath = null;
        this.reference = null;
    }

    public GitOperationException(GitErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.repositoryPath = null;
        this.reference = null;
    }

    public GitOperationException(GitErrorCode errorCode, String message,
                                  String repositoryPath, String reference) {
        super(message);
        this.errorCode = errorCode;
        this.repositoryPath = repositoryPath;
        this.reference = reference;
    }

    public GitOperationException(GitErrorCode errorCode, String message,
                                  String repositoryPath, String reference, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.repositoryPath = repositoryPath;
        this.reference = reference;
    }

    public GitErrorCode getErrorCode() {
        return errorCode;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public String getReference() {
        return reference;
    }

    public enum GitErrorCode {
        
        UNKNOWN("Unknown error"),

        REPOSITORY_NOT_FOUND("Repository not found"),

        INVALID_REPOSITORY("Not a valid Git repository"),

        REFERENCE_NOT_FOUND("Reference not found"),

        INVALID_REFERENCE("Invalid reference"),

        FILE_NOT_FOUND("File not found"),

        FILE_READ_ERROR("Failed to read file"),

        COMMIT_PARSE_ERROR("Failed to parse commit"),

        DIFF_ERROR("Failed to get diff"),

        AUTHENTICATION_REQUIRED("Authentication required"),

        REMOTE_ERROR("Remote operation failed"),

        CLONE_ERROR("Clone operation failed"),

        CHECKOUT_ERROR("Checkout operation failed"),

        IO_ERROR("IO error");

        private final String description;

        GitErrorCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static GitOperationException repositoryNotFound(String path) {
        return new GitOperationException(
            GitErrorCode.REPOSITORY_NOT_FOUND,
            "Repository not found at: " + path,
            path,
            null
        );
    }

    public static GitOperationException invalidRepository(String path) {
        return new GitOperationException(
            GitErrorCode.INVALID_REPOSITORY,
            "Not a valid Git repository: " + path,
            path,
            null
        );
    }

    public static GitOperationException referenceNotFound(String ref) {
        return new GitOperationException(
            GitErrorCode.REFERENCE_NOT_FOUND,
            "Reference not found: " + ref,
            null,
            ref
        );
    }

    public static GitOperationException referenceNotFound(String ref, String repoPath) {
        return new GitOperationException(
            GitErrorCode.REFERENCE_NOT_FOUND,
            "Reference not found: " + ref + " in repository: " + repoPath,
            repoPath,
            ref
        );
    }

    public static GitOperationException invalidReference(String ref) {
        return new GitOperationException(
            GitErrorCode.INVALID_REFERENCE,
            "Invalid reference: " + ref,
            null,
            ref
        );
    }

    public static GitOperationException fileNotFound(String filePath, String ref) {
        return new GitOperationException(
            GitErrorCode.FILE_NOT_FOUND,
            "File not found: " + filePath + " at reference: " + ref,
            null,
            ref
        );
    }

    public static GitOperationException fileReadError(String filePath, String ref, Throwable cause) {
        return new GitOperationException(
            GitErrorCode.FILE_READ_ERROR,
            "Failed to read file: " + filePath + " at reference: " + ref,
            null,
            ref,
            cause
        );
    }

    public static GitOperationException diffError(String oldRef, String newRef, Throwable cause) {
        return new GitOperationException(
            GitErrorCode.DIFF_ERROR,
            "Failed to get diff between: " + oldRef + " and " + newRef,
            null,
            oldRef + ".." + newRef,
            cause
        );
    }

    public static GitOperationException ioError(String message, Throwable cause) {
        return new GitOperationException(
            GitErrorCode.IO_ERROR,
            message,
            cause
        );
    }

    public static GitOperationException cloneError(String url, Throwable cause) {
        return new GitOperationException(
            GitErrorCode.CLONE_ERROR,
            "Failed to clone repository: " + url,
            url,
            null,
            cause
        );
    }
}
