package io.github.mohmk10.changeloghub.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class CompareMojoTest {

    private CompareMojo mojo;
    private File oldSpec;
    private File newSpec;
    private File newSpecBreaking;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mojo = new CompareMojo();
        mojo.setVerbose(false);
        mojo.setFormat("console");

        oldSpec = getResourceFile("api-v1.yaml");
        newSpec = getResourceFile("api-v2-minor.yaml");
        newSpecBreaking = getResourceFile("api-v2-breaking.yaml");
    }

    private File getResourceFile(String name) {
        return new File(getClass().getClassLoader().getResource(name).getFile());
    }

    @Test
    void testExecuteWithNoBreakingChanges() throws Exception {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpec);
        mojo.setFailOnBreaking(false);
        mojo.setOutputDirectory(tempDir.toFile());

        // Should not throw
        mojo.execute();
    }

    @Test
    void testExecuteWithBreakingChangesNoFail() throws Exception {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setFailOnBreaking(false);
        mojo.setOutputDirectory(tempDir.toFile());

        // Should not throw when failOnBreaking is false
        mojo.execute();
    }

    @Test
    void testExecuteWithBreakingChangesFail() {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setFailOnBreaking(true);
        mojo.setOutputDirectory(tempDir.toFile());

        // Should throw MojoFailureException
        assertThatThrownBy(() -> mojo.execute())
            .isInstanceOf(MojoFailureException.class)
            .hasMessageContaining("Breaking changes detected");
    }

    @Test
    void testExecuteWithSkip() throws Exception {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setFailOnBreaking(true);
        mojo.setSkip(true);

        // Should not throw when skip is true
        mojo.execute();
    }

    @Test
    void testExecuteWithOutputFile() throws Exception {
        File outputFile = tempDir.resolve("changelog.md").toFile();

        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpec);
        mojo.setFailOnBreaking(false);
        mojo.setFormat("markdown");
        mojo.setOutputFile(outputFile);

        mojo.execute();

        assertThat(outputFile).exists();
        String content = Files.readString(outputFile.toPath());
        assertThat(content).contains("Changelog");
    }

    @Test
    void testExecuteWithJsonFormat() throws Exception {
        File outputFile = tempDir.resolve("changelog.json").toFile();

        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpec);
        mojo.setFailOnBreaking(false);
        mojo.setFormat("json");
        mojo.setOutputFile(outputFile);

        mojo.execute();

        assertThat(outputFile).exists();
        String content = Files.readString(outputFile.toPath());
        assertThat(content).contains("{");
        assertThat(content).contains("}");
    }

    @Test
    void testExecuteWithHtmlFormat() throws Exception {
        File outputFile = tempDir.resolve("changelog.html").toFile();

        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpec);
        mojo.setFailOnBreaking(false);
        mojo.setFormat("html");
        mojo.setOutputFile(outputFile);

        mojo.execute();

        assertThat(outputFile).exists();
        String content = Files.readString(outputFile.toPath());
        assertThat(content).contains("<!DOCTYPE html>");
        assertThat(content).contains("</html>");
    }

    @Test
    void testExecuteWithMissingOldSpec() {
        mojo.setOldSpec(new File("nonexistent.yaml"));
        mojo.setNewSpec(newSpec);
        mojo.setOutputDirectory(tempDir.toFile());

        assertThatThrownBy(() -> mojo.execute())
            .isInstanceOf(MojoExecutionException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void testExecuteWithMissingNewSpec() {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(new File("nonexistent.yaml"));
        mojo.setOutputDirectory(tempDir.toFile());

        assertThatThrownBy(() -> mojo.execute())
            .isInstanceOf(MojoExecutionException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void testExecuteWithInvalidFormat() {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpec);
        mojo.setFailOnBreaking(false);
        mojo.setFormat("invalid");
        mojo.setOutputDirectory(tempDir.toFile());

        assertThatThrownBy(() -> mojo.execute())
            .isInstanceOf(MojoExecutionException.class)
            .hasMessageContaining("Invalid format");
    }

    @Test
    void testExecuteWithVerboseMode() throws Exception {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpec);
        mojo.setFailOnBreaking(false);
        mojo.setVerbose(true);
        mojo.setOutputDirectory(tempDir.toFile());

        // Should not throw
        mojo.execute();
    }

    @Test
    void testOutputDirectoryCreation() throws Exception {
        File nestedDir = tempDir.resolve("nested/output/dir").toFile();

        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpec);
        mojo.setFailOnBreaking(false);
        mojo.setFormat("markdown");
        mojo.setOutputDirectory(nestedDir);

        mojo.execute();

        assertThat(nestedDir).exists();
        assertThat(nestedDir.listFiles()).isNotEmpty();
    }
}
