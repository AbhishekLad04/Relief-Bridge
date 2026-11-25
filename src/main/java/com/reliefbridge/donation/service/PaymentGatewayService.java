package com.reliefbridge.donation.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.reliefbridge.donation.dto.DonationOrderResponse;

@Service
public class PaymentGatewayService {

    @Value("${razorpay.key.id:rzp_test_ReliefBridgeDemoKey}")
    private String keyId;

    @Value("${razorpay.key.secret:ReliefBridgeSecretKey2026}")
    private String keySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    @Value("${razorpay.company.name:ReliefBridge Humanitarian Aid}")
    private String companyName;

    public DonationOrderResponse createOrder(String donationReference, BigDecimal amount, String campaignTitle) {
        // Generate an order ID compatible with Razorpay convention (order_...)
        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        boolean isMockMode = keyId == null || keyId.contains("DemoKey") || keyId.isBlank();

        return new DonationOrderResponse(
                donationReference,
                orderId,
                amount,
                currency,
                keyId,
                campaignTitle,
                companyName,
                isMockMode
        );
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        if (orderId == null || paymentId == null || signature == null) {
            return false;
        }

        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String expectedSignature = hexString.toString();

            // Constant-time comparison to prevent timing attacks
            boolean signatureMatches = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.trim().getBytes(StandardCharsets.UTF_8)
            );

            // If signature matches cryptographic HMAC, verification passes
            if (signatureMatches) {
                return true;
            }

            // In sandbox/demo mode, accept standard mock signatures starting with 'mock_sig_' or 'simulated_sig_'
            boolean isMockMode = keyId == null || keyId.contains("DemoKey") || keyId.isBlank();
            if (isMockMode && (signature.startsWith("mock_sig_") || signature.startsWith("simulated_sig_") || signature.equalsIgnoreCase(expectedSignature))) {
                return true;
            }

            return false;
        } catch (Exception ex) {
            System.err.println("Signature verification exception: " + ex.getMessage());
            return false;
        }
    }

    public String generateMockSignature(String orderId, String paymentId) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            return "mock_sig_" + UUID.randomUUID().toString().replace("-", "");
        }
    }

    public String getKeyId() {
        return keyId;
    }

    public String getCurrency() {
        return currency;
    }
}
