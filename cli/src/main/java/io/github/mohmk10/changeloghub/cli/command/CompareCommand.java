package io.github.mohmk10.changeloghub.cli.command;

import io.github.mohmk10.changeloghub.cli.exception.CliException;
import io.github.mohmk10.changeloghub.cli.output.ConsoleOutputHandler;
import io.github.mohmk10.changeloghub.cli.output.FileOutputHandler;
import io.github.mohmk10.changeloghub.cli.output.OutputHandler;
import io.github.mohmk10.changeloghub.core.generator.ChangelogGenerator;
import io.github.mohmk10.changeloghub.core.generator.impl.DefaultChangelogGenerator;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.reporter.ReportFormat;
import io.github.mohmk10.changeloghub.core.reporter.Reporter;
import io.github.mohmk10.changeloghub.core.reporter.ReporterFactory;
import io.github.mohmk10.changeloghub.parser.openapi.OpenApiParser;
import io.github.mohmk10.changeloghub.parser.openapi.impl.DefaultOpenApiParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;

@Command(
    name = "compare",
    description = "Compare two API specifications and generate a changelog report",
    mixinStandardHelpOptions = true,
    footer = {
        "",
        "Examples:",
        "  changelog-hub compare api-v1.yaml api-v2.yaml",
        "  changelog-hub compare old.yaml new.yaml -f markdown",
        "  changelog-hub compare old.yaml new.yaml -f json -o changelog.json",
        "  changelog-hub compare old.yaml new.yaml --fail-on-breaking"
    }
)
public class CompareCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Old API specification file (baseline)")
    private File oldSpec;

    @Parameters(index = "1", description = "New API specification file (to compare)")
    private File newSpec;

    @Option(names = {"-f", "--format"},
            description = "Output format: console, markdown, json, html (default: ${DEFAULT-VALUE})",
            defaultValue = "console")
    private String format;

    @Option(names = {"-o", "--output"},
            description = "Output file (optional, defaults to stdout)")
    private File outputFile;

    @Option(names = {"--fail-on-breaking"},
            description = "Exit with error code 1 if breaking changes are found")
    private boolean failOnBreaking;

    @Option(names = {"-v", "--verbose"},
            description = "Enable verbose output")
    private boolean verbose;

    @Option(names = {"-q", "--quiet"},
            description = "Quiet mode, minimal output")
    private boolean quiet;

    private final OpenApiParser parser;
    private final ChangelogGenerator changelogGenerator;

    public CompareCommand() {
        this.parser = new DefaultOpenApiParser();
        this.changelogGenerator = new DefaultChangelogGenerator();
    }

    public CompareCommand(OpenApiParser parser, ChangelogGenerator changelogGenerator) {
        this.parser = parser;
        this.changelogGenerator = changelogGenerator;
    }

    @Override
    public Integer call() throws Exception {
        validateInputFiles();

        if (verbose && !quiet) {
            System.err.println("Comparing: " + oldSpec.getName() + " -> " + newSpec.getName());
        }

        ApiSpec oldApiSpec = parseFile(oldSpec, "old");
        ApiSpec newApiSpec = parseFile(newSpec, "new");

        if (verbose && !quiet) {
            System.err.println("Old API: " + oldApiSpec.getName() + " v" + oldApiSpec.getVersion());
            System.err.println("New API: " + newApiSpec.getName() + " v" + newApiSpec.getVersion());
        }

        Changelog changelog = changelogGenerator.generate(oldApiSpec, newApiSpec);

        ReportFormat reportFormat = parseFormat(format);
        Reporter reporter = ReporterFactory.create(reportFormat);
        String report = reporter.report(changelog);

        writeOutput(report);

        if (!quiet) {
            printSummary(changelog);
        }

        if (failOnBreaking && !changelog.getBreakingChanges().isEmpty()) {
            if (!quiet) {
                System.err.println("Breaking changes detected! Exiting with error code 1.");
            }
            return 1;
        }

        return 0;
    }

    private void validateInputFiles() throws CliException {
        if (!oldSpec.exists()) {
            throw new CliException("Old spec file not found: " + oldSpec.getAbsolutePath());
        }
        if (!newSpec.exists()) {
            throw new CliException("New spec file not found: " + newSpec.getAbsolutePath());
        }
        if (!oldSpec.isFile()) {
            throw new CliException("Old spec is not a file: " + oldSpec.getAbsolutePath());
        }
        if (!newSpec.isFile()) {
            throw new CliException("New spec is not a file: " + newSpec.getAbsolutePath());
        }
    }

    private ApiSpec parseFile(File file, String label) throws CliException {
        try {
            String content = Files.readString(file.toPath());
            return parser.parse(content);
        } catch (IOException e) {
            throw new CliException("Failed to read " + label + " spec file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CliException("Failed to parse " + label + " spec file: " + e.getMessage(), e);
        }
    }

    private ReportFormat parseFormat(String format) throws CliException {
        try {
            return ReportFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CliException("Invalid format: " + format + ". Valid formats: console, markdown, json, html");
        }
    }

    private void writeOutput(String report) throws CliException {
        try (OutputHandler handler = createOutputHandler()) {
            handler.write(report);
        } catch (IOException e) {
            throw new CliException("Failed to write output: " + e.getMessage(), e);
        }
    }

    private OutputHandler createOutputHandler() throws IOException {
        if (outputFile != null) {
            return new FileOutputHandler(outputFile);
        }
        return new ConsoleOutputHandler();
    }

    private void printSummary(Changelog changelog) {
        int totalChanges = changelog.getChanges().size();
        int breakingCount = changelog.getBreakingChanges().size();
        long warningCount = changelog.getChanges().stream()
            .filter(c -> c.getSeverity() == io.github.mohmk10.changeloghub.core.model.Severity.WARNING)
            .count();
        long infoCount = changelog.getChanges().stream()
            .filter(c -> c.getSeverity() == io.github.mohmk10.changeloghub.core.model.Severity.INFO)
            .count();

        System.err.println();
        System.err.println("Summary: " + totalChanges + " changes detected");
        System.err.println("  - Breaking: " + breakingCount);
        System.err.println("  - Warnings: " + warningCount);
        System.err.println("  - Info: " + infoCount);

        if (changelog.getRiskAssessment() != null) {
            System.err.println("  - Risk Level: " + changelog.getRiskAssessment().getLevel());
            System.err.println("  - Recommended: " + changelog.getRiskAssessment().getSemverRecommendation() + " version bump");
        }
    }
}
