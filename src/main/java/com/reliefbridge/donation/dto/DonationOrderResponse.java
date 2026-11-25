package com.reliefbridge.donation.dto;

import java.math.BigDecimal;

public class DonationOrderResponse {

    private String donationReference;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private String campaignTitle;
    private String companyName;
    private boolean isMockMode;

    public DonationOrderResponse() {
    }

    public DonationOrderResponse(
            String donationReference,
            String orderId,
            BigDecimal amount,
            String currency,
            String keyId,
            String campaignTitle,
            String companyName,
            boolean isMockMode) {
        this.donationReference = donationReference;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
        this.campaignTitle = campaignTitle;
        this.companyName = companyName;
        this.isMockMode = isMockMode;
    }

    public String getDonationReference() {
        return donationReference;
    }

    public void setDonationReference(String donationReference) {
        this.donationReference = donationReference;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getCampaignTitle() {
        return campaignTitle;
    }

    public void setCampaignTitle(String campaignTitle) {
        this.campaignTitle = campaignTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public boolean isMockMode() {
        return isMockMode;
    }

    public void setMockMode(boolean mockMode) {
        isMockMode = mockMode;
    }
}
