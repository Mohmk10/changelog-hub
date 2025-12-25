package io.github.mohmk10.changeloghub.cli.exception;

/**
 * Exception thrown for CLI-related errors.
 */
public class CliException extends RuntimeException {

    private final int exitCode;

    public CliException(String message) {
        super(message);
        this.exitCode = 1;
    }

    public CliException(String message, Throwable cause) {
        super(message, cause);
        this.exitCode = 1;
    }

    public CliException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public CliException(String message, Throwable cause, int exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
