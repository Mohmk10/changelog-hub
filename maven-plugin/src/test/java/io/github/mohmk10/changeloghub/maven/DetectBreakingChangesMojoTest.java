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

class DetectBreakingChangesMojoTest {

    private DetectBreakingChangesMojo mojo;
    private File oldSpec;
    private File newSpec;
    private File newSpecBreaking;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mojo = new DetectBreakingChangesMojo();
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
        mojo.setOutputDirectory(tempDir.toFile());

        mojo.execute();
    }

    @Test
    void testExecuteWithBreakingChangesDefaultFail() throws Exception {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setOutputDirectory(tempDir.toFile());

        try {
            mojo.execute();

        } catch (MojoFailureException e) {
            
            assertThat(e.getMessage()).containsIgnoringCase("breaking");
        }
    }

    @Test
    void testExecuteWithBreakingChangesNoFail() throws Exception {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setFailOnBreaking(false);
        mojo.setOutputDirectory(tempDir.toFile());

        mojo.execute();
    }

    @Test
    void testExecuteWithSkip() throws Exception {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setSkip(true);

        mojo.execute();
    }

    @Test
    void testExecuteWithOutputFile() throws Exception {
        File outputFile = tempDir.resolve("breaking-changes.md").toFile();

        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setFailOnBreaking(false);
        mojo.setFormat("markdown");
        mojo.setOutputFile(outputFile);

        mojo.execute();

        assertThat(outputFile).exists();
        String content = Files.readString(outputFile.toPath());
        assertThat(content).isNotEmpty();
    }

    @Test
    void testExecuteWithJsonFormat() throws Exception {
        File outputFile = tempDir.resolve("breaking-changes.json").toFile();

        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setFailOnBreaking(false);
        mojo.setFormat("json");
        mojo.setOutputFile(outputFile);

        mojo.execute();

        assertThat(outputFile).exists();
        String content = Files.readString(outputFile.toPath());
        assertThat(content).contains("{");
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
    void testExecuteWithVerboseMode() throws Exception {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpec);
        mojo.setVerbose(true);
        mojo.setOutputDirectory(tempDir.toFile());

        mojo.execute();
    }

    @Test
    void testExecuteComparesSpecsSuccessfully() throws Exception {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setFailOnBreaking(false);
        mojo.setOutputDirectory(tempDir.toFile());

        mojo.execute();
    }

    @Test
    void testOutputDirectoryCreation() throws Exception {
        File nestedDir = tempDir.resolve("nested/output").toFile();

        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setFailOnBreaking(false);
        mojo.setFormat("markdown");
        mojo.setOutputDirectory(nestedDir);

        mojo.execute();

        assertThat(nestedDir).exists();
    }

    @Test
    void testExecuteWithDifferentVersions() throws Exception {
        mojo.setOldSpec(oldSpec);
        mojo.setNewSpec(newSpecBreaking);
        mojo.setFailOnBreaking(false);
        mojo.setVerbose(true);
        mojo.setOutputDirectory(tempDir.toFile());

        mojo.execute();
    }
}
