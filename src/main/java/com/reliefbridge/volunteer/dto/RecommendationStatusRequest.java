package com.reliefbridge.volunteer.dto;

import com.reliefbridge.volunteer.enums.RecommendationStatus;

import jakarta.validation.constraints.NotNull;

public class RecommendationStatusRequest {

    @NotNull(message = "Recommendation status is required")
    private RecommendationStatus status;

    public RecommendationStatus getStatus() {
        return status;
    }

    public void setStatus(RecommendationStatus status) {
        this.status = status;
    }
}