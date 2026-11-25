package com.reliefbridge.volunteer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reliefbridge.volunteer.dto.VolunteerSkillRequest;
import com.reliefbridge.volunteer.dto.VolunteerSkillResponse;
import com.reliefbridge.volunteer.entity.VolunteerSkill;
import com.reliefbridge.volunteer.repository.VolunteerSkillRepository;

@Service
@Transactional
public class VolunteerSkillService {

    private final VolunteerSkillRepository skillRepository;

    public VolunteerSkillService(VolunteerSkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    // 1. Add New Skill

    public VolunteerSkillResponse createSkill(VolunteerSkillRequest request) {

        String skillName = request.getSkillName().trim();

        if (skillRepository.existsBySkillNameIgnoreCase(skillName)) {
            throw new IllegalArgumentException(
                    "This volunteer skill already exists");
        }

        VolunteerSkill skill = new VolunteerSkill();

        skill.setSkillName(skillName);
        skill.setDescription(request.getDescription());

        VolunteerSkill savedSkill = skillRepository.save(skill);

        return mapToResponse(savedSkill);
    }

    // 2. Get All Available Skills

    @Transactional(readOnly = true)
    public List<VolunteerSkillResponse> getAllSkills() {

        return skillRepository.findAll(Sort.by(Sort.Direction.ASC, "skillName"))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. Get Skill By ID

    @Transactional(readOnly = true)
    public VolunteerSkillResponse getSkillById(Long skillId) {

        VolunteerSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Volunteer skill not found"));

        return mapToResponse(skill);
    }

    // 4. Convert Entity into Response DTO

    private VolunteerSkillResponse mapToResponse(VolunteerSkill skill) {

        VolunteerSkillResponse response = new VolunteerSkillResponse();

        response.setId(skill.getId());
        response.setSkillName(skill.getSkillName());
        response.setDescription(skill.getDescription());

        return response;
    }
}