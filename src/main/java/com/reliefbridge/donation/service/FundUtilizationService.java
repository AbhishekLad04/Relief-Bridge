package com.reliefbridge.donation.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.campaign.entity.Campaign;
import com.reliefbridge.campaign.repository.CampaignRepository;
import com.reliefbridge.donation.dto.FundUtilizationRequest;
import com.reliefbridge.donation.dto.FundUtilizationResponse;
import com.reliefbridge.donation.dto.VerifyUtilizationRequest;
import com.reliefbridge.donation.entity.DonationStatus;
import com.reliefbridge.donation.entity.ExpenseCategory;
import com.reliefbridge.donation.entity.FundUtilization;
import com.reliefbridge.donation.entity.UtilizationStatus;
import com.reliefbridge.donation.repository.DonationRepository;
import com.reliefbridge.donation.repository.FundUtilizationRepository;
import com.reliefbridge.notification.entity.NotificationType;
import com.reliefbridge.notification.event.ReliefBridgeNotificationEvent;

@Service
@Transactional
public class FundUtilizationService {

    private final FundUtilizationRepository fundUtilizationRepository;
    private final CampaignRepository campaignRepository;
    private final DonationRepository donationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FundUtilizationService(
            FundUtilizationRepository fundUtilizationRepository,
            CampaignRepository campaignRepository,
            DonationRepository donationRepository,
            ApplicationEventPublisher eventPublisher) {

        this.fundUtilizationRepository = fundUtilizationRepository;
        this.campaignRepository = campaignRepository;
        this.donationRepository = donationRepository;
        this.eventPublisher = eventPublisher;
    }

    // 1. SUBMIT FUND UTILIZATION (NGO ONLY)
    public FundUtilizationResponse submitUtilization(FundUtilizationRequest request, User currentUser) {
        if (currentUser == null || (currentUser.getRole() != Role.NGO && currentUser.getRole() != Role.ADMIN)) {
            throw new AccessDeniedException("Only authorized NGOs can submit fund utilization records.");
        }

        if (currentUser.getRole() == Role.NGO && !currentUser.isApproved()) {
            throw new AccessDeniedException("Your NGO account is pending approval.");
        }

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found with ID: " + request.getCampaignId()));

        if (!campaign.getNgo().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You can only submit fund utilization records for your own campaigns.");
        }

        // Strict financial check: Prevent overspending beyond collected donations
        BigDecimal collected = donationRepository.sumAmountByCampaignIdAndStatus(campaign.getId(), DonationStatus.SUCCESS);
        if (collected == null) collected = BigDecimal.ZERO;

        BigDecimal verified = fundUtilizationRepository.sumAmountByCampaignIdAndStatus(campaign.getId(), UtilizationStatus.VERIFIED);
        if (verified == null) verified = BigDecimal.ZERO;

        BigDecimal availableFunds = collected.subtract(verified);

        if (request.getAmount().compareTo(availableFunds) > 0) {
            throw new IllegalArgumentException("Expense amount (₹" + request.getAmount() +
                    ") exceeds current available campaign funds (₹" + availableFunds + "). Overspending is not permitted.");
        }

        FundUtilization fu = new FundUtilization();
        fu.setCampaign(campaign);
        fu.setNgo(currentUser);
        fu.setCategory(request.getCategory());
        fu.setDescription(request.getDescription().trim());
        fu.setAmount(request.getAmount());
        fu.setExpenseDate(request.getExpenseDate());
        fu.setInvoiceNumber(request.getInvoiceNumber());
        fu.setInvoiceUrl(request.getInvoiceUrl());
        fu.setPhotoUrl(request.getPhotoUrl());
        fu.setStatus(UtilizationStatus.PENDING);

        FundUtilization saved = fundUtilizationRepository.save(fu);

        // Notify Admins
        eventPublisher.publishEvent(new ReliefBridgeNotificationEvent(
                campaign.getNgo(),
                "Fund Utilization Submitted",
                "Your expense submission of ₹" + saved.getAmount() + " for '" + campaign.getTitle() +
                        "' was submitted for administrative verification.",
                NotificationType.UTILIZATION,
                "UTILIZATION",
                saved.getId()
        ));

        return new FundUtilizationResponse(saved);
    }

