package com.reliefbridge.disaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reliefbridge.disaster.entity.Disaster;
import com.reliefbridge.disaster.entity.DisasterStatus;

import java.util.List;

@Repository
public interface DisasterRepository extends JpaRepository<Disaster, Long> {

    // Find disasters reported by a particular user
    List<Disaster> findByReportedById(Long userId);

    // Find disasters based on verification status
    List<Disaster> findByStatus(DisasterStatus status);

}