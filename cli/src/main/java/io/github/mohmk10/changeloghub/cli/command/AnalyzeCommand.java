package io.github.mohmk10.changeloghub.cli.command;

import io.github.mohmk10.changeloghub.cli.exception.CliException;
import io.github.mohmk10.changeloghub.cli.output.ConsoleOutputHandler;
import io.github.mohmk10.changeloghub.cli.output.FileOutputHandler;
import io.github.mohmk10.changeloghub.cli.output.OutputHandler;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(
    name = "analyze",
    description = "Analyze a single API specification and display statistics",
    mixinStandardHelpOptions = true,
    footer = {
        "",
        "Examples:",
        "  changelog-hub analyze api.yaml",
        "  changelog-hub analyze api.yaml -v",
        "  changelog-hub analyze api.yaml -f json -o stats.json"
    }
)
public class AnalyzeCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "API specification file to analyze")
    private File spec;

    @Option(names = {"-f", "--format"},
            description = "Output format: console, json (default: ${DEFAULT-VALUE})",
            defaultValue = "console")
    private String format;

    @Option(names = {"-o", "--output"},
            description = "Output file (optional)")
    private File outputFile;

    @Option(names = {"-v", "--verbose"},
            description = "Enable verbose output with endpoint details")
    private boolean verbose;

    private final OpenApiParser parser;

    public AnalyzeCommand() {
        this.parser = new DefaultOpenApiParser();
    }

    public AnalyzeCommand(OpenApiParser parser) {
        this.parser = parser;
    }

    @Override
    public Integer call() throws Exception {
        validateInputFile();

        ApiSpec apiSpec = parseFile(spec);

        String output = generateAnalysis(apiSpec);
        writeOutput(output);

        return 0;
    }

    private void validateInputFile() throws CliException {
        if (!spec.exists()) {
            throw new CliException("Spec file not found: " + spec.getAbsolutePath());
        }
        if (!spec.isFile()) {
            throw new CliException("Not a file: " + spec.getAbsolutePath());
        }
    }

    private ApiSpec parseFile(File file) throws CliException {
        try {
            String content = Files.readString(file.toPath());
            return parser.parse(content);
        } catch (IOException e) {
            throw new CliException("Failed to read spec file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CliException("Failed to parse spec file: " + e.getMessage(), e);
        }
    }

    private String generateAnalysis(ApiSpec apiSpec) {
        if ("json".equalsIgnoreCase(format)) {
            return generateJsonAnalysis(apiSpec);
        }
        return generateConsoleAnalysis(apiSpec);
    }

    private String generateConsoleAnalysis(ApiSpec apiSpec) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("API Analysis Report\n");
        sb.append("===================\n\n");

        sb.append("General Information\n");
        sb.append("-------------------\n");
        sb.append("  Name:    ").append(apiSpec.getName()).append("\n");
        sb.append("  Version: ").append(apiSpec.getVersion()).append("\n");
        sb.append("  Type:    ").append(apiSpec.getType()).append("\n");
        sb.append("\n");

        List<Endpoint> endpoints = apiSpec.getEndpoints();
        sb.append("Statistics\n");
        sb.append("----------\n");
        sb.append("  Total Endpoints: ").append(endpoints.size()).append("\n");

        Map<String, Long> methodCounts = endpoints.stream()
            .collect(Collectors.groupingBy(e -> e.getMethod().name(), Collectors.counting()));

        sb.append("  By HTTP Method:\n");
        methodCounts.forEach((method, count) ->
            sb.append("    - ").append(method).append(": ").append(count).append("\n"));

        long totalParams = endpoints.stream()
            .mapToInt(e -> e.getParameters().size())
            .sum();
        sb.append("  Total Parameters: ").append(totalParams).append("\n");

        long endpointsWithBody = endpoints.stream()
            .filter(e -> e.getRequestBody() != null)
            .count();
        sb.append("  Endpoints with Request Body: ").append(endpointsWithBody).append("\n");

        List<Endpoint> deprecatedEndpoints = endpoints.stream()
            .filter(Endpoint::isDeprecated)
            .toList();
        sb.append("  Deprecated Endpoints: ").append(deprecatedEndpoints.size()).append("\n");

        if (!deprecatedEndpoints.isEmpty()) {
            sb.append("\nDeprecated Endpoints\n");
            sb.append("--------------------\n");
            for (Endpoint endpoint : deprecatedEndpoints) {
                sb.append("  - ").append(endpoint.getMethod()).append(" ")
                  .append(endpoint.getPath()).append("\n");
            }
        }

        if (verbose) {
            sb.append("\nEndpoint Details\n");
            sb.append("----------------\n");
            for (Endpoint endpoint : endpoints) {
                sb.append("  ").append(endpoint.getMethod()).append(" ").append(endpoint.getPath());
                if (endpoint.isDeprecated()) {
                    sb.append(" [DEPRECATED]");
                }
                sb.append("\n");
                if (endpoint.getSummary() != null && !endpoint.getSummary().isEmpty()) {
                    sb.append("    Summary: ").append(endpoint.getSummary()).append("\n");
                }
                if (!endpoint.getParameters().isEmpty()) {
                    sb.append("    Parameters: ").append(endpoint.getParameters().size()).append("\n");
                }
                if (endpoint.getRequestBody() != null) {
                    sb.append("    Request Body: ").append(endpoint.getRequestBody().getContentType()).append("\n");
                }
                sb.append("    Responses: ").append(endpoint.getResponses().size()).append("\n");
            }
        }

        return sb.toString();
    }

    private String generateJsonAnalysis(ApiSpec apiSpec) {
        StringBuilder sb = new StringBuilder();
        List<Endpoint> endpoints = apiSpec.getEndpoints();

        Map<String, Long> methodCounts = endpoints.stream()
            .collect(Collectors.groupingBy(e -> e.getMethod().name(), Collectors.counting()));

        List<Endpoint> deprecatedEndpoints = endpoints.stream()
            .filter(Endpoint::isDeprecated)
            .toList();

        long totalParams = endpoints.stream()
            .mapToInt(e -> e.getParameters().size())
            .sum();

        sb.append("{\n");
        sb.append("  \"apiName\": \"").append(escapeJson(apiSpec.getName())).append("\",\n");
        sb.append("  \"version\": \"").append(escapeJson(apiSpec.getVersion())).append("\",\n");
        sb.append("  \"type\": \"").append(apiSpec.getType()).append("\",\n");
        sb.append("  \"statistics\": {\n");
        sb.append("    \"totalEndpoints\": ").append(endpoints.size()).append(",\n");
        sb.append("    \"totalParameters\": ").append(totalParams).append(",\n");
        sb.append("    \"deprecatedEndpoints\": ").append(deprecatedEndpoints.size()).append(",\n");
        sb.append("    \"methodCounts\": {\n");

        int i = 0;
        for (Map.Entry<String, Long> entry : methodCounts.entrySet()) {
            sb.append("      \"").append(entry.getKey()).append("\": ").append(entry.getValue());
            if (i++ < methodCounts.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("    }\n");
        sb.append("  },\n");
        sb.append("  \"deprecatedEndpoints\": [\n");

        for (int j = 0; j < deprecatedEndpoints.size(); j++) {
            Endpoint ep = deprecatedEndpoints.get(j);
            sb.append("    {\n");
            sb.append("      \"method\": \"").append(ep.getMethod()).append("\",\n");
            sb.append("      \"path\": \"").append(escapeJson(ep.getPath())).append("\"\n");
            sb.append("    }");
            if (j < deprecatedEndpoints.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
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
}
