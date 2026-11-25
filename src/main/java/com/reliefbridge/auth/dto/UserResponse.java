package com.reliefbridge.auth.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;

    private boolean isDonor;
    private boolean isVolunteer;

    private String skills;
    private String orgName;
    private String regNo;
    private boolean approved;
    private LocalDateTime createdAt;

    public UserResponse(User user) {

        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();

        this.isDonor = user.isDonor();
        this.isVolunteer = user.isVolunteer();

        this.skills = user.getSkills();
        this.orgName = user.getOrgName();
        this.regNo = user.getRegNo();

        this.approved = user.isApproved();
        this.createdAt = user.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    @JsonProperty("isDonor")
    public boolean isDonor() {
        return isDonor;
    }

    @JsonProperty("isVolunteer")
    public boolean isVolunteer() {
        return isVolunteer;
    }

    public String getSkills() {
        return skills;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getRegNo() {
        return regNo;
    }

    public boolean isApproved() {
        return approved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}