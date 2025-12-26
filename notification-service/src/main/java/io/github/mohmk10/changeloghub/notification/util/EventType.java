package io.github.mohmk10.changeloghub.notification.util;

import io.github.mohmk10.changeloghub.core.model.Severity;

public enum EventType {
    BREAKING_CHANGE_DETECTED("Breaking Change Detected", Severity.BREAKING, true),
    DANGEROUS_CHANGE_DETECTED("Dangerous Change Detected", Severity.DANGEROUS, true),
    HIGH_RISK_RELEASE("High Risk Release", Severity.DANGEROUS, true),
    DEPRECATION_ADDED("Deprecation Added", Severity.WARNING, false),
    API_VERSION_RELEASED("API Version Released", Severity.INFO, false),
    CONSUMER_IMPACTED("Consumer Impacted", Severity.DANGEROUS, true),
    SCHEMA_CHANGED("Schema Changed", Severity.WARNING, false),
    ENDPOINT_REMOVED("Endpoint Removed", Severity.BREAKING, true),
    PARAMETER_CHANGED("Parameter Changed", Severity.WARNING, false);

    private final String description;
    private final Severity defaultSeverity;
    private final boolean critical;

    EventType(String description, Severity defaultSeverity, boolean critical) {
        this.description = description;
        this.defaultSeverity = defaultSeverity;
        this.critical = critical;
    }

    public String getDescription() {
        return description;
    }

    public Severity getDefaultSeverity() {
        return defaultSeverity;
    }

    public boolean isCritical() {
        return critical;
    }

    public boolean requiresImmediateNotification() {
        return critical;
    }

    public String getColorCode() {
        return switch (defaultSeverity) {
            case BREAKING -> "#dc3545"; 
            case DANGEROUS -> "#fd7e14"; 
            case WARNING -> "#ffc107"; 
            case INFO -> "#28a745"; 
        };
    }

    public String getEmoji() {
        return switch (defaultSeverity) {
            case BREAKING -> "\u26A0\uFE0F"; 
            case DANGEROUS -> "\u2757"; 
            case WARNING -> "\uD83D\uDCA1"; 
            case INFO -> "\u2139\uFE0F"; 
        };
    }
}
