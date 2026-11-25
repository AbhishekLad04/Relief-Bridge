package com.reliefbridge.volunteer.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "volunteer_skills")
public class VolunteerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "skill")
    private Set<VolunteerProfileSkill> volunteerProfiles = new HashSet<>();

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<VolunteerProfileSkill> getVolunteerProfiles() {
        return volunteerProfiles;
    }

    public void setVolunteerProfiles(Set<VolunteerProfileSkill> volunteerProfiles) {
        this.volunteerProfiles = volunteerProfiles;
    }
}