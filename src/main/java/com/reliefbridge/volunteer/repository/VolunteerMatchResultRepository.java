package com.reliefbridge.volunteer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reliefbridge.volunteer.entity.VolunteerMatchResult;

@Repository
public interface VolunteerMatchResultRepository extends JpaRepository<VolunteerMatchResult, Long> {

    List<VolunteerMatchResult> findByRequirement_IdOrderByRankPositionAsc(Long requirementId);

}