package io.github.mohmk10.changeloghub.maven;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
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
 * Compare two API specifications and detect breaking changes.
 *
 * Usage: mvn changelog:compare -Dchangelog.oldSpec=old.yaml -Dchangelog.newSpec=new.yaml
 */
@Mojo(name = "compare", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class CompareMojo extends AbstractChangelogMojo {

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
     */
    @Parameter(property = "changelog.failOnBreaking", defaultValue = "false")
    private boolean failOnBreaking;

    /**
     * Output file for the changelog report.
     * If not specified, output goes to the outputDirectory.
     */
    @Parameter(property = "changelog.outputFile")
    private File outputFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping changelog comparison");
            return;
        }

        getLog().info("Comparing API specifications...");
        logVerbose("Old spec: " + oldSpec.getAbsolutePath());
        logVerbose("New spec: " + newSpec.getAbsolutePath());

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

        // Generate report
        Reporter reporter = getReporter();
        String report = reporter.report(changelog);

        // Write output
        writeOutput(report, changelog);

        // Log summary
        logSummary(changelog);

        // Fail if breaking changes and failOnBreaking is true
        if (failOnBreaking && !changelog.getBreakingChanges().isEmpty()) {
            throw new MojoFailureException("Breaking changes detected! Found " +
                changelog.getBreakingChanges().size() + " breaking change(s). " +
                "Set changelog.failOnBreaking=false to ignore.");
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

    private void writeOutput(String report, Changelog changelog) throws MojoExecutionException {
        if (outputFile != null) {
            writeFile(outputFile, report);
            getLog().info("Changelog written to: " + outputFile.getAbsolutePath());
        } else if (!"console".equalsIgnoreCase(format)) {
            ensureOutputDirectory();
            String extension = getFileExtension();
            File output = new File(outputDirectory, "CHANGELOG" + extension);
            writeFile(output, report);
            getLog().info("Changelog written to: " + output.getAbsolutePath());
        } else {
            // Console output
            getLog().info("");
            getLog().info(report);
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

    private void logSummary(Changelog changelog) {
        int totalChanges = changelog.getChanges().size();
        int breakingCount = changelog.getBreakingChanges().size();
        long warningCount = changelog.getChanges().stream()
            .filter(c -> c.getSeverity() == Severity.WARNING)
            .count();
        long infoCount = changelog.getChanges().stream()
            .filter(c -> c.getSeverity() == Severity.INFO)
            .count();

        getLog().info("-------------------------------------------");
        getLog().info("Changelog Summary");
        getLog().info("-------------------------------------------");
        getLog().info("Total changes:    " + totalChanges);
        getLog().info("Breaking changes: " + breakingCount);
        getLog().info("Warnings:         " + warningCount);
        getLog().info("Additions:        " + infoCount);

        if (changelog.getRiskAssessment() != null) {
            getLog().info("Risk level:       " + changelog.getRiskAssessment().getLevel());
            getLog().info("Recommended:      " + changelog.getRiskAssessment().getSemverRecommendation() + " version bump");
        }
        getLog().info("-------------------------------------------");
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
