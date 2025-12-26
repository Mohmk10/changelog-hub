package io.github.mohmk10.changeloghub.analytics.metrics;

import io.github.mohmk10.changeloghub.analytics.model.ApiMetrics;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;

public interface MetricsCalculator {

    ApiMetrics calculate(ApiSpec spec);

    int calculateComplexity(ApiSpec spec);

    double calculateDocumentationCoverage(ApiSpec spec);
}
