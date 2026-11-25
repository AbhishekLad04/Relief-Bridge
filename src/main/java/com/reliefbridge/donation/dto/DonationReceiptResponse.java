package com.reliefbridge.donation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.reliefbridge.donation.entity.Donation;

public class DonationReceiptResponse {

    private String platformName = "ReliefBridge Humanitarian Aid Platform";
    private String platformTagline = "Connecting Compassion to Crisis";
    private String receiptNumber;
    private String donationReference;
    private Long donationId;
    private String donorName;
    private String donorEmail;
    private String campaignTitle;
    private String disasterTitle;
    private String ngoName;
    private BigDecimal amount;
    private String currency = "INR";
    private String paymentMethod;
    private String transactionReference;
    private LocalDateTime paymentDate;
    private String paymentStatus;

    public DonationReceiptResponse() {
    }

    public DonationReceiptResponse(Donation donation) {
        this.receiptNumber = donation.getReceiptNumber();
        this.donationReference = donation.getDonationReference();
        this.donationId = donation.getId();
        this.donorName = donation.isAnonymous() ? "Anonymous Supporter" : donation.getDonorName();
        this.donorEmail = donation.isAnonymous() ? null : donation.getDonorEmail();

        if (donation.getCampaign() != null) {
            this.campaignTitle = donation.getCampaign().getTitle();
            if (donation.getCampaign().getDisaster() != null) {
                this.disasterTitle = donation.getCampaign().getDisaster().getTitle();
            }
            if (donation.getCampaign().getNgo() != null) {
                this.ngoName = donation.getCampaign().getNgo().getName();
            }
        }

        this.amount = donation.getAmount();
        this.paymentMethod = donation.getPaymentMethod() != null ? donation.getPaymentMethod().name() : "RAZORPAY";
        this.transactionReference = donation.getRazorpayPaymentId() != null ? donation.getRazorpayPaymentId() : donation.getDonationReference();
        this.paymentDate = donation.getCompletedAt() != null ? donation.getCompletedAt() : donation.getCreatedAt();
        this.paymentStatus = donation.getStatus() != null ? donation.getStatus().name() : "SUCCESS";
    }

    // Getters and Setters

    public String getPlatformName() {
        return platformName;
    }

    public String getPlatformTagline() {
        return platformTagline;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getDonationReference() {
        return donationReference;
    }

    public void setDonationReference(String donationReference) {
        this.donationReference = donationReference;
    }

    public Long getDonationId() {
        return donationId;
    }

    public void setDonationId(Long donationId) {
        this.donationId = donationId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorEmail() {
        return donorEmail;
    }

    public void setDonorEmail(String donorEmail) {
        this.donorEmail = donorEmail;
    }

    public String getCampaignTitle() {
        return campaignTitle;
    }

    public void setCampaignTitle(String campaignTitle) {
        this.campaignTitle = campaignTitle;
    }

    public String getDisasterTitle() {
        return disasterTitle;
    }

    public void setDisasterTitle(String disasterTitle) {
        this.disasterTitle = disasterTitle;
    }

    public String getNgoName() {
        return ngoName;
    }

    public void setNgoName(String ngoName) {
        this.ngoName = ngoName;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
