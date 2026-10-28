package com.sentinel.ledger.entity;

import com.sentinel.ledger.enums.ImportBatchStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_batches")
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "uploaded_by", nullable = false, length = 100)
    private String uploadedBy;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "total_rows", nullable = false)
    private Integer totalRows = 0;

    @Column(name = "valid_rows", nullable = false)
    private Integer validRows = 0;

    @Column(name = "invalid_rows", nullable = false)
    private Integer invalidRows = 0;

    @Column(name = "duplicate_candidates", nullable = false)
    private Integer duplicateCandidates = 0;

    @Column(name = "excluded_rows", nullable = false)
    private Integer excludedRows = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ImportBatchStatus status = ImportBatchStatus.UPLOADED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "committed_at")
    private LocalDateTime committedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ImportBatch() {}

    public ImportBatch(Institution institution, String uploadedBy, String fileName, Long fileSize) {
        this.institution = institution;
        this.uploadedBy = uploadedBy;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.status = ImportBatchStatus.UPLOADED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }

    public Integer getValidRows() { return validRows; }
    public void setValidRows(Integer validRows) { this.validRows = validRows; }

    public Integer getInvalidRows() { return invalidRows; }
    public void setInvalidRows(Integer invalidRows) { this.invalidRows = invalidRows; }

    public Integer getDuplicateCandidates() { return duplicateCandidates; }
    public void setDuplicateCandidates(Integer duplicateCandidates) { this.duplicateCandidates = duplicateCandidates; }

    public Integer getExcludedRows() { return excludedRows; }
    public void setExcludedRows(Integer excludedRows) { this.excludedRows = excludedRows; }

    public ImportBatchStatus getStatus() { return status; }
    public void setStatus(ImportBatchStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCommittedAt() { return committedAt; }
    public void setCommittedAt(LocalDateTime committedAt) { this.committedAt = committedAt; }
}
