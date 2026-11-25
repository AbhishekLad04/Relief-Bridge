package com.reliefbridge.campaign.dto;

import com.reliefbridge.campaign.entity.CampaignStatus;

import jakarta.validation.constraints.NotNull;

public class CampaignStatusUpdateRequest {

    @NotNull(message = "Campaign status is required")
    private CampaignStatus status;

    public CampaignStatusUpdateRequest() {
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }
}
