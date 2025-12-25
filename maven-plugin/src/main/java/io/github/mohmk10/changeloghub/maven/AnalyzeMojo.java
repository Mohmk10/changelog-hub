package io.github.mohmk10.changeloghub.maven;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Analyze a single API specification and generate statistics.
 *
 * Usage: mvn changelog:analyze -Dchangelog.spec=api.yaml
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class AnalyzeMojo extends AbstractChangelogMojo {

    /**
     * The API specification file to analyze.
     */
    @Parameter(property = "changelog.spec", required = true)
    private File spec;

    /**
     * Output file for the analysis report.
     * If not specified, output goes to the console.
     */
    @Parameter(property = "changelog.outputFile")
    private File outputFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping API analysis");
            return;
        }

        getLog().info("Analyzing API specification...");
        logVerbose("Spec: " + spec.getAbsolutePath());

        // Validate input file
        validateFileExists(spec, "API specification");

        // Parse specification
        ApiSpec apiSpec = parseSpec(spec);

        logVerbose("API: " + apiSpec.getName() + " v" + apiSpec.getVersion());

        // Analyze API
        ApiAnalysis analysis = analyzeApi(apiSpec);

        // Generate output
        String report = generateReport(apiSpec, analysis);

        // Write output
        writeOutput(report);

        // Log summary
        logSummary(apiSpec, analysis);
    }

    private ApiSpec parseSpec(File file) throws MojoExecutionException {
        try {
            String content = readFile(file);
            return getParser().parse(content);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to parse specification: " + e.getMessage(), e);
        }
    }

    private ApiAnalysis analyzeApi(ApiSpec apiSpec) {
        ApiAnalysis analysis = new ApiAnalysis();

        if (apiSpec.getEndpoints() != null) {
            analysis.totalEndpoints = apiSpec.getEndpoints().size();

            for (Endpoint endpoint : apiSpec.getEndpoints()) {
                // Count deprecated
                if (endpoint.isDeprecated()) {
                    analysis.deprecatedEndpoints++;
                }

                // Count methods
                String method = endpoint.getMethod().name();
                analysis.methodDistribution.merge(method, 1, Integer::sum);
            }
        }

        return analysis;
    }

    private String generateReport(ApiSpec apiSpec, ApiAnalysis analysis) {
        StringBuilder sb = new StringBuilder();

        if ("json".equalsIgnoreCase(format)) {
            sb.append(generateJsonReport(apiSpec, analysis));
        } else if ("markdown".equalsIgnoreCase(format)) {
            sb.append(generateMarkdownReport(apiSpec, analysis));
        } else if ("html".equalsIgnoreCase(format)) {
            sb.append(generateHtmlReport(apiSpec, analysis));
        } else {
            sb.append(generateConsoleReport(apiSpec, analysis));
        }

        return sb.toString();
    }

    private String generateConsoleReport(ApiSpec apiSpec, ApiAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    API ANALYSIS REPORT                       ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n");
        sb.append("\n");
        sb.append("📋 API Information\n");
        sb.append("   Name:        ").append(apiSpec.getName()).append("\n");
        sb.append("   Version:     ").append(apiSpec.getVersion()).append("\n");
        sb.append("   Description: ").append(getDescription(apiSpec)).append("\n");
        sb.append("\n");
        sb.append("📊 Statistics\n");
        sb.append("   Total Endpoints: ").append(analysis.totalEndpoints).append("\n");
        sb.append("   Deprecated:      ").append(analysis.deprecatedEndpoints).append("\n");
        sb.append("\n");
        sb.append("📈 Methods Distribution\n");
        for (Map.Entry<String, Integer> entry : analysis.methodDistribution.entrySet()) {
            sb.append("   ").append(String.format("%-8s", entry.getKey())).append(": ").append(entry.getValue()).append("\n");
        }

        if (verbose && apiSpec.getEndpoints() != null && !apiSpec.getEndpoints().isEmpty()) {
            sb.append("\n");
            sb.append("📍 Endpoints\n");
            for (Endpoint endpoint : apiSpec.getEndpoints()) {
                String deprecated = endpoint.isDeprecated() ? " [DEPRECATED]" : "";
                sb.append("   ").append(String.format("%-8s", endpoint.getMethod().name()))
                  .append(" ").append(endpoint.getPath()).append(deprecated).append("\n");
            }
        }

        return sb.toString();
    }

    private String generateMarkdownReport(ApiSpec apiSpec, ApiAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("# API Analysis Report\n\n");
        sb.append("## API Information\n\n");
        sb.append("| Property | Value |\n");
        sb.append("|----------|-------|\n");
        sb.append("| Name | ").append(apiSpec.getName()).append(" |\n");
        sb.append("| Version | ").append(apiSpec.getVersion()).append(" |\n");
        sb.append("| Description | ").append(getDescription(apiSpec)).append(" |\n\n");
        sb.append("## Statistics\n\n");
        sb.append("| Metric | Count |\n");
        sb.append("|--------|-------|\n");
        sb.append("| Total Endpoints | ").append(analysis.totalEndpoints).append(" |\n");
        sb.append("| Deprecated | ").append(analysis.deprecatedEndpoints).append(" |\n\n");
        sb.append("## Methods Distribution\n\n");
        sb.append("| Method | Count |\n");
        sb.append("|--------|-------|\n");
        for (Map.Entry<String, Integer> entry : analysis.methodDistribution.entrySet()) {
            sb.append("| ").append(entry.getKey()).append(" | ").append(entry.getValue()).append(" |\n");
        }
        sb.append("\n");

        if (apiSpec.getEndpoints() != null && !apiSpec.getEndpoints().isEmpty()) {
            sb.append("## Endpoints\n\n");
            sb.append("| Method | Path | Deprecated |\n");
            sb.append("|--------|------|------------|\n");
            for (Endpoint endpoint : apiSpec.getEndpoints()) {
                sb.append("| ").append(endpoint.getMethod().name())
                  .append(" | `").append(endpoint.getPath()).append("` | ")
                  .append(endpoint.isDeprecated() ? "Yes" : "No").append(" |\n");
            }
        }

        return sb.toString();
    }

    private String generateJsonReport(ApiSpec apiSpec, ApiAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"api\": {\n");
        sb.append("    \"name\": \"").append(escapeJson(apiSpec.getName())).append("\",\n");
        sb.append("    \"version\": \"").append(escapeJson(apiSpec.getVersion())).append("\",\n");
        String desc = getDescription(apiSpec);
        sb.append("    \"description\": ").append(!"N/A".equals(desc) ?
            "\"" + escapeJson(desc) + "\"" : "null").append("\n");
        sb.append("  },\n");
        sb.append("  \"statistics\": {\n");
        sb.append("    \"totalEndpoints\": ").append(analysis.totalEndpoints).append(",\n");
        sb.append("    \"deprecatedEndpoints\": ").append(analysis.deprecatedEndpoints).append("\n");
        sb.append("  },\n");
        sb.append("  \"methodDistribution\": {\n");
        int i = 0;
        int size = analysis.methodDistribution.size();
        for (Map.Entry<String, Integer> entry : analysis.methodDistribution.entrySet()) {
            sb.append("    \"").append(entry.getKey()).append("\": ").append(entry.getValue());
            sb.append(++i < size ? ",\n" : "\n");
        }
        sb.append("  },\n");
        sb.append("  \"endpoints\": [\n");
        if (apiSpec.getEndpoints() != null) {
            int j = 0;
            int endpointCount = apiSpec.getEndpoints().size();
            for (Endpoint endpoint : apiSpec.getEndpoints()) {
                sb.append("    {\n");
                sb.append("      \"method\": \"").append(endpoint.getMethod().name()).append("\",\n");
                sb.append("      \"path\": \"").append(escapeJson(endpoint.getPath())).append("\",\n");
                sb.append("      \"deprecated\": ").append(endpoint.isDeprecated()).append("\n");
                sb.append("    }");
                sb.append(++j < endpointCount ? ",\n" : "\n");
            }
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateHtmlReport(ApiSpec apiSpec, ApiAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <title>API Analysis - ").append(escapeHtml(apiSpec.getName())).append("</title>\n");
        sb.append("  <style>\n");
        sb.append("    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 40px; background: #f5f5f5; }\n");
        sb.append("    .container { max-width: 900px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        sb.append("    h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n");
        sb.append("    h2 { color: #34495e; margin-top: 30px; }\n");
        sb.append("    table { width: 100%; border-collapse: collapse; margin: 15px 0; }\n");
        sb.append("    th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        sb.append("    th { background: #3498db; color: white; }\n");
        sb.append("    tr:hover { background: #f8f9fa; }\n");
        sb.append("    .method { font-weight: bold; padding: 3px 8px; border-radius: 4px; }\n");
        sb.append("    .GET { background: #61affe; color: white; }\n");
        sb.append("    .POST { background: #49cc90; color: white; }\n");
        sb.append("    .PUT { background: #fca130; color: white; }\n");
        sb.append("    .DELETE { background: #f93e3e; color: white; }\n");
        sb.append("    .PATCH { background: #50e3c2; color: white; }\n");
        sb.append("    .deprecated { color: #e74c3c; font-weight: bold; }\n");
        sb.append("    .stat-card { display: inline-block; padding: 20px; margin: 10px; background: #ecf0f1; border-radius: 8px; text-align: center; min-width: 120px; }\n");
        sb.append("    .stat-value { font-size: 32px; font-weight: bold; color: #2c3e50; }\n");
        sb.append("    .stat-label { color: #7f8c8d; margin-top: 5px; }\n");
        sb.append("  </style>\n");
        sb.append("</head>\n<body>\n");
        sb.append("  <div class=\"container\">\n");
        sb.append("    <h1>📊 API Analysis Report</h1>\n");
        sb.append("    <h2>API Information</h2>\n");
        sb.append("    <table>\n");
        sb.append("      <tr><th>Property</th><th>Value</th></tr>\n");
        sb.append("      <tr><td>Name</td><td>").append(escapeHtml(apiSpec.getName())).append("</td></tr>\n");
        sb.append("      <tr><td>Version</td><td>").append(escapeHtml(apiSpec.getVersion())).append("</td></tr>\n");
        sb.append("      <tr><td>Description</td><td>").append(escapeHtml(getDescription(apiSpec))).append("</td></tr>\n");
        sb.append("    </table>\n");
        sb.append("    <h2>Statistics</h2>\n");
        sb.append("    <div>\n");
        sb.append("      <div class=\"stat-card\"><div class=\"stat-value\">").append(analysis.totalEndpoints).append("</div><div class=\"stat-label\">Total Endpoints</div></div>\n");
        sb.append("      <div class=\"stat-card\"><div class=\"stat-value\">").append(analysis.deprecatedEndpoints).append("</div><div class=\"stat-label\">Deprecated</div></div>\n");
        sb.append("    </div>\n");
        if (apiSpec.getEndpoints() != null && !apiSpec.getEndpoints().isEmpty()) {
            sb.append("    <h2>Endpoints</h2>\n");
            sb.append("    <table>\n");
            sb.append("      <tr><th>Method</th><th>Path</th><th>Status</th></tr>\n");
            for (Endpoint endpoint : apiSpec.getEndpoints()) {
                String methodName = endpoint.getMethod().name();
                sb.append("      <tr><td><span class=\"method ").append(methodName).append("\">")
                  .append(methodName).append("</span></td><td>")
                  .append(escapeHtml(endpoint.getPath())).append("</td><td>")
                  .append(endpoint.isDeprecated() ? "<span class=\"deprecated\">Deprecated</span>" : "Active")
                  .append("</td></tr>\n");
            }
            sb.append("    </table>\n");
        }
        sb.append("  </div>\n");
        sb.append("</body>\n</html>\n");
        return sb.toString();
    }

    private String getDescription(ApiSpec apiSpec) {
        Object desc = apiSpec.getMetadata().get("description");
        return desc != null ? desc.toString() : "N/A";
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    private void writeOutput(String report) throws MojoExecutionException {
        if (outputFile != null) {
            writeFile(outputFile, report);
            getLog().info("Analysis report written to: " + outputFile.getAbsolutePath());
        } else if (!"console".equalsIgnoreCase(format)) {
            ensureOutputDirectory();
            String extension = getFileExtension();
            File output = new File(outputDirectory, "API-ANALYSIS" + extension);
            writeFile(output, report);
            getLog().info("Analysis report written to: " + output.getAbsolutePath());
        } else {
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

    private void logSummary(ApiSpec apiSpec, ApiAnalysis analysis) {
        getLog().info("-------------------------------------------");
        getLog().info("Analysis Summary");
        getLog().info("-------------------------------------------");
        getLog().info("API:              " + apiSpec.getName() + " v" + apiSpec.getVersion());
        getLog().info("Total endpoints:  " + analysis.totalEndpoints);
        getLog().info("Deprecated:       " + analysis.deprecatedEndpoints);
        getLog().info("-------------------------------------------");
    }

    /**
     * Internal class to hold analysis results.
     */
    private static class ApiAnalysis {
        int totalEndpoints = 0;
        int deprecatedEndpoints = 0;
        Map<String, Integer> methodDistribution = new HashMap<>();
    }

    // Setters for testing
    public void setSpec(File spec) {
        this.spec = spec;
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
