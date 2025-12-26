package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.util.List;

/**
 * Interface for generating analytics reports.
 */
public interface ReportGenerator {

    /**
     * Generate an API evolution report.
     *
     * @param apiName the API name
     * @param history changelog history
     * @return evolution report
     */
    ApiEvolutionReport generateEvolutionReport(String apiName, List<Changelog> history);

    /**
     * Generate a technical debt report.
     *
     * @param spec the API specification
     * @return technical debt report
     */
    TechnicalDebtReport generateDebtReport(ApiSpec spec);

    /**
     * Generate a stability report.
     *
     * @param apiName the API name
     * @param history changelog history
     * @return stability report
     */
    StabilityReport generateStabilityReport(String apiName, List<Changelog> history);

    /**
     * Generate a risk trend report.
     *
     * @param apiName the API name
     * @param history changelog history
     * @return risk trend report
     */
    RiskTrendReport generateRiskTrendReport(String apiName, List<Changelog> history);

    /**
     * Generate a compliance report.
     *
     * @param spec the API specification
     * @param history changelog history
     * @return compliance report
     */
    ComplianceReport generateComplianceReport(ApiSpec spec, List<Changelog> history);
}
