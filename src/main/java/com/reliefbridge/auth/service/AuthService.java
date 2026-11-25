package com.reliefbridge.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reliefbridge.auth.dto.AuthResponse;
import com.reliefbridge.auth.dto.LoginRequest;
import com.reliefbridge.auth.dto.RegisterRequest;
import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;
import com.reliefbridge.auth.security.JwtService;
import com.reliefbridge.exception.EmailAlreadyExistsException;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,JwtService jwtService) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public User register(RegisterRequest request) {
		
		if (request.getRole() == null) {
			throw new IllegalArgumentException("Role is Required");
		}
		
		if (request.getRole() == Role.ADMIN) {
			throw new IllegalArgumentException("Public Registration as Admin is not Allowed");
		}
		
		String email = request.getEmail().trim().toLowerCase();
		
		if (userRepository.existsByEmail(email)) {
		    throw new EmailAlreadyExistsException("Email is already registered.");
		}
		
		User user = new User();
		user.setName(request.getName().trim());
		user.setEmail(email);
		
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		
		user.setRole(request.getRole());
		
		if (request.getRole() == Role.CITIZEN) {
			
			user.setDonor(request.isDonor());
			user.setVolunteer(request.isVolunteer());
			
			if (request.isVolunteer()) {
				
				if (request.getSkills() == null || request.getSkills().isBlank()) {
					throw new IllegalArgumentException("Skills are Required when Volunteer is Selected");
				}
				user.setSkills(request.getSkills().trim());
			}else {
				user.setSkills(null);
			}
			
			user.setApproved(true);
			user.setOrgName(null);
			user.setRegNo(null);
			
		}else if (request.getRole() == Role.NGO) {
			
			if (request.getOrgName() == null || request.getOrgName().isBlank()) {
				throw new IllegalArgumentException("Organisation name is Required for NGO Registration");
			}
			
			if (request.getRegNo() == null || request.getRegNo().isBlank()) {
				throw new IllegalArgumentException("Registration No is Required for NGO Registration");
			}
			
			user.setOrgName(request.getOrgName().trim());
			user.setRegNo(request.getRegNo().trim());
	
			user.setApproved(false);
			
			user.setDonor(false);
			user.setVolunteer(false);
			user.setSkills(null);
			
		}		
		return userRepository.save(user);
	}
	
	public AuthResponse login(LoginRequest request) {
		
		String email = request.getEmail().trim().toLowerCase();
		
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Email or Password"));
		
		boolean passwordMatches = passwordEncoder.matches(request.getPassword(),user.getPassword());
		
		if (!passwordMatches) {
			throw new IllegalArgumentException("Invalid Email or Password");
		}
		
		String token = jwtService.generateToken(user);
		
		return new AuthResponse(token,
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.isDonor(),
				user.isVolunteer()  );
		
	}
}
