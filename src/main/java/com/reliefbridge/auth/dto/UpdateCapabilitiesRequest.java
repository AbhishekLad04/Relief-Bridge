package com.reliefbridge.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateCapabilitiesRequest {

    private boolean isDonor;
    private boolean isVolunteer;
    private String skills;

    @JsonProperty("isDonor")
    public boolean isDonor() {
        return isDonor;
    }

    @JsonProperty("isDonor")
    public void setDonor(boolean isDonor) {
        this.isDonor = isDonor;
    }

    @JsonProperty("isVolunteer")
    public boolean isVolunteer() {
        return isVolunteer;
    }

    @JsonProperty("isVolunteer")
    public void setVolunteer(boolean isVolunteer) {
        this.isVolunteer = isVolunteer;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }
}