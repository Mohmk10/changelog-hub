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

public abstract class AbstractChangelogMojo extends AbstractMojo {

    @Parameter(property = "changelog.skip", defaultValue = "false")
    protected boolean skip;

    @Parameter(property = "changelog.verbose", defaultValue = "false")
    protected boolean verbose;

    @Parameter(property = "changelog.format", defaultValue = "console")
    protected String format;

    @Parameter(property = "changelog.outputDirectory", defaultValue = "${project.build.directory}/changelog")
    protected File outputDirectory;

    private OpenApiParser parser;
    private ApiComparator comparator;
    private BreakingChangeDetector breakingChangeDetector;
    private AnalysisService analysisService;
    private ChangelogGenerator changelogGenerator;

    protected OpenApiParser getParser() {
        if (parser == null) {
            parser = new DefaultOpenApiParser();
        }
        return parser;
    }

    protected ApiComparator getComparator() {
        if (comparator == null) {
            comparator = new DefaultApiComparator();
        }
        return comparator;
    }

    protected BreakingChangeDetector getBreakingChangeDetector() {
        if (breakingChangeDetector == null) {
            breakingChangeDetector = new DefaultBreakingChangeDetector();
        }
        return breakingChangeDetector;
    }

    protected AnalysisService getAnalysisService() {
        if (analysisService == null) {
            analysisService = new DefaultAnalysisService();
        }
        return analysisService;
    }

    protected ChangelogGenerator getChangelogGenerator() {
        if (changelogGenerator == null) {
            changelogGenerator = new DefaultChangelogGenerator();
        }
        return changelogGenerator;
    }

    protected Reporter getReporter() throws MojoExecutionException {
        try {
            ReportFormat reportFormat = ReportFormat.valueOf(format.toUpperCase());
            return ReporterFactory.create(reportFormat);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Invalid format: " + format +
                ". Valid formats: console, markdown, json, html");
        }
    }

    protected String readFile(File file) throws MojoExecutionException {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read file: " + file.getAbsolutePath(), e);
        }
    }

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

    protected void logVerbose(String message) {
        if (verbose) {
            getLog().info(message);
        }
    }
}
