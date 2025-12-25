package io.github.mohmk10.changeloghub.core.service.impl;

import io.github.mohmk10.changeloghub.core.comparator.ApiComparator;
import io.github.mohmk10.changeloghub.core.comparator.impl.DefaultApiComparator;
import io.github.mohmk10.changeloghub.core.detector.BreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.detector.impl.DefaultBreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.RiskAssessment;
import io.github.mohmk10.changeloghub.core.model.RiskLevel;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.core.service.AnalysisService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultAnalysisService implements AnalysisService {

    private final ApiComparator apiComparator;
    private final BreakingChangeDetector breakingChangeDetector;

    public DefaultAnalysisService() {
        this.apiComparator = new DefaultApiComparator();
        this.breakingChangeDetector = new DefaultBreakingChangeDetector();
    }

    public DefaultAnalysisService(ApiComparator apiComparator,
                                   BreakingChangeDetector breakingChangeDetector) {
        this.apiComparator = apiComparator;
        this.breakingChangeDetector = breakingChangeDetector;
    }

    @Override
    public Changelog analyze(ApiSpec oldSpec, ApiSpec newSpec) {
        Changelog changelog = apiComparator.compare(oldSpec, newSpec);
        List<BreakingChange> breakingChanges = breakingChangeDetector.detect(changelog.getChanges());

        changelog.setBreakingChanges(breakingChanges);
        changelog.setRiskAssessment(assessRisk(changelog));

        return changelog;
    }

    @Override
    public RiskAssessment assessRisk(Changelog changelog) {
        if (changelog == null) {
            return createEmptyRiskAssessment();
        }

        List<Change> changes = changelog.getChanges();
        List<BreakingChange> breakingChanges = changelog.getBreakingChanges();

        Map<Severity, Integer> changesBySeverity = countBySeverity(changes);
        int breakingCountFromSeverity = changesBySeverity.getOrDefault(Severity.BREAKING, 0);
        int breakingCountFromList = breakingChanges != null ? breakingChanges.size() : 0;
        int breakingCount = Math.max(breakingCountFromSeverity, breakingCountFromList);
        int totalCount = changes != null ? changes.size() : 0;

        int dangerousCount = changesBySeverity.getOrDefault(Severity.DANGEROUS, 0);
        int warningCount = changesBySeverity.getOrDefault(Severity.WARNING, 0);

        int overallScore = calculateOverallScore(breakingCount, dangerousCount, warningCount);
        RiskLevel level = determineRiskLevel(overallScore);
        String semverRecommendation = determineSemverRecommendation(changesBySeverity, breakingCount);
        String recommendation = generateRecommendation(level, breakingCount, totalCount);

        RiskAssessment assessment = new RiskAssessment();
        assessment.setOverallScore(overallScore);
        assessment.setLevel(level);
        assessment.setBreakingChangesCount(breakingCount);
        assessment.setTotalChangesCount(totalCount);
        assessment.setChangesBySeverity(changesBySeverity);
        assessment.setRecommendation(recommendation);
        assessment.setSemverRecommendation(semverRecommendation);

        return assessment;
    }

    private RiskAssessment createEmptyRiskAssessment() {
        RiskAssessment assessment = new RiskAssessment();
        assessment.setOverallScore(0);
        assessment.setLevel(RiskLevel.LOW);
        assessment.setBreakingChangesCount(0);
        assessment.setTotalChangesCount(0);
        assessment.setRecommendation("No changes detected.");
        assessment.setSemverRecommendation("PATCH");
        return assessment;
    }

    private Map<Severity, Integer> countBySeverity(List<Change> changes) {
        Map<Severity, Integer> counts = new HashMap<>();

        if (changes == null) {
            return counts;
        }

        for (Change change : changes) {
            Severity severity = change.getSeverity();
            if (severity != null) {
                counts.merge(severity, 1, Integer::sum);
            }
        }

        return counts;
    }

    private int calculateOverallScore(int breakingCount, int dangerousCount, int warningCount) {
        int score = (breakingCount * 30) + (dangerousCount * 15) + (warningCount * 5);
        return Math.min(100, score);
    }

    private RiskLevel determineRiskLevel(int score) {
        if (score >= 76) {
            return RiskLevel.CRITICAL;
        } else if (score >= 51) {
            return RiskLevel.HIGH;
        } else if (score >= 26) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    private String determineSemverRecommendation(Map<Severity, Integer> changesBySeverity, int breakingCount) {
        if (breakingCount > 0 || changesBySeverity.getOrDefault(Severity.BREAKING, 0) > 0) {
            return "MAJOR";
        }

        int infoCount = changesBySeverity.getOrDefault(Severity.INFO, 0);
        int warningCount = changesBySeverity.getOrDefault(Severity.WARNING, 0);
        int dangerousCount = changesBySeverity.getOrDefault(Severity.DANGEROUS, 0);

        if (infoCount > 0 || warningCount > 0 || dangerousCount > 0) {
            return "MINOR";
        }

        return "PATCH";
    }

    private String generateRecommendation(RiskLevel level, int breakingCount, int totalCount) {
        switch (level) {
            case CRITICAL:
                return "Critical changes detected. Major version bump required. "
                        + "Extensive testing and migration planning recommended. "
                        + breakingCount + " breaking change(s) out of " + totalCount + " total change(s).";
            case HIGH:
                return "Significant changes detected. Careful review required before deployment. "
                        + "Consider a major version bump. "
                        + breakingCount + " breaking change(s) detected.";
            case MEDIUM:
                return "Moderate changes detected. Review recommended before deployment. "
                        + "Minor version bump suggested.";
            case LOW:
                return "Minor changes detected. Safe to deploy with standard testing. "
                        + "Patch version bump suggested.";
            default:
                return "Review changes before deployment.";
        }
    }
}
