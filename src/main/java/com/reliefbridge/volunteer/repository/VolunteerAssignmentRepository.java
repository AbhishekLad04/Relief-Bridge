package com.reliefbridge.volunteer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reliefbridge.volunteer.entity.VolunteerAssignment;

@Repository
public interface VolunteerAssignmentRepository extends JpaRepository<VolunteerAssignment, Long> {

    List<VolunteerAssignment> findByRequirement_Id(Long requirementId);

    List<VolunteerAssignment> findByVolunteerProfile_Id(Long volunteerProfileId);

}