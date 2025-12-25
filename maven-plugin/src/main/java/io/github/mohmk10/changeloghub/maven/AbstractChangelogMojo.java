package io.github.mohmk10.changeloghub.maven;

import io.github.mohmk10.changeloghub.core.comparator.ApiComparator;
import io.github.mohmk10.changeloghub.core.comparator.impl.DefaultApiComparator;
import io.github.mohmk10.changeloghub.core.detector.BreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.detector.impl.DefaultBreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.generator.ChangelogGenerator;
import io.github.mohmk10.changeloghub.core.generator.impl.DefaultChangelogGenerator;
import io.github.mohmk10.changeloghub.core.reporter.ReportFormat;
import io.github.mohmk10.changeloghub.core.reporter.Reporter;
import io.github.mohmk10.changeloghub.core.reporter.ReporterFactory;
import io.github.mohmk10.changeloghub.core.service.AnalysisService;
import io.github.mohmk10.changeloghub.core.service.impl.DefaultAnalysisService;
import io.github.mohmk10.changeloghub.parser.openapi.OpenApiParser;
import io.github.mohmk10.changeloghub.parser.openapi.impl.DefaultOpenApiParser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Abstract base class for Changelog Hub Maven Mojos.
 * Provides common parameters and utility methods.
 */
public abstract class AbstractChangelogMojo extends AbstractMojo {

    /**
     * Skip the execution of this plugin.
     */
    @Parameter(property = "changelog.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * Enable verbose output.
     */
    @Parameter(property = "changelog.verbose", defaultValue = "false")
    protected boolean verbose;

    /**
     * Output format: console, markdown, json, html.
     */
    @Parameter(property = "changelog.format", defaultValue = "console")
    protected String format;

    /**
     * Directory to write output files.
     */
    @Parameter(property = "changelog.outputDirectory", defaultValue = "${project.build.directory}/changelog")
    protected File outputDirectory;

    // Lazily initialized services
    private OpenApiParser parser;
    private ApiComparator comparator;
    private BreakingChangeDetector breakingChangeDetector;
    private AnalysisService analysisService;
    private ChangelogGenerator changelogGenerator;

    /**
     * Get the OpenAPI parser instance.
     */
    protected OpenApiParser getParser() {
        if (parser == null) {
            parser = new DefaultOpenApiParser();
        }
        return parser;
    }

    /**
     * Get the API comparator instance.
     */
    protected ApiComparator getComparator() {
        if (comparator == null) {
            comparator = new DefaultApiComparator();
        }
        return comparator;
    }

    /**
     * Get the breaking change detector instance.
     */
    protected BreakingChangeDetector getBreakingChangeDetector() {
        if (breakingChangeDetector == null) {
            breakingChangeDetector = new DefaultBreakingChangeDetector();
        }
        return breakingChangeDetector;
    }

    /**
     * Get the analysis service instance.
     */
    protected AnalysisService getAnalysisService() {
        if (analysisService == null) {
            analysisService = new DefaultAnalysisService();
        }
        return analysisService;
    }

    /**
     * Get the changelog generator instance.
     */
    protected ChangelogGenerator getChangelogGenerator() {
        if (changelogGenerator == null) {
            changelogGenerator = new DefaultChangelogGenerator();
        }
        return changelogGenerator;
    }

    /**
     * Get the reporter for the specified format.
     */
    protected Reporter getReporter() throws MojoExecutionException {
        try {
            ReportFormat reportFormat = ReportFormat.valueOf(format.toUpperCase());
            return ReporterFactory.create(reportFormat);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Invalid format: " + format +
                ". Valid formats: console, markdown, json, html");
        }
    }

    /**
     * Read the content of a file.
     */
    protected String readFile(File file) throws MojoExecutionException {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Write content to a file.
     */
    protected void writeFile(File file, String content) throws MojoExecutionException {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                Files.createDirectories(parent.toPath());
            }
            Files.writeString(file.toPath(), content);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Ensure output directory exists.
     */
    protected void ensureOutputDirectory() throws MojoExecutionException {
        if (outputDirectory != null && !outputDirectory.exists()) {
            try {
                Files.createDirectories(outputDirectory.toPath());
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to create output directory: " +
                    outputDirectory.getAbsolutePath(), e);
            }
        }
    }

    /**
     * Validate that a file exists and is readable.
     */
    protected void validateFileExists(File file, String description) throws MojoExecutionException {
        if (file == null) {
            throw new MojoExecutionException(description + " is required");
        }
        if (!file.exists()) {
            throw new MojoExecutionException(description + " not found: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new MojoExecutionException(description + " is not a file: " + file.getAbsolutePath());
        }
    }

    /**
     * Log a message if verbose mode is enabled.
     */
    protected void logVerbose(String message) {
        if (verbose) {
            getLog().info(message);
        }
    }
}
