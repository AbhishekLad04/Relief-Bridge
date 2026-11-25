package com.reliefbridge.volunteer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reliefbridge.volunteer.entity.VolunteerProfileSkill;

@Repository
public interface VolunteerProfileSkillRepository extends JpaRepository<VolunteerProfileSkill, Long> {

    List<VolunteerProfileSkill> findByVolunteerProfile_Id(Long volunteerProfileId);

}