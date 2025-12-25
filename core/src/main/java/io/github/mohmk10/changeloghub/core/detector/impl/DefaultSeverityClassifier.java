package io.github.mohmk10.changeloghub.core.detector.impl;

import io.github.mohmk10.changeloghub.core.detector.SeverityClassifier;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Severity;

public class DefaultSeverityClassifier implements SeverityClassifier {

    @Override
    public Severity classify(Change change) {
        if (change == null) {
            return Severity.INFO;
        }

        ChangeType type = change.getType();
        ChangeCategory category = change.getCategory();

        if (type == ChangeType.REMOVED) {
            return classifyRemoval(category);
        }

        if (type == ChangeType.ADDED) {
            return classifyAddition(change);
        }

        if (type == ChangeType.MODIFIED) {
            return classifyModification(change);
        }

        if (type == ChangeType.DEPRECATED) {
            return Severity.WARNING;
        }

        return Severity.INFO;
    }

    private Severity classifyRemoval(ChangeCategory category) {
        switch (category) {
            case ENDPOINT:
                return Severity.BREAKING;
            case PARAMETER:
            case RESPONSE:
            case REQUEST_BODY:
                return Severity.DANGEROUS;
            default:
                return Severity.WARNING;
        }
    }

    private Severity classifyAddition(Change change) {
        ChangeCategory category = change.getCategory();

        if (category == ChangeCategory.PARAMETER) {
            return isRequiredAddition(change) ? Severity.BREAKING : Severity.INFO;
        }

        if (category == ChangeCategory.REQUEST_BODY) {
            return isRequiredAddition(change) ? Severity.BREAKING : Severity.INFO;
        }

        return Severity.INFO;
    }

    private Severity classifyModification(Change change) {
        ChangeCategory category = change.getCategory();
        String path = change.getPath();

        if (category == ChangeCategory.ENDPOINT) {
            if (isPathOrMethodChange(path)) {
                return Severity.BREAKING;
            }
        }

        if (category == ChangeCategory.PARAMETER) {
            if (isTypeChange(path) || isRequiredChange(path, change)) {
                return Severity.BREAKING;
            }
            if (isLocationChange(path)) {
                return Severity.BREAKING;
            }
        }

        if (category == ChangeCategory.REQUEST_BODY) {
            if (isRequiredChange(path, change)) {
                return Severity.BREAKING;
            }
            if (isSchemaChange(path)) {
                return Severity.DANGEROUS;
            }
        }

        if (category == ChangeCategory.RESPONSE) {
            if (isSchemaChange(path)) {
                return Severity.DANGEROUS;
            }
            return Severity.WARNING;
        }

        return Severity.WARNING;
    }

    private boolean isRequiredAddition(Change change) {
        String description = change.getDescription();
        if (description != null && description.toLowerCase().contains("required")) {
            return true;
        }

        return false;
    }

    private boolean isPathOrMethodChange(String path) {
        return path != null && !path.contains(".");
    }

    private boolean isTypeChange(String path) {
        return path != null && path.endsWith(".type");
    }

    private boolean isRequiredChange(String path, Change change) {
        if (path != null && path.endsWith(".required")) {
            Object oldValue = change.getOldValue();
            Object newValue = change.getNewValue();
            return Boolean.FALSE.equals(oldValue) && Boolean.TRUE.equals(newValue);
        }
        return false;
    }

    private boolean isLocationChange(String path) {
        return path != null && path.endsWith(".location");
    }

    private boolean isSchemaChange(String path) {
        return path != null && path.endsWith(".schema");
    }
}
