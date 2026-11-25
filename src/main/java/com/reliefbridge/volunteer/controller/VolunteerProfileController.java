package com.reliefbridge.volunteer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.reliefbridge.auth.entity.User;
import com.reliefbridge.volunteer.dto.VolunteerProfileRequest;
import com.reliefbridge.volunteer.dto.VolunteerProfileResponse;
import com.reliefbridge.volunteer.service.VolunteerProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/volunteer/profile")
public class VolunteerProfileController {

    private final VolunteerProfileService profileService;

    public VolunteerProfileController(
            VolunteerProfileService profileService) {
        this.profileService = profileService;
    }

    // CREATE VOLUNTEER PROFILE
    @PostMapping
    public ResponseEntity<VolunteerProfileResponse> createProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody VolunteerProfileRequest request) {

        VolunteerProfileResponse response =
                profileService.createProfile(
                        currentUser.getId(), request);

        return ResponseEntity.ok(response);
    }

    // UPDATE VOLUNTEER PROFILE
    @PutMapping
    public ResponseEntity<VolunteerProfileResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody VolunteerProfileRequest request) {

        VolunteerProfileResponse response =
                profileService.updateProfile(
                        currentUser.getId(), request);

        return ResponseEntity.ok(response);
    }

    // GET VOLUNTEER PROFILE
    @GetMapping
    public ResponseEntity<VolunteerProfileResponse> getProfile(
            @AuthenticationPrincipal User currentUser) {

        VolunteerProfileResponse response =
                profileService.getProfile(currentUser.getId());

        return ResponseEntity.ok(response);
    }
}