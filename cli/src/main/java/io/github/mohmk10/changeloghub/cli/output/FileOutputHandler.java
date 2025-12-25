package io.github.mohmk10.changeloghub.cli.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Output handler that writes to a file.
 */
public class FileOutputHandler implements OutputHandler {

    private final File outputFile;
    private final PrintWriter writer;

    public FileOutputHandler(File outputFile) throws IOException {
        this.outputFile = outputFile;
        ensureParentDirectoriesExist();
        this.writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
    }

    public FileOutputHandler(Path outputPath) throws IOException {
        this(outputPath.toFile());
    }

    @Override
    public void write(String content) throws IOException {
        writer.print(content);
    }

    @Override
    public void writeLine(String line) throws IOException {
        writer.println(line);
    }

    @Override
    public void writeError(String error) throws IOException {
        System.err.println(error);
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    public File getOutputFile() {
        return outputFile;
    }

    private void ensureParentDirectoriesExist() throws IOException {
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
    }
}
