package com.sentinel.ledger.dto;

import com.sentinel.ledger.enums.CredentialStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CredentialDto {
    private Long id;
    private String credentialId;
    private Long studentId;
    private String studentName;
    private String syntheticSubjectRef;
    private Long institutionId;
    private String institutionName;
    private String qualification;
    private Integer completionYear;
    private String cgpaOrGrade;
    private String issuer;
    private LocalDate issueDate;
    private CredentialStatus status;
    private String keyId;
    private String algorithm;
    private String signature;
    private String publicKeyPem;
    private LocalDateTime createdAt;

    public CredentialDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSyntheticSubjectRef() { return syntheticSubjectRef; }
    public void setSyntheticSubjectRef(String syntheticSubjectRef) { this.syntheticSubjectRef = syntheticSubjectRef; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Integer getCompletionYear() { return completionYear; }
    public void setCompletionYear(Integer completionYear) { this.completionYear = completionYear; }

    public String getCgpaOrGrade() { return cgpaOrGrade; }
    public void setCgpaOrGrade(String cgpaOrGrade) { this.cgpaOrGrade = cgpaOrGrade; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public CredentialStatus getStatus() { return status; }
    public void setStatus(CredentialStatus status) { this.status = status; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getPublicKeyPem() { return publicKeyPem; }
    public void setPublicKeyPem(String publicKeyPem) { this.publicKeyPem = publicKeyPem; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