    // 2. VERIFY / REJECT FUND UTILIZATION (ADMIN ONLY)
    public FundUtilizationResponse verifyUtilization(Long utilizationId, VerifyUtilizationRequest request, User adminUser) {
        if (adminUser == null || adminUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can verify or reject fund utilization records.");
        }

        FundUtilization fu = fundUtilizationRepository.findById(utilizationId)
                .orElseThrow(() -> new IllegalArgumentException("Fund utilization record not found with ID: " + utilizationId));

        if (fu.getStatus() == UtilizationStatus.VERIFIED) {
            throw new IllegalArgumentException("This financial record is already verified and cannot be altered.");
        }

        fu.setStatus(request.getStatus());
        fu.setVerifiedBy(adminUser);
        fu.setVerifiedAt(LocalDateTime.now());

        if (request.getStatus() == UtilizationStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting a utilization record.");
            }
            fu.setRejectionReason(request.getRejectionReason().trim());
        } else {
            fu.setRejectionReason(null);
        }

        FundUtilization updated = fundUtilizationRepository.save(fu);

        // Notify NGO
        String statusText = updated.getStatus() == UtilizationStatus.VERIFIED ? "Approved & Verified" : "Rejected";
        eventPublisher.publishEvent(new ReliefBridgeNotificationEvent(
                updated.getNgo(),
                "Fund Utilization " + statusText,
                "Expense record of ₹" + updated.getAmount() + " for '" + updated.getCampaign().getTitle() +
                        "' was marked as " + statusText + (updated.getRejectionReason() != null ? ": " + updated.getRejectionReason() : "."),
                NotificationType.UTILIZATION,
                "UTILIZATION",
                updated.getId()
        ));

        return new FundUtilizationResponse(updated);
    }

    // 3. GET CAMPAIGN UTILIZATIONS (PUBLIC TRANSPARENCY)
    @Transactional(readOnly = true)
    public List<FundUtilizationResponse> getVerifiedCampaignUtilizations(Long campaignId) {
        return fundUtilizationRepository.findByCampaignIdAndStatusOrderByExpenseDateDesc(
                campaignId, UtilizationStatus.VERIFIED)
                .stream()
                .map(FundUtilizationResponse::new)
                .collect(Collectors.toList());
    }

    // 4. GET ALL CAMPAIGN UTILIZATIONS (NGO / ADMIN)
    @Transactional(readOnly = true)
    public Page<FundUtilizationResponse> getAllCampaignUtilizations(Long campaignId, Pageable pageable) {
        return fundUtilizationRepository.findByCampaignIdOrderByExpenseDateDesc(campaignId, pageable)
                .map(FundUtilizationResponse::new);
    }

    // 5. GET MY NGO UTILIZATIONS (NGO)
    @Transactional(readOnly = true)
    public Page<FundUtilizationResponse> getMyNgoUtilizations(User currentUser, Pageable pageable) {
        if (currentUser == null || (currentUser.getRole() != Role.NGO && currentUser.getRole() != Role.ADMIN)) {
            throw new AccessDeniedException("Authentication required.");
        }
        return fundUtilizationRepository.findByNgoIdOrderByExpenseDateDesc(currentUser.getId(), pageable)
                .map(FundUtilizationResponse::new);
    }

    // 6. GET PENDING UTILIZATIONS (ADMIN ONLY)
    @Transactional(readOnly = true)
    public Page<FundUtilizationResponse> getPendingUtilizations(User currentUser, Pageable pageable) {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can review pending utilization submissions.");
        }
        return fundUtilizationRepository.findByStatusOrderByCreatedAtDesc(UtilizationStatus.PENDING, pageable)
                .map(FundUtilizationResponse::new);
    }

    // 7. GET CATEGORY BREAKDOWN FOR CAMPAIGN
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getExpenseCategoryBreakdown(Long campaignId) {
        List<Object[]> results = fundUtilizationRepository.sumAmountByCategoryAndCampaign(
                campaignId, UtilizationStatus.VERIFIED);

        Map<String, BigDecimal> breakdown = new HashMap<>();
        for (Object[] row : results) {
            ExpenseCategory cat = (ExpenseCategory) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            breakdown.put(cat.name(), amount);
        }
        return breakdown;
    }
}
