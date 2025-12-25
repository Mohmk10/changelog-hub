package io.github.mohmk10.changeloghub.cli.command;

import io.github.mohmk10.changeloghub.cli.ChangelogHubCli;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValidateCommand Tests")
class ValidateCommandTest {

    @TempDir
    Path tempDir;

    private Path validSpecPath;
    private Path invalidSpecPath;

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() throws IOException {
        validSpecPath = tempDir.resolve("api-v1.yaml");
        invalidSpecPath = tempDir.resolve("invalid.yaml");

        copyTestResource("api-v1.yaml", validSpecPath);
        Files.writeString(invalidSpecPath, "invalid: yaml: content: [\n  broken");

        originalOut = System.out;
        originalErr = System.err;
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
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
    @DisplayName("Should validate valid spec successfully")
    void testValidateValidSpec() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("validate", validSpecPath.toString());

        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString();
        assertThat(output).contains("Validation successful");
    }

    @Test
    @DisplayName("Should fail for invalid spec")
    void testValidateInvalidSpec() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("validate", invalidSpecPath.toString());

        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    @DisplayName("Should perform strict validation")
    void testValidateStrictMode() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("validate", validSpecPath.toString(), "--strict");

        String output = outContent.toString() + errContent.toString();
        assertThat(output).isNotEmpty();
    }

    @Test
    @DisplayName("Should fail for nonexistent file")
    void testValidateNonexistentFile() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("validate", "nonexistent.yaml");

        assertThat(exitCode).isEqualTo(1);
        String output = errContent.toString();
        assertThat(output).contains("File not found");
    }

    @Test
    @DisplayName("Should show verbose output")
    void testValidateVerbose() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("validate", validSpecPath.toString(), "-v");

        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    @DisplayName("Should display help for validate command")
    void testValidateHelp() {
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(new ChangelogHubCli());
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("validate", "--help");

        assertThat(exitCode).isEqualTo(0);
        String output = sw.toString();
        assertThat(output).contains("validate");
        assertThat(output).contains("--strict");
    }
}
