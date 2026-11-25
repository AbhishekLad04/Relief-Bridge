package com.reliefbridge.volunteer.dto;

import com.reliefbridge.volunteer.enums.AssignmentStatus;

import jakarta.validation.constraints.NotNull;

public class AssignmentStatusRequest {

    @NotNull(message = "Assignment status is required")
    private AssignmentStatus status;

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }
}