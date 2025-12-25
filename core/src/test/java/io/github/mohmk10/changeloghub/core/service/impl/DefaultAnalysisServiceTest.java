package io.github.mohmk10.changeloghub.core.service.impl;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.ApiType;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.Endpoint;
import io.github.mohmk10.changeloghub.core.model.HttpMethod;
import io.github.mohmk10.changeloghub.core.model.RiskAssessment;
import io.github.mohmk10.changeloghub.core.model.RiskLevel;
import io.github.mohmk10.changeloghub.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAnalysisServiceTest {

    private DefaultAnalysisService analysisService;

    @BeforeEach
    void setUp() {
        analysisService = new DefaultAnalysisService();
    }

    @Test
    void testAnalyze() {
        Endpoint oldEndpoint = Endpoint.builder()
                .path("/api/users")
                .method(HttpMethod.GET)
                .build();

        ApiSpec oldSpec = ApiSpec.builder()
                .name("Test API")
                .version("1.0.0")
                .type(ApiType.REST)
                .addEndpoint(oldEndpoint)
                .build();

        ApiSpec newSpec = ApiSpec.builder()
                .name("Test API")
                .version("2.0.0")
                .type(ApiType.REST)
                .build();

        Changelog changelog = analysisService.analyze(oldSpec, newSpec);

        assertThat(changelog).isNotNull();
        assertThat(changelog.getChanges()).isNotEmpty();
        assertThat(changelog.getBreakingChanges()).isNotEmpty();
        assertThat(changelog.getRiskAssessment()).isNotNull();
    }

    @Test
    void testAssessRiskLow() {
        Change infoChange = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.INFO)
                .build();

        Changelog changelog = Changelog.builder()
                .changes(Arrays.asList(infoChange))
                .build();

        RiskAssessment assessment = analysisService.assessRisk(changelog);

        assertThat(assessment.getLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(assessment.getOverallScore()).isLessThanOrEqualTo(25);
        assertThat(assessment.getSemverRecommendation()).isEqualTo("MINOR");
    }

    @Test
    void testAssessRiskMedium() {
        Change warningChange1 = Change.builder()
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.WARNING)
                .build();

        Change warningChange2 = Change.builder()
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.WARNING)
                .build();

        Change dangerousChange = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.PARAMETER)
                .severity(Severity.DANGEROUS)
                .build();

        Changelog changelog = Changelog.builder()
                .changes(Arrays.asList(warningChange1, warningChange2, dangerousChange))
                .build();

        RiskAssessment assessment = analysisService.assessRisk(changelog);

        assertThat(assessment.getLevel()).isIn(RiskLevel.MEDIUM, RiskLevel.LOW);
    }

    @Test
    void testAssessRiskHigh() {
        Change breakingChange1 = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .build();

        Change breakingChange2 = Change.builder()
                .type(ChangeType.MODIFIED)
                .category(ChangeCategory.PARAMETER)
                .severity(Severity.BREAKING)
                .build();

        Changelog changelog = Changelog.builder()
                .changes(Arrays.asList(breakingChange1, breakingChange2))
                .breakingChanges(Arrays.asList())
                .build();

        RiskAssessment assessment = analysisService.assessRisk(changelog);

        assertThat(assessment.getOverallScore()).isGreaterThanOrEqualTo(51);
        assertThat(assessment.getLevel()).isIn(RiskLevel.HIGH, RiskLevel.CRITICAL);
        assertThat(assessment.getSemverRecommendation()).isEqualTo("MAJOR");
    }

    @Test
    void testAssessRiskCritical() {
        Change breakingChange1 = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .build();

        Change breakingChange2 = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .build();

        Change breakingChange3 = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .build();

        Change breakingChange4 = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .build();

        Changelog changelog = Changelog.builder()
                .changes(Arrays.asList(breakingChange1, breakingChange2, breakingChange3, breakingChange4))
                .breakingChanges(Arrays.asList())
                .build();

        RiskAssessment assessment = analysisService.assessRisk(changelog);

        assertThat(assessment.getLevel()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(assessment.getOverallScore()).isGreaterThanOrEqualTo(76);
    }

    @Test
    void testSemverRecommendationMajor() {
        Change breakingChange = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .build();

        Changelog changelog = Changelog.builder()
                .changes(Arrays.asList(breakingChange))
                .build();

        RiskAssessment assessment = analysisService.assessRisk(changelog);

        assertThat(assessment.getSemverRecommendation()).isEqualTo("MAJOR");
    }

    @Test
    void testSemverRecommendationMinor() {
        Change infoChange = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.INFO)
                .build();

        Changelog changelog = Changelog.builder()
                .changes(Arrays.asList(infoChange))
                .build();

        RiskAssessment assessment = analysisService.assessRisk(changelog);

        assertThat(assessment.getSemverRecommendation()).isEqualTo("MINOR");
    }

    @Test
    void testSemverRecommendationPatch() {
        Changelog changelog = Changelog.builder()
                .build();

        RiskAssessment assessment = analysisService.assessRisk(changelog);

        assertThat(assessment.getSemverRecommendation()).isEqualTo("PATCH");
    }

    @Test
    void testAssessRiskWithNullChangelog() {
        RiskAssessment assessment = analysisService.assessRisk(null);

        assertThat(assessment).isNotNull();
        assertThat(assessment.getLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(assessment.getOverallScore()).isZero();
    }

    @Test
    void testChangesBySeverityTracking() {
        Change breakingChange = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .build();

        Change warningChange = Change.builder()
                .type(ChangeType.DEPRECATED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.WARNING)
                .build();

        Change infoChange = Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.INFO)
                .build();

        Changelog changelog = Changelog.builder()
                .changes(Arrays.asList(breakingChange, warningChange, infoChange))
                .build();

        RiskAssessment assessment = analysisService.assessRisk(changelog);

        assertThat(assessment.getChangesBySeverity()).containsKey(Severity.BREAKING);
        assertThat(assessment.getChangesBySeverity()).containsKey(Severity.WARNING);
        assertThat(assessment.getChangesBySeverity()).containsKey(Severity.INFO);
        assertThat(assessment.getTotalChangesCount()).isEqualTo(3);
    }

    @Test
    void testRecommendationGeneration() {
        Change breakingChange = Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.ENDPOINT)
                .severity(Severity.BREAKING)
                .build();

        Changelog changelog = Changelog.builder()
                .changes(Arrays.asList(breakingChange))
                .build();

        RiskAssessment assessment = analysisService.assessRisk(changelog);

        assertThat(assessment.getRecommendation()).isNotNull();
        assertThat(assessment.getRecommendation()).isNotEmpty();
    }
}
