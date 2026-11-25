package com.reliefbridge.volunteer.dto;

import java.math.BigDecimal;
import java.util.List;

public class VolunteerMatchResponse {

    private Long requirementId;

    private String requirementTitle;

    private String modelName;

    private List<MatchedVolunteer> recommendedVolunteers;

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public String getRequirementTitle() {
        return requirementTitle;
    }

    public void setRequirementTitle(String requirementTitle) {
        this.requirementTitle = requirementTitle;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<MatchedVolunteer> getRecommendedVolunteers() {
        return recommendedVolunteers;
    }

    public void setRecommendedVolunteers(List<MatchedVolunteer> recommendedVolunteers) {
        this.recommendedVolunteers = recommendedVolunteers;
    }

    public static class MatchedVolunteer {

        private Long volunteerProfileId;

        private Long userId;

        private String volunteerName;

        private String city;

        private String availabilityStatus;

        private Integer experienceYears;

        private List<String> skills;

        private BigDecimal matchScore;

        private Integer rankPosition;

        private String recommendationReason;

        public Long getVolunteerProfileId() {
            return volunteerProfileId;
        }

        public void setVolunteerProfileId(Long volunteerProfileId) {
            this.volunteerProfileId = volunteerProfileId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getVolunteerName() {
            return volunteerName;
        }

        public void setVolunteerName(String volunteerName) {
            this.volunteerName = volunteerName;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAvailabilityStatus() {
            return availabilityStatus;
        }

        public void setAvailabilityStatus(String availabilityStatus) {
            this.availabilityStatus = availabilityStatus;
        }

        public Integer getExperienceYears() {
            return experienceYears;
        }

        public void setExperienceYears(Integer experienceYears) {
            this.experienceYears = experienceYears;
        }

        public List<String> getSkills() {
            return skills;
        }

        public void setSkills(List<String> skills) {
            this.skills = skills;
        }

        public BigDecimal getMatchScore() {
            return matchScore;
        }

        public void setMatchScore(BigDecimal matchScore) {
            this.matchScore = matchScore;
        }

        public Integer getRankPosition() {
            return rankPosition;
        }

        public void setRankPosition(Integer rankPosition) {
            this.rankPosition = rankPosition;
        }

        public String getRecommendationReason() {
            return recommendationReason;
        }

        public void setRecommendationReason(String recommendationReason) {
            this.recommendationReason = recommendationReason;
        }
    }
}