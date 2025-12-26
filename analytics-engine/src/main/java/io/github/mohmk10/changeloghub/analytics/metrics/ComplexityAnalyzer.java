package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.util.AnalyticsConstants;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.Parameter;

/**
 * Analyzes the complexity of API specifications.
 */
public class ComplexityAnalyzer {

    /**
     * Analyze the overall complexity of an API specification.
     *
     * @param spec the API specification
     * @return complexity score (0-100)
     */
    public int analyzeComplexity(ApiSpec spec) {
        if (spec == null || spec.getEndpoints() == null || spec.getEndpoints().isEmpty()) {
            return 0;
        }

        int endpointComplexity = calculateEndpointComplexity(spec);
        int parameterComplexity = calculateParameterComplexity(spec);
        int responseComplexity = calculateResponseComplexity(spec);
        int schemaComplexity = calculateSchemaComplexity(spec);

        // Weighted average
        double totalComplexity = (endpointComplexity * 0.3) +
                (parameterComplexity * 0.25) +
                (responseComplexity * 0.2) +
                (schemaComplexity * 0.25);

        return Math.min(100, (int) Math.round(totalComplexity));
    }

    /**
     * Calculate endpoint count complexity.
     *
     * @param spec the API specification
     * @return complexity score for endpoints
     */
    public int calculateEndpointComplexity(ApiSpec spec) {
        if (spec == null || spec.getEndpoints() == null) {
            return 0;
        }

        int count = spec.getEndpoints().size();

        // Simple scaling: more endpoints = higher complexity
        // 0-10 endpoints: 0-30
        // 10-50 endpoints: 30-60
        // 50+ endpoints: 60-100
        if (count <= 10) {
            return count * 3;
        } else if (count <= 50) {
            return 30 + ((count - 10) * 30 / 40);
        } else {
            return Math.min(100, 60 + ((count - 50) / 2));
        }
    }

    /**
     * Calculate parameter complexity.
     *
     * @param spec the API specification
     * @return complexity score for parameters
     */
    public int calculateParameterComplexity(ApiSpec spec) {
        if (spec == null || spec.getEndpoints() == null) {
            return 0;
        }

        int totalParams = 0;
        int requiredParams = 0;
        int maxParamsPerEndpoint = 0;

        for (Endpoint endpoint : spec.getEndpoints()) {
            if (endpoint.getParameters() != null) {
                int paramCount = endpoint.getParameters().size();
                totalParams += paramCount;
                maxParamsPerEndpoint = Math.max(maxParamsPerEndpoint, paramCount);

                for (Parameter param : endpoint.getParameters()) {
                    if (param.isRequired()) {
                        requiredParams++;
                    }
                }
            }
        }

        int endpointCount = spec.getEndpoints().size();
        double avgParams = endpointCount > 0 ? (double) totalParams / endpointCount : 0;

        // Score based on average params and max params
        int avgScore = (int) Math.min(50, avgParams * 10);
        int maxScore = Math.min(30, maxParamsPerEndpoint * 3);
        int requiredScore = totalParams > 0 ?
                (int) ((double) requiredParams / totalParams * 20) : 0;

        return Math.min(100, avgScore + maxScore + requiredScore);
    }

    /**
     * Calculate response complexity.
     *
     * @param spec the API specification
     * @return complexity score for responses
     */
    public int calculateResponseComplexity(ApiSpec spec) {
        if (spec == null || spec.getEndpoints() == null) {
            return 0;
        }

        int totalResponses = 0;
        int uniqueStatusCodes = 0;

        for (Endpoint endpoint : spec.getEndpoints()) {
            if (endpoint.getResponses() != null) {
                totalResponses += endpoint.getResponses().size();
            }
        }

        int endpointCount = spec.getEndpoints().size();
        double avgResponses = endpointCount > 0 ? (double) totalResponses / endpointCount : 0;

        // Score based on average response codes
        // 1-2 responses: low complexity
        // 3-5 responses: medium complexity
        // 5+ responses: high complexity
        if (avgResponses <= 2) {
            return (int) (avgResponses * 15);
        } else if (avgResponses <= 5) {
            return (int) (30 + (avgResponses - 2) * 15);
        } else {
            return Math.min(100, (int) (75 + (avgResponses - 5) * 5));
        }
    }

    /**
     * Calculate schema complexity.
     *
     * @param spec the API specification
     * @return complexity score for schemas
     */
    public int calculateSchemaComplexity(ApiSpec spec) {
        if (spec == null) {
            return 0;
        }

        // Estimate schema complexity from endpoints
        int estimatedSchemas = 0;
        int deepNesting = 0;

        if (spec.getEndpoints() != null) {
            for (Endpoint endpoint : spec.getEndpoints()) {
                // Estimate schemas from responses
                if (endpoint.getResponses() != null) {
                    estimatedSchemas += endpoint.getResponses().size();
                }
                // Estimate from request body
                if (endpoint.getRequestBody() != null) {
                    estimatedSchemas++;
                }
            }
        }

        // Score based on estimated schemas
        int schemaScore = Math.min(50, estimatedSchemas * 2);
        int nestingScore = Math.min(50, deepNesting * 10);

        return Math.min(100, schemaScore + nestingScore);
    }

    /**
     * Get complexity level description.
     *
     * @param score the complexity score
     * @return description of the complexity level
     */
    public String getComplexityLevel(int score) {
        if (score <= 20) return "Simple";
        if (score <= 40) return "Low";
        if (score <= 60) return "Moderate";
        if (score <= 80) return "High";
        return "Very High";
    }

    /**
     * Check if the API is considered complex.
     *
     * @param score the complexity score
     * @return true if complexity is high
     */
    public boolean isComplex(int score) {
        return score > 60;
    }

    /**
     * Analyze technical debt in an API specification.
     *
     * @param spec the API specification
     * @return technical debt analysis
     */
    public io.github.mohmk10.changeloghub.analytics.model.TechnicalDebt analyzeTechnicalDebt(ApiSpec spec) {
        if (spec == null) {
            return io.github.mohmk10.changeloghub.analytics.model.TechnicalDebt.builder().build();
        }

        int deprecatedCount = 0;
        int missingDocCount = 0;
        int inconsistentNaming = 0;

        if (spec.getEndpoints() != null) {
            for (Endpoint endpoint : spec.getEndpoints()) {
                if (endpoint.isDeprecated()) {
                    deprecatedCount++;
                }
                if (endpoint.getDescription() == null || endpoint.getDescription().isEmpty()) {
                    missingDocCount++;
                }
                // Check naming convention (should be lowercase with hyphens)
                String path = endpoint.getPath();
                if (path != null && !path.matches("^[a-z0-9/{}-]*$")) {
                    inconsistentNaming++;
                }
            }
        }

        int debtScore = calculateDebtScore(deprecatedCount, missingDocCount, inconsistentNaming,
                spec.getEndpoints() != null ? spec.getEndpoints().size() : 0);

        return io.github.mohmk10.changeloghub.analytics.model.TechnicalDebt.builder()
                .apiName(spec.getName())
                .deprecatedEndpointsCount(deprecatedCount)
                .missingDocumentationCount(missingDocCount)
                .inconsistentNamingCount(inconsistentNaming)
                .debtScore(debtScore)
                .build();
    }

    private int calculateDebtScore(int deprecated, int missingDoc, int inconsistent, int totalEndpoints) {
        if (totalEndpoints == 0) {
            return 0;
        }
        double ratio = (double) (deprecated + missingDoc + inconsistent) / (totalEndpoints * 3);
        return (int) Math.min(100, ratio * 100);
    }
}
