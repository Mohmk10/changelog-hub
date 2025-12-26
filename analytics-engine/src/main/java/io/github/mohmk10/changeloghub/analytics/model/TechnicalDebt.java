package io.github.mohmk10.changeloghub.analytics.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TechnicalDebt {

    private String apiName;
    private int deprecatedEndpointsCount;
    private LocalDate deprecatedSinceOldest;
    private int unusedSchemasCount;
    private int inconsistentNamingCount;
    private int missingDocumentationCount;
    private int duplicateEndpointsCount;
    private int outdatedResponseCodesCount;
    private int debtScore;
    private List<DebtItem> items;
    private LocalDateTime analyzedAt;

    public TechnicalDebt() {
        this.items = new ArrayList<>();
        this.analyzedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public int getDeprecatedEndpointsCount() {
        return deprecatedEndpointsCount;
    }

    public void setDeprecatedEndpointsCount(int deprecatedEndpointsCount) {
        this.deprecatedEndpointsCount = deprecatedEndpointsCount;
    }

    public LocalDate getDeprecatedSinceOldest() {
        return deprecatedSinceOldest;
    }

    public void setDeprecatedSinceOldest(LocalDate deprecatedSinceOldest) {
        this.deprecatedSinceOldest = deprecatedSinceOldest;
    }

    public int getUnusedSchemasCount() {
        return unusedSchemasCount;
    }

    public void setUnusedSchemasCount(int unusedSchemasCount) {
        this.unusedSchemasCount = unusedSchemasCount;
    }

    public int getInconsistentNamingCount() {
        return inconsistentNamingCount;
    }

    public void setInconsistentNamingCount(int inconsistentNamingCount) {
        this.inconsistentNamingCount = inconsistentNamingCount;
    }

    public int getMissingDocumentationCount() {
        return missingDocumentationCount;
    }

    public void setMissingDocumentationCount(int missingDocumentationCount) {
        this.missingDocumentationCount = missingDocumentationCount;
    }

    public int getDuplicateEndpointsCount() {
        return duplicateEndpointsCount;
    }

    public void setDuplicateEndpointsCount(int duplicateEndpointsCount) {
        this.duplicateEndpointsCount = duplicateEndpointsCount;
    }

    public int getOutdatedResponseCodesCount() {
        return outdatedResponseCodesCount;
    }

    public void setOutdatedResponseCodesCount(int outdatedResponseCodesCount) {
        this.outdatedResponseCodesCount = outdatedResponseCodesCount;
    }

    public int getDebtScore() {
        return debtScore;
    }

    public void setDebtScore(int debtScore) {
        this.debtScore = Math.max(0, Math.min(100, debtScore));
    }

    public List<DebtItem> getItems() {
        return items;
    }

    public void setItems(List<DebtItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public void addItem(DebtItem item) {
        this.items.add(item);
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public int getTotalIssues() {
        return deprecatedEndpointsCount + unusedSchemasCount + inconsistentNamingCount +
                missingDocumentationCount + duplicateEndpointsCount + outdatedResponseCodesCount;
    }

    public boolean hasHighDebt() {
        return debtScore > 50;
    }

    public boolean hasCriticalDebt() {
        return debtScore > 75;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TechnicalDebt that = (TechnicalDebt) o;
        return debtScore == that.debtScore && Objects.equals(apiName, that.apiName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiName, debtScore);
    }

    public static class DebtItem {
        private DebtType type;
        private String path;
        private String description;
        private int severity;
        private LocalDate since;
        private String recommendation;

        public enum DebtType {
            DEPRECATED_ENDPOINT("Deprecated endpoint still active"),
            UNUSED_SCHEMA("Schema not referenced"),
            INCONSISTENT_NAMING("Naming convention violation"),
            MISSING_DOCUMENTATION("Missing or incomplete documentation"),
            DUPLICATE_ENDPOINT("Duplicate functionality"),
            OUTDATED_RESPONSE_CODE("Non-standard response code"),
            MISSING_EXAMPLES("Missing API examples"),
            OUTDATED_SCHEMA("Outdated schema definition");

            private final String description;

            DebtType(String description) {
                this.description = description;
            }

            public String getDescription() {
                return description;
            }
        }

        public DebtItem() {}

        public DebtItem(DebtType type, String path, String description) {
            this.type = type;
            this.path = path;
            this.description = description;
            this.severity = 5; 
        }

        public DebtItem(DebtType type, String path, String description, int severity) {
            this.type = type;
            this.path = path;
            this.description = description;
            this.severity = severity;
        }

        public DebtType getType() {
            return type;
        }

        public void setType(DebtType type) {
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

        public int getSeverity() {
            return severity;
        }

        public void setSeverity(int severity) {
            this.severity = severity;
        }

        public LocalDate getSince() {
            return since;
        }

        public void setSince(LocalDate since) {
            this.since = since;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
    }

    public static class Builder {
        private final TechnicalDebt debt = new TechnicalDebt();

        public Builder apiName(String apiName) {
            debt.apiName = apiName;
            return this;
        }

        public Builder deprecatedEndpointsCount(int count) {
            debt.deprecatedEndpointsCount = count;
            return this;
        }

        public Builder deprecatedSinceOldest(LocalDate date) {
            debt.deprecatedSinceOldest = date;
            return this;
        }

        public Builder unusedSchemasCount(int count) {
            debt.unusedSchemasCount = count;
            return this;
        }

        public Builder inconsistentNamingCount(int count) {
            debt.inconsistentNamingCount = count;
            return this;
        }

        public Builder missingDocumentationCount(int count) {
            debt.missingDocumentationCount = count;
            return this;
        }

        public Builder duplicateEndpointsCount(int count) {
            debt.duplicateEndpointsCount = count;
            return this;
        }

        public Builder outdatedResponseCodesCount(int count) {
            debt.outdatedResponseCodesCount = count;
            return this;
        }

        public Builder debtScore(int score) {
            debt.setDebtScore(score);
            return this;
        }

        public Builder items(List<DebtItem> items) {
            debt.setItems(items);
            return this;
        }

        public Builder addItem(DebtItem item) {
            debt.addItem(item);
            return this;
        }

        public TechnicalDebt build() {
            return debt;
        }
    }
}
