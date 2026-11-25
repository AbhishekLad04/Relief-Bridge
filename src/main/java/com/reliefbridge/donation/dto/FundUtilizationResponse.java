package com.reliefbridge.donation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.reliefbridge.donation.entity.ExpenseCategory;
import com.reliefbridge.donation.entity.FundUtilization;
import com.reliefbridge.donation.entity.UtilizationStatus;

public class FundUtilizationResponse {

    private Long id;
    private Long campaignId;
    private String campaignTitle;
    private Long ngoId;
    private String ngoName;
    private String ngoOrgName;
    private ExpenseCategory category;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String invoiceNumber;
    private String invoiceUrl;
    private String photoUrl;
    private UtilizationStatus status;
    private String rejectionReason;
    private Long verifiedById;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FundUtilizationResponse() {
    }

    public FundUtilizationResponse(FundUtilization fu) {
        this.id = fu.getId();
        if (fu.getCampaign() != null) {
            this.campaignId = fu.getCampaign().getId();
            this.campaignTitle = fu.getCampaign().getTitle();
        }
        if (fu.getNgo() != null) {
            this.ngoId = fu.getNgo().getId();
            this.ngoName = fu.getNgo().getName();
            this.ngoOrgName = fu.getNgo().getOrgName();
        }
        this.category = fu.getCategory();
        this.description = fu.getDescription();
        this.amount = fu.getAmount();
        this.expenseDate = fu.getExpenseDate();
        this.invoiceNumber = fu.getInvoiceNumber();
        this.invoiceUrl = fu.getInvoiceUrl();
        this.photoUrl = fu.getPhotoUrl();
        this.status = fu.getStatus();
        this.rejectionReason = fu.getRejectionReason();
        if (fu.getVerifiedBy() != null) {
            this.verifiedById = fu.getVerifiedBy().getId();
            this.verifiedByName = fu.getVerifiedBy().getName();
        }
        this.verifiedAt = fu.getVerifiedAt();
        this.createdAt = fu.getCreatedAt();
        this.updatedAt = fu.getUpdatedAt();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getCampaignTitle() {
        return campaignTitle;
    }

    public void setCampaignTitle(String campaignTitle) {
        this.campaignTitle = campaignTitle;
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

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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

    public Long getVerifiedById() {
        return verifiedById;
    }

    public void setVerifiedById(Long verifiedById) {
        this.verifiedById = verifiedById;
    }

    public String getVerifiedByName() {
        return verifiedByName;
    }

    public void setVerifiedByName(String verifiedByName) {
        this.verifiedByName = verifiedByName;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
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
