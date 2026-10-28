package com.sentinel.ledger.dto;

import com.sentinel.ledger.enums.ImportRowStatus;

public class UpdateImportRowRequest {
    private String syntheticApaarId;
    private String studentName;
    private String program;
    private String academicYear;
    private Integer admissionYear;
    private String admissionCategory;
    private ImportRowStatus status;

    public UpdateImportRowRequest() {}

    public String getSyntheticApaarId() { return syntheticApaarId; }
    public void setSyntheticApaarId(String syntheticApaarId) { this.syntheticApaarId = syntheticApaarId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }

    public String getAdmissionCategory() { return admissionCategory; }
    public void setAdmissionCategory(String admissionCategory) { this.admissionCategory = admissionCategory; }

    public ImportRowStatus getStatus() { return status; }
    public void setStatus(ImportRowStatus status) { this.status = status; }
}
