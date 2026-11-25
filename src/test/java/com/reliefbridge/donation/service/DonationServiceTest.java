package com.reliefbridge.donation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;
import com.reliefbridge.campaign.entity.Campaign;
import com.reliefbridge.campaign.entity.CampaignStatus;
import com.reliefbridge.campaign.repository.CampaignRepository;
import com.reliefbridge.donation.dto.CreateDonationOrderRequest;
import com.reliefbridge.donation.dto.DonationOrderResponse;
import com.reliefbridge.donation.dto.DonationResponse;
import com.reliefbridge.donation.dto.VerifyPaymentRequest;
import com.reliefbridge.donation.entity.Donation;
import com.reliefbridge.donation.entity.DonationStatus;
import com.reliefbridge.donation.repository.DonationRepository;
import com.reliefbridge.donation.repository.FundUtilizationRepository;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private FundUtilizationRepository fundUtilizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DonationService donationService;

    private Campaign campaign;
    private User citizenUser;
    private Donation donation;

    @BeforeEach
    void setUp() {
        citizenUser = new User();
        citizenUser.setId(5L);
        citizenUser.setName("Amit Donor");
        citizenUser.setEmail("amit@donor.com");
        citizenUser.setRole(Role.CITIZEN);
        citizenUser.setDonor(false);

        campaign = new Campaign();
        campaign.setId(10L);
        campaign.setTitle("Earthquake Relief Campaign");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setTargetAmount(BigDecimal.valueOf(1000000.00));
        campaign.setCollectedAmount(BigDecimal.valueOf(200000.00));

        donation = new Donation();
        donation.setId(50L);
        donation.setDonationReference("DON-20261125-XYZ123");
        donation.setCampaign(campaign);
        donation.setDonor(citizenUser);
        donation.setDonorName("Amit Donor");
        donation.setAmount(BigDecimal.valueOf(5000.00));
        donation.setStatus(DonationStatus.PENDING);
        donation.setRazorpayOrderId("order_test_12345");
    }

    @Test
    void testInitiateDonation_Success() {
        CreateDonationOrderRequest request = new CreateDonationOrderRequest();
        request.setCampaignId(10L);
        request.setAmount(BigDecimal.valueOf(2500.00));
        request.setDonorName("Amit Donor");

        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));
        when(paymentGatewayService.createOrder(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(new DonationOrderResponse(
                        "DON-REF", "order_123", BigDecimal.valueOf(2500.00), "INR", "key_1", "Earthquake Relief", "ReliefBridge", true
                ));
        when(donationRepository.save(any(Donation.class))).thenReturn(donation);

        DonationOrderResponse orderResponse = donationService.initiateDonation(request, citizenUser);

        assertNotNull(orderResponse);
        assertEquals("order_123", orderResponse.getOrderId());
        verify(donationRepository).save(any(Donation.class));
    }

    @Test
    void testInitiateDonation_MinimumAmountCheck() {
        CreateDonationOrderRequest request = new CreateDonationOrderRequest();
        request.setCampaignId(10L);
        request.setAmount(BigDecimal.valueOf(5.00)); // Less than ₹10 minimum

        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));

        assertThrows(IllegalArgumentException.class, () -> donationService.initiateDonation(request, citizenUser));
    }

    @Test
    void testVerifyPayment_Success() {
        VerifyPaymentRequest verifyRequest = new VerifyPaymentRequest();
        verifyRequest.setDonationReference("DON-20261125-XYZ123");
        verifyRequest.setRazorpayOrderId("order_test_12345");
        verifyRequest.setRazorpayPaymentId("pay_test_98765");
        verifyRequest.setRazorpaySignature("valid_hmac_signature");

        when(donationRepository.findByDonationReference("DON-20261125-XYZ123")).thenReturn(Optional.of(donation));
        when(paymentGatewayService.verifySignature("order_test_12345", "pay_test_98765", "valid_hmac_signature")).thenReturn(true);
        when(donationRepository.save(any(Donation.class))).thenAnswer(i -> i.getArgument(0));

        DonationResponse response = donationService.verifyPayment(verifyRequest, citizenUser);

        assertNotNull(response);
        assertEquals(DonationStatus.SUCCESS, response.getStatus());
        assertEquals("pay_test_98765", response.getRazorpayPaymentId());
        assertNotNull(response.getReceiptNumber());
        assertTrue(response.getReceiptNumber().startsWith("REC-"));
        assertEquals(BigDecimal.valueOf(205000.00), campaign.getCollectedAmount());
        assertTrue(citizenUser.isDonor());
    }

    @Test
    void testVerifyPayment_InvalidSignature_MarksAsFailed() {
        VerifyPaymentRequest verifyRequest = new VerifyPaymentRequest();
        verifyRequest.setDonationReference("DON-20261125-XYZ123");
        verifyRequest.setRazorpayOrderId("order_test_12345");
        verifyRequest.setRazorpayPaymentId("pay_tampered");
        verifyRequest.setRazorpaySignature("invalid_signature");

        when(donationRepository.findByDonationReference("DON-20261125-XYZ123")).thenReturn(Optional.of(donation));
        when(paymentGatewayService.verifySignature("order_test_12345", "pay_tampered", "invalid_signature")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> donationService.verifyPayment(verifyRequest, citizenUser));
        assertEquals(DonationStatus.FAILED, donation.getStatus());
        verify(donationRepository).save(donation);
    }
}
