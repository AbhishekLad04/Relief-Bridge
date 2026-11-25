package com.reliefbridge.campaign.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;
import com.reliefbridge.campaign.dto.CampaignFinancialSummary;
import com.reliefbridge.campaign.dto.CampaignRequest;
import com.reliefbridge.campaign.dto.CampaignResponse;
import com.reliefbridge.campaign.dto.CampaignStatusUpdateRequest;
import com.reliefbridge.campaign.entity.Campaign;
import com.reliefbridge.campaign.entity.CampaignCategory;
import com.reliefbridge.campaign.entity.CampaignStatus;
import com.reliefbridge.campaign.repository.CampaignRepository;
import com.reliefbridge.disaster.entity.Disaster;
import com.reliefbridge.disaster.repository.DisasterRepository;
import com.reliefbridge.donation.entity.DonationStatus;
import com.reliefbridge.donation.entity.UtilizationStatus;
import com.reliefbridge.donation.repository.DonationRepository;
import com.reliefbridge.donation.repository.FundUtilizationRepository;
import com.reliefbridge.notification.entity.NotificationType;
import com.reliefbridge.notification.event.ReliefBridgeNotificationEvent;

@Service
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private final FundUtilizationRepository fundUtilizationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CampaignService(
            CampaignRepository campaignRepository,
            DisasterRepository disasterRepository,
            UserRepository userRepository,
            DonationRepository donationRepository,
            FundUtilizationRepository fundUtilizationRepository,
            ApplicationEventPublisher eventPublisher) {

        this.campaignRepository = campaignRepository;
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
        this.donationRepository = donationRepository;
        this.fundUtilizationRepository = fundUtilizationRepository;
        this.eventPublisher = eventPublisher;
    }

    // 1. CREATE CAMPAIGN
    public CampaignResponse createCampaign(CampaignRequest request, Long ngoUserId) {
        User ngo = userRepository.findById(ngoUserId)
                .orElseThrow(() -> new IllegalArgumentException("NGO user not found."));

        if (ngo.getRole() != Role.NGO && ngo.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only registered NGOs or administrators can create relief campaigns.");
        }

        if (ngo.getRole() == Role.NGO && !ngo.isApproved()) {
            throw new AccessDeniedException("Your NGO account is pending administrative approval. You cannot create campaigns yet.");
        }

        Disaster disaster = disasterRepository.findById(request.getDisasterId())
                .orElseThrow(() -> new IllegalArgumentException("Disaster not found with ID: " + request.getDisasterId()));

        if (request.getStartDate() != null && request.getEndDate() != null &&
                request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Campaign end date cannot be earlier than start date.");
        }

        Campaign campaign = new Campaign();
        campaign.setTitle(request.getTitle().trim());
        campaign.setDescription(request.getDescription().trim());
        campaign.setDisaster(disaster);
        campaign.setNgo(ngo);
        campaign.setCategory(request.getCategory());
        campaign.setTargetAmount(request.getTargetAmount());
        campaign.setCollectedAmount(BigDecimal.ZERO);
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setLocation(request.getLocation().trim());
        campaign.setRequiredResources(request.getRequiredResources());
        campaign.setImageUrl(request.getImageUrl());
        campaign.setStatus(CampaignStatus.ACTIVE);

        Campaign savedCampaign = campaignRepository.save(campaign);

        // Notify Admins
        eventPublisher.publishEvent(new ReliefBridgeNotificationEvent(
                ngo,
                "Campaign Created Successfully",
                "Your relief campaign '" + savedCampaign.getTitle() + "' is now active and open for donations.",
                NotificationType.CAMPAIGN,
                "CAMPAIGN",
                savedCampaign.getId()
        ));

        return new CampaignResponse(savedCampaign);
    }

    // 2. GET CAMPAIGN BY ID
    @Transactional(readOnly = true)
    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found with ID: " + id));

        return new CampaignResponse(campaign);
    }

    // 3. SEARCH / FILTER CAMPAIGNS (PUBLIC)
    @Transactional(readOnly = true)
    public Page<CampaignResponse> searchCampaigns(
            CampaignStatus status,
            CampaignCategory category,
            Long disasterId,
            Long ngoId,
            String keyword,
            Pageable pageable) {

        Specification<Campaign> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (disasterId != null) {
                predicates.add(cb.equal(root.get("disaster").get("id"), disasterId));
            }
            if (ngoId != null) {
                predicates.add(cb.equal(root.get("ngo").get("id"), ngoId));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("location")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return campaignRepository.findAll(spec, pageable).map(CampaignResponse::new);
    }

    // 4. GET MY CAMPAIGNS (NGO)
    @Transactional(readOnly = true)
    public List<CampaignResponse> getMyCampaigns(Long ngoUserId) {
        return campaignRepository.findByNgoIdOrderByCreatedAtDesc(ngoUserId)
                .stream()
                .map(CampaignResponse::new)
                .collect(Collectors.toList());
    }

    // 5. UPDATE CAMPAIGN
    public CampaignResponse updateCampaign(Long campaignId, CampaignRequest request, Long currentUserId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found with ID: " + campaignId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        boolean isOwner = campaign.getNgo().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to update this campaign.");
        }

        if (request.getStartDate() != null && request.getEndDate() != null &&
                request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Campaign end date cannot be earlier than start date.");
        }

        campaign.setTitle(request.getTitle().trim());
        campaign.setDescription(request.getDescription().trim());
        campaign.setCategory(request.getCategory());
        campaign.setTargetAmount(request.getTargetAmount());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setLocation(request.getLocation().trim());
        campaign.setRequiredResources(request.getRequiredResources());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            campaign.setImageUrl(request.getImageUrl());
        }

        Campaign updated = campaignRepository.save(campaign);
        return new CampaignResponse(updated);
    }

    // 6. UPDATE CAMPAIGN STATUS
    public CampaignResponse updateCampaignStatus(Long campaignId, CampaignStatusUpdateRequest request, Long currentUserId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found with ID: " + campaignId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        boolean isOwner = campaign.getNgo().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to change the status of this campaign.");
        }

        campaign.setStatus(request.getStatus());
        Campaign updated = campaignRepository.save(campaign);

        // Notify Campaign NGO
        eventPublisher.publishEvent(new ReliefBridgeNotificationEvent(
                campaign.getNgo(),
                "Campaign Status Updated",
                "Your campaign '" + campaign.getTitle() + "' status changed to " + request.getStatus(),
                NotificationType.CAMPAIGN,
                "CAMPAIGN",
                campaign.getId()
        ));

        return new CampaignResponse(updated);
    }

    // 7. GET CAMPAIGN FINANCIAL SUMMARY
    @Transactional(readOnly = true)
    public CampaignFinancialSummary getCampaignFinancials(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found with ID: " + campaignId));

        BigDecimal collected = donationRepository.sumAmountByCampaignIdAndStatus(campaignId, DonationStatus.SUCCESS);
        BigDecimal verifiedUtilized = fundUtilizationRepository.sumAmountByCampaignIdAndStatus(campaignId, UtilizationStatus.VERIFIED);
        BigDecimal pendingUtilized = fundUtilizationRepository.sumAmountByCampaignIdAndStatus(campaignId, UtilizationStatus.PENDING);
        long contributorsCount = donationRepository.countContributorsByCampaignIdAndStatus(campaignId, DonationStatus.SUCCESS);
        long totalDonations = donationRepository.countByCampaignIdAndStatus(campaignId, DonationStatus.SUCCESS);
        long totalUtilizations = fundUtilizationRepository.countByCampaignIdAndStatus(campaignId, UtilizationStatus.VERIFIED);

        return new CampaignFinancialSummary(
                campaign.getId(),
                campaign.getTitle(),
                campaign.getTargetAmount(),
                collected != null ? collected : BigDecimal.ZERO,
                verifiedUtilized != null ? verifiedUtilized : BigDecimal.ZERO,
                pendingUtilized != null ? pendingUtilized : BigDecimal.ZERO,
                contributorsCount,
                totalDonations,
                totalUtilizations
        );
    }
}
