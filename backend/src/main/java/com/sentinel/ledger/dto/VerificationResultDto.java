package com.sentinel.ledger.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VerificationResultDto {
    private boolean valid;
    private boolean signatureValid;
    private String credentialStatus; // VALID, REVOKED, EXPIRED, TAMPERED, NOT_FOUND
    private String credentialId;
    private String syntheticSubjectRef; // Masked or synthetic identifier only
    private String maskedStudentName;    // Minimum necessary disclosure: e.g. "R**** S****"
    private String studentName;          // Unmasked name disclosed ONLY to authorized institutional registrar
    private String issuer;
    private String institutionName;
    private String qualification;
    private Integer completionYear;
    private LocalDate issueDate;
    private String algorithm;
    private String keyId;
    private String verificationNotes;
    private LocalDateTime verifiedAt;

    public VerificationResultDto() {
        this.verifiedAt = LocalDateTime.now();
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public boolean isSignatureValid() { return signatureValid; }
    public void setSignatureValid(boolean signatureValid) { this.signatureValid = signatureValid; }

    public String getCredentialStatus() { return credentialStatus; }
    public void setCredentialStatus(String credentialStatus) { this.credentialStatus = credentialStatus; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getSyntheticSubjectRef() { return syntheticSubjectRef; }
    public void setSyntheticSubjectRef(String syntheticSubjectRef) { this.syntheticSubjectRef = syntheticSubjectRef; }

    public String getMaskedStudentName() { return maskedStudentName; }
    public void setMaskedStudentName(String maskedStudentName) { this.maskedStudentName = maskedStudentName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Integer getCompletionYear() { return completionYear; }
    public void setCompletionYear(Integer completionYear) { this.completionYear = completionYear; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
}
