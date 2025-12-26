package io.github.mohmk10.changeloghub.analytics.impl;

import io.github.mohmk10.changeloghub.analytics.AnalyticsService;
import io.github.mohmk10.changeloghub.analytics.aggregator.ComparisonAggregator;
import io.github.mohmk10.changeloghub.analytics.aggregator.HistoryAggregator;
import io.github.mohmk10.changeloghub.analytics.aggregator.MetricsAggregator;
import io.github.mohmk10.changeloghub.analytics.insight.InsightGenerator;
import io.github.mohmk10.changeloghub.analytics.insight.RecommendationEngine;
import io.github.mohmk10.changeloghub.analytics.metrics.ComplexityAnalyzer;
import io.github.mohmk10.changeloghub.analytics.metrics.DefaultMetricsCalculator;
import io.github.mohmk10.changeloghub.analytics.metrics.MetricsCalculator;
import io.github.mohmk10.changeloghub.analytics.metrics.RiskCalculator;
import io.github.mohmk10.changeloghub.analytics.metrics.StabilityScorer;
import io.github.mohmk10.changeloghub.analytics.metrics.TrendAnalyzer;
import io.github.mohmk10.changeloghub.analytics.metrics.VelocityCalculator;
import io.github.mohmk10.changeloghub.analytics.model.*;
import io.github.mohmk10.changeloghub.analytics.report.*;
import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DefaultAnalyticsService implements AnalyticsService {

    private final MetricsCalculator metricsCalculator;
    private final StabilityScorer stabilityScorer;
    private final RiskCalculator riskCalculator;
    private final VelocityCalculator velocityCalculator;
    private final ComplexityAnalyzer complexityAnalyzer;
    private final TrendAnalyzer trendAnalyzer;
    private final InsightGenerator insightGenerator;
    private final RecommendationEngine recommendationEngine;
    private final MetricsAggregator metricsAggregator;
    private final HistoryAggregator historyAggregator;
    private final ComparisonAggregator comparisonAggregator;

    public DefaultAnalyticsService() {
        this.metricsCalculator = new DefaultMetricsCalculator();
        this.stabilityScorer = new StabilityScorer();
        this.riskCalculator = new RiskCalculator();
        this.velocityCalculator = new VelocityCalculator();
        this.complexityAnalyzer = new ComplexityAnalyzer();
        this.trendAnalyzer = new TrendAnalyzer();
        this.insightGenerator = new InsightGenerator();
        this.recommendationEngine = new RecommendationEngine();
        this.metricsAggregator = new MetricsAggregator();
        this.historyAggregator = new HistoryAggregator();
        this.comparisonAggregator = new ComparisonAggregator();
    }

    @Override
    public ApiMetrics calculateMetrics(ApiSpec spec) {
        return metricsCalculator.calculate(spec);
    }

    @Override
    public StabilityScore calculateStability(List<Changelog> history) {
        return stabilityScorer.calculate(history);
    }

    @Override
    public RiskTrend analyzeRiskTrend(List<Changelog> history) {
        return riskCalculator.analyzeTrend(history);
    }

    @Override
    public ChangeVelocity calculateVelocity(List<Changelog> history) {
        return velocityCalculator.calculate(history);
    }

    @Override
    public TechnicalDebt analyzeTechnicalDebt(ApiSpec spec) {
        return complexityAnalyzer.analyzeTechnicalDebt(spec);
    }

    @Override
    public ApiEvolution analyzeEvolution(List<Changelog> history) {
        return historyAggregator.aggregate(history);
    }

    @Override
    public List<Insight> generateInsights(List<Changelog> history) {
        return insightGenerator.generate(null, history);
    }

    @Override
    public List<Recommendation> generateRecommendations(ApiMetrics metrics, StabilityScore stability) {
        return recommendationEngine.generateRecommendations(metrics, stability);
    }

    @Override
    public List<Recommendation> generateDebtRecommendations(TechnicalDebt debt) {
        return recommendationEngine.recommendForHighDebt(debt);
    }

    @Override
    public ApiEvolutionReport generateEvolutionReport(String apiName, List<Changelog> history) {
        ApiEvolution evolution = analyzeEvolution(history);
        StabilityScore stability = calculateStability(history);
        RiskTrend riskTrend = analyzeRiskTrend(history);
        ChangeVelocity velocity = calculateVelocity(history);
        List<Insight> insights = generateInsights(history);
        List<Recommendation> recommendations = generateRecommendations(null, stability);

        LocalDate startDate = history.isEmpty() ? LocalDate.now() :
                history.stream()
                        .map(c -> c.getGeneratedAt().toLocalDate())
                        .min(LocalDate::compareTo)
                        .orElse(LocalDate.now());

        LocalDate endDate = history.isEmpty() ? LocalDate.now() :
                history.stream()
                        .map(c -> c.getGeneratedAt().toLocalDate())
                        .max(LocalDate::compareTo)
                        .orElse(LocalDate.now());

        return ApiEvolutionReport.builder()
                .apiName(apiName)
                .startDate(startDate)
                .endDate(endDate)
                .versions(evolution != null ? evolution.getVersions() : new ArrayList<>())
                .overallStability(stability)
                .riskTrend(riskTrend)
                .velocity(velocity)
                .insights(insights)
                .recommendations(recommendations)
                .build();
    }

    @Override
    public TechnicalDebtReport generateDebtReport(ApiSpec spec) {
        TechnicalDebt debt = analyzeTechnicalDebt(spec);
        List<Recommendation> recommendations = generateDebtRecommendations(debt);

        int priorityScore = debt != null ? calculateDebtPriority(debt) : 0;

        return TechnicalDebtReport.builder()
                .apiName(spec != null ? spec.getName() : "Unknown API")
                .debt(debt)
                .items(debt != null ? debt.getItems() : new ArrayList<>())
                .recommendations(recommendations)
                .priorityScore(priorityScore)
                .build();
    }

    @Override
    public StabilityReport generateStabilityReport(String apiName, List<Changelog> history) {
        StabilityScore currentStability = calculateStability(history);
        List<Insight> insights = generateInsights(history);
        List<Recommendation> recommendations = generateRecommendations(null, currentStability);

        int breakingChangesTotal = history.stream()
                .mapToInt(c -> c.getBreakingChanges().size())
                .sum();

        return StabilityReport.builder()
                .apiName(apiName)
                .currentStability(currentStability)
                .factors(currentStability != null ? currentStability.getFactors() : new ArrayList<>())
                .insights(insights)
                .recommendations(recommendations)
                .versionsAnalyzed(history.size())
                .breakingChangesTotal(breakingChangesTotal)
                .build();
    }

    @Override
    public RiskTrendReport generateRiskTrendReport(String apiName, List<Changelog> history) {
        RiskTrend trend = analyzeRiskTrend(history);
        List<Insight> insights = generateInsights(history);

        List<Recommendation> recommendations = new ArrayList<>();
        if (trend != null && trend.getDirection() == TrendDirection.DEGRADING) {
            recommendations.addAll(recommendationEngine.recommendForPoorStability(calculateStability(history)));
        }

        LocalDate startDate = history.isEmpty() ? LocalDate.now() :
                history.stream()
                        .map(c -> c.getGeneratedAt().toLocalDate())
                        .min(LocalDate::compareTo)
                        .orElse(LocalDate.now());

        LocalDate endDate = history.isEmpty() ? LocalDate.now() :
                history.stream()
                        .map(c -> c.getGeneratedAt().toLocalDate())
                        .max(LocalDate::compareTo)
                        .orElse(LocalDate.now());

        List<RiskTrendReport.RiskDataPoint> dataPoints = new ArrayList<>();
        for (Changelog changelog : history) {
            int riskScore = riskCalculator.calculateRisk(changelog);
            dataPoints.add(new RiskTrendReport.RiskDataPoint(
                    changelog.getGeneratedAt().toLocalDate(),
                    riskScore,
                    changelog.getBreakingChanges().size(),
                    changelog.getToVersion()
            ));
        }

        return RiskTrendReport.builder()
                .apiName(apiName)
                .startDate(startDate)
                .endDate(endDate)
                .overallTrend(trend)
                .dataPoints(dataPoints)
                .periodRisks(trend != null ? trend.getPeriodRisks() : new ArrayList<>())
                .insights(insights)
                .recommendations(recommendations)
                .build();
    }

    @Override
    public ComplianceReport generateComplianceReport(ApiSpec spec, List<Changelog> history) {
        List<ComplianceReport.ComplianceCheck> checks = new ArrayList<>();
        List<ComplianceReport.ComplianceViolation> violations = new ArrayList<>();

        runComplianceChecks(spec, history, checks, violations);

        ComplianceStatus.Status overallStatus = determineComplianceStatus(checks);

        int passedCount = (int) checks.stream()
                .filter(c -> c.getStatus() == ComplianceReport.ComplianceCheck.CheckStatus.PASSED)
                .count();

        ComplianceStatus status = ComplianceStatus.builder()
                .status(overallStatus)
                .complianceScore(checks.isEmpty() ? 100 : (passedCount * 100 / checks.size()))
                .totalChecks(checks.size())
                .passedChecks(passedCount)
                .build();

        List<Recommendation> recommendations = new ArrayList<>();
        if (!violations.isEmpty()) {
            recommendations.add(Recommendation.builder()
                    .type(Recommendation.RecommendationType.COMPLIANCE)
                    .title("Address Compliance Violations")
                    .description(String.format("%d compliance violation(s) detected", violations.size()))
                    .action("Review and fix compliance issues")
                    .priority(8)
                    .effort(5)
                    .impact(7)
                    .build());
        }

        return ComplianceReport.builder()
                .apiName(spec != null ? spec.getName() : "Unknown API")
                .overallStatus(status)
                .checks(checks)
                .violations(violations)
                .recommendations(recommendations)
                .build();
    }

    @Override
    public ApiMetrics aggregateMetrics(List<ApiSpec> specs) {
        
        int totalEndpoints = 0;
        int totalDeprecated = 0;
        double totalDocCoverage = 0;
        int totalComplexity = 0;

        for (ApiSpec spec : specs) {
            ApiMetrics m = calculateMetrics(spec);
            totalEndpoints += m.getTotalEndpoints();
            totalDeprecated += m.getDeprecatedEndpoints();
            totalDocCoverage += m.getDocumentationCoverage();
            totalComplexity += m.getComplexityScore();
        }

        int count = specs.size();
        return ApiMetrics.builder()
                .totalEndpoints(totalEndpoints)
                .deprecatedEndpoints(totalDeprecated)
                .documentationCoverage(count > 0 ? totalDocCoverage / count : 0)
                .complexityScore(count > 0 ? totalComplexity / count : 0)
                .build();
    }

    @Override
    public ApiMetrics compareSpecs(ApiSpec oldSpec, ApiSpec newSpec) {
        ApiMetrics oldMetrics = calculateMetrics(oldSpec);
        ApiMetrics newMetrics = calculateMetrics(newSpec);

        return ApiMetrics.builder()
                .totalEndpoints(newMetrics.getTotalEndpoints() - oldMetrics.getTotalEndpoints())
                .totalChanges(newMetrics.getTotalChanges() - oldMetrics.getTotalChanges())
                .breakingChanges(newMetrics.getBreakingChanges() - oldMetrics.getBreakingChanges())
                .deprecatedEndpoints(newMetrics.getDeprecatedEndpoints() - oldMetrics.getDeprecatedEndpoints())
                .documentationCoverage(newMetrics.getDocumentationCoverage() - oldMetrics.getDocumentationCoverage())
                .complexityScore(newMetrics.getComplexityScore() - oldMetrics.getComplexityScore())
                .build();
    }

    private int calculateDebtPriority(TechnicalDebt debt) {
        int priority = 0;
        priority += debt.getDeprecatedEndpointsCount() * 3;
        priority += debt.getMissingDocumentationCount() * 2;
        priority += debt.getInconsistentNamingCount();
        return Math.min(priority, 100);
    }

    private void runComplianceChecks(ApiSpec spec, List<Changelog> history,
                                      List<ComplianceReport.ComplianceCheck> checks,
                                      List<ComplianceReport.ComplianceViolation> violations) {

        boolean hasVersion = spec != null && spec.getVersion() != null && !spec.getVersion().isEmpty();
        checks.add(new ComplianceReport.ComplianceCheck(
                "API Version",
                "Versioning",
                hasVersion ? ComplianceReport.ComplianceCheck.CheckStatus.PASSED :
                        ComplianceReport.ComplianceCheck.CheckStatus.FAILED,
                hasVersion ? "API version is specified" : "API version is missing"
        ));
        if (!hasVersion) {
            violations.add(new ComplianceReport.ComplianceViolation(
                    "VERSION_REQUIRED",
                    "ERROR",
                    "API Specification",
                    "API version must be specified"
            ));
        }

        boolean hasName = spec != null && spec.getName() != null && !spec.getName().isEmpty();
        checks.add(new ComplianceReport.ComplianceCheck(
                "API Name",
                "Documentation",
                hasName ? ComplianceReport.ComplianceCheck.CheckStatus.PASSED :
                        ComplianceReport.ComplianceCheck.CheckStatus.WARNING,
                hasName ? "API name is provided" : "API name is missing"
        ));

        if (history != null && !history.isEmpty()) {
            boolean hasProperDeprecation = checkDeprecationPolicy(history);
            checks.add(new ComplianceReport.ComplianceCheck(
                    "Deprecation Policy",
                    "Stability",
                    hasProperDeprecation ? ComplianceReport.ComplianceCheck.CheckStatus.PASSED :
                            ComplianceReport.ComplianceCheck.CheckStatus.WARNING,
                    hasProperDeprecation ? "Deprecation policy followed" : "Breaking changes without deprecation detected"
            ));
        }

        boolean followsSemver = checkSemverCompliance(history);
        checks.add(new ComplianceReport.ComplianceCheck(
                "Semantic Versioning",
                "Versioning",
                followsSemver ? ComplianceReport.ComplianceCheck.CheckStatus.PASSED :
                        ComplianceReport.ComplianceCheck.CheckStatus.WARNING,
                followsSemver ? "Semantic versioning is followed" : "Inconsistent version increments detected"
        ));

        if (spec != null && spec.getEndpoints() != null) {
            long documented = spec.getEndpoints().stream()
                    .filter(e -> e.getDescription() != null && !e.getDescription().isEmpty())
                    .count();
            double coverage = spec.getEndpoints().isEmpty() ? 1.0 :
                    (double) documented / spec.getEndpoints().size();

            checks.add(new ComplianceReport.ComplianceCheck(
                    "Endpoint Documentation",
                    "Documentation",
                    coverage >= 0.8 ? ComplianceReport.ComplianceCheck.CheckStatus.PASSED :
                            coverage >= 0.5 ? ComplianceReport.ComplianceCheck.CheckStatus.WARNING :
                                    ComplianceReport.ComplianceCheck.CheckStatus.FAILED,
                    String.format("%.0f%% of endpoints are documented", coverage * 100)
            ));
        }
    }

    private boolean checkDeprecationPolicy(List<Changelog> history) {
        
        for (Changelog changelog : history) {
            if (!changelog.getBreakingChanges().isEmpty()) {
                
                long deprecatedChanges = changelog.getChanges().stream()
                        .filter(c -> c.getType() == ChangeType.DEPRECATED)
                        .count();
                if (deprecatedChanges == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkSemverCompliance(List<Changelog> history) {
        if (history == null || history.size() < 2) {
            return true;
        }

        for (Changelog changelog : history) {
            if (!changelog.getBreakingChanges().isEmpty()) {
                String version = changelog.getToVersion();
                if (version != null && version.startsWith("0.")) {
                    
                    continue;
                }

            }
        }
        return true;
    }

    private ComplianceStatus.Status determineComplianceStatus(List<ComplianceReport.ComplianceCheck> checks) {
        long failed = checks.stream()
                .filter(c -> c.getStatus() == ComplianceReport.ComplianceCheck.CheckStatus.FAILED)
                .count();

        long warnings = checks.stream()
                .filter(c -> c.getStatus() == ComplianceReport.ComplianceCheck.CheckStatus.WARNING)
                .count();

        if (failed > 0) {
            return ComplianceStatus.Status.NON_COMPLIANT;
        } else if (warnings > 0) {
            return ComplianceStatus.Status.PARTIAL;
        }
        return ComplianceStatus.Status.COMPLIANT;
    }
}
