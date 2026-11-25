package com.reliefbridge.volunteer.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;
import com.reliefbridge.disaster.entity.Disaster;
import com.reliefbridge.disaster.repository.DisasterRepository;
import com.reliefbridge.volunteer.dto.VolunteerRequirementRequest;
import com.reliefbridge.volunteer.dto.VolunteerRequirementResponse;
import com.reliefbridge.volunteer.entity.VolunteerRequirement;
import com.reliefbridge.volunteer.entity.VolunteerRequirementSkill;
import com.reliefbridge.volunteer.entity.VolunteerSkill;
import com.reliefbridge.volunteer.repository.VolunteerRequirementRepository;
import com.reliefbridge.volunteer.repository.VolunteerRequirementSkillRepository;
import com.reliefbridge.volunteer.repository.VolunteerSkillRepository;

@Service
@Transactional
public class VolunteerRequirementService {

    private final VolunteerRequirementRepository requirementRepository;
    private final VolunteerRequirementSkillRepository requirementSkillRepository;
    private final VolunteerSkillRepository skillRepository;
    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;

    public VolunteerRequirementService(
            VolunteerRequirementRepository requirementRepository,
            VolunteerRequirementSkillRepository requirementSkillRepository,
            VolunteerSkillRepository skillRepository,
            DisasterRepository disasterRepository,
            UserRepository userRepository) {

        this.requirementRepository = requirementRepository;
        this.requirementSkillRepository = requirementSkillRepository;
        this.skillRepository = skillRepository;
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
    }

    // CREATE VOLUNTEER REQUIREMENT

    public VolunteerRequirementResponse createRequirement(
            Long ngoUserId,
            VolunteerRequirementRequest request) {

        // Check NGO user
        User ngo = userRepository.findById(ngoUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "NGO user not found"));

        // Verify NGO role
        if (ngo.getRole() != Role.NGO) {
            throw new IllegalArgumentException(
                    "Only NGOs can create volunteer requirements");
        }

        // Verify NGO approval
        if (!ngo.isApproved()) {
            throw new IllegalArgumentException(
                    "Your NGO account is not approved yet");
        }

        // Validate dates
        validateDates(request);

        // Find disaster
        Disaster disaster = disasterRepository.findById(request.getDisasterId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Disaster not found"));

        // Create requirement
        VolunteerRequirement requirement = new VolunteerRequirement();

        requirement.setNgo(ngo);
        requirement.setDisaster(disaster);
        requirement.setTitle(request.getTitle().trim());
        requirement.setDescription(request.getDescription());
        requirement.setLocation(request.getLocation().trim());
        requirement.setRequiredVolunteers(request.getRequiredVolunteers());
        requirement.setUrgency(request.getUrgency());
        requirement.setStartDate(request.getStartDate());
        requirement.setEndDate(request.getEndDate());

        // Default status
        requirement.setStatus(
                com.reliefbridge.volunteer.enums.RequirementStatus.OPEN);

        // Assign required skills
        assignRequiredSkills(requirement, request);

        // Save requirement
        VolunteerRequirement savedRequirement =
                requirementRepository.save(requirement);

        return mapToResponse(savedRequirement);
    }

    // ASSIGN REQUIRED SKILLS

