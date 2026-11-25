package com.reliefbridge.volunteer.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.reliefbridge.auth.entity.User;

import com.reliefbridge.volunteer.dto.VolunteerAssignmentRequest;
import com.reliefbridge.volunteer.dto.VolunteerAssignmentResponse;
import com.reliefbridge.volunteer.dto.AssignmentStatusRequest;

import com.reliefbridge.volunteer.service.VolunteerAssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/volunteer/assignments")
public class VolunteerAssignmentController {

    private final VolunteerAssignmentService assignmentService;

    public VolunteerAssignmentController(
            VolunteerAssignmentService assignmentService) {

        this.assignmentService = assignmentService;
    }

    // 1. ASSIGN VOLUNTEER (NGO)

    @PostMapping
    public ResponseEntity<VolunteerAssignmentResponse> assignVolunteer(
            @Valid @RequestBody VolunteerAssignmentRequest request,
            @AuthenticationPrincipal User currentUser) {

        VolunteerAssignmentResponse response =
                assignmentService.assignVolunteer(
                        request,
                        currentUser.getId());

        return ResponseEntity.ok(response);
    }


    // 2. GET ASSIGNMENTS FOR REQUIREMENT (NGO / ADMIN)

    @GetMapping("/requirement/{requirementId}")
    public ResponseEntity<List<VolunteerAssignmentResponse>> getRequirementAssignments(
            @PathVariable Long requirementId,
            @AuthenticationPrincipal User currentUser) {

        List<VolunteerAssignmentResponse> response =
                assignmentService.getRequirementAssignments(
                        requirementId,
                        currentUser.getId());

        return ResponseEntity.ok(response);
    }


    // 3. GET MY ASSIGNMENTS (VOLUNTEER)

    @GetMapping("/my")
    public ResponseEntity<List<VolunteerAssignmentResponse>> getMyAssignments(
            @AuthenticationPrincipal User currentUser) {

        List<VolunteerAssignmentResponse> response =
                assignmentService.getMyAssignments(
                        currentUser.getId());

        return ResponseEntity.ok(response);
    }


    // 4. UPDATE ASSIGNMENT STATUS

    @PatchMapping("/{assignmentId}/status")
    public ResponseEntity<VolunteerAssignmentResponse> updateAssignmentStatus(
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentStatusRequest request,
            @AuthenticationPrincipal User currentUser) {

        VolunteerAssignmentResponse response =
                assignmentService.updateAssignmentStatus(
                        assignmentId,
                        currentUser.getId(),
                        request.getStatus());

        return ResponseEntity.ok(response);
    }
}