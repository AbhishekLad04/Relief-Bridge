package com.reliefbridge.volunteer.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;

import com.reliefbridge.volunteer.dto.VolunteerAssignmentRequest;
import com.reliefbridge.volunteer.dto.VolunteerAssignmentResponse;

import com.reliefbridge.volunteer.entity.VolunteerAssignment;
import com.reliefbridge.volunteer.entity.VolunteerProfile;
import com.reliefbridge.volunteer.entity.VolunteerRequirement;

import com.reliefbridge.volunteer.enums.AssignmentStatus;
import com.reliefbridge.volunteer.enums.AvailabilityStatus;
import com.reliefbridge.volunteer.enums.RequirementStatus;

import com.reliefbridge.volunteer.repository.VolunteerAssignmentRepository;
import com.reliefbridge.volunteer.repository.VolunteerProfileRepository;
import com.reliefbridge.volunteer.repository.VolunteerRequirementRepository;

@Service
@Transactional
public class VolunteerAssignmentService {

    private final VolunteerAssignmentRepository assignmentRepository;
    private final VolunteerProfileRepository profileRepository;
    private final VolunteerRequirementRepository requirementRepository;
    private final UserRepository userRepository;

    public VolunteerAssignmentService(
            VolunteerAssignmentRepository assignmentRepository,
            VolunteerProfileRepository profileRepository,
            VolunteerRequirementRepository requirementRepository,
            UserRepository userRepository) {

        this.assignmentRepository = assignmentRepository;
        this.profileRepository = profileRepository;
        this.requirementRepository = requirementRepository;
        this.userRepository = userRepository;
    }

    // 1. ASSIGN VOLUNTEER TO REQUIREMENT

    public VolunteerAssignmentResponse assignVolunteer(
            VolunteerAssignmentRequest request,
            Long ngoUserId) {

        User ngo = userRepository.findById(ngoUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "NGO user not found"));

        if (ngo.getRole() != Role.NGO || !ngo.isApproved()) {
            throw new IllegalArgumentException(
                    "Only approved NGOs can assign volunteers");
        }

