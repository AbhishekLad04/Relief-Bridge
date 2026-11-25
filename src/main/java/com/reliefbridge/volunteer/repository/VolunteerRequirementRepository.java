package com.reliefbridge.volunteer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reliefbridge.volunteer.entity.VolunteerRequirement;

@Repository
public interface VolunteerRequirementRepository extends JpaRepository<VolunteerRequirement, Long> {

    List<VolunteerRequirement> findByNgo_Id(Long ngoUserId);

    List<VolunteerRequirement> findByDisaster_Id(Long disasterId);

}