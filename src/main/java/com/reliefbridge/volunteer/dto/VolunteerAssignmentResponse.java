package com.reliefbridge.volunteer.dto;

import java.time.LocalDateTime;

import com.reliefbridge.volunteer.enums.AssignmentStatus;

public class VolunteerAssignmentResponse {

    private Long id;

    private Long requirementId;

    private String requirementTitle;

    private Long volunteerProfileId;

    private Long volunteerUserId;

    private String volunteerName;

    private Long assignedByUserId;

    private AssignmentStatus status;

    private LocalDateTime assignedAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime completedAt;

    private String remarks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public String getRequirementTitle() {
        return requirementTitle;
    }

    public void setRequirementTitle(String requirementTitle) {
        this.requirementTitle = requirementTitle;
    }

    public Long getVolunteerProfileId() {
        return volunteerProfileId;
    }

    public void setVolunteerProfileId(Long volunteerProfileId) {
        this.volunteerProfileId = volunteerProfileId;
    }

    public Long getVolunteerUserId() {
        return volunteerUserId;
    }

    public void setVolunteerUserId(Long volunteerUserId) {
        this.volunteerUserId = volunteerUserId;
    }

    public String getVolunteerName() {
        return volunteerName;
    }

    public void setVolunteerName(String volunteerName) {
        this.volunteerName = volunteerName;
    }

    public Long getAssignedByUserId() {
        return assignedByUserId;
    }

    public void setAssignedByUserId(Long assignedByUserId) {
        this.assignedByUserId = assignedByUserId;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}