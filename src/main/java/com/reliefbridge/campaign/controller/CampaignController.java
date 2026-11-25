package com.reliefbridge.campaign.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reliefbridge.auth.entity.User;
import com.reliefbridge.campaign.dto.CampaignFinancialSummary;
import com.reliefbridge.campaign.dto.CampaignRequest;
import com.reliefbridge.campaign.dto.CampaignResponse;
import com.reliefbridge.campaign.dto.CampaignStatusUpdateRequest;
import com.reliefbridge.campaign.entity.CampaignCategory;
import com.reliefbridge.campaign.entity.CampaignStatus;
import com.reliefbridge.campaign.service.CampaignService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    // 1. CREATE CAMPAIGN (NGO / ADMIN)
    @PostMapping
    public ResponseEntity<CampaignResponse> createCampaign(
            @Valid @RequestBody CampaignRequest request,
            @AuthenticationPrincipal User currentUser) {

        CampaignResponse response = campaignService.createCampaign(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. SEARCH & LIST CAMPAIGNS (PUBLIC)
    @GetMapping
    public ResponseEntity<Page<CampaignResponse>> searchCampaigns(
            @RequestParam(required = false) CampaignStatus status,
            @RequestParam(required = false) CampaignCategory category,
            @RequestParam(required = false) Long disasterId,
            @RequestParam(required = false) Long ngoId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<CampaignResponse> campaigns = campaignService.searchCampaigns(
                status, category, disasterId, ngoId, keyword, pageRequest);

        return ResponseEntity.ok(campaigns);
    }

    // 3. GET MY CAMPAIGNS (NGO)
    @GetMapping("/my")
    public ResponseEntity<List<CampaignResponse>> getMyCampaigns(
            @AuthenticationPrincipal User currentUser) {

        List<CampaignResponse> campaigns = campaignService.getMyCampaigns(currentUser.getId());
        return ResponseEntity.ok(campaigns);
    }

    // 4. GET CAMPAIGN BY ID (PUBLIC)
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable Long id) {
        CampaignResponse campaign = campaignService.getCampaignById(id);
        return ResponseEntity.ok(campaign);
    }

    // 5. UPDATE CAMPAIGN (NGO / ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request,
            @AuthenticationPrincipal User currentUser) {

        CampaignResponse response = campaignService.updateCampaign(id, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // 6. UPDATE CAMPAIGN STATUS (NGO / ADMIN)
    @PatchMapping("/{id}/status")
    public ResponseEntity<CampaignResponse> updateCampaignStatus(
            @PathVariable Long id,
            @Valid @RequestBody CampaignStatusUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {

        CampaignResponse response = campaignService.updateCampaignStatus(id, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // 7. GET CAMPAIGN FINANCIAL SUMMARY (PUBLIC)
    @GetMapping("/{id}/financials")
    public ResponseEntity<CampaignFinancialSummary> getCampaignFinancials(@PathVariable Long id) {
        CampaignFinancialSummary summary = campaignService.getCampaignFinancials(id);
        return ResponseEntity.ok(summary);
    }
}
