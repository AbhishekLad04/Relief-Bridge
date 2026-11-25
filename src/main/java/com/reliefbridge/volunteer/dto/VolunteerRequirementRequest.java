package com.reliefbridge.volunteer.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.reliefbridge.volunteer.enums.RequirementUrgency;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VolunteerRequirementRequest {

    @NotNull(message = "Disaster ID is required")
    private Long disasterId;

    @NotBlank(message = "Requirement title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @NotNull(message = "Number of volunteers is required")
    @Min(value = 1, message = "At least one volunteer is required")
    private Integer requiredVolunteers;

    @NotNull(message = "Urgency is required")
    private RequirementUrgency urgency;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Valid
    private List<RequiredSkillRequest> requiredSkills;

    public Long getDisasterId() {
        return disasterId;
    }

    public void setDisasterId(Long disasterId) {
        this.disasterId = disasterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getRequiredVolunteers() {
        return requiredVolunteers;
    }

    public void setRequiredVolunteers(Integer requiredVolunteers) {
        this.requiredVolunteers = requiredVolunteers;
    }

    public RequirementUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(RequirementUrgency urgency) {
        this.urgency = urgency;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public List<RequiredSkillRequest> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<RequiredSkillRequest> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    // Nested DTO for requirement skills

    public static class RequiredSkillRequest {

        @NotNull(message = "Skill ID is required")
        private Long skillId;

        @NotNull(message = "Mandatory status is required")
        private Boolean mandatory;

        public Long getSkillId() {
            return skillId;
        }

        public void setSkillId(Long skillId) {
            this.skillId = skillId;
        }

        public Boolean getMandatory() {
            return mandatory;
        }

        public void setMandatory(Boolean mandatory) {
            this.mandatory = mandatory;
        }
    }
}