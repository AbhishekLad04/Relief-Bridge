package com.reliefbridge.volunteer.ai;

import java.util.ArrayList;
import java.util.List;

public class AiVolunteerInput {

    private Long volunteer_id;
    private String name;
    private List<String> skills = new ArrayList<>();
    private String experience_description = "";
    private String bio = "";

    public Long getVolunteer_id() {
        return volunteer_id;
    }

    public void setVolunteer_id(Long volunteer_id) {
        this.volunteer_id = volunteer_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getExperience_description() {
        return experience_description;
    }

    public void setExperience_description(String experience_description) {
        this.experience_description = experience_description;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}