package com.reliefbridge.volunteer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reliefbridge.volunteer.entity.VolunteerRequirementSkill;

@Repository
public interface VolunteerRequirementSkillRepository extends JpaRepository<VolunteerRequirementSkill, Long> {

    List<VolunteerRequirementSkill> findByRequirement_Id(Long requirementId);

}