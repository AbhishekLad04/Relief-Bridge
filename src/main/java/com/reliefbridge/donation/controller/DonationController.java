package com.reliefbridge.donation.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reliefbridge.auth.entity.User;
import com.reliefbridge.donation.dto.CreateDonationOrderRequest;
import com.reliefbridge.donation.dto.DonationOrderResponse;
import com.reliefbridge.donation.dto.DonationReceiptResponse;
import com.reliefbridge.donation.dto.DonationResponse;
import com.reliefbridge.donation.dto.OverallDonationStatsResponse;
import com.reliefbridge.donation.dto.VerifyPaymentRequest;
import com.reliefbridge.donation.entity.DonationStatus;
import com.reliefbridge.donation.service.DonationService;
import com.reliefbridge.donation.service.PaymentGatewayService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationService donationService;
    private final PaymentGatewayService paymentGatewayService;

    public DonationController(
            DonationService donationService,
            PaymentGatewayService paymentGatewayService) {
        this.donationService = donationService;
        this.paymentGatewayService = paymentGatewayService;
    }

    // 1. INITIATE DONATION & CREATE PAYMENT ORDER
    @PostMapping("/order")
    public ResponseEntity<DonationOrderResponse> initiateDonation(
            @Valid @RequestBody CreateDonationOrderRequest request,
            @AuthenticationPrincipal User currentUser) {

        DonationOrderResponse response = donationService.initiateDonation(request, currentUser);
        return ResponseEntity.ok(response);
    }

    // 2. VERIFY PAYMENT SIGNATURE
    @PostMapping("/verify")
    public ResponseEntity<DonationResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal User currentUser) {

        DonationResponse response = donationService.verifyPayment(request, currentUser);
        return ResponseEntity.ok(response);
    }

    // 3. GENERATE TEST SIGNATURE (SANDBOX / DEMO HELPER)
    @GetMapping("/mock-signature")
    public ResponseEntity<Map<String, String>> generateMockSignature(
            @RequestParam String orderId,
            @RequestParam String paymentId) {

        String signature = paymentGatewayService.generateMockSignature(orderId, paymentId);
        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "paymentId", paymentId,
                "signature", signature
        ));
    }

    // 4. GET DONATION BY REFERENCE
    @GetMapping("/reference/{reference}")
    public ResponseEntity<DonationResponse> getDonationByReference(
            @PathVariable String reference,
            @AuthenticationPrincipal User currentUser) {

        DonationResponse response = donationService.getDonationByReference(reference, currentUser);
        return ResponseEntity.ok(response);
    }

    // 5. GET DONATION RECEIPT (PUBLIC / DONOR)
    @GetMapping("/receipt/{receiptOrRef}")
    public ResponseEntity<DonationReceiptResponse> getReceipt(
            @PathVariable String receiptOrRef,
            @AuthenticationPrincipal User currentUser) {

        DonationReceiptResponse receipt = donationService.getReceipt(receiptOrRef, currentUser);
        return ResponseEntity.ok(receipt);
    }

    // 6. GET MY DONATIONS (CITIZEN AUTHENTICATED)
    @GetMapping("/my")
    public ResponseEntity<Page<DonationResponse>> getMyDonations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DonationResponse> donations = donationService.getMyDonations(currentUser, pageRequest);
        return ResponseEntity.ok(donations);
    }

    // 7. GET CAMPAIGN DONATIONS (PUBLIC)
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<Page<DonationResponse>> getCampaignDonations(
            @PathVariable Long campaignId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DonationResponse> donations = donationService.getCampaignDonations(campaignId, pageRequest);
        return ResponseEntity.ok(donations);
    }

    // 8. GET NGO CAMPAIGN DONATIONS (NGO AUTHENTICATED)
    @GetMapping("/ngo/my")
    public ResponseEntity<Page<DonationResponse>> getNgoCampaignDonations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DonationResponse> donations = donationService.getNgoCampaignDonations(currentUser, pageRequest);
        return ResponseEntity.ok(donations);
    }

    // 9. GET ALL DONATIONS (ADMIN ONLY)
    @GetMapping("/admin/all")
    public ResponseEntity<Page<DonationResponse>> getAllDonations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Long campaignId,
            @RequestParam(required = false) DonationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DonationResponse> donations = donationService.getAllDonations(campaignId, status, pageRequest, currentUser);
        return ResponseEntity.ok(donations);
    }

    // 10. GET OVERALL PLATFORM DONATION STATS (PUBLIC)
    @GetMapping("/stats")
    public ResponseEntity<OverallDonationStatsResponse> getOverallDonationStats() {
        OverallDonationStatsResponse stats = donationService.getOverallDonationStats();
        return ResponseEntity.ok(stats);
    }
}
