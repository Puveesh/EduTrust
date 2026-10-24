package com.sentinel.ledger.dto;

import com.sentinel.ledger.enums.ImportRowStatus;

public class ImportRowDto {
    private Long id;
    private Long batchId;
    private Integer rowNumber;
    private String syntheticApaarId;
    private String studentName;
    private String program;
    private String academicYear;
    private Integer admissionYear;
    private String admissionCategory;
    private ImportRowStatus status;
    private String validationErrorsJson;
    private String duplicateExplanation;
    private boolean edited;

    public ImportRowDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public Integer getRowNumber() { return rowNumber; }
    public void setRowNumber(Integer rowNumber) { this.rowNumber = rowNumber; }

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

    public String getValidationErrorsJson() { return validationErrorsJson; }
    public void setValidationErrorsJson(String validationErrorsJson) { this.validationErrorsJson = validationErrorsJson; }

    public String getDuplicateExplanation() { return duplicateExplanation; }
    public void setDuplicateExplanation(String duplicateExplanation) { this.duplicateExplanation = duplicateExplanation; }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }
}