        VolunteerRequirement requirement =
                requirementRepository.findById(request.getRequirementId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Volunteer requirement not found"));

        if (!requirement.getNgo().getId().equals(ngoUserId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to assign volunteers to this requirement");
        }

        if (requirement.getStatus() != RequirementStatus.OPEN) {
            throw new IllegalArgumentException(
                    "Volunteers can only be assigned to OPEN requirements");
        }

        VolunteerProfile profile =
                profileRepository.findById(request.getVolunteerProfileId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Volunteer profile not found"));

        if (profile.getUser().getRole() != Role.CITIZEN
                || !profile.getUser().isVolunteer()) {

            throw new IllegalArgumentException(
                    "Selected user is not an eligible volunteer");
        }

        if (profile.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE) {
            throw new IllegalArgumentException(
                    "Selected volunteer is not currently available");
        }

        List<VolunteerAssignment> existingAssignments =
                assignmentRepository.findByRequirement_Id(
                        requirement.getId());

        boolean alreadyAssigned = existingAssignments.stream()
                .anyMatch(a ->
                        a.getVolunteerProfile().getId().equals(profile.getId())
                        && isActiveAssignment(a.getStatus()));

        if (alreadyAssigned) {
            throw new IllegalArgumentException(
                    "Volunteer is already assigned to this requirement");
        }

        long assignedCount = existingAssignments.stream()
                .filter(a -> isActiveAssignment(a.getStatus()))
                .count();

        if (assignedCount >= requirement.getRequiredVolunteers()) {
            throw new IllegalArgumentException(
                    "Required volunteer limit has already been reached");
        }

        VolunteerAssignment assignment = new VolunteerAssignment();

        assignment.setRequirement(requirement);
        assignment.setVolunteerProfile(profile);
        assignment.setAssignedBy(ngo);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setRemarks(request.getRemarks());

        VolunteerAssignment saved =
                assignmentRepository.save(assignment);

        // Automatically mark requirement as FILLED
        if (assignedCount + 1 >= requirement.getRequiredVolunteers()) {

            requirement.setStatus(RequirementStatus.FILLED);

            requirementRepository.save(requirement);
        }

        return mapToResponse(saved);
    }

    // 2. GET ASSIGNMENTS FOR REQUIREMENT (NGO / ADMIN)

    @Transactional(readOnly = true)
    public List<VolunteerAssignmentResponse> getRequirementAssignments(
            Long requirementId,
            Long requesterUserId) {

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        VolunteerRequirement requirement =
                requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Volunteer requirement not found"));

        if (requester.getRole() != Role.ADMIN) {

            if (requester.getRole() != Role.NGO || !requester.isApproved()) {
                throw new IllegalArgumentException(
                        "Only approved NGOs and Admin can view these assignments");
            }

            if (!requirement.getNgo().getId().equals(requesterUserId)) {
                throw new IllegalArgumentException(
                        "You are not authorized to view these assignments");
            }
        }

        return assignmentRepository
                .findByRequirement_Id(requirementId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. GET ASSIGNMENTS FOR LOGGED-IN VOLUNTEER

    @Transactional(readOnly = true)
    public List<VolunteerAssignmentResponse> getMyAssignments(
            Long volunteerUserId) {

        User volunteer = userRepository.findById(volunteerUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Volunteer user not found"));

        if (volunteer.getRole() != Role.CITIZEN
                || !volunteer.isVolunteer()) {

            throw new IllegalArgumentException(
                    "Only registered volunteers can view their assignments");
        }

        VolunteerProfile profile =
                profileRepository.findByUser_Id(volunteerUserId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Volunteer profile not found"));

        return assignmentRepository
                .findByVolunteerProfile_Id(profile.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 4. UPDATE ASSIGNMENT STATUS

    public VolunteerAssignmentResponse updateAssignmentStatus(
            Long assignmentId,
            Long requesterUserId,
            AssignmentStatus newStatus) {

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        VolunteerAssignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Assignment not found"));

        AssignmentStatus currentStatus = assignment.getStatus();

        boolean isVolunteer =
                requester.getRole() == Role.CITIZEN
                && requester.isVolunteer()
                && assignment.getVolunteerProfile()
                        .getUser().getId().equals(requesterUserId);

        boolean isAdmin = requester.getRole() == Role.ADMIN;

        boolean isOwningNgo =
                requester.getRole() == Role.NGO
                && requester.isApproved()
                && assignment.getRequirement()
                        .getNgo().getId().equals(requesterUserId);

        if (!isVolunteer && !isAdmin && !isOwningNgo) {
            throw new IllegalArgumentException(
                    "You are not authorized to update this assignment");
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Assignment status is required");
        }

        if (isVolunteer) {

            if (currentStatus != AssignmentStatus.ASSIGNED) {
                throw new IllegalArgumentException(
                        "You can only accept or reject an ASSIGNED opportunity");
            }

            if (newStatus != AssignmentStatus.ACCEPTED
                    && newStatus != AssignmentStatus.REJECTED) {

                throw new IllegalArgumentException(
                        "Volunteer can only ACCEPT or REJECT an assignment");
            }
        }

        if (isAdmin || isOwningNgo) {

            if (newStatus == AssignmentStatus.IN_PROGRESS) {

                if (currentStatus != AssignmentStatus.ACCEPTED) {
                    throw new IllegalArgumentException(
                            "Only ACCEPTED assignments can move to IN_PROGRESS");
                }

            } else if (newStatus == AssignmentStatus.COMPLETED) {

                if (currentStatus != AssignmentStatus.IN_PROGRESS) {
                    throw new IllegalArgumentException(
                            "Only IN_PROGRESS assignments can be completed");
                }

                assignment.setCompletedAt(LocalDateTime.now());

            } else if (newStatus == AssignmentStatus.CANCELLED) {

                if (currentStatus == AssignmentStatus.COMPLETED
                        || currentStatus == AssignmentStatus.CANCELLED
                        || currentStatus == AssignmentStatus.REJECTED) {

                    throw new IllegalArgumentException(
                            "This assignment cannot be cancelled");
                }

            } else {

                throw new IllegalArgumentException(
                        "NGO/Admin can only move assignments to IN_PROGRESS, COMPLETED or CANCELLED");
            }
        }

        if (newStatus == AssignmentStatus.ACCEPTED) {
            assignment.setAcceptedAt(LocalDateTime.now());
        }

        assignment.setStatus(newStatus);

        VolunteerAssignment updated =
                assignmentRepository.save(assignment);

        // Recalculate requirement status after rejection or cancellation
        if (currentStatus != AssignmentStatus.REJECTED
                && currentStatus != AssignmentStatus.CANCELLED
                && (newStatus == AssignmentStatus.REJECTED
                    || newStatus == AssignmentStatus.CANCELLED)) {

            updateRequirementStatus(assignment.getRequirement());
        }

        return mapToResponse(updated);
    }

    // 5. AUTOMATICALLY UPDATE REQUIREMENT STATUS

    private void updateRequirementStatus(
            VolunteerRequirement requirement) {

        List<VolunteerAssignment> assignments =
                assignmentRepository.findByRequirement_Id(
                        requirement.getId());

        long activeCount = assignments.stream()
                .filter(a -> isActiveAssignment(a.getStatus()))
                .count();

        int requiredCount = requirement.getRequiredVolunteers();

        if (activeCount >= requiredCount) {

            requirement.setStatus(RequirementStatus.FILLED);

        } else if (requirement.getStatus() == RequirementStatus.FILLED) {

            requirement.setStatus(RequirementStatus.OPEN);
        }

        requirementRepository.save(requirement);
    }

    // 6. CHECK ACTIVE ASSIGNMENT

    private boolean isActiveAssignment(AssignmentStatus status) {

        return status == AssignmentStatus.ASSIGNED
                || status == AssignmentStatus.ACCEPTED
                || status == AssignmentStatus.IN_PROGRESS
                || status == AssignmentStatus.COMPLETED;
    }

    // 7. MAP ENTITY TO RESPONSE DTO

    private VolunteerAssignmentResponse mapToResponse(
            VolunteerAssignment assignment) {

        VolunteerAssignmentResponse response =
                new VolunteerAssignmentResponse();

        response.setId(assignment.getId());

        response.setRequirementId(
                assignment.getRequirement().getId());

        response.setRequirementTitle(
                assignment.getRequirement().getTitle());

        response.setVolunteerProfileId(
                assignment.getVolunteerProfile().getId());

        response.setVolunteerUserId(
                assignment.getVolunteerProfile()
                        .getUser().getId());

        response.setVolunteerName(
                assignment.getVolunteerProfile()
                        .getUser().getName());

        response.setAssignedByUserId(
                assignment.getAssignedBy().getId());

        response.setStatus(assignment.getStatus());

        response.setAssignedAt(assignment.getAssignedAt());

        response.setAcceptedAt(assignment.getAcceptedAt());

        response.setCompletedAt(assignment.getCompletedAt());

        response.setRemarks(assignment.getRemarks());

        return response;
    }
}