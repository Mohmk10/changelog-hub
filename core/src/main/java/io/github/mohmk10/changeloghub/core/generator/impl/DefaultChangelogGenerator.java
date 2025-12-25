package io.github.mohmk10.changeloghub.core.generator.impl;

import io.github.mohmk10.changeloghub.core.comparator.ApiComparator;
import io.github.mohmk10.changeloghub.core.comparator.impl.DefaultApiComparator;
import io.github.mohmk10.changeloghub.core.detector.BreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.detector.SeverityClassifier;
import io.github.mohmk10.changeloghub.core.detector.impl.DefaultBreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.detector.impl.DefaultSeverityClassifier;
import io.github.mohmk10.changeloghub.core.generator.ChangelogGenerator;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.RiskAssessment;
import io.github.mohmk10.changeloghub.core.model.RiskLevel;
import io.github.mohmk10.changeloghub.core.model.Severity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultChangelogGenerator implements ChangelogGenerator {

    private final ApiComparator apiComparator;
    private final BreakingChangeDetector breakingChangeDetector;
    private final SeverityClassifier severityClassifier;

    public DefaultChangelogGenerator() {
        this.apiComparator = new DefaultApiComparator();
        this.breakingChangeDetector = new DefaultBreakingChangeDetector();
        this.severityClassifier = new DefaultSeverityClassifier();
    }

    public DefaultChangelogGenerator(ApiComparator apiComparator,
                                      BreakingChangeDetector breakingChangeDetector,
                                      SeverityClassifier severityClassifier) {
        this.apiComparator = apiComparator;
        this.breakingChangeDetector = breakingChangeDetector;
        this.severityClassifier = severityClassifier;
    }

    @Override
    public Changelog generate(ApiSpec oldSpec, ApiSpec newSpec) {
        Changelog changelog = apiComparator.compare(oldSpec, newSpec);

        classifyChanges(changelog.getChanges());

        List<BreakingChange> breakingChanges = breakingChangeDetector.detect(changelog.getChanges());
        changelog.setBreakingChanges(breakingChanges);

        RiskAssessment riskAssessment = calculateRiskAssessment(changelog);
        changelog.setRiskAssessment(riskAssessment);

        return changelog;
    }

    private void classifyChanges(List<Change> changes) {
        if (changes == null) {
            return;
        }

        for (Change change : changes) {
            if (change.getSeverity() == null) {
                Severity severity = severityClassifier.classify(change);
                change.setSeverity(severity);
            }
        }
    }

    private RiskAssessment calculateRiskAssessment(Changelog changelog) {
        List<Change> changes = changelog.getChanges();
        List<BreakingChange> breakingChanges = changelog.getBreakingChanges();

        Map<Severity, Integer> changesBySeverity = countBySeverity(changes);

        int breakingCount = Math.max(
                changesBySeverity.getOrDefault(Severity.BREAKING, 0),
                breakingChanges != null ? breakingChanges.size() : 0
        );
        int dangerousCount = changesBySeverity.getOrDefault(Severity.DANGEROUS, 0);
        int warningCount = changesBySeverity.getOrDefault(Severity.WARNING, 0);
        int totalCount = changes != null ? changes.size() : 0;

        int overallScore = calculateScore(breakingCount, dangerousCount, warningCount);
        RiskLevel level = determineLevel(overallScore);
        String semverRecommendation = determineSemver(changesBySeverity, breakingCount);
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

    private int calculateScore(int breakingCount, int dangerousCount, int warningCount) {
        int score = (breakingCount * 30) + (dangerousCount * 15) + (warningCount * 5);
        return Math.min(100, score);
    }

    private RiskLevel determineLevel(int score) {
        if (score >= 76) return RiskLevel.CRITICAL;
        if (score >= 51) return RiskLevel.HIGH;
        if (score >= 26) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private String determineSemver(Map<Severity, Integer> changesBySeverity, int breakingCount) {
        if (breakingCount > 0 || changesBySeverity.getOrDefault(Severity.BREAKING, 0) > 0) {
            return "MAJOR";
        }
        if (changesBySeverity.getOrDefault(Severity.INFO, 0) > 0 ||
            changesBySeverity.getOrDefault(Severity.WARNING, 0) > 0 ||
            changesBySeverity.getOrDefault(Severity.DANGEROUS, 0) > 0) {
            return "MINOR";
        }
        return "PATCH";
    }

    private String generateRecommendation(RiskLevel level, int breakingCount, int totalCount) {
        switch (level) {
            case CRITICAL:
                return "Critical changes detected. Major version bump required. " +
                        breakingCount + " breaking change(s) out of " + totalCount + " total.";
            case HIGH:
                return "Significant changes detected. Consider a major version bump.";
            case MEDIUM:
                return "Moderate changes detected. Minor version bump suggested.";
            case LOW:
                return "Minor changes detected. Patch version bump suggested.";
            default:
                return "Review changes before deployment.";
        }
    }
}
