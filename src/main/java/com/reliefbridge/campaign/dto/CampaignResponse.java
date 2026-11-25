package com.reliefbridge.campaign.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.reliefbridge.campaign.entity.Campaign;
import com.reliefbridge.campaign.entity.CampaignCategory;
import com.reliefbridge.campaign.entity.CampaignStatus;

public class CampaignResponse {

    private Long id;
    private String title;
    private String description;
    private Long disasterId;
    private String disasterTitle;
    private Long ngoId;
    private String ngoName;
    private String ngoOrgName;
    private CampaignCategory category;
    private BigDecimal targetAmount;
    private BigDecimal collectedAmount;
    private double progressPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String requiredResources;
    private String imageUrl;
    private CampaignStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CampaignResponse() {
    }

    public CampaignResponse(Campaign campaign) {
        this.id = campaign.getId();
        this.title = campaign.getTitle();
        this.description = campaign.getDescription();
        if (campaign.getDisaster() != null) {
            this.disasterId = campaign.getDisaster().getId();
            this.disasterTitle = campaign.getDisaster().getTitle();
        }
        if (campaign.getNgo() != null) {
            this.ngoId = campaign.getNgo().getId();
            this.ngoName = campaign.getNgo().getName();
            this.ngoOrgName = campaign.getNgo().getOrgName();
        }
        this.category = campaign.getCategory();
        this.targetAmount = campaign.getTargetAmount();
        this.collectedAmount = campaign.getCollectedAmount() != null ? campaign.getCollectedAmount() : BigDecimal.ZERO;

        if (this.targetAmount != null && this.targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.progressPercentage = this.collectedAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(this.targetAmount, 2, RoundingMode.HALF_UP)
                    .doubleValue();
        } else {
            this.progressPercentage = 0.0;
        }

        this.startDate = campaign.getStartDate();
        this.endDate = campaign.getEndDate();
        this.location = campaign.getLocation();
        this.requiredResources = campaign.getRequiredResources();
        this.imageUrl = campaign.getImageUrl();
        this.status = campaign.getStatus();
        this.createdAt = campaign.getCreatedAt();
        this.updatedAt = campaign.getUpdatedAt();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDisasterId() {
        return disasterId;
    }

    public void setDisasterId(Long disasterId) {
        this.disasterId = disasterId;
    }

    public String getDisasterTitle() {
        return disasterTitle;
    }

    public void setDisasterTitle(String disasterTitle) {
        this.disasterTitle = disasterTitle;
    }

    public Long getNgoId() {
        return ngoId;
    }

    public void setNgoId(Long ngoId) {
        this.ngoId = ngoId;
    }

    public String getNgoName() {
        return ngoName;
    }

    public void setNgoName(String ngoName) {
        this.ngoName = ngoName;
    }

    public String getNgoOrgName() {
        return ngoOrgName;
    }

    public void setNgoOrgName(String ngoOrgName) {
        this.ngoOrgName = ngoOrgName;
    }

    public CampaignCategory getCategory() {
        return category;
    }

    public void setCategory(CampaignCategory category) {
        this.category = category;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCollectedAmount() {
        return collectedAmount;
    }

    public void setCollectedAmount(BigDecimal collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRequiredResources() {
        return requiredResources;
    }

    public void setRequiredResources(String requiredResources) {
        this.requiredResources = requiredResources;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
