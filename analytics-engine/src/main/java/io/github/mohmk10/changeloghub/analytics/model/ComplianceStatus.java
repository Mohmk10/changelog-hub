package io.github.mohmk10.changeloghub.analytics.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the compliance status of an API against standards and best practices.
 */
public class ComplianceStatus {

    private String apiName;
    private int complianceScore;
    private ComplianceLevel level;
    private Status status;
    private int totalChecks;
    private int passedChecks;
    private boolean semverCompliant;
    private boolean deprecationPolicyCompliant;
    private boolean documentationCompliant;
    private boolean namingConventionCompliant;
    private boolean securityCompliant;
    private List<ComplianceViolation> violations;
    private LocalDateTime analyzedAt;

    public enum Status {
        COMPLIANT,
        PARTIAL,
        NON_COMPLIANT
    }

    public enum ComplianceLevel {
        FULLY_COMPLIANT("Fully Compliant", 90, 100),
        MOSTLY_COMPLIANT("Mostly Compliant", 70, 89),
        PARTIALLY_COMPLIANT("Partially Compliant", 50, 69),
        NON_COMPLIANT("Non-Compliant", 0, 49);

        private final String label;
        private final int minScore;
        private final int maxScore;

        ComplianceLevel(String label, int minScore, int maxScore) {
            this.label = label;
            this.minScore = minScore;
            this.maxScore = maxScore;
        }

        public String getLabel() {
            return label;
        }

        public static ComplianceLevel fromScore(int score) {
            if (score >= 90) return FULLY_COMPLIANT;
            if (score >= 70) return MOSTLY_COMPLIANT;
            if (score >= 50) return PARTIALLY_COMPLIANT;
            return NON_COMPLIANT;
        }
    }

    public ComplianceStatus() {
        this.violations = new ArrayList<>();
        this.analyzedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public int getComplianceScore() {
        return complianceScore;
    }

    public void setComplianceScore(int complianceScore) {
        this.complianceScore = Math.max(0, Math.min(100, complianceScore));
        this.level = ComplianceLevel.fromScore(this.complianceScore);
    }

    public ComplianceLevel getLevel() {
        return level;
    }

    public void setLevel(ComplianceLevel level) {
        this.level = level;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getTotalChecks() {
        return totalChecks;
    }

    public void setTotalChecks(int totalChecks) {
        this.totalChecks = totalChecks;
    }

    public int getPassedChecks() {
        return passedChecks;
    }

    public void setPassedChecks(int passedChecks) {
        this.passedChecks = passedChecks;
    }

    public boolean isSemverCompliant() {
        return semverCompliant;
    }

    public void setSemverCompliant(boolean semverCompliant) {
        this.semverCompliant = semverCompliant;
    }

    public boolean isDeprecationPolicyCompliant() {
        return deprecationPolicyCompliant;
    }

    public void setDeprecationPolicyCompliant(boolean deprecationPolicyCompliant) {
        this.deprecationPolicyCompliant = deprecationPolicyCompliant;
    }

    public boolean isDocumentationCompliant() {
        return documentationCompliant;
    }

    public void setDocumentationCompliant(boolean documentationCompliant) {
        this.documentationCompliant = documentationCompliant;
    }

    public boolean isNamingConventionCompliant() {
        return namingConventionCompliant;
    }

    public void setNamingConventionCompliant(boolean namingConventionCompliant) {
        this.namingConventionCompliant = namingConventionCompliant;
    }

    public boolean isSecurityCompliant() {
        return securityCompliant;
    }

    public void setSecurityCompliant(boolean securityCompliant) {
        this.securityCompliant = securityCompliant;
    }

    public List<ComplianceViolation> getViolations() {
        return violations;
    }

    public void setViolations(List<ComplianceViolation> violations) {
        this.violations = violations != null ? new ArrayList<>(violations) : new ArrayList<>();
    }

    public void addViolation(ComplianceViolation violation) {
        this.violations.add(violation);
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public boolean isFullyCompliant() {
        return level == ComplianceLevel.FULLY_COMPLIANT;
    }

    public boolean hasViolations() {
        return !violations.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplianceStatus that = (ComplianceStatus) o;
        return complianceScore == that.complianceScore && Objects.equals(apiName, that.apiName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiName, complianceScore);
    }

    /**
     * Represents a compliance violation.
     */
    public static class ComplianceViolation {
        private ViolationType type;
        private String path;
        private String description;
        private String recommendation;
        private int severity;

        public enum ViolationType {
            SEMVER_VIOLATION("Semantic versioning violation"),
            DEPRECATION_VIOLATION("Deprecation policy violation"),
            DOCUMENTATION_VIOLATION("Documentation requirement violation"),
            NAMING_VIOLATION("Naming convention violation"),
            SECURITY_VIOLATION("Security best practice violation");

            private final String description;

            ViolationType(String description) {
                this.description = description;
            }

            public String getDescription() {
                return description;
            }
        }

        public ComplianceViolation() {}

        public ComplianceViolation(ViolationType type, String path, String description) {
            this.type = type;
            this.path = path;
            this.description = description;
        }

        public ViolationType getType() {
            return type;
        }

        public void setType(ViolationType type) {
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }

        public int getSeverity() {
            return severity;
        }

        public void setSeverity(int severity) {
            this.severity = severity;
        }
    }

    public static class Builder {
        private final ComplianceStatus status = new ComplianceStatus();

        public Builder apiName(String apiName) {
            status.apiName = apiName;
            return this;
        }

        public Builder complianceScore(int score) {
            status.setComplianceScore(score);
            return this;
        }

        public Builder status(Status s) {
            status.status = s;
            return this;
        }

        public Builder totalChecks(int count) {
            status.totalChecks = count;
            return this;
        }

        public Builder passedChecks(int count) {
            status.passedChecks = count;
            return this;
        }

        public Builder semverCompliant(boolean compliant) {
            status.semverCompliant = compliant;
            return this;
        }

        public Builder deprecationPolicyCompliant(boolean compliant) {
            status.deprecationPolicyCompliant = compliant;
            return this;
        }

        public Builder documentationCompliant(boolean compliant) {
            status.documentationCompliant = compliant;
            return this;
        }

        public Builder namingConventionCompliant(boolean compliant) {
            status.namingConventionCompliant = compliant;
            return this;
        }

        public Builder securityCompliant(boolean compliant) {
            status.securityCompliant = compliant;
            return this;
        }

        public Builder violations(List<ComplianceViolation> violations) {
            status.setViolations(violations);
            return this;
        }

        public Builder addViolation(ComplianceViolation violation) {
            status.addViolation(violation);
            return this;
        }

        public ComplianceStatus build() {
            return status;
        }
    }
}
