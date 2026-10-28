package com.sentinel.ledger.entity;

import com.sentinel.ledger.enums.ImportRowStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "import_rows")
public class ImportRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ImportBatch batch;

    @Column(name = "row_num", nullable = false)
    private Integer rowNumber;

    @Column(name = "raw_payload_json", columnDefinition = "TEXT")
    private String rawPayloadJson;

    @Column(name = "synthetic_apaar_id", length = 50)
    private String syntheticApaarId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(length = 150)
    private String program;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "admission_year")
    private Integer admissionYear;

    @Column(name = "admission_category", length = 50)
    private String admissionCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ImportRowStatus status = ImportRowStatus.VALID;

    @Column(name = "validation_errors_json", columnDefinition = "TEXT")
    private String validationErrorsJson;

    @Column(name = "duplicate_explanation", columnDefinition = "TEXT")
    private String duplicateExplanation;

    @Column(name = "is_edited", nullable = false)
    private boolean edited = false;

    public ImportRow() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ImportBatch getBatch() { return batch; }
    public void setBatch(ImportBatch batch) { this.batch = batch; }

    public Integer getRowNumber() { return rowNumber; }
    public void setRowNumber(Integer rowNumber) { this.rowNumber = rowNumber; }

    public String getRawPayloadJson() { return rawPayloadJson; }
    public void setRawPayloadJson(String rawPayloadJson) { this.rawPayloadJson = rawPayloadJson; }

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
