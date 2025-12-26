package io.github.mohmk10.changeloghub.analytics.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class ApiMetrics {

    private String apiName;
    private String version;
    private int totalEndpoints;
    private int totalChanges;
    private int breakingChanges;
    private int deprecatedEndpoints;
    private int totalParameters;
    private int totalResponses;
    private double averageParametersPerEndpoint;
    private double averageResponseCodesPerEndpoint;
    private int complexityScore;
    private double documentationCoverage;
    private int schemaCount;
    private int nestedSchemaDepth;
    private LocalDateTime analyzedAt;

    public ApiMetrics() {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getTotalEndpoints() {
        return totalEndpoints;
    }

    public void setTotalEndpoints(int totalEndpoints) {
        this.totalEndpoints = totalEndpoints;
    }

    public int getTotalChanges() {
        return totalChanges;
    }

    public void setTotalChanges(int totalChanges) {
        this.totalChanges = totalChanges;
    }

    public int getBreakingChanges() {
        return breakingChanges;
    }

    public void setBreakingChanges(int breakingChanges) {
        this.breakingChanges = breakingChanges;
    }

    public int getDeprecatedEndpoints() {
        return deprecatedEndpoints;
    }

    public void setDeprecatedEndpoints(int deprecatedEndpoints) {
        this.deprecatedEndpoints = deprecatedEndpoints;
    }

    public int getTotalParameters() {
        return totalParameters;
    }

    public void setTotalParameters(int totalParameters) {
        this.totalParameters = totalParameters;
    }

    public int getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(int totalResponses) {
        this.totalResponses = totalResponses;
    }

    public double getAverageParametersPerEndpoint() {
        return averageParametersPerEndpoint;
    }

    public void setAverageParametersPerEndpoint(double averageParametersPerEndpoint) {
        this.averageParametersPerEndpoint = averageParametersPerEndpoint;
    }

    public double getAverageResponseCodesPerEndpoint() {
        return averageResponseCodesPerEndpoint;
    }

    public void setAverageResponseCodesPerEndpoint(double averageResponseCodesPerEndpoint) {
        this.averageResponseCodesPerEndpoint = averageResponseCodesPerEndpoint;
    }

    public int getComplexityScore() {
        return complexityScore;
    }

    public void setComplexityScore(int complexityScore) {
        this.complexityScore = complexityScore;
    }

    public double getDocumentationCoverage() {
        return documentationCoverage;
    }

    public void setDocumentationCoverage(double documentationCoverage) {
        this.documentationCoverage = documentationCoverage;
    }

    public int getSchemaCount() {
        return schemaCount;
    }

    public void setSchemaCount(int schemaCount) {
        this.schemaCount = schemaCount;
    }

    public int getNestedSchemaDepth() {
        return nestedSchemaDepth;
    }

    public void setNestedSchemaDepth(int nestedSchemaDepth) {
        this.nestedSchemaDepth = nestedSchemaDepth;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public double getBreakingChangeRatio() {
        if (totalChanges == 0) return 0.0;
        return (double) breakingChanges / totalChanges;
    }

    public double getDeprecationRatio() {
        if (totalEndpoints == 0) return 0.0;
        return (double) deprecatedEndpoints / totalEndpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiMetrics that = (ApiMetrics) o;
        return totalEndpoints == that.totalEndpoints &&
                totalChanges == that.totalChanges &&
                breakingChanges == that.breakingChanges &&
                Objects.equals(apiName, that.apiName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiName, version, totalEndpoints, totalChanges, breakingChanges);
    }

    @Override
    public String toString() {
        return "ApiMetrics{" +
                "apiName='" + apiName + '\'' +
                ", version='" + version + '\'' +
                ", endpoints=" + totalEndpoints +
                ", changes=" + totalChanges +
                ", breaking=" + breakingChanges +
                ", complexity=" + complexityScore +
                '}';
    }

    public static class Builder {
        private final ApiMetrics metrics = new ApiMetrics();

        public Builder apiName(String apiName) {
            metrics.apiName = apiName;
            return this;
        }

        public Builder version(String version) {
            metrics.version = version;
            return this;
        }

        public Builder totalEndpoints(int totalEndpoints) {
            metrics.totalEndpoints = totalEndpoints;
            return this;
        }

        public Builder totalChanges(int totalChanges) {
            metrics.totalChanges = totalChanges;
            return this;
        }

        public Builder breakingChanges(int breakingChanges) {
            metrics.breakingChanges = breakingChanges;
            return this;
        }

        public Builder deprecatedEndpoints(int deprecatedEndpoints) {
            metrics.deprecatedEndpoints = deprecatedEndpoints;
            return this;
        }

        public Builder totalParameters(int totalParameters) {
            metrics.totalParameters = totalParameters;
            return this;
        }

        public Builder totalResponses(int totalResponses) {
            metrics.totalResponses = totalResponses;
            return this;
        }

        public Builder averageParametersPerEndpoint(double avg) {
            metrics.averageParametersPerEndpoint = avg;
            return this;
        }

        public Builder averageResponseCodesPerEndpoint(double avg) {
            metrics.averageResponseCodesPerEndpoint = avg;
            return this;
        }

        public Builder complexityScore(int score) {
            metrics.complexityScore = score;
            return this;
        }

        public Builder documentationCoverage(double coverage) {
            metrics.documentationCoverage = coverage;
            return this;
        }

        public Builder schemaCount(int count) {
            metrics.schemaCount = count;
            return this;
        }

        public Builder nestedSchemaDepth(int depth) {
            metrics.nestedSchemaDepth = depth;
            return this;
        }

        public Builder analyzedAt(LocalDateTime analyzedAt) {
            metrics.analyzedAt = analyzedAt;
            return this;
        }

        public ApiMetrics build() {
            return metrics;
        }
    }
}
