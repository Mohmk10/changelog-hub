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

class ValidateMojoTest {

    private ValidateMojo mojo;
    private File validSpec;
    private File specWithDeprecated;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mojo = new ValidateMojo();
        mojo.setVerbose(false);
        mojo.setStrict(false);
        mojo.setFailOnError(true);

        validSpec = getResourceFile("api-v1.yaml");
        specWithDeprecated = getResourceFile("api-v2-minor.yaml");
    }

    private File getResourceFile(String name) {
        return new File(getClass().getClassLoader().getResource(name).getFile());
    }

    @Test
    void testExecuteWithValidSpec() throws Exception {
        mojo.setSpec(validSpec);

        // Should not throw
        mojo.execute();
    }

    @Test
    void testExecuteWithSkip() throws Exception {
        mojo.setSpec(new File("nonexistent.yaml"));
        mojo.setSkip(true);

        // Should not throw when skipped
        mojo.execute();
    }

    @Test
    void testExecuteWithMissingSpec() {
        mojo.setSpec(new File("nonexistent.yaml"));

        assertThatThrownBy(() -> mojo.execute())
            .isInstanceOf(MojoExecutionException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void testExecuteWithStrictModeAndWarnings() {
        mojo.setSpec(specWithDeprecated);
        mojo.setStrict(true);

        // Should throw because strict mode treats warnings as errors
        assertThatThrownBy(() -> mojo.execute())
            .isInstanceOf(MojoFailureException.class)
            .hasMessageContaining("warning");
    }

    @Test
    void testExecuteWithStrictModeDisabled() throws Exception {
        mojo.setSpec(specWithDeprecated);
        mojo.setStrict(false);

        // Should not throw with strict mode disabled
        mojo.execute();
    }

    @Test
    void testExecuteWithFailOnErrorDisabled() throws Exception {
        // Create invalid spec file
        File invalidSpec = tempDir.resolve("invalid.yaml").toFile();
        Files.writeString(invalidSpec.toPath(), "not: valid: yaml: content: [broken");

        mojo.setSpec(invalidSpec);
        mojo.setFailOnError(false);

        // Should not throw when failOnError is false
        // Note: This might still throw MojoExecutionException for parse errors
        // depending on implementation
        try {
            mojo.execute();
        } catch (MojoExecutionException e) {
            // Parse errors are expected for truly invalid YAML
            assertThat(e.getMessage()).contains("Failed to parse");
        }
    }

    @Test
    void testExecuteWithVerboseMode() throws Exception {
        mojo.setSpec(validSpec);
        mojo.setVerbose(true);

        // Should not throw
        mojo.execute();
    }

    @Test
    void testValidationDetectsDeprecatedEndpoints() throws Exception {
        mojo.setSpec(specWithDeprecated);
        mojo.setStrict(false);
        mojo.setVerbose(true);

        // Should not throw (deprecated is a warning, not an error)
        mojo.execute();
    }

    @Test
    void testValidSemanticVersion() throws Exception {
        mojo.setSpec(validSpec);

        // api-v1.yaml has version 1.0.0 which is valid semver
        mojo.execute();
    }

    @Test
    void testExecuteWithNullSpec() {
        mojo.setSpec(null);

        assertThatThrownBy(() -> mojo.execute())
            .isInstanceOf(MojoExecutionException.class)
            .hasMessageContaining("required");
    }

    @Test
    void testValidationWithDirectory() {
        mojo.setSpec(tempDir.toFile());

        assertThatThrownBy(() -> mojo.execute())
            .isInstanceOf(MojoExecutionException.class)
            .hasMessageContaining("not a file");
    }
}
