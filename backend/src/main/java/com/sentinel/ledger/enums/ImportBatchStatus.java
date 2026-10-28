package com.sentinel.ledger.enums;

public enum ImportBatchStatus {
    UPLOADED,
    PREVIEWED,
    NEEDS_CORRECTION,
    READY_TO_COMMIT,
    COMMITTED,
    FAILED,
    CANCELLED
}
