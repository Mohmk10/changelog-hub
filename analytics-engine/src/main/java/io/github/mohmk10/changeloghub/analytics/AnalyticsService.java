package io.github.mohmk10.changeloghub.analytics;

import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.report.*;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.util.List;

public interface AnalyticsService {

    ApiMetrics calculateMetrics(ApiSpec spec);

    StabilityScore calculateStability(List<Changelog> history);

    RiskTrend analyzeRiskTrend(List<Changelog> history);

    ChangeVelocity calculateVelocity(List<Changelog> history);

    TechnicalDebt analyzeTechnicalDebt(ApiSpec spec);

    ApiEvolution analyzeEvolution(List<Changelog> history);

    List<Insight> generateInsights(List<Changelog> history);

    List<Recommendation> generateRecommendations(ApiMetrics metrics, StabilityScore stability);

    List<Recommendation> generateDebtRecommendations(TechnicalDebt debt);

    ApiEvolutionReport generateEvolutionReport(String apiName, List<Changelog> history);

    TechnicalDebtReport generateDebtReport(ApiSpec spec);

    StabilityReport generateStabilityReport(String apiName, List<Changelog> history);

    RiskTrendReport generateRiskTrendReport(String apiName, List<Changelog> history);

    ComplianceReport generateComplianceReport(ApiSpec spec, List<Changelog> history);

    ApiMetrics aggregateMetrics(List<ApiSpec> specs);

    ApiMetrics compareSpecs(ApiSpec oldSpec, ApiSpec newSpec);
}
