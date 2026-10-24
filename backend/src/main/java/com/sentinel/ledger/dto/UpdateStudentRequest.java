package com.sentinel.ledger.dto;

import com.sentinel.ledger.enums.AdmissionCategory;
import com.sentinel.ledger.enums.AdmissionStatus;
import com.sentinel.ledger.enums.VerificationStatus;
import java.time.LocalDate;

public class UpdateStudentRequest {
    private String studentName;
    private String program;
    private String academicYear;
    private Integer admissionYear;
    private AdmissionCategory admissionCategory;
    private AdmissionStatus admissionStatus;
    private VerificationStatus verificationStatus;
    private String gender;
    private LocalDate dob;
    private String updateReason;

    public UpdateStudentRequest() {}

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

    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
}
