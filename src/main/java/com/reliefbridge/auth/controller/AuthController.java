package com.reliefbridge.auth.controller;

import org.springframework.http.HttpStatus; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.reliefbridge.auth.dto.AuthResponse;
import com.reliefbridge.auth.dto.LoginRequest;
import com.reliefbridge.auth.dto.RegisterRequest;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		super();
		this.authService = authService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
		try {
			User user = authService.register(request);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(java.util.Map.of(
							"message","Registration Successful",
							"userId",user.getId(),
							"name",user.getName(),
							"email",user.getEmail(),
							"role",user.getRole(),
							"isDonor",user.isDonor(),
							"isVolunteer",user.isVolunteer(),
							"approved",user.isApproved()
					));
			
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,ex.getMessage());
		}
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
		
		try {
			AuthResponse response = authService.login(request);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
		}
	}
	
}
