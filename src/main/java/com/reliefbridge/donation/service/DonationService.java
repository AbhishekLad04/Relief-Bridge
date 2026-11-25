package com.reliefbridge.donation.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;
import com.reliefbridge.campaign.entity.Campaign;
import com.reliefbridge.campaign.entity.CampaignStatus;
import com.reliefbridge.campaign.repository.CampaignRepository;
import com.reliefbridge.donation.dto.CreateDonationOrderRequest;
import com.reliefbridge.donation.dto.DonationOrderResponse;
import com.reliefbridge.donation.dto.DonationReceiptResponse;
import com.reliefbridge.donation.dto.DonationResponse;
import com.reliefbridge.donation.dto.OverallDonationStatsResponse;
import com.reliefbridge.donation.dto.VerifyPaymentRequest;
import com.reliefbridge.donation.entity.Donation;
import com.reliefbridge.donation.entity.DonationStatus;
import com.reliefbridge.donation.entity.PaymentMethod;
import com.reliefbridge.donation.repository.DonationRepository;
import com.reliefbridge.donation.repository.FundUtilizationRepository;
import com.reliefbridge.notification.entity.NotificationType;
import com.reliefbridge.notification.event.ReliefBridgeNotificationEvent;

@Service
@Transactional
public class DonationService {

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final FundUtilizationRepository fundUtilizationRepository;
    private final UserRepository userRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final ApplicationEventPublisher eventPublisher;

    public DonationService(
            DonationRepository donationRepository,
            CampaignRepository campaignRepository,
            FundUtilizationRepository fundUtilizationRepository,
            UserRepository userRepository,
            PaymentGatewayService paymentGatewayService,
            ApplicationEventPublisher eventPublisher) {

        this.donationRepository = donationRepository;
        this.campaignRepository = campaignRepository;
        this.fundUtilizationRepository = fundUtilizationRepository;
        this.userRepository = userRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.eventPublisher = eventPublisher;
    }

