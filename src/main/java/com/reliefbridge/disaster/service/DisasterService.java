package com.reliefbridge.disaster.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.disaster.dto.DisasterRequest;
import com.reliefbridge.disaster.dto.DisasterResponse;
import com.reliefbridge.disaster.entity.Disaster;
import com.reliefbridge.disaster.entity.DisasterStatus;
import com.reliefbridge.disaster.repository.DisasterRepository;

@Service
@Transactional
public class DisasterService {

    private final DisasterRepository disasterRepository;

    public DisasterService(DisasterRepository disasterRepository) {
        this.disasterRepository = disasterRepository;
    }

    // =========================================
    // 1. GET CURRENT LOGGED-IN USER
    // =========================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof User)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User authentication required."
            );
        }

        return (User) authentication.getPrincipal();
    }


    // =========================================
    // 2. CHECK NGO APPROVAL
    // =========================================

    private void checkNGOApproval(User user) {

        if (user.getRole() == Role.NGO && !user.isApproved()) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your NGO account is not approved yet."
            );
        }
    }


    // =========================================
    // 3. CREATE DISASTER
    // =========================================

    public DisasterResponse createDisaster(DisasterRequest request) {

        User user = getCurrentUser();

        // Check allowed roles
        if (user.getRole() != Role.ADMIN &&
                user.getRole() != Role.NGO &&
                user.getRole() != Role.CITIZEN) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to report disasters."
            );
        }

        // Check NGO approval
        checkNGOApproval(user);

        // Validate request
        validateRequest(request);

        Disaster disaster = new Disaster();

        disaster.setTitle(request.getTitle().trim());
        disaster.setDescription(request.getDescription().trim());
        disaster.setDisasterType(request.getDisasterType());
        disaster.setLocation(request.getLocation().trim());
        disaster.setSeverity(request.getSeverity());

        // Automatically assign logged-in user
        disaster.setReportedBy(user);

        // Every new report starts as PENDING
        disaster.setStatus(DisasterStatus.PENDING);

        Disaster savedDisaster = disasterRepository.save(disaster);

        return convertToResponse(savedDisaster);
    }


    // =========================================
    // 4. GET ALL DISASTERS
    // =========================================

    @Transactional(readOnly = true)
    public List<DisasterResponse> getAllDisasters() {

        return disasterRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================================
    // 5. GET DISASTER BY ID
    // =========================================

    @Transactional(readOnly = true)
    public DisasterResponse getDisasterById(Long id) {

        Disaster disaster = findDisaster(id);

        return convertToResponse(disaster);
    }


    // =========================================
    // 6. GET MY DISASTERS
    // =========================================

    @Transactional(readOnly = true)
    public List<DisasterResponse> getMyDisasters() {

        User user = getCurrentUser();

        return disasterRepository.findByReportedById(user.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================================
    // 7. UPDATE DISASTER
    // =========================================

    public DisasterResponse updateDisaster(Long id, DisasterRequest request) {

        User currentUser = getCurrentUser();
        Disaster disaster = findDisaster(id);

        // Admin can update any disaster
        if (currentUser.getRole() != Role.ADMIN) {

            // NGO approval check
            checkNGOApproval(currentUser);

            // Only the owner can update their disaster
            if (!disaster.getReportedBy().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not allowed to update this disaster"
                );
            }

            // Only PENDING disasters can be updated by non-admin users
            if (disaster.getStatus() != DisasterStatus.PENDING) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Only pending disasters can be updated"
                );
            }
        }

        // Validate incoming request
        validateRequest(request);

        // Update editable fields
        disaster.setTitle(request.getTitle());
        disaster.setDescription(request.getDescription());
        disaster.setDisasterType(request.getDisasterType());
        disaster.setLocation(request.getLocation());
        disaster.setSeverity(request.getSeverity());

        // Save changes
        Disaster updatedDisaster = disasterRepository.save(disaster);

        return convertToResponse(updatedDisaster);
    }

    // =========================================
    // 8. VERIFY DISASTER
    // =========================================

    public DisasterResponse verifyDisaster(Long id) {

        User user = getCurrentUser();

        checkAdmin(user);

        Disaster disaster = findDisaster(id);

        if (disaster.getStatus() != DisasterStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending disasters can be verified."
            );
        }

        disaster.setStatus(DisasterStatus.VERIFIED);

        Disaster updatedDisaster = disasterRepository.save(disaster);

        return convertToResponse(updatedDisaster);
    }


    // =========================================
    // 9. REJECT DISASTER
    // =========================================

    public DisasterResponse rejectDisaster(Long id) {

        User user = getCurrentUser();

        checkAdmin(user);

        Disaster disaster = findDisaster(id);

        if (disaster.getStatus() != DisasterStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending disasters can be rejected."
            );
        }

        disaster.setStatus(DisasterStatus.REJECTED);

        Disaster updatedDisaster = disasterRepository.save(disaster);

        return convertToResponse(updatedDisaster);
    }


    // =========================================
    // 10. DELETE DISASTER
    // =========================================

    public void deleteDisaster(Long id) {

        User user = getCurrentUser();

        checkAdmin(user);

        Disaster disaster = findDisaster(id);

        disasterRepository.delete(disaster);
    }


    // =========================================
    // 11. CHECK ADMIN ROLE
    // =========================================

    private void checkAdmin(User user) {

        if (user.getRole() != Role.ADMIN) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN can perform this operation."
            );
        }
    }


    // =========================================
    // 12. FIND DISASTER
    // =========================================

    private Disaster findDisaster(Long id) {

        return disasterRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Disaster not found with ID: " + id
                        )
                );
    }


    // =========================================
    // 13. VALIDATE REQUEST
    // =========================================

    private void validateRequest(DisasterRequest request) {

        if (request == null ||
                request.getTitle() == null ||
                request.getTitle().isBlank() ||
                request.getDescription() == null ||
                request.getDescription().isBlank() ||
                request.getDisasterType() == null ||
                request.getLocation() == null ||
                request.getLocation().isBlank() ||
                request.getSeverity() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "All disaster fields are required."
            );
        }

        if (request.getTitle().trim().length() > 150 ||
                request.getLocation().trim().length() > 200) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title or location exceeds the allowed length."
            );
        }
    }


    // =========================================
    // 14. CONVERT ENTITY TO RESPONSE DTO
    // =========================================

    private DisasterResponse convertToResponse(Disaster disaster) {

        DisasterResponse response = new DisasterResponse();

        response.setId(disaster.getId());
        response.setTitle(disaster.getTitle());
        response.setDescription(disaster.getDescription());
        response.setDisasterType(disaster.getDisasterType());
        response.setLocation(disaster.getLocation());
        response.setSeverity(disaster.getSeverity());
        response.setStatus(disaster.getStatus());

        response.setReportedById(
                disaster.getReportedBy().getId()
        );

        response.setReportedByName(
                disaster.getReportedBy().getName()
        );

        response.setCreatedAt(disaster.getCreatedAt());
        response.setUpdatedAt(disaster.getUpdatedAt());

        return response;
    }

}