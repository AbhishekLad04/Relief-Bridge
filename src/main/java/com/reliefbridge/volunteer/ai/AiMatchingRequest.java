package com.reliefbridge.volunteer.ai;

import java.util.ArrayList;
import java.util.List;

public class AiMatchingRequest {

    private String requirement;
    private List<AiVolunteerInput> volunteers = new ArrayList<>();
    private Integer top_k = 10;

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public List<AiVolunteerInput> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(List<AiVolunteerInput> volunteers) {
        this.volunteers = volunteers;
    }

    public Integer getTop_k() {
        return top_k;
    }

    public void setTop_k(Integer top_k) {
        this.top_k = top_k;
    }
}