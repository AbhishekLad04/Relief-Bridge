package com.reliefbridge.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reliefbridge.auth.dto.UserResponse;
import com.reliefbridge.auth.service.UserService;

@RestController
@RequestMapping("/api/admin/ngos")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // =====================================
    // ADMIN: APPROVE NGO
    // =====================================

    @PatchMapping("/{ngoId}/approve")
    public ResponseEntity<UserResponse> approveNgo(
            @PathVariable Long ngoId) {

        UserResponse response = userService.approveNgo(ngoId);

        return ResponseEntity.ok(response);
    }
}