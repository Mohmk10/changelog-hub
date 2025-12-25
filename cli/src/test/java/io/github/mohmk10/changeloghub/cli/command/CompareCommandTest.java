package io.github.mohmk10.changeloghub.cli.command;

import io.github.mohmk10.changeloghub.cli.ChangelogHubCli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CompareCommand Tests")
class CompareCommandTest {

    @TempDir
    Path tempDir;

    private Path oldSpecPath;
    private Path newSpecPath;
    private Path newSpecBreakingPath;

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() throws IOException {
        oldSpecPath = tempDir.resolve("api-v1.yaml");
        newSpecPath = tempDir.resolve("api-v2-minor.yaml");
        newSpecBreakingPath = tempDir.resolve("api-v2-breaking.yaml");

        copyTestResource("api-v1.yaml", oldSpecPath);
        copyTestResource("api-v2-minor.yaml", newSpecPath);
        copyTestResource("api-v2-breaking.yaml", newSpecBreakingPath);

        originalOut = System.out;
        originalErr = System.err;
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void copyTestResource(String resourceName, Path target) throws IOException {
        try (var is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is != null) {
                Files.copy(is, target);
            }
        }
    }

    @Test
    @DisplayName("Should compare valid files successfully")
    void testCompareWithValidFiles() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("compare",
            oldSpecPath.toString(),
            newSpecPath.toString());

        assertThat(exitCode).isEqualTo(0);
        String output = errContent.toString();
        assertThat(output).contains("Summary:");
    }

    @Test
    @DisplayName("Should detect breaking changes")
    void testCompareWithBreakingChanges() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("compare",
            oldSpecPath.toString(),
            newSpecBreakingPath.toString());

        assertThat(exitCode).isEqualTo(0);
        String output = errContent.toString();
        assertThat(output).contains("Breaking:");
    }

    @Test
    @DisplayName("Should generate markdown format output")
    void testCompareWithMarkdownFormat() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("compare",
            oldSpecPath.toString(),
            newSpecBreakingPath.toString(),
            "-f", "markdown");

        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString();
        assertThat(output).contains("# Changelog:");
    }

    @Test
    @DisplayName("Should generate JSON format output")
    void testCompareWithJsonFormat() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("compare",
            oldSpecPath.toString(),
            newSpecBreakingPath.toString(),
            "-f", "json");

        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString();
        assertThat(output).contains("\"apiName\"");
        assertThat(output).contains("\"changes\"");
    }

    @Test
    @DisplayName("Should generate HTML format output")
    void testCompareWithHtmlFormat() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("compare",
            oldSpecPath.toString(),
            newSpecBreakingPath.toString(),
            "-f", "html");

        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString();
        assertThat(output).contains("<!DOCTYPE html>");
        assertThat(output).contains("</html>");
    }

    @Test
    @DisplayName("Should write output to file")
    void testCompareWithOutputFile() throws IOException {
        Path outputPath = tempDir.resolve("changelog.md");

        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("compare",
            oldSpecPath.toString(),
            newSpecBreakingPath.toString(),
            "-f", "markdown",
            "-o", outputPath.toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(Files.exists(outputPath)).isTrue();
        String content = Files.readString(outputPath);
        assertThat(content).contains("# Changelog:");
    }

    @Test
    @DisplayName("Should fail on breaking changes with --fail-on-breaking")
    void testCompareFailOnBreaking() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("compare",
            oldSpecPath.toString(),
            newSpecBreakingPath.toString(),
            "--fail-on-breaking");

        assertThat(exitCode).isEqualTo(1);
        String output = errContent.toString();
        assertThat(output).contains("Breaking changes detected");
    }

    @Test
    @DisplayName("Should return error for invalid file")
    void testCompareWithInvalidFile() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("compare",
            "nonexistent.yaml",
            newSpecPath.toString());

        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    @DisplayName("Should show verbose output")
    void testCompareVerboseMode() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("compare",
            oldSpecPath.toString(),
            newSpecPath.toString(),
            "-v");

        assertThat(exitCode).isEqualTo(0);
        String output = errContent.toString();
        assertThat(output).contains("Comparing:");
    }

    @Test
    @DisplayName("Should show quiet output")
    void testCompareQuietMode() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("compare",
            oldSpecPath.toString(),
            newSpecPath.toString(),
            "-q");

        assertThat(exitCode).isEqualTo(0);
        String output = errContent.toString();
        assertThat(output).doesNotContain("Summary:");
    }
}
