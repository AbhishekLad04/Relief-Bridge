package com.reliefbridge.volunteer.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;
import com.reliefbridge.volunteer.dto.VolunteerProfileRequest;
import com.reliefbridge.volunteer.dto.VolunteerProfileResponse;
import com.reliefbridge.volunteer.entity.VolunteerProfile;
import com.reliefbridge.volunteer.entity.VolunteerProfileSkill;
import com.reliefbridge.volunteer.entity.VolunteerSkill;
import com.reliefbridge.volunteer.repository.VolunteerProfileRepository;
import com.reliefbridge.volunteer.repository.VolunteerSkillRepository;

@Service
@Transactional
public class VolunteerProfileService {

    private final VolunteerProfileRepository profileRepository;

    private final VolunteerSkillRepository skillRepository;

    private final UserRepository userRepository;

    public VolunteerProfileService(
            VolunteerProfileRepository profileRepository,
            VolunteerSkillRepository skillRepository,
            UserRepository userRepository) {

        this.profileRepository = profileRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    // 1. Create Volunteer Profile

    public VolunteerProfileResponse createProfile(
            Long userId,
            VolunteerProfileRequest request) {

        User user = getEligibleVolunteer(userId);

        if (profileRepository.existsByUser_Id(userId)) {
            throw new IllegalArgumentException(
                    "Volunteer profile already exists for this user");
        }

        VolunteerProfile profile = new VolunteerProfile();

        profile.setUser(user);

        updateProfileFields(profile, request);

        assignSkills(profile, request.getSkillIds());

        VolunteerProfile savedProfile =
                profileRepository.save(profile);

        return mapToResponse(savedProfile);
    }

    // 2. Update Volunteer Profile

    public VolunteerProfileResponse updateProfile(
            Long userId,
            VolunteerProfileRequest request) {

        getEligibleVolunteer(userId);

        VolunteerProfile profile = profileRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Volunteer profile not found"));

        updateProfileFields(profile, request);

        assignSkills(profile, request.getSkillIds());

        VolunteerProfile updatedProfile =
                profileRepository.save(profile);

        return mapToResponse(updatedProfile);
    }

    // 3. Get Volunteer Profile

    @Transactional(readOnly = true)
    public VolunteerProfileResponse getProfile(Long userId) {

        VolunteerProfile profile = profileRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Volunteer profile not found"));

        return mapToResponse(profile);
    }

    // 4. Check Volunteer Eligibility

    private User getEligibleVolunteer(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        if (user.getRole() != Role.CITIZEN) {
            throw new IllegalArgumentException(
                    "Only CITIZEN users can create volunteer profiles");
        }

        if (!user.isVolunteer()) {
            throw new IllegalArgumentException(
                    "Please enable volunteer activity in your account first");
        }

        return user;
    }

    // 5. Update Profile Fields

    private void updateProfileFields(
            VolunteerProfile profile,
            VolunteerProfileRequest request) {

        profile.setPhone(request.getPhone());

        profile.setAddress(request.getAddress());

        profile.setCity(request.getCity());

        profile.setState(request.getState());

        profile.setExperienceYears(
                request.getExperienceYears() == null
                        ? 0
                        : request.getExperienceYears());

        profile.setExperienceDescription(
                request.getExperienceDescription());

        profile.setBio(request.getBio());

        profile.setAvailabilityStatus(
                request.getAvailabilityStatus());
    }

    // 6. Assign Skills to Volunteer Profile

    private void assignSkills(
            VolunteerProfile profile,
            List<Long> skillIds) {

        Set<Long> uniqueSkillIds = skillIds == null
                ? new HashSet<>()
                : new HashSet<>(skillIds);

        List<VolunteerSkill> skills =
                skillRepository.findAllById(uniqueSkillIds);

        if (skills.size() != uniqueSkillIds.size()) {
            throw new IllegalArgumentException(
                    "One or more selected skill IDs are invalid");
        }

        profile.getSkills().clear();

        for (VolunteerSkill skill : skills) {

            VolunteerProfileSkill profileSkill =
                    new VolunteerProfileSkill();

            profileSkill.setVolunteerProfile(profile);

            profileSkill.setSkill(skill);

            profile.getSkills().add(profileSkill);
        }
    }

    // 7. Convert Entity into Response DTO

    private VolunteerProfileResponse mapToResponse(
            VolunteerProfile profile) {

        VolunteerProfileResponse response =
                new VolunteerProfileResponse();

        response.setId(profile.getId());

        response.setUserId(profile.getUser().getId());

        response.setName(profile.getUser().getName());

        response.setEmail(profile.getUser().getEmail());

        response.setPhone(profile.getPhone());

        response.setAddress(profile.getAddress());

        response.setCity(profile.getCity());

        response.setState(profile.getState());

        response.setExperienceYears(profile.getExperienceYears());

        response.setExperienceDescription(
                profile.getExperienceDescription());

        response.setBio(profile.getBio());

        response.setAvailabilityStatus(
                profile.getAvailabilityStatus());

        response.setSkills(
                profile.getSkills()
                        .stream()
                        .map(profileSkill ->
                                profileSkill.getSkill().getSkillName())
                        .collect(Collectors.toList()));

        response.setCreatedAt(profile.getCreatedAt());

        response.setUpdatedAt(profile.getUpdatedAt());

        return response;
    }
}