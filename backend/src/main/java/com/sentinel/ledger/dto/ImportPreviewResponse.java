package com.sentinel.ledger.dto;

import java.util.List;

public class ImportPreviewResponse {
    private ImportBatchDto batch;
    private List<ImportRowDto> rows;
    private boolean hasErrors;
    private boolean hasDuplicates;
    private int intakeExceededCount;

    public ImportPreviewResponse() {}

    public ImportPreviewResponse(ImportBatchDto batch, List<ImportRowDto> rows, boolean hasErrors, boolean hasDuplicates, int intakeExceededCount) {
        this.batch = batch;
        this.rows = rows;
        this.hasErrors = hasErrors;
        this.hasDuplicates = hasDuplicates;
        this.intakeExceededCount = intakeExceededCount;
    }

    public ImportBatchDto getBatch() { return batch; }
    public void setBatch(ImportBatchDto batch) { this.batch = batch; }

    public List<ImportRowDto> getRows() { return rows; }
    public void setRows(List<ImportRowDto> rows) { this.rows = rows; }

    public boolean isHasErrors() { return hasErrors; }
    public void setHasErrors(boolean hasErrors) { this.hasErrors = hasErrors; }

    public boolean isHasDuplicates() { return hasDuplicates; }
    public void setHasDuplicates(boolean hasDuplicates) { this.hasDuplicates = hasDuplicates; }

    public int getIntakeExceededCount() { return intakeExceededCount; }
    public void setIntakeExceededCount(int intakeExceededCount) { this.intakeExceededCount = intakeExceededCount; }
}
