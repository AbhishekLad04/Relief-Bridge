package com.reliefbridge.volunteer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.reliefbridge.volunteer.dto.VolunteerSkillRequest;
import com.reliefbridge.volunteer.dto.VolunteerSkillResponse;
import com.reliefbridge.volunteer.service.VolunteerSkillService;

import jakarta.validation.Valid;

@RestController
public class VolunteerSkillController {

    private final VolunteerSkillService skillService;

    public VolunteerSkillController(
            VolunteerSkillService skillService) {
        this.skillService = skillService;
    }

    // GET ALL VOLUNTEER SKILLS
    @GetMapping("/api/volunteer/skills")
    public ResponseEntity<List<VolunteerSkillResponse>> getAllSkills() {

        return ResponseEntity.ok(skillService.getAllSkills());
    }

    // CREATE VOLUNTEER SKILL - ADMIN ONLY
    @PostMapping("/api/admin/volunteer/skills")
    public ResponseEntity<VolunteerSkillResponse> createSkill(
            @Valid @RequestBody VolunteerSkillRequest request) {

        VolunteerSkillResponse response =
                skillService.createSkill(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}