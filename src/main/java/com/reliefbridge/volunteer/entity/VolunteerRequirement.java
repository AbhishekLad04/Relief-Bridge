package com.reliefbridge.volunteer.entity;

import java.time.LocalDateTime; 
import java.util.HashSet;
import java.util.Set;

import com.reliefbridge.auth.entity.User;
import com.reliefbridge.disaster.entity.Disaster;
import com.reliefbridge.volunteer.enums.RequirementStatus;
import com.reliefbridge.volunteer.enums.RequirementUrgency;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "volunteer_requirements")
public class VolunteerRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disaster_id", nullable = false)
    private Disaster disaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ngo_user_id", nullable = false)
    private User ngo;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "required_volunteers", nullable = false)
    private Integer requiredVolunteers;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequirementUrgency urgency = RequirementUrgency.MEDIUM;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequirementStatus status = RequirementStatus.OPEN;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "requirement",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<VolunteerRequirementSkill> requiredSkills = new HashSet<>();

    @OneToMany(mappedBy = "requirement")
    private Set<VolunteerMatchResult> matchResults = new HashSet<>();

    @OneToMany(mappedBy = "requirement")
    private Set<VolunteerAssignment> assignments = new HashSet<>();

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Disaster getDisaster() {
        return disaster;
    }

    public void setDisaster(Disaster disaster) {
        this.disaster = disaster;
    }

    public User getNgo() {
        return ngo;
    }

    public void setNgo(User ngo) {
        this.ngo = ngo;
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

    public RequirementUrgency  getUrgency() {
        return urgency;
    }

    public void setUrgency(RequirementUrgency  urgency) {
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

    public RequirementStatus  getStatus() {
        return status;
    }

    public void setStatus(RequirementStatus  status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<VolunteerRequirementSkill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(Set<VolunteerRequirementSkill> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public Set<VolunteerMatchResult> getMatchResults() {
        return matchResults;
    }

    public void setMatchResults(Set<VolunteerMatchResult> matchResults) {
        this.matchResults = matchResults;
    }

    public Set<VolunteerAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(Set<VolunteerAssignment> assignments) {
        this.assignments = assignments;
    }
}