package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.ApiMetrics;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.Parameter;

public class DefaultMetricsCalculator implements MetricsCalculator {

    private final ComplexityAnalyzer complexityAnalyzer;

    public DefaultMetricsCalculator() {
        this.complexityAnalyzer = new ComplexityAnalyzer();
    }

    @Override
    public ApiMetrics calculate(ApiSpec spec) {
        if (spec == null) {
            return ApiMetrics.builder().build();
        }

        int totalEndpoints = spec.getEndpoints() != null ? spec.getEndpoints().size() : 0;
        int deprecatedEndpoints = 0;
        int totalParameters = 0;
        int totalResponses = 0;

        if (spec.getEndpoints() != null) {
            for (Endpoint endpoint : spec.getEndpoints()) {
                if (endpoint.isDeprecated()) {
                    deprecatedEndpoints++;
                }
                if (endpoint.getParameters() != null) {
                    totalParameters += endpoint.getParameters().size();
                }
                if (endpoint.getResponses() != null) {
                    totalResponses += endpoint.getResponses().size();
                }
            }
        }

        double avgParams = totalEndpoints > 0 ? (double) totalParameters / totalEndpoints : 0;
        double avgResponses = totalEndpoints > 0 ? (double) totalResponses / totalEndpoints : 0;
        double docCoverage = calculateDocumentationCoverage(spec);
        int complexity = calculateComplexity(spec);

        return ApiMetrics.builder()
                .apiName(spec.getName())
                .version(spec.getVersion())
                .totalEndpoints(totalEndpoints)
                .deprecatedEndpoints(deprecatedEndpoints)
                .totalParameters(totalParameters)
                .totalResponses(totalResponses)
                .averageParametersPerEndpoint(avgParams)
                .averageResponseCodesPerEndpoint(avgResponses)
                .documentationCoverage(docCoverage)
                .complexityScore(complexity)
                .build();
    }

    @Override
    public int calculateComplexity(ApiSpec spec) {
        return complexityAnalyzer.analyzeComplexity(spec);
    }

    @Override
    public double calculateDocumentationCoverage(ApiSpec spec) {
        if (spec == null || spec.getEndpoints() == null || spec.getEndpoints().isEmpty()) {
            return 0.0;
        }

        int documented = 0;
        for (Endpoint endpoint : spec.getEndpoints()) {
            if (endpoint.getDescription() != null && !endpoint.getDescription().isEmpty()) {
                documented++;
            }
        }

        return (double) documented / spec.getEndpoints().size();
    }
}
