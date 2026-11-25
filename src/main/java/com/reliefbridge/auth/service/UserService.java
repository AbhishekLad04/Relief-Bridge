package com.reliefbridge.auth.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reliefbridge.auth.dto.ChangePasswordRequest;
import com.reliefbridge.auth.dto.UpdateCapabilitiesRequest;
import com.reliefbridge.auth.dto.UserResponse;
import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================
    // GET LOGGED-IN USER PROFILE
    // =====================================

    public UserResponse getMyProfile(User currentUser) {

        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException(
                    "Authentication required."
            );
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found."
                        )
                );

        return new UserResponse(user);
    }


    // =====================================
    // UPDATE DONOR / VOLUNTEER CAPABILITIES
    // =====================================

    @Transactional
    public UserResponse updateCapabilities(
            User currentUser,
            UpdateCapabilitiesRequest request) {

        // Verify authentication
        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException(
                    "Authentication required."
            );
        }

        // Only citizens can update capabilities
        if (currentUser.getRole() != Role.CITIZEN) {
            throw new AccessDeniedException(
                    "Only citizens can update donor and volunteer capabilities."
            );
        }

        // Validate volunteer skills
        if (request.isVolunteer()
                && (request.getSkills() == null
                || request.getSkills().isBlank())) {

            throw new IllegalArgumentException(
                    "Skills are required when volunteer capability is enabled."
            );
        }

        // Fetch user from database
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found."
                        )
                );

        // Update capabilities
        user.setDonor(request.isDonor());
        user.setVolunteer(request.isVolunteer());

        // Update skills
        if (request.isVolunteer()) {
            user.setSkills(request.getSkills().trim());
        } else {
            user.setSkills(null);
        }

        User updatedUser = userRepository.save(user);

        return new UserResponse(updatedUser);
    }


    // =====================================
    // CHANGE PASSWORD
    // =====================================

    @Transactional
    public void changePassword(
            User currentUser,
            ChangePasswordRequest request) {

        // Verify authentication
        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException(
                    "Authentication required."
            );
        }

        // Fetch user from database
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found."
                        )
                );

        // Verify current password
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "Current password is incorrect."
            );
        }

        // Prevent using the same password
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "New password must be different from current password."
            );
        }

        // Encrypt and save new password
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        // Invalidate all previously generated JWT tokens
        user.setTokenVersion(
                user.getTokenVersion() + 1
        );

        userRepository.save(user);
    }
    
 // =====================================
 // ADMIN: APPROVE NGO
 // =====================================

 @Transactional
 public UserResponse approveNgo(Long ngoId) {

     User ngo = userRepository.findById(ngoId)
             .orElseThrow(() ->
                     new UsernameNotFoundException(
                             "User not found."
                     )
             );

     if (ngo.getRole() != Role.NGO) {
         throw new IllegalArgumentException(
                 "Selected user is not an NGO."
         );
     }

     if (ngo.isApproved()) {
         throw new IllegalArgumentException(
                 "NGO is already approved."
         );
     }

     ngo.setApproved(true);

     User updatedNgo = userRepository.save(ngo);

     return new UserResponse(updatedNgo);
 }
}