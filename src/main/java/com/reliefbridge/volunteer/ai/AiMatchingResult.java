package com.reliefbridge.volunteer.ai;

import java.util.ArrayList;
import java.util.List;

public class AiMatchingResult {

    private Long volunteer_id;
    private String volunteer_name;
    private Double bge_score;
    private List<String> skills = new ArrayList<>();

    public Long getVolunteer_id() {
        return volunteer_id;
    }

    public void setVolunteer_id(Long volunteer_id) {
        this.volunteer_id = volunteer_id;
    }

    public String getVolunteer_name() {
        return volunteer_name;
    }

    public void setVolunteer_name(String volunteer_name) {
        this.volunteer_name = volunteer_name;
    }

    public Double getBge_score() {
        return bge_score;
    }

    public void setBge_score(Double bge_score) {
        this.bge_score = bge_score;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}