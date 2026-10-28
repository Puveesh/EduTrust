package com.sentinel.ledger.entity;

import com.sentinel.ledger.enums.CredentialStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credentials", indexes = {
    @Index(name = "idx_cred_id", columnList = "credential_id"),
    @Index(name = "idx_cred_subject", columnList = "synthetic_subject_ref")
})
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credential_id", nullable = false, unique = true, length = 100)
    private String credentialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "synthetic_subject_ref", nullable = false, length = 50)
    private String syntheticSubjectRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 150)
    private String qualification;

    @Column(name = "completion_year", nullable = false)
    private Integer completionYear;

    @Column(name = "cgpa_or_grade", length = 20)
    private String cgpaOrGrade;

    @Column(nullable = false, length = 200)
    private String issuer;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CredentialStatus status = CredentialStatus.VALID;

    @Column(name = "key_id", nullable = false, length = 100)
    private String keyId;

    @Column(nullable = false, length = 50)
    private String algorithm = "SHA256withRSA";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String signature;

    @Column(name = "public_key_pem", nullable = false, columnDefinition = "TEXT")
    private String publicKeyPem;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Credential() {}

    public Credential(String credentialId, Student student, String syntheticSubjectRef, Institution institution, String qualification, Integer completionYear, String cgpaOrGrade, String issuer, LocalDate issueDate, CredentialStatus status, String keyId, String algorithm, String signature, String publicKeyPem) {
        this.credentialId = credentialId;
        this.student = student;
        this.syntheticSubjectRef = syntheticSubjectRef;
        this.institution = institution;
        this.qualification = qualification;
        this.completionYear = completionYear;
        this.cgpaOrGrade = cgpaOrGrade;
        this.issuer = issuer;
        this.issueDate = issueDate;
        this.status = status;
        this.keyId = keyId;
        this.algorithm = algorithm;
        this.signature = signature;
        this.publicKeyPem = publicKeyPem;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getSyntheticSubjectRef() { return syntheticSubjectRef; }
    public void setSyntheticSubjectRef(String syntheticSubjectRef) { this.syntheticSubjectRef = syntheticSubjectRef; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

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
