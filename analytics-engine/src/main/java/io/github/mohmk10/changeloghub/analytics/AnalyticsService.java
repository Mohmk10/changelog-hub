package io.github.mohmk10.changeloghub.analytics;

import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.report.*;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.util.List;

/**
 * Main service interface for API analytics operations.
 */
public interface AnalyticsService {

    // ========== Metrics Calculation ==========

    /**
     * Calculate metrics for an API specification.
     *
     * @param spec the API specification
     * @return calculated metrics
     */
    ApiMetrics calculateMetrics(ApiSpec spec);

    /**
     * Calculate stability score from changelog history.
     *
     * @param history list of changelogs
     * @return stability score
     */
    StabilityScore calculateStability(List<Changelog> history);

    /**
     * Analyze risk trend from changelog history.
     *
     * @param history list of changelogs
     * @return risk trend analysis
     */
    RiskTrend analyzeRiskTrend(List<Changelog> history);

    /**
     * Calculate change velocity from changelog history.
     *
     * @param history list of changelogs
     * @return change velocity metrics
     */
    ChangeVelocity calculateVelocity(List<Changelog> history);

    /**
     * Analyze technical debt in an API specification.
     *
     * @param spec the API specification
     * @return technical debt analysis
     */
    TechnicalDebt analyzeTechnicalDebt(ApiSpec spec);

    /**
     * Analyze API evolution from changelog history.
     *
     * @param history list of changelogs
     * @return API evolution analysis
     */
    ApiEvolution analyzeEvolution(List<Changelog> history);

    // ========== Insight Generation ==========

    /**
     * Generate insights from changelog history.
     *
     * @param history list of changelogs
     * @return list of insights
     */
    List<Insight> generateInsights(List<Changelog> history);

    /**
     * Generate recommendations based on metrics and stability.
     *
     * @param metrics API metrics
     * @param stability stability score
     * @return list of recommendations
     */
    List<Recommendation> generateRecommendations(ApiMetrics metrics, StabilityScore stability);

    /**
     * Generate recommendations for technical debt.
     *
     * @param debt technical debt analysis
     * @return list of recommendations
     */
    List<Recommendation> generateDebtRecommendations(TechnicalDebt debt);

    // ========== Report Generation ==========

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

    // ========== Aggregation ==========

    /**
     * Aggregate metrics from multiple API specifications.
     *
     * @param specs list of API specifications
     * @return aggregated metrics
     */
    ApiMetrics aggregateMetrics(List<ApiSpec> specs);

    /**
     * Compare two API specifications.
     *
     * @param oldSpec the old specification
     * @param newSpec the new specification
     * @return comparison results as metrics
     */
    ApiMetrics compareSpecs(ApiSpec oldSpec, ApiSpec newSpec);
}
