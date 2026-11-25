package com.reliefbridge.donation.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.reliefbridge.donation.entity.Donation;
import com.reliefbridge.donation.entity.DonationStatus;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long>, JpaSpecificationExecutor<Donation> {

    Optional<Donation> findByDonationReference(String donationReference);

    Optional<Donation> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Donation> findByReceiptNumber(String receiptNumber);

    List<Donation> findByDonorIdOrderByCreatedAtDesc(Long donorId);

    Page<Donation> findByDonorIdOrderByCreatedAtDesc(Long donorId, Pageable pageable);

    List<Donation> findByCampaignIdAndStatusOrderByCreatedAtDesc(Long campaignId, DonationStatus status);

    Page<Donation> findByCampaignIdAndStatusOrderByCreatedAtDesc(Long campaignId, DonationStatus status, Pageable pageable);

    Page<Donation> findByCampaignNgoIdOrderByCreatedAtDesc(Long ngoId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = :status")
    BigDecimal sumAmountByCampaignIdAndStatus(@Param("campaignId") Long campaignId, @Param("status") DonationStatus status);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") DonationStatus status);

    @Query("SELECT COUNT(d) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = :status")
    long countByCampaignIdAndStatus(@Param("campaignId") Long campaignId, @Param("status") DonationStatus status);

    @Query("SELECT COUNT(d) FROM Donation d WHERE d.status = :status")
    long countByStatus(@Param("status") DonationStatus status);

    @Query("SELECT COUNT(DISTINCT CASE WHEN d.donor IS NOT NULL THEN CAST(d.donor.id AS string) ELSE d.donorEmail END) " +
           "FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = :status")
    long countContributorsByCampaignIdAndStatus(@Param("campaignId") Long campaignId, @Param("status") DonationStatus status);

    @Query("SELECT d FROM Donation d WHERE " +
           "(:campaignId IS NULL OR d.campaign.id = :campaignId) AND " +
           "(:status IS NULL OR d.status = :status) " +
           "ORDER BY d.createdAt DESC")
    Page<Donation> filterDonations(@Param("campaignId") Long campaignId, @Param("status") DonationStatus status, Pageable pageable);
}
