package io.github.mohmk10.changeloghub.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comparisons")
public class Comparison {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "old_spec_name")
    private String oldSpecName;

    @Column(name = "new_spec_name")
    private String newSpecName;

    @Column(name = "old_spec_format")
    private String oldSpecFormat;

    @Column(name = "new_spec_format")
    private String newSpecFormat;

    @Column(name = "breaking_count")
    private Integer breakingCount;

    @Column(name = "dangerous_count")
    private Integer dangerousCount;

    @Column(name = "warning_count")
    private Integer warningCount;

    @Column(name = "info_count")
    private Integer infoCount;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "stability_grade")
    private String stabilityGrade;

    @Column(name = "semver_recommendation")
    private String semverRecommendation;

    @Column(name = "changes_json", columnDefinition = "TEXT")
    private String changesJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getOldSpecName() { return oldSpecName; }
    public void setOldSpecName(String oldSpecName) { this.oldSpecName = oldSpecName; }

    public String getNewSpecName() { return newSpecName; }
    public void setNewSpecName(String newSpecName) { this.newSpecName = newSpecName; }

    public String getOldSpecFormat() { return oldSpecFormat; }
    public void setOldSpecFormat(String oldSpecFormat) { this.oldSpecFormat = oldSpecFormat; }

    public String getNewSpecFormat() { return newSpecFormat; }
    public void setNewSpecFormat(String newSpecFormat) { this.newSpecFormat = newSpecFormat; }

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

    public String getChangesJson() { return changesJson; }
    public void setChangesJson(String changesJson) { this.changesJson = changesJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
