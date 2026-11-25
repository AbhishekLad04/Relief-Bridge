package com.reliefbridge.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reliefbridge.auth.dto.ChangePasswordRequest;
import com.reliefbridge.auth.dto.UpdateCapabilitiesRequest;
import com.reliefbridge.auth.dto.UserResponse;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal User currentUser) {

        UserResponse response = userService.getMyProfile(currentUser);

        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/me/capabilities")
    public ResponseEntity<UserResponse> updateCapabilities(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateCapabilitiesRequest request) {

        UserResponse response =
                userService.updateCapabilities(currentUser, request);

        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/me/password")
    public ResponseEntity<Map<String,String>> changePassword( @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(currentUser, request);

        return ResponseEntity.ok(
            Map.of("message", "Password changed successfully.")
        );
    }
   
    
    
}