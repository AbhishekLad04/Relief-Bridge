package com.reliefbridge.campaign.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.reliefbridge.campaign.entity.Campaign;
import com.reliefbridge.campaign.entity.CampaignStatus;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {

    List<Campaign> findByNgoIdOrderByCreatedAtDesc(Long ngoId);

    List<Campaign> findByDisasterIdOrderByCreatedAtDesc(Long disasterId);

    List<Campaign> findByStatusOrderByCreatedAtDesc(CampaignStatus status);

    long countByStatus(CampaignStatus status);
}

