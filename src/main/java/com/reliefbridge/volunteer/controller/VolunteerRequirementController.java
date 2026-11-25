package com.reliefbridge.volunteer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.entity.Role;

import com.reliefbridge.volunteer.dto.VolunteerRequirementRequest;
import com.reliefbridge.volunteer.dto.VolunteerRequirementResponse;
import com.reliefbridge.volunteer.service.VolunteerRequirementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/volunteer/requirements")
public class VolunteerRequirementController {

    private final VolunteerRequirementService requirementService;

    public VolunteerRequirementController(
            VolunteerRequirementService requirementService) {
        this.requirementService = requirementService;
    }

    // CREATE VOLUNTEER REQUIREMENT
    @PostMapping
    public ResponseEntity<VolunteerRequirementResponse> createRequirement(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody VolunteerRequirementRequest request) {

        VolunteerRequirementResponse response =
                requirementService.createRequirement(
                        currentUser.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // GET MY VOLUNTEER REQUIREMENTS
    @GetMapping("/my")
    public ResponseEntity<List<VolunteerRequirementResponse>> getMyRequirements(
            @AuthenticationPrincipal User currentUser) {

        List<VolunteerRequirementResponse> response =
                requirementService.getMyRequirements(
                        currentUser.getId());

        return ResponseEntity.ok(response);
    }

    // GET REQUIREMENT BY ID (NGO / ADMIN)
    @GetMapping("/{requirementId}")
    public ResponseEntity<VolunteerRequirementResponse> getRequirementById(
            @PathVariable Long requirementId,
            @AuthenticationPrincipal User currentUser) {

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        VolunteerRequirementResponse response =
                requirementService.getRequirementById(
                        requirementId,
                        currentUser.getId(),
                        isAdmin);

        return ResponseEntity.ok(response);
    }
}