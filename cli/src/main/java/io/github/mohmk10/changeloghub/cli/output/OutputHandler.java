package io.github.mohmk10.changeloghub.cli.output;

import java.io.Closeable;
import java.io.IOException;

public interface OutputHandler extends Closeable {

    void write(String content) throws IOException;

    void writeLine(String line) throws IOException;

    void writeError(String error) throws IOException;

    @Override
    void close() throws IOException;
}
