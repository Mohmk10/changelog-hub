package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.ApiMetrics;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;

/**
 * Interface for calculating API metrics.
 */
public interface MetricsCalculator {

    /**
     * Calculate comprehensive metrics for an API specification.
     *
     * @param spec the API specification to analyze
     * @return calculated metrics
     */
    ApiMetrics calculate(ApiSpec spec);

    /**
     * Calculate complexity score for an API.
     *
     * @param spec the API specification
     * @return complexity score (0-100)
     */
    int calculateComplexity(ApiSpec spec);

    /**
     * Calculate documentation coverage.
     *
     * @param spec the API specification
     * @return coverage percentage (0.0-1.0)
     */
    double calculateDocumentationCoverage(ApiSpec spec);
}
