package com.reliefbridge.donation.dto;

import java.math.BigDecimal;

import com.reliefbridge.donation.entity.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateDonationOrderRequest {

    @NotNull(message = "Campaign ID is required")
    private Long campaignId;

    @NotNull(message = "Donation amount is required")
    @DecimalMin(value = "10.00", message = "Minimum donation amount is ₹10.00")
    private BigDecimal amount;

    @NotBlank(message = "Donor name is required")
    @Size(max = 100, message = "Donor name cannot exceed 100 characters")
    private String donorName;

    private String donorEmail;

    private String donorPhone;

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message;

    private boolean isAnonymous = false;

    private PaymentMethod paymentMethod = PaymentMethod.RAZORPAY;

    public CreateDonationOrderRequest() {
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
