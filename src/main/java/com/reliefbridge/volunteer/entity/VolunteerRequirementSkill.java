package com.reliefbridge.volunteer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "volunteer_requirement_skills")
public class VolunteerRequirementSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id", nullable = false)
    private VolunteerRequirement requirement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private VolunteerSkill skill;

    @Column(nullable = false)
    private Boolean mandatory = true;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VolunteerRequirement getRequirement() {
        return requirement;
    }

    public void setRequirement(VolunteerRequirement requirement) {
        this.requirement = requirement;
    }

    public VolunteerSkill getSkill() {
        return skill;
    }

    public void setSkill(VolunteerSkill skill) {
        this.skill = skill;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }
}