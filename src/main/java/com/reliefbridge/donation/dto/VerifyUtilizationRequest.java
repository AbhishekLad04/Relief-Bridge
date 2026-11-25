package com.reliefbridge.donation.dto;

import com.reliefbridge.donation.entity.UtilizationStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VerifyUtilizationRequest {

    @NotNull(message = "Verification status is required")
    private UtilizationStatus status;

    @Size(max = 1000, message = "Rejection reason cannot exceed 1000 characters")
    private String rejectionReason;

    public VerifyUtilizationRequest() {
    }

    public UtilizationStatus getStatus() {
        return status;
    }

    public void setStatus(UtilizationStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
