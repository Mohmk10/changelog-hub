package io.github.mohmk10.changeloghub.core.service;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.RiskAssessment;

public interface AnalysisService {

    Changelog analyze(ApiSpec oldSpec, ApiSpec newSpec);

    RiskAssessment assessRisk(Changelog changelog);
}
