package com.reliefbridge.volunteer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VolunteerAssignmentRequest {

    @NotNull(message = "Requirement ID is required")
    private Long requirementId;

    @NotNull(message = "Volunteer profile ID is required")
    private Long volunteerProfileId;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public Long getVolunteerProfileId() {
        return volunteerProfileId;
    }

    public void setVolunteerProfileId(Long volunteerProfileId) {
        this.volunteerProfileId = volunteerProfileId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}