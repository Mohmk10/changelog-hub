package io.github.mohmk10.changeloghub.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChangelogHubCli Tests")
class ChangelogHubCliTest {

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
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

    @Test
    @DisplayName("Should display help message")
    void testMainHelp() {
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(new ChangelogHubCli());
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("--help");

        assertThat(exitCode).isEqualTo(0);
        String output = sw.toString();
        assertThat(output).contains("changelog-hub");
        assertThat(output).contains("API Breaking Change Detector");
        assertThat(output).contains("compare");
        assertThat(output).contains("analyze");
        assertThat(output).contains("validate");
        assertThat(output).contains("version");
    }

    @Test
    @DisplayName("Should display version")
    void testMainVersion() {
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(new ChangelogHubCli());
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("--version");

        assertThat(exitCode).isEqualTo(0);
        String output = sw.toString();
        assertThat(output).contains("1.0.0-SNAPSHOT");
    }

    @Test
    @DisplayName("Should display subcommand help")
    void testSubcommandHelp() {
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(new ChangelogHubCli());
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("compare", "--help");

        assertThat(exitCode).isEqualTo(0);
        String output = sw.toString();
        assertThat(output).contains("compare");
        assertThat(output).contains("Compare two API specifications");
        assertThat(output).contains("--format");
        assertThat(output).contains("--output");
        assertThat(output).contains("--fail-on-breaking");
    }

    @Test
    @DisplayName("Should show banner when requested")
    void testBanner() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("--banner");

        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString();
        assertThat(output).contains("_____");
    }

    @Test
    @DisplayName("Should return error for unknown command")
    void testUnknownCommand() {
        StringWriter swErr = new StringWriter();
        CommandLine cmd = new CommandLine(new ChangelogHubCli());
        cmd.setErr(new PrintWriter(swErr));

        int exitCode = cmd.execute("unknown");

        assertThat(exitCode).isNotEqualTo(0);
    }
}
