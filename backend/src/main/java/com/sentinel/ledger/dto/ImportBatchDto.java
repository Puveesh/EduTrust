package com.sentinel.ledger.dto;

import com.sentinel.ledger.enums.ImportBatchStatus;
import java.time.LocalDateTime;

public class ImportBatchDto {
    private Long id;
    private Long institutionId;
    private String institutionName;
    private String uploadedBy;
    private String fileName;
    private Long fileSize;
    private Integer totalRows;
    private Integer validRows;
    private Integer invalidRows;
    private Integer duplicateCandidates;
    private Integer excludedRows;
    private ImportBatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime committedAt;

    public ImportBatchDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

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
