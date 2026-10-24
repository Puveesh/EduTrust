package com.sentinel.ledger.dto;

import com.sentinel.ledger.enums.AdmissionCategory;
import com.sentinel.ledger.enums.AdmissionStatus;
import com.sentinel.ledger.enums.VerificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateStudentRequest {

    private Long institutionId; // Optional for admin, inferred for registrar

    @NotBlank(message = "Synthetic APAAR ID is required")
    private String syntheticApaarId;

    @NotBlank(message = "Enrollment number is required")
    private String enrollmentNumber;

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Program/Course is required")
    private String program;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotNull(message = "Admission year is required")
    private Integer admissionYear;

    @NotNull(message = "Admission category is required")
    private AdmissionCategory admissionCategory;

    private AdmissionStatus admissionStatus = AdmissionStatus.ADMITTED;
    private VerificationStatus verificationStatus = VerificationStatus.VERIFIED;
    private String gender = "UNSPECIFIED";
    private LocalDate dob;

    public CreateStudentRequest() {}

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

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
}
