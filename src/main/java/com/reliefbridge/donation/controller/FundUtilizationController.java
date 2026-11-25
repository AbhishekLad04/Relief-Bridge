package com.reliefbridge.donation.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reliefbridge.auth.entity.User;
import com.reliefbridge.donation.dto.FundUtilizationRequest;
import com.reliefbridge.donation.dto.FundUtilizationResponse;
import com.reliefbridge.donation.dto.VerifyUtilizationRequest;
import com.reliefbridge.donation.service.FundUtilizationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fund-utilizations")
public class FundUtilizationController {

    private final FundUtilizationService fundUtilizationService;

    public FundUtilizationController(FundUtilizationService fundUtilizationService) {
        this.fundUtilizationService = fundUtilizationService;
    }

    // 1. SUBMIT FUND UTILIZATION (NGO ONLY)
    @PostMapping
    public ResponseEntity<FundUtilizationResponse> submitUtilization(
            @Valid @RequestBody FundUtilizationRequest request,
            @AuthenticationPrincipal User currentUser) {

        FundUtilizationResponse response = fundUtilizationService.submitUtilization(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. VERIFY / REJECT FUND UTILIZATION (ADMIN ONLY)
    @PatchMapping("/{id}/verify")
    public ResponseEntity<FundUtilizationResponse> verifyUtilization(
            @PathVariable Long id,
            @Valid @RequestBody VerifyUtilizationRequest request,
            @AuthenticationPrincipal User currentUser) {

        FundUtilizationResponse response = fundUtilizationService.verifyUtilization(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    // 3. GET VERIFIED EXPENSES FOR CAMPAIGN (PUBLIC TRANSPARENCY)
    @GetMapping("/campaign/{campaignId}/verified")
    public ResponseEntity<List<FundUtilizationResponse>> getVerifiedCampaignUtilizations(
            @PathVariable Long campaignId) {

        List<FundUtilizationResponse> utilizations =
                fundUtilizationService.getVerifiedCampaignUtilizations(campaignId);
        return ResponseEntity.ok(utilizations);
    }

    // 4. GET ALL EXPENSES FOR CAMPAIGN (NGO / ADMIN)
    @GetMapping("/campaign/{campaignId}/all")
    public ResponseEntity<Page<FundUtilizationResponse>> getAllCampaignUtilizations(
            @PathVariable Long campaignId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
        Page<FundUtilizationResponse> utilizations =
                fundUtilizationService.getAllCampaignUtilizations(campaignId, pageRequest);
        return ResponseEntity.ok(utilizations);
    }

    // 5. GET MY NGO EXPENSES (NGO)
    @GetMapping("/my")
    public ResponseEntity<Page<FundUtilizationResponse>> getMyNgoUtilizations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
        Page<FundUtilizationResponse> utilizations =
                fundUtilizationService.getMyNgoUtilizations(currentUser, pageRequest);
        return ResponseEntity.ok(utilizations);
    }

    // 6. GET PENDING SUBMISSIONS (ADMIN ONLY)
    @GetMapping("/pending")
    public ResponseEntity<Page<FundUtilizationResponse>> getPendingUtilizations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FundUtilizationResponse> utilizations =
                fundUtilizationService.getPendingUtilizations(currentUser, pageRequest);
        return ResponseEntity.ok(utilizations);
    }

    // 7. GET CATEGORY BREAKDOWN (PUBLIC TRANSPARENCY)
    @GetMapping("/campaign/{campaignId}/categories")
    public ResponseEntity<Map<String, BigDecimal>> getExpenseCategoryBreakdown(
            @PathVariable Long campaignId) {

        Map<String, BigDecimal> breakdown =
                fundUtilizationService.getExpenseCategoryBreakdown(campaignId);
        return ResponseEntity.ok(breakdown);
    }
}
