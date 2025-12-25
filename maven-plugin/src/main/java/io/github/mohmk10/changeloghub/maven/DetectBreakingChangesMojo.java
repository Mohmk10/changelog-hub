package io.github.mohmk10.changeloghub.maven;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.core.reporter.Reporter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Detect breaking changes between two API specifications.
 * Simplified alias for CI/CD integration - fails by default if breaking changes are found.
 *
 * Usage: mvn changelog:detect -Dchangelog.oldSpec=old.yaml -Dchangelog.newSpec=new.yaml
 */
@Mojo(name = "detect", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class DetectBreakingChangesMojo extends AbstractChangelogMojo {

    /**
     * The old (baseline) API specification file.
     */
    @Parameter(property = "changelog.oldSpec", required = true)
    private File oldSpec;

    /**
     * The new API specification file to compare against the baseline.
     */
    @Parameter(property = "changelog.newSpec", required = true)
    private File newSpec;

    /**
     * Fail the build if breaking changes are detected.
     * Default is TRUE for detect goal (unlike compare goal).
     */
    @Parameter(property = "changelog.failOnBreaking", defaultValue = "true")
    private boolean failOnBreaking;

    /**
     * Output file for the breaking changes report.
     * If not specified, output goes to the console.
     */
    @Parameter(property = "changelog.outputFile")
    private File outputFile;

    /**
     * Minimum severity to report. Options: INFO, WARNING, BREAKING
     */
    @Parameter(property = "changelog.minSeverity", defaultValue = "BREAKING")
    private String minSeverity;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping breaking change detection");
            return;
        }

        getLog().info("Detecting breaking changes...");
        logVerbose("Old spec: " + oldSpec.getAbsolutePath());
        logVerbose("New spec: " + newSpec.getAbsolutePath());
        logVerbose("Fail on breaking: " + failOnBreaking);

        // Validate input files
        validateFileExists(oldSpec, "Old API specification");
        validateFileExists(newSpec, "New API specification");

        // Parse specifications
        ApiSpec oldApiSpec = parseSpec(oldSpec, "old");
        ApiSpec newApiSpec = parseSpec(newSpec, "new");

        logVerbose("Old API: " + oldApiSpec.getName() + " v" + oldApiSpec.getVersion());
        logVerbose("New API: " + newApiSpec.getName() + " v" + newApiSpec.getVersion());

        // Generate changelog
        Changelog changelog = getChangelogGenerator().generate(oldApiSpec, newApiSpec);

        // Log results
        logBreakingChanges(changelog);

        // Write output if configured
        if (outputFile != null || !"console".equalsIgnoreCase(format)) {
            writeOutput(changelog);
        }

        // Fail if breaking changes detected
        if (failOnBreaking && !changelog.getBreakingChanges().isEmpty()) {
            String message = buildFailureMessage(changelog);
            throw new MojoFailureException(message);
        }

        if (changelog.getBreakingChanges().isEmpty()) {
            getLog().info("✓ No breaking changes detected!");
        }
    }

    private ApiSpec parseSpec(File file, String label) throws MojoExecutionException {
        try {
            String content = readFile(file);
            return getParser().parse(content);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to parse " + label + " specification: " + e.getMessage(), e);
        }
    }

    private void logBreakingChanges(Changelog changelog) {
        getLog().info("-------------------------------------------");
        getLog().info("Breaking Change Detection Results");
        getLog().info("-------------------------------------------");

        if (changelog.getBreakingChanges().isEmpty()) {
            getLog().info("No breaking changes detected");
        } else {
            getLog().error("Found " + changelog.getBreakingChanges().size() + " breaking change(s):");
            getLog().info("");

            for (BreakingChange bc : changelog.getBreakingChanges()) {
                getLog().error("  ✗ " + bc.getDescription());
                if (bc.getPath() != null) {
                    getLog().error("    Path: " + bc.getPath());
                }
                if (bc.getMigrationSuggestion() != null && verbose) {
                    getLog().info("    Migration: " + bc.getMigrationSuggestion());
                }
            }
        }

        // Show warnings if verbose
        if (verbose) {
            long warningCount = changelog.getChanges().stream()
                .filter(c -> c.getSeverity() == Severity.WARNING)
                .count();
            if (warningCount > 0) {
                getLog().info("");
                getLog().warn("Found " + warningCount + " warning(s):");
                changelog.getChanges().stream()
                    .filter(c -> c.getSeverity() == Severity.WARNING)
                    .forEach(c -> getLog().warn("  ⚠ " + c.getDescription()));
            }
        }

        getLog().info("-------------------------------------------");

        if (changelog.getRiskAssessment() != null) {
            getLog().info("Risk level:       " + changelog.getRiskAssessment().getLevel());
            getLog().info("Semver:           " + changelog.getRiskAssessment().getSemverRecommendation() + " version bump required");
        }

        getLog().info("-------------------------------------------");
    }

    private void writeOutput(Changelog changelog) throws MojoExecutionException {
        Reporter reporter = getReporter();
        String report = reporter.report(changelog);

        if (outputFile != null) {
            writeFile(outputFile, report);
            getLog().info("Report written to: " + outputFile.getAbsolutePath());
        } else {
            ensureOutputDirectory();
            String extension = getFileExtension();
            File output = new File(outputDirectory, "BREAKING-CHANGES" + extension);
            writeFile(output, report);
            getLog().info("Report written to: " + output.getAbsolutePath());
        }
    }

    private String getFileExtension() {
        return switch (format.toLowerCase()) {
            case "markdown" -> ".md";
            case "json" -> ".json";
            case "html" -> ".html";
            default -> ".txt";
        };
    }

    private String buildFailureMessage(Changelog changelog) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║              BREAKING CHANGES DETECTED                       ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n");
        sb.append("\n");
        sb.append("Found ").append(changelog.getBreakingChanges().size()).append(" breaking change(s):\n\n");

        int count = 1;
        for (BreakingChange bc : changelog.getBreakingChanges()) {
            sb.append("  ").append(count++).append(". ").append(bc.getDescription()).append("\n");
            if (bc.getPath() != null) {
                sb.append("     Path: ").append(bc.getPath()).append("\n");
            }
            if (bc.getMigrationSuggestion() != null) {
                sb.append("     Fix:  ").append(bc.getMigrationSuggestion()).append("\n");
            }
            sb.append("\n");
        }

        if (changelog.getRiskAssessment() != null) {
            sb.append("Risk Level: ").append(changelog.getRiskAssessment().getLevel()).append("\n");
            sb.append("Recommended: ").append(changelog.getRiskAssessment().getSemverRecommendation())
              .append(" version bump\n\n");
        }

        sb.append("Set -Dchangelog.failOnBreaking=false to allow breaking changes.\n");

        return sb.toString();
    }

    // Setters for testing
    public void setOldSpec(File oldSpec) {
        this.oldSpec = oldSpec;
    }

    public void setNewSpec(File newSpec) {
        this.newSpec = newSpec;
    }

    public void setFailOnBreaking(boolean failOnBreaking) {
        this.failOnBreaking = failOnBreaking;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setMinSeverity(String minSeverity) {
        this.minSeverity = minSeverity;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
