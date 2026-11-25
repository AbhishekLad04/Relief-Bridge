package com.reliefbridge.donation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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

@ExtendWith(MockitoExtension.class)
class FundUtilizationServiceTest {

    @Mock
    private FundUtilizationRepository fundUtilizationRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FundUtilizationService fundUtilizationService;

    private User ngoUser;
    private User adminUser;
    private Campaign campaign;
    private FundUtilization utilization;

    @BeforeEach
    void setUp() {
        ngoUser = new User();
        ngoUser.setId(20L);
        ngoUser.setRole(Role.NGO);
        ngoUser.setApproved(true);
        ngoUser.setName("Desh Seva NGO");

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);
        adminUser.setName("System Admin");

        campaign = new Campaign();
        campaign.setId(200L);
        campaign.setTitle("Flood Rescue Medical Kits");
        campaign.setNgo(ngoUser);
        campaign.setTargetAmount(BigDecimal.valueOf(100000.00));

        utilization = new FundUtilization();
        utilization.setId(500L);
        utilization.setCampaign(campaign);
        utilization.setNgo(ngoUser);
        utilization.setCategory(ExpenseCategory.MEDICAL_SUPPLIES);
        utilization.setDescription("Purchased 50 first aid kits");
        utilization.setAmount(BigDecimal.valueOf(25000.00));
        utilization.setExpenseDate(LocalDate.now());
        utilization.setStatus(UtilizationStatus.PENDING);
    }

    @Test
    void testSubmitUtilization_Success() {
        FundUtilizationRequest request = new FundUtilizationRequest();
        request.setCampaignId(200L);
        request.setCategory(ExpenseCategory.MEDICAL_SUPPLIES);
        request.setDescription("Purchased 50 first aid kits");
        request.setAmount(BigDecimal.valueOf(25000.00));
        request.setExpenseDate(LocalDate.now());

        when(campaignRepository.findById(200L)).thenReturn(Optional.of(campaign));
        when(donationRepository.sumAmountByCampaignIdAndStatus(200L, DonationStatus.SUCCESS))
                .thenReturn(BigDecimal.valueOf(80000.00));
        when(fundUtilizationRepository.sumAmountByCampaignIdAndStatus(200L, UtilizationStatus.VERIFIED))
                .thenReturn(BigDecimal.valueOf(10000.00));
        when(fundUtilizationRepository.save(any(FundUtilization.class))).thenAnswer(i -> {
            FundUtilization f = i.getArgument(0);
            f.setId(501L);
            return f;
        });

        FundUtilizationResponse response = fundUtilizationService.submitUtilization(request, ngoUser);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals(BigDecimal.valueOf(25000.00), response.getAmount());
        assertEquals(UtilizationStatus.PENDING, response.getStatus());
    }

    @Test
    void testSubmitUtilization_OverspendingExceedsAvailableFunds_ThrowsException() {
        FundUtilizationRequest request = new FundUtilizationRequest();
        request.setCampaignId(200L);
        request.setCategory(ExpenseCategory.TEMPORARY_SHELTER);
        request.setDescription("Emergency Tents");
        request.setAmount(BigDecimal.valueOf(50000.00)); // Trying to spend ₹50,000 when only ₹20,000 available
        request.setExpenseDate(LocalDate.now());

        when(campaignRepository.findById(200L)).thenReturn(Optional.of(campaign));
        when(donationRepository.sumAmountByCampaignIdAndStatus(200L, DonationStatus.SUCCESS))
                .thenReturn(BigDecimal.valueOf(30000.00));
        when(fundUtilizationRepository.sumAmountByCampaignIdAndStatus(200L, UtilizationStatus.VERIFIED))
                .thenReturn(BigDecimal.valueOf(10000.00)); // Available: 30000 - 10000 = 20000

        assertThrows(IllegalArgumentException.class, () -> fundUtilizationService.submitUtilization(request, ngoUser));
    }

    @Test
    void testVerifyUtilization_Admin_Success() {
        when(fundUtilizationRepository.findById(500L)).thenReturn(Optional.of(utilization));
        when(fundUtilizationRepository.save(any(FundUtilization.class))).thenAnswer(i -> i.getArgument(0));

        VerifyUtilizationRequest verifyReq = new VerifyUtilizationRequest();
        verifyReq.setStatus(UtilizationStatus.VERIFIED);

        FundUtilizationResponse response = fundUtilizationService.verifyUtilization(500L, verifyReq, adminUser);

        assertNotNull(response);
        assertEquals(UtilizationStatus.VERIFIED, response.getStatus());
        assertEquals(1L, response.getVerifiedById());
        assertNotNull(response.getVerifiedAt());
    }

    @Test
    void testVerifyUtilization_NonAdmin_ThrowsAccessDenied() {
        VerifyUtilizationRequest verifyReq = new VerifyUtilizationRequest();
        verifyReq.setStatus(UtilizationStatus.VERIFIED);

        assertThrows(AccessDeniedException.class, () -> fundUtilizationService.verifyUtilization(500L, verifyReq, ngoUser));
    }

    @Test
    void testVerifyUtilization_ImmutableOnceVerified() {
        utilization.setStatus(UtilizationStatus.VERIFIED);
        when(fundUtilizationRepository.findById(500L)).thenReturn(Optional.of(utilization));

        VerifyUtilizationRequest verifyReq = new VerifyUtilizationRequest();
        verifyReq.setStatus(UtilizationStatus.REJECTED);
        verifyReq.setRejectionReason("Try alter");

        assertThrows(IllegalArgumentException.class, () -> fundUtilizationService.verifyUtilization(500L, verifyReq, adminUser));
    }
}
