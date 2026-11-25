package com.reliefbridge.campaign.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CampaignFinancialSummary {

    private Long campaignId;
    private String campaignTitle;
    private BigDecimal targetAmount;
    private BigDecimal collectedAmount;
    private BigDecimal verifiedUtilizedAmount;
    private BigDecimal pendingUtilizedAmount;
    private BigDecimal remainingBalance;
    private double progressPercentage;
    private long contributorsCount;
    private long totalDonationsCount;
    private long totalUtilizationsCount;

    public CampaignFinancialSummary() {
    }

    public CampaignFinancialSummary(
            Long campaignId,
            String campaignTitle,
            BigDecimal targetAmount,
            BigDecimal collectedAmount,
            BigDecimal verifiedUtilizedAmount,
            BigDecimal pendingUtilizedAmount,
            long contributorsCount,
            long totalDonationsCount,
            long totalUtilizationsCount) {

        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.targetAmount = targetAmount != null ? targetAmount : BigDecimal.ZERO;
        this.collectedAmount = collectedAmount != null ? collectedAmount : BigDecimal.ZERO;
        this.verifiedUtilizedAmount = verifiedUtilizedAmount != null ? verifiedUtilizedAmount : BigDecimal.ZERO;
        this.pendingUtilizedAmount = pendingUtilizedAmount != null ? pendingUtilizedAmount : BigDecimal.ZERO;

        this.remainingBalance = this.collectedAmount.subtract(this.verifiedUtilizedAmount);
        if (this.remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
            this.remainingBalance = BigDecimal.ZERO;
        }

        if (this.targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.progressPercentage = this.collectedAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(this.targetAmount, 2, RoundingMode.HALF_UP)
                    .doubleValue();
        } else {
            this.progressPercentage = 0.0;
        }

        this.contributorsCount = contributorsCount;
        this.totalDonationsCount = totalDonationsCount;
        this.totalUtilizationsCount = totalUtilizationsCount;
    }

    // Getters and Setters

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

    public BigDecimal getVerifiedUtilizedAmount() {
        return verifiedUtilizedAmount;
    }

    public void setVerifiedUtilizedAmount(BigDecimal verifiedUtilizedAmount) {
        this.verifiedUtilizedAmount = verifiedUtilizedAmount;
    }

    public BigDecimal getPendingUtilizedAmount() {
        return pendingUtilizedAmount;
    }

    public void setPendingUtilizedAmount(BigDecimal pendingUtilizedAmount) {
        this.pendingUtilizedAmount = pendingUtilizedAmount;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public long getContributorsCount() {
        return contributorsCount;
    }

    public void setContributorsCount(long contributorsCount) {
        this.contributorsCount = contributorsCount;
    }

    public long getTotalDonationsCount() {
        return totalDonationsCount;
    }

    public void setTotalDonationsCount(long totalDonationsCount) {
        this.totalDonationsCount = totalDonationsCount;
    }

    public long getTotalUtilizationsCount() {
        return totalUtilizationsCount;
    }

    public void setTotalUtilizationsCount(long totalUtilizationsCount) {
        this.totalUtilizationsCount = totalUtilizationsCount;
    }
}
