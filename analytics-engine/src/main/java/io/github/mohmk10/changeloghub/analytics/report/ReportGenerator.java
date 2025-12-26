package io.github.mohmk10.changeloghub.analytics.report;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.util.List;

public interface ReportGenerator {

    ApiEvolutionReport generateEvolutionReport(String apiName, List<Changelog> history);

    TechnicalDebtReport generateDebtReport(ApiSpec spec);

    StabilityReport generateStabilityReport(String apiName, List<Changelog> history);

    RiskTrendReport generateRiskTrendReport(String apiName, List<Changelog> history);

    ComplianceReport generateComplianceReport(ApiSpec spec, List<Changelog> history);
}
