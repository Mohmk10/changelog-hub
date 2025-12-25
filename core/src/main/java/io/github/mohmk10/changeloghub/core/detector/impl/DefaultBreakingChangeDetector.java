package io.github.mohmk10.changeloghub.core.detector.impl;

import io.github.mohmk10.changeloghub.core.detector.BreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.detector.SeverityClassifier;
import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultBreakingChangeDetector implements BreakingChangeDetector {

    private final SeverityClassifier severityClassifier;

    public DefaultBreakingChangeDetector() {
        this.severityClassifier = new DefaultSeverityClassifier();
    }

    public DefaultBreakingChangeDetector(SeverityClassifier severityClassifier) {
        this.severityClassifier = severityClassifier;
    }

    @Override
    public List<BreakingChange> detect(List<Change> changes) {
        if (changes == null) {
            return new ArrayList<>();
        }

        return changes.stream()
                .filter(this::isBreaking)
                .map(this::convertToBreakingChange)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isBreaking(Change change) {
        if (change == null) {
            return false;
        }

        Severity severity = change.getSeverity();
        if (severity == null) {
            severity = severityClassifier.classify(change);
        }

        return severity == Severity.BREAKING || severity == Severity.DANGEROUS;
    }

    private BreakingChange convertToBreakingChange(Change change) {
        int impactScore = calculateImpactScore(change);
        String migrationSuggestion = generateMigrationSuggestion(change);

        return BreakingChange.breakingChangeBuilder()
                .id(change.getId())
                .type(change.getType())
                .category(change.getCategory())
                .severity(change.getSeverity())
                .path(change.getPath())
                .description(change.getDescription())
                .oldValue(change.getOldValue())
                .newValue(change.getNewValue())
                .detectedAt(change.getDetectedAt())
                .impactScore(impactScore)
                .migrationSuggestion(migrationSuggestion)
                .build();
    }

    private int calculateImpactScore(Change change) {
        int baseScore = calculateBaseScore(change);
        int categoryMultiplier = getCategoryMultiplier(change.getCategory());

        int score = (baseScore * categoryMultiplier) / 100;
        return Math.min(100, Math.max(0, score));
    }

    private int calculateBaseScore(Change change) {
        ChangeType type = change.getType();
        Severity severity = change.getSeverity();

        if (type == ChangeType.REMOVED) {
            if (change.getCategory() == ChangeCategory.ENDPOINT) {
                return 100;
            }
            return 80;
        }

        if (type == ChangeType.ADDED && severity == Severity.BREAKING) {
            return 70;
        }

        if (type == ChangeType.MODIFIED) {
            if (severity == Severity.BREAKING) {
                return 85;
            }
            if (severity == Severity.DANGEROUS) {
                return 60;
            }
        }

        return 50;
    }

    private int getCategoryMultiplier(ChangeCategory category) {
        if (category == null) {
            return 100;
        }

        switch (category) {
            case ENDPOINT:
                return 100;
            case REQUEST_BODY:
                return 90;
            case PARAMETER:
                return 80;
            case RESPONSE:
                return 70;
            case SCHEMA:
                return 75;
            case SECURITY:
                return 95;
            default:
                return 100;
        }
    }

    private String generateMigrationSuggestion(Change change) {
        ChangeType type = change.getType();
        ChangeCategory category = change.getCategory();

        if (type == ChangeType.REMOVED) {
            return generateRemovalSuggestion(category, change);
        }

        if (type == ChangeType.ADDED) {
            return generateAdditionSuggestion(category, change);
        }

        if (type == ChangeType.MODIFIED) {
            return generateModificationSuggestion(category, change);
        }

        return "Review the change and update client code accordingly.";
    }

    private String generateRemovalSuggestion(ChangeCategory category, Change change) {
        switch (category) {
            case ENDPOINT:
                return "Update all API consumers to stop using the removed endpoint. "
                        + "Consider using an alternative endpoint if available.";
            case PARAMETER:
                return "Remove the parameter '" + extractName(change.getPath())
                        + "' from all API calls.";
            case RESPONSE:
                return "Update response handling code to account for the removed response type.";
            case REQUEST_BODY:
                return "Remove the request body from API calls to this endpoint.";
            default:
                return "Update client code to handle the removal.";
        }
    }

    private String generateAdditionSuggestion(ChangeCategory category, Change change) {
        switch (category) {
            case PARAMETER:
                return "Add the new required parameter '" + extractName(change.getPath())
                        + "' to all API calls.";
            case REQUEST_BODY:
                return "Include the required request body in API calls to this endpoint.";
            default:
                return "Update client code to provide the new required field.";
        }
    }

    private String generateModificationSuggestion(ChangeCategory category, Change change) {
        String path = change.getPath();

        if (path != null && path.contains(".type")) {
            return "Update the data type for '" + extractName(path)
                    + "' from '" + change.getOldValue() + "' to '" + change.getNewValue() + "'.";
        }

        if (path != null && path.contains(".required")) {
            return "The field is now required. Ensure all API calls include this field.";
        }

        if (path != null && path.contains(".schema")) {
            return "Update request/response handling to match the new schema structure.";
        }

        switch (category) {
            case ENDPOINT:
                return "Update the endpoint path or method in all API consumers.";
            case PARAMETER:
                return "Update parameter handling to match the new specification.";
            case RESPONSE:
                return "Update response parsing to handle the modified structure.";
            default:
                return "Review and update client code to match the new API specification.";
        }
    }

    private String extractName(String path) {
        if (path == null) {
            return "field";
        }

        if (path.contains(":")) {
            String[] parts = path.split(":");
            if (parts.length > 1) {
                String name = parts[1];
                if (name.contains(".")) {
                    return name.substring(0, name.indexOf("."));
                }
                return name;
            }
        }

        return path;
    }
}
