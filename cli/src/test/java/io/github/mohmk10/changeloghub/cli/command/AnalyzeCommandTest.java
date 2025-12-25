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
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AnalyzeCommand Tests")
class AnalyzeCommandTest {

    @TempDir
    Path tempDir;

    private Path specPath;
    private Path specWithDeprecatedPath;

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() throws IOException {
        specPath = tempDir.resolve("api-v1.yaml");
        specWithDeprecatedPath = tempDir.resolve("api-v2-minor.yaml");

        copyTestResource("api-v1.yaml", specPath);
        copyTestResource("api-v2-minor.yaml", specWithDeprecatedPath);

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
    @DisplayName("Should analyze valid spec file")
    void testAnalyzeValidSpec() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("analyze", specPath.toString());

        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString();
        assertThat(output).contains("API Analysis Report");
        assertThat(output).contains("User API");
        assertThat(output).contains("1.0.0");
        assertThat(output).contains("Total Endpoints:");
    }

    @Test
    @DisplayName("Should show deprecated endpoints")
    void testAnalyzeWithDeprecatedEndpoints() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("analyze", specWithDeprecatedPath.toString());

        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString();
        assertThat(output).contains("Deprecated Endpoints:");
    }

    @Test
    @DisplayName("Should fail for invalid file")
    void testAnalyzeInvalidFile() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("analyze", "nonexistent.yaml");

        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    @DisplayName("Should output JSON format")
    void testAnalyzeJsonFormat() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("analyze", specPath.toString(), "-f", "json");

        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString();
        assertThat(output).contains("\"apiName\"");
        assertThat(output).contains("\"statistics\"");
        assertThat(output).contains("\"totalEndpoints\"");
    }

    @Test
    @DisplayName("Should show verbose output with endpoint details")
    void testAnalyzeVerbose() {
        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("analyze", specPath.toString(), "-v");

        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString();
        assertThat(output).contains("Endpoint Details");
    }

    @Test
    @DisplayName("Should write output to file")
    void testAnalyzeWithOutputFile() throws IOException {
        Path outputPath = tempDir.resolve("analysis.json");

        CommandLine cmd = new CommandLine(new ChangelogHubCli());

        int exitCode = cmd.execute("analyze",
            specPath.toString(),
            "-f", "json",
            "-o", outputPath.toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(Files.exists(outputPath)).isTrue();
        String content = Files.readString(outputPath);
        assertThat(content).contains("\"apiName\"");
    }
}
