package com.reliefbridge.donation.dto;

import java.math.BigDecimal;

public class OverallDonationStatsResponse {

    private BigDecimal totalFundsCollected;
    private BigDecimal totalFundsUtilized;
    private BigDecimal totalAvailableFunds;
    private long totalDonationsCount;
    private long totalContributorsCount;
    private long activeCampaignsCount;

    public OverallDonationStatsResponse() {
    }

    public OverallDonationStatsResponse(
            BigDecimal totalFundsCollected,
            BigDecimal totalFundsUtilized,
            long totalDonationsCount,
            long totalContributorsCount,
            long activeCampaignsCount) {

        this.totalFundsCollected = totalFundsCollected != null ? totalFundsCollected : BigDecimal.ZERO;
        this.totalFundsUtilized = totalFundsUtilized != null ? totalFundsUtilized : BigDecimal.ZERO;
        this.totalAvailableFunds = this.totalFundsCollected.subtract(this.totalFundsUtilized);
        if (this.totalAvailableFunds.compareTo(BigDecimal.ZERO) < 0) {
            this.totalAvailableFunds = BigDecimal.ZERO;
        }

        this.totalDonationsCount = totalDonationsCount;
        this.totalContributorsCount = totalContributorsCount;
        this.activeCampaignsCount = activeCampaignsCount;
    }

    public BigDecimal getTotalFundsCollected() {
        return totalFundsCollected;
    }

    public void setTotalFundsCollected(BigDecimal totalFundsCollected) {
        this.totalFundsCollected = totalFundsCollected;
    }

    public BigDecimal getTotalFundsUtilized() {
        return totalFundsUtilized;
    }

    public void setTotalFundsUtilized(BigDecimal totalFundsUtilized) {
        this.totalFundsUtilized = totalFundsUtilized;
    }

    public BigDecimal getTotalAvailableFunds() {
        return totalAvailableFunds;
    }

    public void setTotalAvailableFunds(BigDecimal totalAvailableFunds) {
        this.totalAvailableFunds = totalAvailableFunds;
    }

    public long getTotalDonationsCount() {
        return totalDonationsCount;
    }

    public void setTotalDonationsCount(long totalDonationsCount) {
        this.totalDonationsCount = totalDonationsCount;
    }

    public long getTotalContributorsCount() {
        return totalContributorsCount;
    }

    public void setTotalContributorsCount(long totalContributorsCount) {
        this.totalContributorsCount = totalContributorsCount;
    }

    public long getActiveCampaignsCount() {
        return activeCampaignsCount;
    }

    public void setActiveCampaignsCount(long activeCampaignsCount) {
        this.activeCampaignsCount = activeCampaignsCount;
    }
}
