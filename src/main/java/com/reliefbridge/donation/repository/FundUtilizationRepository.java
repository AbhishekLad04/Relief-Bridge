package com.reliefbridge.donation.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.reliefbridge.donation.entity.ExpenseCategory;
import com.reliefbridge.donation.entity.FundUtilization;
import com.reliefbridge.donation.entity.UtilizationStatus;

@Repository
public interface FundUtilizationRepository extends JpaRepository<FundUtilization, Long> {

    List<FundUtilization> findByCampaignIdOrderByExpenseDateDesc(Long campaignId);

    Page<FundUtilization> findByCampaignIdOrderByExpenseDateDesc(Long campaignId, Pageable pageable);

    List<FundUtilization> findByCampaignIdAndStatusOrderByExpenseDateDesc(Long campaignId, UtilizationStatus status);

    Page<FundUtilization> findByNgoIdOrderByExpenseDateDesc(Long ngoId, Pageable pageable);

    Page<FundUtilization> findByStatusOrderByCreatedAtDesc(UtilizationStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(fu.amount), 0) FROM FundUtilization fu WHERE fu.campaign.id = :campaignId AND fu.status = :status")
    BigDecimal sumAmountByCampaignIdAndStatus(@Param("campaignId") Long campaignId, @Param("status") UtilizationStatus status);

    @Query("SELECT COALESCE(SUM(fu.amount), 0) FROM FundUtilization fu WHERE fu.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") UtilizationStatus status);

    @Query("SELECT COUNT(fu) FROM FundUtilization fu WHERE fu.campaign.id = :campaignId AND fu.status = :status")
    long countByCampaignIdAndStatus(@Param("campaignId") Long campaignId, @Param("status") UtilizationStatus status);

    @Query("SELECT COUNT(fu) FROM FundUtilization fu WHERE fu.status = :status")
    long countByStatus(@Param("status") UtilizationStatus status);

    @Query("SELECT fu.category, COALESCE(SUM(fu.amount), 0) FROM FundUtilization fu " +
           "WHERE fu.campaign.id = :campaignId AND fu.status = :status " +
           "GROUP BY fu.category")
    List<Object[]> sumAmountByCategoryAndCampaign(@Param("campaignId") Long campaignId, @Param("status") UtilizationStatus status);

    @Query("SELECT fu.category, COALESCE(SUM(fu.amount), 0) FROM FundUtilization fu " +
           "WHERE fu.status = :status " +
           "GROUP BY fu.category")
    List<Object[]> sumAmountByCategory(@Param("status") UtilizationStatus status);
}
