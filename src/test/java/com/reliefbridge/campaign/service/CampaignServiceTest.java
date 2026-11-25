package com.reliefbridge.campaign.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

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

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private DisasterRepository disasterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private FundUtilizationRepository fundUtilizationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CampaignService campaignService;

    private User ngoUser;
    private Disaster disaster;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        ngoUser = new User();
        ngoUser.setId(10L);
        ngoUser.setName("Relief NGO");
        ngoUser.setEmail("ngo@test.com");
        ngoUser.setRole(Role.NGO);
        ngoUser.setApproved(true);

        disaster = new Disaster();
        disaster.setId(1L);
        disaster.setTitle("Flood Relief Operation");

        campaign = new Campaign();
        campaign.setId(100L);
        campaign.setTitle("Flood Victim Emergency Food Drive");
        campaign.setDescription("Emergency food supplies distribution.");
        campaign.setDisaster(disaster);
        campaign.setNgo(ngoUser);
        campaign.setCategory(CampaignCategory.FOOD_AND_WATER);
        campaign.setTargetAmount(BigDecimal.valueOf(500000.00));
        campaign.setCollectedAmount(BigDecimal.valueOf(150000.00));
        campaign.setLocation("Assam, India");
        campaign.setStatus(CampaignStatus.ACTIVE);
    }

    @Test
    void testCreateCampaign_Success() {
        CampaignRequest request = new CampaignRequest();
        request.setTitle("New Relief Drive");
        request.setDescription("Providing immediate supplies");
        request.setDisasterId(1L);
        request.setCategory(CampaignCategory.EMERGENCY_RESCUE);
        request.setTargetAmount(BigDecimal.valueOf(250000.00));
        request.setLocation("Guwahati");

        when(userRepository.findById(10L)).thenReturn(Optional.of(ngoUser));
        when(disasterRepository.findById(1L)).thenReturn(Optional.of(disaster));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(i -> {
            Campaign c = i.getArgument(0);
            c.setId(101L);
            return c;
        });

        CampaignResponse response = campaignService.createCampaign(request, 10L);

        assertNotNull(response);
        assertEquals("New Relief Drive", response.getTitle());
        assertEquals(101L, response.getId());
        verify(campaignRepository).save(any(Campaign.class));
    }

    @Test
    void testCreateCampaign_UnapprovedNgo_ThrowsAccessDenied() {
        ngoUser.setApproved(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(ngoUser));

        CampaignRequest request = new CampaignRequest();
        request.setTitle("Unapproved NGO Drive");
        request.setDisasterId(1L);

        assertThrows(AccessDeniedException.class, () -> campaignService.createCampaign(request, 10L));
    }

    @Test
    void testUpdateCampaignStatus_Success() {
        when(campaignRepository.findById(100L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(10L)).thenReturn(Optional.of(ngoUser));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(campaign);

        CampaignStatusUpdateRequest statusReq = new CampaignStatusUpdateRequest();
        statusReq.setStatus(CampaignStatus.COMPLETED);

        CampaignResponse response = campaignService.updateCampaignStatus(100L, statusReq, 10L);

        assertNotNull(response);
        assertEquals(CampaignStatus.COMPLETED, response.getStatus());
    }

    @Test
    void testUpdateCampaignStatus_NonOwner_ThrowsAccessDenied() {
        User otherNgo = new User();
        otherNgo.setId(99L);
        otherNgo.setRole(Role.NGO);

        when(campaignRepository.findById(100L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(99L)).thenReturn(Optional.of(otherNgo));

        CampaignStatusUpdateRequest statusReq = new CampaignStatusUpdateRequest();
        statusReq.setStatus(CampaignStatus.CANCELLED);

        assertThrows(AccessDeniedException.class, () -> campaignService.updateCampaignStatus(100L, statusReq, 99L));
    }

    @Test
    void testGetCampaignFinancials() {
        when(campaignRepository.findById(100L)).thenReturn(Optional.of(campaign));
        when(donationRepository.sumAmountByCampaignIdAndStatus(100L, DonationStatus.SUCCESS))
                .thenReturn(BigDecimal.valueOf(150000.00));
        when(fundUtilizationRepository.sumAmountByCampaignIdAndStatus(100L, UtilizationStatus.VERIFIED))
                .thenReturn(BigDecimal.valueOf(50000.00));
        when(fundUtilizationRepository.sumAmountByCampaignIdAndStatus(100L, UtilizationStatus.PENDING))
                .thenReturn(BigDecimal.valueOf(10000.00));
        when(donationRepository.countContributorsByCampaignIdAndStatus(100L, DonationStatus.SUCCESS)).thenReturn(25L);
        when(donationRepository.countByCampaignIdAndStatus(100L, DonationStatus.SUCCESS)).thenReturn(30L);
        when(fundUtilizationRepository.countByCampaignIdAndStatus(100L, UtilizationStatus.VERIFIED)).thenReturn(3L);

        CampaignFinancialSummary summary = campaignService.getCampaignFinancials(100L);

        assertNotNull(summary);
        assertEquals(BigDecimal.valueOf(150000.00), summary.getCollectedAmount());
        assertEquals(BigDecimal.valueOf(50000.00), summary.getVerifiedUtilizedAmount());
        assertEquals(BigDecimal.valueOf(100000.00), summary.getRemainingBalance());
        assertEquals(25L, summary.getContributorsCount());
        assertEquals(30.0, summary.getProgressPercentage(), 0.01);
    }
}
