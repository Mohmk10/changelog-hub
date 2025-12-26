package io.github.mohmk10.changeloghub.maven;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class ValidateMojo extends AbstractChangelogMojo {

    @Parameter(property = "changelog.spec", required = true)
    private File spec;

    @Parameter(property = "changelog.strict", defaultValue = "false")
    private boolean strict;

    @Parameter(property = "changelog.failOnError", defaultValue = "true")
    private boolean failOnError;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping API validation");
            return;
        }

        getLog().info("Validating API specification...");

        if (spec == null) {
            throw new MojoExecutionException("API specification is required");
        }

        logVerbose("Spec: " + spec.getAbsolutePath());
        logVerbose("Strict mode: " + strict);

        validateFileExists(spec, "API specification");

        ValidationResult result = validateSpec(spec);

        logValidationResult(result);

        if (failOnError && result.hasErrors()) {
            throw new MojoFailureException("Validation failed with " + result.getErrorCount() + " error(s). " +
                "Set changelog.failOnError=false to ignore.");
        }

        if (strict && result.hasWarnings()) {
            throw new MojoFailureException("Validation failed with " + result.getWarningCount() + " warning(s) in strict mode. " +
                "Set changelog.strict=false to allow warnings.");
        }

        if (result.isValid()) {
            getLog().info("✓ Validation passed!");
        }
    }

    private ValidationResult validateSpec(File file) throws MojoExecutionException {
        ValidationResult result = new ValidationResult();

        ApiSpec apiSpec;
        try {
            String content = readFile(file);
            apiSpec = getParser().parse(content);
            result.addInfo("Successfully parsed specification");
        } catch (Exception e) {
            result.addError("Failed to parse specification: " + e.getMessage());
            return result;
        }

        if (apiSpec.getName() == null || apiSpec.getName().isBlank()) {
            result.addError("API name is missing or empty");
        } else {
            result.addInfo("API name: " + apiSpec.getName());
        }

        if (apiSpec.getVersion() == null || apiSpec.getVersion().isBlank()) {
            result.addError("API version is missing or empty");
        } else {
            result.addInfo("API version: " + apiSpec.getVersion());
            
            if (!isValidSemVer(apiSpec.getVersion())) {
                result.addWarning("Version '" + apiSpec.getVersion() + "' does not follow semantic versioning (MAJOR.MINOR.PATCH)");
            }
        }

        if (apiSpec.getEndpoints() == null || apiSpec.getEndpoints().isEmpty()) {
            result.addWarning("No endpoints defined in the specification");
        } else {
            result.addInfo("Found " + apiSpec.getEndpoints().size() + " endpoint(s)");

            for (Endpoint endpoint : apiSpec.getEndpoints()) {
                validateEndpoint(endpoint, result);
            }
        }

        long deprecatedCount = apiSpec.getEndpoints().stream()
            .filter(Endpoint::isDeprecated)
            .count();
        if (deprecatedCount > 0) {
            result.addWarning(deprecatedCount + " deprecated endpoint(s) found");
        }

        return result;
    }

    private void validateEndpoint(Endpoint endpoint, ValidationResult result) {
        String methodName = endpoint.getMethod() != null ? endpoint.getMethod().name() : "UNKNOWN";
        String prefix = "[" + methodName + " " + endpoint.getPath() + "] ";

        if (endpoint.getPath() == null || endpoint.getPath().isBlank()) {
            result.addError(prefix + "Path is missing or empty");
        } else if (!endpoint.getPath().startsWith("/")) {
            result.addWarning(prefix + "Path should start with '/'");
        }

        if (endpoint.getMethod() == null) {
            result.addError(prefix + "HTTP method is missing");
        }

        if (strict && (endpoint.getOperationId() == null || endpoint.getOperationId().isBlank())) {
            result.addWarning(prefix + "Missing operationId (recommended for code generation)");
        }

        if (strict && (endpoint.getSummary() == null || endpoint.getSummary().isBlank())) {
            result.addWarning(prefix + "Missing summary (recommended for documentation)");
        }
    }

    private boolean isValidSemVer(String version) {
        return version.matches("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.-]+)?(\\+[a-zA-Z0-9.-]+)?$");
    }

    private void logValidationResult(ValidationResult result) {
        getLog().info("-------------------------------------------");
        getLog().info("Validation Results");
        getLog().info("-------------------------------------------");

        for (String error : result.getErrors()) {
            getLog().error("✗ " + error);
        }
        for (String warning : result.getWarnings()) {
            getLog().warn("⚠ " + warning);
        }
        if (verbose) {
            for (String info : result.getInfos()) {
                getLog().info("ℹ " + info);
            }
        }

        getLog().info("-------------------------------------------");
        getLog().info("Errors:   " + result.getErrorCount());
        getLog().info("Warnings: " + result.getWarningCount());
        getLog().info("-------------------------------------------");
    }

    private static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> infos = new ArrayList<>();

        void addError(String message) {
            errors.add(message);
        }

        void addWarning(String message) {
            warnings.add(message);
        }

        void addInfo(String message) {
            infos.add(message);
        }

        boolean hasErrors() {
            return !errors.isEmpty();
        }

        boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        boolean isValid() {
            return errors.isEmpty();
        }

        int getErrorCount() {
            return errors.size();
        }

        int getWarningCount() {
            return warnings.size();
        }

        List<String> getErrors() {
            return errors;
        }

        List<String> getWarnings() {
            return warnings;
        }

        List<String> getInfos() {
            return infos;
        }
    }

    public void setSpec(File spec) {
        this.spec = spec;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
