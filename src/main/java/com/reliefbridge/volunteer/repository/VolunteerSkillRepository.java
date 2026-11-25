package com.reliefbridge.volunteer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reliefbridge.volunteer.entity.VolunteerSkill;

@Repository
public interface VolunteerSkillRepository extends JpaRepository<VolunteerSkill, Long> {

    boolean existsBySkillNameIgnoreCase(String skillName);

}