    private void assignRequiredSkills(
            VolunteerRequirement requirement,
            VolunteerRequirementRequest request) {

        List<VolunteerRequirementRequest.RequiredSkillRequest> requestedSkills =
                request.getRequiredSkills();

        if (requestedSkills == null || requestedSkills.isEmpty()) {
            return;
        }

        // Prevent duplicate skill IDs
        Set<Long> skillIds = new HashSet<>();

        for (VolunteerRequirementRequest.RequiredSkillRequest skillRequest
                : requestedSkills) {

            if (!skillIds.add(skillRequest.getSkillId())) {
                throw new IllegalArgumentException(
                        "Duplicate skill IDs are not allowed");
            }
        }

        // Fetch skills
        List<VolunteerSkill> skills =
                skillRepository.findAllById(skillIds);

        if (skills.size() != skillIds.size()) {
            throw new IllegalArgumentException(
                    "One or more selected skills do not exist");
        }

        // Create requirement-skill relationships
        for (VolunteerRequirementRequest.RequiredSkillRequest skillRequest
                : requestedSkills) {

            VolunteerSkill skill = skills.stream()
                    .filter(s -> s.getId().equals(skillRequest.getSkillId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Selected skill not found"));

            VolunteerRequirementSkill requirementSkill =
                    new VolunteerRequirementSkill();

            requirementSkill.setRequirement(requirement);
            requirementSkill.setSkill(skill);
            requirementSkill.setMandatory(skillRequest.getMandatory());

            requirement.getRequiredSkills().add(requirementSkill);
        }
    }

    // VALIDATE REQUIREMENT DATES

    private void validateDates(VolunteerRequirementRequest request) {

        if (request.getStartDate() != null
                && request.getEndDate() != null
                && !request.getEndDate().isAfter(request.getStartDate())) {

            throw new IllegalArgumentException(
                    "End date must be after start date");
        }
    }

    // GET REQUIREMENTS CREATED BY AN NGO

    @Transactional(readOnly = true)
    public List<VolunteerRequirementResponse> getMyRequirements(
            Long ngoUserId) {

        return requirementRepository.findByNgo_Id(ngoUserId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET REQUIREMENT BY ID

    @Transactional(readOnly = true)
    public VolunteerRequirementResponse getRequirementById(
            Long requirementId,
            Long requesterUserId,
            boolean isAdmin) {

        VolunteerRequirement requirement =
                requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Volunteer requirement not found"));

        // Only the owner NGO or Admin can access this method's result
        if (!isAdmin
                && !requirement.getNgo().getId().equals(requesterUserId)) {

            throw new IllegalArgumentException(
                    "You are not authorized to access this requirement");
        }

        return mapToResponse(requirement);
    }

    // MAP ENTITY TO RESPONSE DTO

    private VolunteerRequirementResponse mapToResponse(
            VolunteerRequirement requirement) {

        VolunteerRequirementResponse response =
                new VolunteerRequirementResponse();

        response.setId(requirement.getId());

        response.setDisasterId(
                requirement.getDisaster().getId());

        response.setNgoUserId(
                requirement.getNgo().getId());

        response.setNgoName(
                requirement.getNgo().getOrgName() != null
                        ? requirement.getNgo().getOrgName()
                        : requirement.getNgo().getName());

        response.setTitle(requirement.getTitle());
        response.setDescription(requirement.getDescription());
        response.setLocation(requirement.getLocation());

        response.setRequiredVolunteers(
                requirement.getRequiredVolunteers());

        response.setUrgency(requirement.getUrgency());
        response.setStartDate(requirement.getStartDate());
        response.setEndDate(requirement.getEndDate());
        response.setStatus(requirement.getStatus());

        response.setCreatedAt(requirement.getCreatedAt());
        response.setUpdatedAt(requirement.getUpdatedAt());

        // Map required skills
        List<VolunteerRequirementResponse.RequiredSkillResponse>
                skillResponses = new ArrayList<>();

        for (VolunteerRequirementSkill requirementSkill
                : requirement.getRequiredSkills()) {

            VolunteerRequirementResponse.RequiredSkillResponse
                    skillResponse =
                    new VolunteerRequirementResponse.RequiredSkillResponse();

            skillResponse.setSkillId(
                    requirementSkill.getSkill().getId());

            skillResponse.setSkillName(
                    requirementSkill.getSkill().getSkillName());

            skillResponse.setMandatory(
                    requirementSkill.getMandatory());

            skillResponses.add(skillResponse);
        }

        response.setRequiredSkills(skillResponses);

        return response;
    }
}