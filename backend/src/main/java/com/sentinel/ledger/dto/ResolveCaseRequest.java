package com.sentinel.ledger.dto;

import com.sentinel.ledger.enums.CaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResolveCaseRequest {
    @NotNull(message = "Resolution status is required")
    private CaseStatus status;

    @NotBlank(message = "Resolution reason is mandatory")
    private String resolutionReason;

    public ResolveCaseRequest() {}

    public ResolveCaseRequest(CaseStatus status, String resolutionReason) {
        this.status = status;
        this.resolutionReason = resolutionReason;
    }

    public CaseStatus getStatus() { return status; }
    public void setStatus(CaseStatus status) { this.status = status; }

    public String getResolutionReason() { return resolutionReason; }
    public void setResolutionReason(String resolutionReason) { this.resolutionReason = resolutionReason; }
}
