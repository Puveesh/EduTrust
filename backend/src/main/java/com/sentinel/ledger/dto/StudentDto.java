package com.sentinel.ledger.dto;

import com.sentinel.ledger.enums.AdmissionCategory;
import com.sentinel.ledger.enums.AdmissionStatus;
import com.sentinel.ledger.enums.VerificationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentDto {
    private Long id;
    private Long institutionId;
    private String institutionName;
    private String institutionCode;
    private String syntheticApaarId;
    private String enrollmentNumber;
    private String studentName;
    private String program;
    private String academicYear;
    private Integer admissionYear;
    private AdmissionCategory admissionCategory;
    private AdmissionStatus admissionStatus;
    private VerificationStatus verificationStatus;
    private String gender;
    private LocalDate dob;
    private boolean credentialIssued;
    private String credentialId;
    private LocalDate credentialIssueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StudentDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getInstitutionCode() { return institutionCode; }
    public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }

    public String getSyntheticApaarId() { return syntheticApaarId; }
    public void setSyntheticApaarId(String syntheticApaarId) { this.syntheticApaarId = syntheticApaarId; }

    public String getEnrollmentNumber() { return enrollmentNumber; }
    public void setEnrollmentNumber(String enrollmentNumber) { this.enrollmentNumber = enrollmentNumber; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }

    public AdmissionCategory getAdmissionCategory() { return admissionCategory; }
    public void setAdmissionCategory(AdmissionCategory admissionCategory) { this.admissionCategory = admissionCategory; }

    public AdmissionStatus getAdmissionStatus() { return admissionStatus; }
    public void setAdmissionStatus(AdmissionStatus admissionStatus) { this.admissionStatus = admissionStatus; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public boolean isCredentialIssued() { return credentialIssued; }
    public void setCredentialIssued(boolean credentialIssued) { this.credentialIssued = credentialIssued; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public LocalDate getCredentialIssueDate() { return credentialIssueDate; }
    public void setCredentialIssueDate(LocalDate credentialIssueDate) { this.credentialIssueDate = credentialIssueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
