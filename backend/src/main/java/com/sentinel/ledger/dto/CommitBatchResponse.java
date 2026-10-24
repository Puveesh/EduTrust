package com.sentinel.ledger.dto;

import java.util.List;

public class CommitBatchResponse {
    private Long batchId;
    private String status;
    private int committedRecordsCount;
    private int excludedRecordsCount;
    private int flaggedReviewCasesCount;
    private String message;
    private List<String> flaggedCaseNumbers;

    public CommitBatchResponse() {}

    public CommitBatchResponse(Long batchId, String status, int committedRecordsCount, int excludedRecordsCount, int flaggedReviewCasesCount, String message, List<String> flaggedCaseNumbers) {
        this.batchId = batchId;
        this.status = status;
        this.committedRecordsCount = committedRecordsCount;
        this.excludedRecordsCount = excludedRecordsCount;
        this.flaggedReviewCasesCount = flaggedReviewCasesCount;
        this.message = message;
        this.flaggedCaseNumbers = flaggedCaseNumbers;
    }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCommittedRecordsCount() { return committedRecordsCount; }
    public void setCommittedRecordsCount(int committedRecordsCount) { this.committedRecordsCount = committedRecordsCount; }

    public int getExcludedRecordsCount() { return excludedRecordsCount; }
    public void setExcludedRecordsCount(int excludedRecordsCount) { this.excludedRecordsCount = excludedRecordsCount; }

    public int getFlaggedReviewCasesCount() { return flaggedReviewCasesCount; }
    public void setFlaggedReviewCasesCount(int flaggedReviewCasesCount) { this.flaggedReviewCasesCount = flaggedReviewCasesCount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getFlaggedCaseNumbers() { return flaggedCaseNumbers; }
    public void setFlaggedCaseNumbers(List<String> flaggedCaseNumbers) { this.flaggedCaseNumbers = flaggedCaseNumbers; }
}
