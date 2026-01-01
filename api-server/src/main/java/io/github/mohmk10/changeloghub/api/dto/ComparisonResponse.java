package io.github.mohmk10.changeloghub.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ComparisonResponse {
    private UUID id;
    private String oldSpecName;
    private String newSpecName;
    private String format;
    private Integer breakingCount;
    private Integer dangerousCount;
    private Integer warningCount;
    private Integer infoCount;
    private Integer riskScore;
    private String stabilityGrade;
    private String semverRecommendation;
    private List<ChangeDto> changes;
    private LocalDateTime createdAt;

    public ComparisonResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getOldSpecName() { return oldSpecName; }
    public void setOldSpecName(String oldSpecName) { this.oldSpecName = oldSpecName; }

    public String getNewSpecName() { return newSpecName; }
    public void setNewSpecName(String newSpecName) { this.newSpecName = newSpecName; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public Integer getBreakingCount() { return breakingCount; }
    public void setBreakingCount(Integer breakingCount) { this.breakingCount = breakingCount; }

    public Integer getDangerousCount() { return dangerousCount; }
    public void setDangerousCount(Integer dangerousCount) { this.dangerousCount = dangerousCount; }

    public Integer getWarningCount() { return warningCount; }
    public void setWarningCount(Integer warningCount) { this.warningCount = warningCount; }

    public Integer getInfoCount() { return infoCount; }
    public void setInfoCount(Integer infoCount) { this.infoCount = infoCount; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getStabilityGrade() { return stabilityGrade; }
    public void setStabilityGrade(String stabilityGrade) { this.stabilityGrade = stabilityGrade; }

    public String getSemverRecommendation() { return semverRecommendation; }
    public void setSemverRecommendation(String semverRecommendation) { this.semverRecommendation = semverRecommendation; }

    public List<ChangeDto> getChanges() { return changes; }
    public void setChanges(List<ChangeDto> changes) { this.changes = changes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class ChangeDto {
        private String type;
        private String severity;
        private String path;
        private String description;
        private String oldValue;
        private String newValue;

        public ChangeDto() {}

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getOldValue() { return oldValue; }
        public void setOldValue(String oldValue) { this.oldValue = oldValue; }

        public String getNewValue() { return newValue; }
        public void setNewValue(String newValue) { this.newValue = newValue; }
    }
}
