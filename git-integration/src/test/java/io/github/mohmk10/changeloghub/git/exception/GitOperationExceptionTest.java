package io.github.mohmk10.changeloghub.git.exception;

import io.github.mohmk10.changeloghub.git.exception.GitOperationException.GitErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitOperationExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        GitOperationException ex = new GitOperationException("Test error");

        assertThat(ex.getMessage()).isEqualTo("Test error");
        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.UNKNOWN);
        assertThat(ex.getRepositoryPath()).isNull();
        assertThat(ex.getReference()).isNull();
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("cause");
        GitOperationException ex = new GitOperationException("Test error", cause);

        assertThat(ex.getMessage()).isEqualTo("Test error");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateWithErrorCode() {
        GitOperationException ex = new GitOperationException(
            GitErrorCode.REPOSITORY_NOT_FOUND,
            "Repo not found"
        );

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.REPOSITORY_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Repo not found");
    }

    @Test
    void shouldCreateWithFullContext() {
        GitOperationException ex = new GitOperationException(
            GitErrorCode.REFERENCE_NOT_FOUND,
            "Ref not found",
            "/path/to/repo",
            "v1.0.0"
        );

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.REFERENCE_NOT_FOUND);
        assertThat(ex.getRepositoryPath()).isEqualTo("/path/to/repo");
        assertThat(ex.getReference()).isEqualTo("v1.0.0");
    }

    @Test
    void shouldCreateRepositoryNotFound() {
        GitOperationException ex = GitOperationException.repositoryNotFound("/path/to/repo");

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.REPOSITORY_NOT_FOUND);
        assertThat(ex.getMessage()).contains("/path/to/repo");
        assertThat(ex.getRepositoryPath()).isEqualTo("/path/to/repo");
    }

    @Test
    void shouldCreateInvalidRepository() {
        GitOperationException ex = GitOperationException.invalidRepository("/path/to/repo");

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.INVALID_REPOSITORY);
        assertThat(ex.getMessage()).contains("/path/to/repo");
    }

    @Test
    void shouldCreateReferenceNotFound() {
        GitOperationException ex = GitOperationException.referenceNotFound("v1.0.0");

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.REFERENCE_NOT_FOUND);
        assertThat(ex.getMessage()).contains("v1.0.0");
        assertThat(ex.getReference()).isEqualTo("v1.0.0");
    }

    @Test
    void shouldCreateReferenceNotFoundWithRepo() {
        GitOperationException ex = GitOperationException.referenceNotFound("v1.0.0", "/path/to/repo");

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.REFERENCE_NOT_FOUND);
        assertThat(ex.getMessage()).contains("v1.0.0");
        assertThat(ex.getMessage()).contains("/path/to/repo");
    }

    @Test
    void shouldCreateInvalidReference() {
        GitOperationException ex = GitOperationException.invalidReference("invalid-ref");

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.INVALID_REFERENCE);
        assertThat(ex.getMessage()).contains("invalid-ref");
    }

    @Test
    void shouldCreateFileNotFound() {
        GitOperationException ex = GitOperationException.fileNotFound("api.yaml", "v1.0.0");

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.FILE_NOT_FOUND);
        assertThat(ex.getMessage()).contains("api.yaml");
        assertThat(ex.getMessage()).contains("v1.0.0");
    }

    @Test
    void shouldCreateFileReadError() {
        RuntimeException cause = new RuntimeException("IO error");
        GitOperationException ex = GitOperationException.fileReadError("api.yaml", "v1.0.0", cause);

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.FILE_READ_ERROR);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateDiffError() {
        RuntimeException cause = new RuntimeException("Diff failed");
        GitOperationException ex = GitOperationException.diffError("v1.0.0", "v2.0.0", cause);

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.DIFF_ERROR);
        assertThat(ex.getMessage()).contains("v1.0.0");
        assertThat(ex.getMessage()).contains("v2.0.0");
    }

    @Test
    void shouldCreateIoError() {
        RuntimeException cause = new RuntimeException("IO problem");
        GitOperationException ex = GitOperationException.ioError("IO operation failed", cause);

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.IO_ERROR);
    }

    @Test
    void shouldCreateCloneError() {
        RuntimeException cause = new RuntimeException("Clone failed");
        GitOperationException ex = GitOperationException.cloneError("https://github.com/repo", cause);

        assertThat(ex.getErrorCode()).isEqualTo(GitErrorCode.CLONE_ERROR);
        assertThat(ex.getMessage()).contains("https://github.com/repo");
    }

    @Test
    void shouldHaveErrorCodeDescriptions() {
        assertThat(GitErrorCode.UNKNOWN.getDescription()).isEqualTo("Unknown error");
        assertThat(GitErrorCode.REPOSITORY_NOT_FOUND.getDescription()).isEqualTo("Repository not found");
        assertThat(GitErrorCode.INVALID_REPOSITORY.getDescription()).isEqualTo("Not a valid Git repository");
        assertThat(GitErrorCode.REFERENCE_NOT_FOUND.getDescription()).isEqualTo("Reference not found");
        assertThat(GitErrorCode.FILE_NOT_FOUND.getDescription()).isEqualTo("File not found");
        assertThat(GitErrorCode.DIFF_ERROR.getDescription()).isEqualTo("Failed to get diff");
        assertThat(GitErrorCode.CLONE_ERROR.getDescription()).isEqualTo("Clone operation failed");
    }
}
