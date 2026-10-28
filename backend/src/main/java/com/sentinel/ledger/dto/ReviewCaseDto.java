package com.sentinel.ledger.dto;

import com.sentinel.ledger.enums.CaseStatus;
import com.sentinel.ledger.enums.CaseType;
import com.sentinel.ledger.enums.Severity;
import java.time.LocalDateTime;

public class ReviewCaseDto {
    private Long id;
    private String caseNumber;
    private CaseType caseType;
    private CaseStatus status;
    private Severity severity;
    private Long institutionId;
    private String institutionName;
    private Long targetStudentId;
    private String targetStudentName;
    private String evidenceJson;
    private String resolutionReason;
    private String decisionHistoryJson;
    private String resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    public ReviewCaseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public CaseType getCaseType() { return caseType; }
    public void setCaseType(CaseType caseType) { this.caseType = caseType; }

    public CaseStatus getStatus() { return status; }
    public void setStatus(CaseStatus status) { this.status = status; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public Long getTargetStudentId() { return targetStudentId; }
    public void setTargetStudentId(Long targetStudentId) { this.targetStudentId = targetStudentId; }

    public String getTargetStudentName() { return targetStudentName; }
    public void setTargetStudentName(String targetStudentName) { this.targetStudentName = targetStudentName; }

    public String getEvidenceJson() { return evidenceJson; }
    public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }

    public String getResolutionReason() { return resolutionReason; }
    public void setResolutionReason(String resolutionReason) { this.resolutionReason = resolutionReason; }

    public String getDecisionHistoryJson() { return decisionHistoryJson; }
    public void setDecisionHistoryJson(String decisionHistoryJson) { this.decisionHistoryJson = decisionHistoryJson; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
