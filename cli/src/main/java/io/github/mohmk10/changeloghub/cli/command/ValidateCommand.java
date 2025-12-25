package io.github.mohmk10.changeloghub.cli.command;

import io.github.mohmk10.changeloghub.cli.exception.CliException;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.parser.openapi.OpenApiParser;
import io.github.mohmk10.changeloghub.parser.openapi.impl.DefaultOpenApiParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
    name = "validate",
    description = "Validate an API specification file",
    mixinStandardHelpOptions = true,
    footer = {
        "",
        "Examples:",
        "  changelog-hub validate api.yaml",
        "  changelog-hub validate api.yaml --strict"
    }
)
public class ValidateCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "API specification file to validate")
    private File spec;

    @Option(names = {"--strict"},
            description = "Enable strict validation mode with additional checks")
    private boolean strict;

    @Option(names = {"-v", "--verbose"},
            description = "Enable verbose output")
    private boolean verbose;

    private final OpenApiParser parser;

    public ValidateCommand() {
        this.parser = new DefaultOpenApiParser();
    }

    public ValidateCommand(OpenApiParser parser) {
        this.parser = parser;
    }

    @Override
    public Integer call() throws Exception {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (!spec.exists()) {
            System.err.println("Error: File not found: " + spec.getAbsolutePath());
            return 1;
        }

        if (!spec.isFile()) {
            System.err.println("Error: Not a file: " + spec.getAbsolutePath());
            return 1;
        }

        ApiSpec apiSpec;
        try {
            String content = Files.readString(spec.toPath());
            apiSpec = parser.parse(content);
        } catch (IOException e) {
            System.err.println("Error: Failed to read file: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            System.err.println("Error: Failed to parse specification: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }

        validateApiSpec(apiSpec, errors, warnings);

        if (strict) {
            performStrictValidation(apiSpec, errors, warnings);
        }

        printResults(errors, warnings);

        if (!errors.isEmpty()) {
            return 1;
        }

        System.out.println("Validation successful: " + spec.getName());
        return 0;
    }

    private void validateApiSpec(ApiSpec apiSpec, List<String> errors, List<String> warnings) {
        if (apiSpec.getName() == null || apiSpec.getName().isEmpty()) {
            warnings.add("API name is missing");
        }

        if (apiSpec.getVersion() == null || apiSpec.getVersion().isEmpty()) {
            warnings.add("API version is missing");
        }

        if (apiSpec.getEndpoints() == null || apiSpec.getEndpoints().isEmpty()) {
            warnings.add("No endpoints defined in the specification");
        }

        for (Endpoint endpoint : apiSpec.getEndpoints()) {
            validateEndpoint(endpoint, errors, warnings);
        }
    }

    private void validateEndpoint(Endpoint endpoint, List<String> errors, List<String> warnings) {
        String location = endpoint.getMethod() + " " + endpoint.getPath();

        if (endpoint.getPath() == null || endpoint.getPath().isEmpty()) {
            errors.add("Endpoint path is empty");
        }

        if (endpoint.getMethod() == null) {
            errors.add("Endpoint method is null for path: " + endpoint.getPath());
        }

        if (endpoint.getResponses() == null || endpoint.getResponses().isEmpty()) {
            warnings.add("No responses defined for: " + location);
        }

        if (endpoint.getOperationId() == null || endpoint.getOperationId().isEmpty()) {
            if (verbose) {
                warnings.add("Missing operationId for: " + location);
            }
        }
    }

    private void performStrictValidation(ApiSpec apiSpec, List<String> errors, List<String> warnings) {
        for (Endpoint endpoint : apiSpec.getEndpoints()) {
            String location = endpoint.getMethod() + " " + endpoint.getPath();

            if (endpoint.getSummary() == null || endpoint.getSummary().isEmpty()) {
                warnings.add("Missing summary for: " + location);
            }

            if (endpoint.getOperationId() == null || endpoint.getOperationId().isEmpty()) {
                errors.add("Missing operationId for: " + location);
            }

            boolean hasSuccessResponse = endpoint.getResponses().stream()
                .anyMatch(r -> r.getStatusCode().startsWith("2"));
            if (!hasSuccessResponse) {
                warnings.add("No success response (2xx) defined for: " + location);
            }

            if (endpoint.isDeprecated() && endpoint.getSummary() != null
                && !endpoint.getSummary().toLowerCase().contains("deprecated")) {
                warnings.add("Deprecated endpoint without deprecation notice in summary: " + location);
            }
        }

        List<String> operationIds = apiSpec.getEndpoints().stream()
            .map(Endpoint::getOperationId)
            .filter(id -> id != null && !id.isEmpty())
            .toList();

        long uniqueCount = operationIds.stream().distinct().count();
        if (uniqueCount < operationIds.size()) {
            errors.add("Duplicate operationIds found in the specification");
        }
    }

    private void printResults(List<String> errors, List<String> warnings) {
        if (!errors.isEmpty()) {
            System.err.println("\nErrors (" + errors.size() + "):");
            for (String error : errors) {
                System.err.println("  [ERROR] " + error);
            }
        }

        if (!warnings.isEmpty()) {
            System.err.println("\nWarnings (" + warnings.size() + "):");
            for (String warning : warnings) {
                System.err.println("  [WARN] " + warning);
            }
        }

        if (errors.isEmpty() && warnings.isEmpty()) {
            System.out.println("No issues found.");
        }
    }
}
