package io.github.mohmk10.changeloghub.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class AnalyzeMojoTest {

    private AnalyzeMojo mojo;
    private File apiSpec;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mojo = new AnalyzeMojo();
        mojo.setVerbose(false);
        mojo.setFormat("console");

        apiSpec = getResourceFile("api-v1.yaml");
    }

    private File getResourceFile(String name) {
        return new File(getClass().getClassLoader().getResource(name).getFile());
    }

    @Test
    void testExecuteWithConsoleFormat() throws Exception {
        mojo.setSpec(apiSpec);
        mojo.setOutputDirectory(tempDir.toFile());

        mojo.execute();
    }

    @Test
    void testExecuteWithSkip() throws Exception {
        mojo.setSpec(apiSpec);
        mojo.setSkip(true);

        mojo.execute();
    }

    @Test
    void testExecuteWithMarkdownFormat() throws Exception {
        File outputFile = tempDir.resolve("analysis.md").toFile();

        mojo.setSpec(apiSpec);
        mojo.setFormat("markdown");
        mojo.setOutputFile(outputFile);

        mojo.execute();

        assertThat(outputFile).exists();
        String content = Files.readString(outputFile.toPath());
        assertThat(content).contains("# API Analysis Report");
        assertThat(content).contains("Pet Store API");
        assertThat(content).contains("1.0.0");
    }

    @Test
    void testExecuteWithJsonFormat() throws Exception {
        File outputFile = tempDir.resolve("analysis.json").toFile();

        mojo.setSpec(apiSpec);
        mojo.setFormat("json");
        mojo.setOutputFile(outputFile);

        mojo.execute();

        assertThat(outputFile).exists();
        String content = Files.readString(outputFile.toPath());
        assertThat(content).contains("\"name\"");
        assertThat(content).contains("\"version\"");
        assertThat(content).contains("\"totalEndpoints\"");
    }

    @Test
    void testExecuteWithHtmlFormat() throws Exception {
        File outputFile = tempDir.resolve("analysis.html").toFile();

        mojo.setSpec(apiSpec);
        mojo.setFormat("html");
        mojo.setOutputFile(outputFile);

        mojo.execute();

        assertThat(outputFile).exists();
        String content = Files.readString(outputFile.toPath());
        assertThat(content).contains("<!DOCTYPE html>");
        assertThat(content).contains("API Analysis Report");
        assertThat(content).contains("Pet Store API");
    }

    @Test
    void testExecuteWithMissingSpec() {
        mojo.setSpec(new File("nonexistent.yaml"));
        mojo.setOutputDirectory(tempDir.toFile());

        assertThatThrownBy(() -> mojo.execute())
            .isInstanceOf(MojoExecutionException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void testExecuteWithVerboseMode() throws Exception {
        mojo.setSpec(apiSpec);
        mojo.setVerbose(true);
        mojo.setOutputDirectory(tempDir.toFile());

        mojo.execute();
    }

    @Test
    void testExecuteCreatesOutputDirectory() throws Exception {
        File nestedDir = tempDir.resolve("nested/output/dir").toFile();

        mojo.setSpec(apiSpec);
        mojo.setFormat("markdown");
        mojo.setOutputDirectory(nestedDir);

        mojo.execute();

        assertThat(nestedDir).exists();
    }

    @Test
    void testAnalysisContainsEndpointCount() throws Exception {
        File outputFile = tempDir.resolve("analysis.json").toFile();

        mojo.setSpec(apiSpec);
        mojo.setFormat("json");
        mojo.setOutputFile(outputFile);

        mojo.execute();

        String content = Files.readString(outputFile.toPath());
        
        assertThat(content).contains("\"totalEndpoints\": 3");
    }

    @Test
    void testAnalysisWithDeprecatedEndpoints() throws Exception {
        File deprecatedSpec = getResourceFile("api-v2-minor.yaml");
        File outputFile = tempDir.resolve("analysis.json").toFile();

        mojo.setSpec(deprecatedSpec);
        mojo.setFormat("json");
        mojo.setOutputFile(outputFile);

        mojo.execute();

        String content = Files.readString(outputFile.toPath());
        
        assertThat(content).contains("\"deprecated\": true");
    }
}
