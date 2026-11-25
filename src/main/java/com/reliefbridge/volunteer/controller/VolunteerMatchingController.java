package com.reliefbridge.volunteer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.reliefbridge.auth.entity.User;
import com.reliefbridge.volunteer.dto.VolunteerMatchResponse;
import com.reliefbridge.volunteer.dto.RecommendationStatusRequest;
import com.reliefbridge.volunteer.service.VolunteerMatchingService;

@RestController
@RequestMapping("/api/volunteer/matches")
public class VolunteerMatchingController {

    private final VolunteerMatchingService matchingService;

    public VolunteerMatchingController(
            VolunteerMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    // GENERATE AI RECOMMENDATIONS

    @PostMapping("/requirements/{requirementId}/generate")
    public ResponseEntity<VolunteerMatchResponse> generateMatches(
            @PathVariable Long requirementId,
            @AuthenticationPrincipal User currentUser) {

        VolunteerMatchResponse response =
                matchingService.generateMatches(
                        requirementId,
                        currentUser.getId());

        return ResponseEntity.ok(response);
    }


    // GET AI RECOMMENDATIONS

    @GetMapping("/requirements/{requirementId}")
    public ResponseEntity<VolunteerMatchResponse> getMatches(
            @PathVariable Long requirementId,
            @AuthenticationPrincipal User currentUser) {

        VolunteerMatchResponse response =
                matchingService.getMatches(
                        requirementId,
                        currentUser.getId());

        return ResponseEntity.ok(response);
    }


    // UPDATE RECOMMENDATION STATUS

    @PatchMapping("/{matchId}/status")
    public ResponseEntity<String> updateRecommendationStatus(
            @PathVariable Long matchId,
            @Valid @RequestBody RecommendationStatusRequest request,
            @AuthenticationPrincipal User currentUser) {

        matchingService.updateRecommendationStatus(
                matchId,
                currentUser.getId(),
                request.getStatus());

        return ResponseEntity.ok(
                "Recommendation status updated successfully");
    }
}