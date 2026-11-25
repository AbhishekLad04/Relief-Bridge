package com.reliefbridge.volunteer.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.reliefbridge.volunteer.enums.RequirementStatus;
import com.reliefbridge.volunteer.enums.RequirementUrgency;

public class VolunteerRequirementResponse {

    private Long id;

    private Long disasterId;

    private Long ngoUserId;

    private String ngoName;

    private String title;

    private String description;

    private String location;

    private Integer requiredVolunteers;

    private RequirementUrgency urgency;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private RequirementStatus status;

    private List<RequiredSkillResponse> requiredSkills;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDisasterId() {
        return disasterId;
    }

    public void setDisasterId(Long disasterId) {
        this.disasterId = disasterId;
    }

    public Long getNgoUserId() {
        return ngoUserId;
    }

    public void setNgoUserId(Long ngoUserId) {
        this.ngoUserId = ngoUserId;
    }

    public String getNgoName() {
        return ngoName;
    }

    public void setNgoName(String ngoName) {
        this.ngoName = ngoName;
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

    public RequirementStatus getStatus() {
        return status;
    }

    public void setStatus(RequirementStatus status) {
        this.status = status;
    }

    public List<RequiredSkillResponse> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<RequiredSkillResponse> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class RequiredSkillResponse {

        private Long skillId;

        private String skillName;

        private Boolean mandatory;

        public Long getSkillId() {
            return skillId;
        }

        public void setSkillId(Long skillId) {
            this.skillId = skillId;
        }

        public String getSkillName() {
            return skillName;
        }

        public void setSkillName(String skillName) {
            this.skillName = skillName;
        }

        public Boolean getMandatory() {
            return mandatory;
        }

        public void setMandatory(Boolean mandatory) {
            this.mandatory = mandatory;
        }
    }
}