    // 1. INITIATE DONATION & CREATE ORDER
    public DonationOrderResponse initiateDonation(CreateDonationOrderRequest request, User currentUser) {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found with ID: " + request.getCampaignId()));

        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new IllegalArgumentException("This campaign is not currently active for donations.");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.valueOf(10.00)) < 0) {
            throw new IllegalArgumentException("Minimum donation amount is ₹10.00");
        }

        String refCode = "DON-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

        Donation donation = new Donation();
        donation.setDonationReference(refCode);
        donation.setCampaign(campaign);
        donation.setDonor(currentUser);

        String donorName = request.getDonorName() != null && !request.getDonorName().isBlank()
                ? request.getDonorName().trim()
                : (currentUser != null ? currentUser.getName() : "Anonymous Donor");

        String donorEmail = request.getDonorEmail() != null && !request.getDonorEmail().isBlank()
                ? request.getDonorEmail().trim()
                : (currentUser != null ? currentUser.getEmail() : null);

        donation.setDonorName(donorName);
        donation.setDonorEmail(donorEmail);
        donation.setDonorPhone(request.getDonorPhone());
        donation.setAmount(request.getAmount());
        donation.setMessage(request.getMessage());
        donation.setAnonymous(request.isAnonymous());
        donation.setStatus(DonationStatus.PENDING);
        donation.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.RAZORPAY);

        DonationOrderResponse orderResponse = paymentGatewayService.createOrder(
                refCode, request.getAmount(), campaign.getTitle());

        donation.setRazorpayOrderId(orderResponse.getOrderId());
        donationRepository.save(donation);

        return orderResponse;
    }

    // 2. VERIFY PAYMENT SIGNATURE & COMPLETE DONATION
    public DonationResponse verifyPayment(VerifyPaymentRequest request, User currentUser) {
        Donation donation = donationRepository.findByDonationReference(request.getDonationReference())
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with reference: " + request.getDonationReference()));

        // Idempotency check: if already verified, return existing record
        if (donation.getStatus() == DonationStatus.SUCCESS) {
            return new DonationResponse(donation);
        }

        if (donation.getStatus() != DonationStatus.PENDING) {
            throw new IllegalArgumentException("Donation order is in an invalid state for verification: " + donation.getStatus());
        }

        // Verify cryptographic signature
        boolean isValid = paymentGatewayService.verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            donation.setStatus(DonationStatus.FAILED);
            donation.setRazorpayPaymentId(request.getRazorpayPaymentId());
            donation.setRazorpaySignature(request.getRazorpaySignature());
            donationRepository.save(donation);
            throw new IllegalArgumentException("Payment signature verification failed. Donation marked as failed.");
        }

        // Verification successful
        donation.setStatus(DonationStatus.SUCCESS);
        donation.setRazorpayPaymentId(request.getRazorpayPaymentId());
        donation.setRazorpaySignature(request.getRazorpaySignature());
        donation.setCompletedAt(LocalDateTime.now());

        String receiptNo = "REC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + String.format("%06d", donation.getId());
        donation.setReceiptNumber(receiptNo);

        // Update Campaign Collected Amount
        Campaign campaign = donation.getCampaign();
        BigDecimal currentCollected = campaign.getCollectedAmount() != null ? campaign.getCollectedAmount() : BigDecimal.ZERO;
        campaign.setCollectedAmount(currentCollected.add(donation.getAmount()));
        campaignRepository.save(campaign);

        // Update Donor user status if citizen
        if (donation.getDonor() != null) {
            User donorUser = donation.getDonor();
            if (!donorUser.isDonor()) {
                donorUser.setDonor(true);
                userRepository.save(donorUser);
            }
        }

        Donation savedDonation = donationRepository.save(donation);

        // Dispatch notifications
        if (savedDonation.getDonor() != null) {
            eventPublisher.publishEvent(new ReliefBridgeNotificationEvent(
                    savedDonation.getDonor(),
                    "Donation Successful! Receipt Available",
                    "Thank you for your generous contribution of Rs. " + savedDonation.getAmount() +
                            " to '" + campaign.getTitle() + "'. Receipt #" + receiptNo + " is now ready.",
                    NotificationType.DONATION,
                    "DONATION",
                    savedDonation.getId()
            ));
        }

        if (campaign.getNgo() != null) {
            String donorDisplayName = savedDonation.isAnonymous() ? "An anonymous donor" : savedDonation.getDonorName();
            eventPublisher.publishEvent(new ReliefBridgeNotificationEvent(
                    campaign.getNgo(),
                    "New Campaign Donation Received",
                    donorDisplayName + " donated Rs. " + savedDonation.getAmount() + " to '" + campaign.getTitle() + "'.",
                    NotificationType.DONATION,
                    "CAMPAIGN",
                    campaign.getId()
            ));
        }

        return new DonationResponse(savedDonation);
    }

    // 3. GET DONATION BY REFERENCE
    @Transactional(readOnly = true)
    public DonationResponse getDonationByReference(String reference, User currentUser) {
        Donation donation = donationRepository.findByDonationReference(reference)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with reference: " + reference));

        return new DonationResponse(donation);
    }

    // 4. GET DONATION RECEIPT
    @Transactional(readOnly = true)
    public DonationReceiptResponse getReceipt(String receiptOrRef, User currentUser) {
        Donation donation = donationRepository.findByReceiptNumber(receiptOrRef)
                .or(() -> donationRepository.findByDonationReference(receiptOrRef))
                .orElseThrow(() -> new IllegalArgumentException("Donation receipt not found for identifier: " + receiptOrRef));

        if (donation.getStatus() != DonationStatus.SUCCESS) {
            throw new IllegalArgumentException("Receipt is only generated for successful donations.");
        }

        return new DonationReceiptResponse(donation);
    }

    // 5. GET MY DONATIONS (CITIZEN)
    @Transactional(readOnly = true)
    public Page<DonationResponse> getMyDonations(User currentUser, Pageable pageable) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required.");
        }
        return donationRepository.findByDonorIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(DonationResponse::new);
    }

    // 6. GET CAMPAIGN DONATIONS (PUBLIC / NGO / ADMIN)
    @Transactional(readOnly = true)
    public Page<DonationResponse> getCampaignDonations(Long campaignId, Pageable pageable) {
        return donationRepository.findByCampaignIdAndStatusOrderByCreatedAtDesc(
                campaignId, DonationStatus.SUCCESS, pageable)
                .map(DonationResponse::new);
    }

    // 7. GET NGO CAMPAIGN DONATIONS
    @Transactional(readOnly = true)
    public Page<DonationResponse> getNgoCampaignDonations(User currentUser, Pageable pageable) {
        if (currentUser == null || (currentUser.getRole() != Role.NGO && currentUser.getRole() != Role.ADMIN)) {
            throw new AccessDeniedException("Only NGOs and administrators can view NGO campaign donations.");
        }
        return donationRepository.findByCampaignNgoIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(DonationResponse::new);
    }

    // 8. GET ALL DONATIONS (ADMIN ONLY)
    @Transactional(readOnly = true)
    public Page<DonationResponse> getAllDonations(Long campaignId, DonationStatus status, Pageable pageable, User currentUser) {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can view full donation records.");
        }
        org.springframework.data.jpa.domain.Specification<Donation> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (campaignId != null) {
                predicates.add(cb.equal(root.get("campaign").get("id"), campaignId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return donationRepository.findAll(spec, pageable).map(DonationResponse::new);
    }

    // 9. OVERALL STATS (PUBLIC / ADMIN)
    @Transactional(readOnly = true)
    public OverallDonationStatsResponse getOverallDonationStats() {
        BigDecimal totalCollected = donationRepository.sumAmountByStatus(DonationStatus.SUCCESS);
        BigDecimal totalUtilized = fundUtilizationRepository.sumAmountByStatus(com.reliefbridge.donation.entity.UtilizationStatus.VERIFIED);
        long totalDonations = donationRepository.countByStatus(DonationStatus.SUCCESS);
        long activeCampaigns = campaignRepository.countByStatus(CampaignStatus.ACTIVE);

        return new OverallDonationStatsResponse(
                totalCollected != null ? totalCollected : BigDecimal.ZERO,
                totalUtilized != null ? totalUtilized : BigDecimal.ZERO,
                totalDonations,
                totalDonations, // Contributors estimate
                activeCampaigns
        );
    }
}
