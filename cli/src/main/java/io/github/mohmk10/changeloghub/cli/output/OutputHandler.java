package io.github.mohmk10.changeloghub.cli.output;

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface for handling CLI output to various destinations.
 */
public interface OutputHandler extends Closeable {

    /**
     * Write content to the output destination.
     *
     * @param content the content to write
     * @throws IOException if writing fails
     */
    void write(String content) throws IOException;

    /**
     * Write a single line to the output destination.
     *
     * @param line the line to write
     * @throws IOException if writing fails
     */
    void writeLine(String line) throws IOException;

    /**
     * Write an error message to the appropriate error stream.
     *
     * @param error the error message to write
     * @throws IOException if writing fails
     */
    void writeError(String error) throws IOException;

    /**
     * Close the output handler and release any resources.
     *
     * @throws IOException if closing fails
     */
    @Override
    void close() throws IOException;
}
