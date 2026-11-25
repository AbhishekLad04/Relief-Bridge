package com.reliefbridge.donation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.reliefbridge.donation.entity.Donation;
import com.reliefbridge.donation.entity.DonationStatus;
import com.reliefbridge.donation.entity.PaymentMethod;

public class DonationResponse {

    private Long id;
    private String donationReference;
    private Long campaignId;
    private String campaignTitle;
    private Long donorId;
    private String donorName;
    private String donorEmail;
    private String donorPhone;
    private BigDecimal amount;
    private String message;
    private boolean isAnonymous;
    private DonationStatus status;
    private PaymentMethod paymentMethod;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String receiptNumber;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public DonationResponse() {
    }

    public DonationResponse(Donation donation) {
        this.id = donation.getId();
        this.donationReference = donation.getDonationReference();
        if (donation.getCampaign() != null) {
            this.campaignId = donation.getCampaign().getId();
            this.campaignTitle = donation.getCampaign().getTitle();
        }
        if (donation.getDonor() != null) {
            this.donorId = donation.getDonor().getId();
        }
        this.donorName = donation.isAnonymous() ? "Anonymous Supporter" : donation.getDonorName();
        this.donorEmail = donation.isAnonymous() ? null : donation.getDonorEmail();
        this.donorPhone = donation.isAnonymous() ? null : donation.getDonorPhone();
        this.amount = donation.getAmount();
        this.message = donation.getMessage();
        this.isAnonymous = donation.isAnonymous();
        this.status = donation.getStatus();
        this.paymentMethod = donation.getPaymentMethod();
        this.razorpayOrderId = donation.getRazorpayOrderId();
        this.razorpayPaymentId = donation.getRazorpayPaymentId();
        this.receiptNumber = donation.getReceiptNumber();
        this.createdAt = donation.getCreatedAt();
        this.completedAt = donation.getCompletedAt();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDonationReference() {
        return donationReference;
    }

    public void setDonationReference(String donationReference) {
        this.donationReference = donationReference;
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

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
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

    public String getDonorPhone() {
        return donorPhone;
    }

    public void setDonorPhone(String donorPhone) {
        this.donorPhone = donorPhone;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public void setStatus(DonationStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
