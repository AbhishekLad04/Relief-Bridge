package com.reliefbridge.disaster.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.reliefbridge.disaster.dto.DisasterRequest;
import com.reliefbridge.disaster.dto.DisasterResponse;
import com.reliefbridge.disaster.service.DisasterService;

@RestController
@RequestMapping("/api/disasters")
public class DisasterController {

    private final DisasterService disasterService;

    public DisasterController(DisasterService disasterService) {
        this.disasterService = disasterService;
    }

    // =========================================
    // 1. REPORT A DISASTER
    // =========================================

    @PostMapping
    public ResponseEntity<DisasterResponse> createDisaster(
            @RequestBody DisasterRequest request) {

        DisasterResponse response =
                disasterService.createDisaster(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================
    // 2. GET ALL DISASTERS
    // =========================================

    @GetMapping
    public ResponseEntity<List<DisasterResponse>> getAllDisasters() {

        List<DisasterResponse> disasters =
                disasterService.getAllDisasters();

        return ResponseEntity.ok(disasters);
    }


    // =========================================
    // 3. GET DISASTER BY ID
    // =========================================

    @GetMapping("/{id}")
    public ResponseEntity<DisasterResponse> getDisasterById(
            @PathVariable Long id) {

        DisasterResponse response =
                disasterService.getDisasterById(id);

        return ResponseEntity.ok(response);
    }


    // =========================================
    // 4. GET MY DISASTERS
    // =========================================

    @GetMapping("/my")
    public ResponseEntity<List<DisasterResponse>> getMyDisasters() {

        List<DisasterResponse> disasters =
                disasterService.getMyDisasters();

        return ResponseEntity.ok(disasters);
    }


    // =========================================
    // 5. UPDATE DISASTER
    // =========================================

    @PutMapping("/{id}")
    public ResponseEntity<DisasterResponse> updateDisaster(
            @PathVariable Long id,
            @RequestBody DisasterRequest request) {

        DisasterResponse response =
                disasterService.updateDisaster(id, request);

        return ResponseEntity.ok(response);
    }


    // =========================================
    // 6. VERIFY DISASTER (ADMIN)
    // =========================================

    @PatchMapping("/{id}/verify")
    public ResponseEntity<DisasterResponse> verifyDisaster(
            @PathVariable Long id) {

        DisasterResponse response =
                disasterService.verifyDisaster(id);

        return ResponseEntity.ok(response);
    }


    // =========================================
    // 7. REJECT DISASTER (ADMIN)
    // =========================================

    @PatchMapping("/{id}/reject")
    public ResponseEntity<DisasterResponse> rejectDisaster(
            @PathVariable Long id) {

        DisasterResponse response =
                disasterService.rejectDisaster(id);

        return ResponseEntity.ok(response);
    }


    // =========================================
    // 8. DELETE DISASTER (ADMIN)
    // =========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisaster(
            @PathVariable Long id) {

        disasterService.deleteDisaster(id);

        return ResponseEntity.noContent().build();
    }

}