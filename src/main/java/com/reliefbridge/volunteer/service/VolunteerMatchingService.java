package com.reliefbridge.volunteer.service;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;



import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;
import com.reliefbridge.volunteer.ai.AiMatchingRequest;
import com.reliefbridge.volunteer.ai.AiMatchingResponse;
import com.reliefbridge.volunteer.ai.AiMatchingResult;
import com.reliefbridge.volunteer.ai.AiVolunteerInput;
import com.reliefbridge.volunteer.dto.VolunteerMatchResponse;
import com.reliefbridge.volunteer.entity.VolunteerMatchResult;
import com.reliefbridge.volunteer.entity.VolunteerProfile;
import com.reliefbridge.volunteer.entity.VolunteerRequirement;
import com.reliefbridge.volunteer.enums.AvailabilityStatus;
import com.reliefbridge.volunteer.enums.RecommendationStatus;
import com.reliefbridge.volunteer.enums.RequirementStatus;
import com.reliefbridge.volunteer.repository.VolunteerMatchResultRepository;
import com.reliefbridge.volunteer.repository.VolunteerProfileRepository;
import com.reliefbridge.volunteer.repository.VolunteerRequirementRepository;

import tools.jackson.databind.ObjectMapper;

@Service
@Transactional
public class VolunteerMatchingService {

    private final RestClient volunteerAIRestClient;
    private final VolunteerProfileRepository profileRepository;
    private final VolunteerRequirementRepository requirementRepository;
    private final VolunteerMatchResultRepository matchResultRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public VolunteerMatchingService(
            RestClient volunteerAIRestClient,
            VolunteerProfileRepository profileRepository,
            VolunteerRequirementRepository requirementRepository,
            VolunteerMatchResultRepository matchResultRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {

        this.volunteerAIRestClient = volunteerAIRestClient;
        this.profileRepository = profileRepository;
        this.requirementRepository = requirementRepository;
        this.matchResultRepository = matchResultRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }
    // GENERATE AI VOLUNTEER RECOMMENDATIONS

    public VolunteerMatchResponse generateMatches(
            Long requirementId,
            Long ngoUserId) {

        // 1. Verify NGO
        User ngo = userRepository.findById(ngoUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "NGO user not found"));

        if (ngo.getRole() != Role.NGO || !ngo.isApproved()) {
            throw new IllegalArgumentException(
                    "Only approved NGOs can generate volunteer matches");
        }

        // 2. Find requirement
        VolunteerRequirement requirement =
                requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Volunteer requirement not found"));

        // 3. Verify requirement ownership
        if (!requirement.getNgo().getId().equals(ngoUserId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to generate matches for this requirement");
        }

        // 4. Verify requirement status
        if (requirement.getStatus() != RequirementStatus.OPEN) {
            throw new IllegalArgumentException(
                    "AI matching is available only for OPEN requirements");
        }

        // 5. Fetch eligible volunteers
        List<VolunteerProfile> volunteers =
                profileRepository.findEligibleVolunteers(
                        AvailabilityStatus.AVAILABLE,
                        Role.CITIZEN);

        if (volunteers.isEmpty()) {
            throw new IllegalArgumentException(
                    "No eligible volunteers are currently available");
        }

        // 6. Prepare volunteer lookup map
        Map<Long, VolunteerProfile> volunteerMap =
                volunteers.stream()
                        .collect(Collectors.toMap(
                                VolunteerProfile::getId,
                                Function.identity()));

        // 7. Prepare AI request
        AiMatchingRequest aiRequest = new AiMatchingRequest();

        aiRequest.setRequirement(buildRequirementDescription(requirement));

        List<AiVolunteerInput> aiVolunteers = new ArrayList<>();

        for (VolunteerProfile profile : volunteers) {

            AiVolunteerInput input = new AiVolunteerInput();

            input.setVolunteer_id(profile.getId());

            input.setName(profile.getUser().getName());

            input.setSkills(
                    profile.getSkills().stream()
                            .map(ps -> ps.getSkill().getSkillName())
                            .collect(Collectors.toList())
            );

            input.setExperience_description(
                    profile.getExperienceDescription() != null
                            ? profile.getExperienceDescription()
                            : ""
            );

            input.setBio(
                    profile.getBio() != null
                            ? profile.getBio()
                            : ""
            );

            aiVolunteers.add(input);
        }

        aiRequest.setVolunteers(aiVolunteers);

        aiRequest.setTop_k(Math.min(10, volunteers.size()));

     // 8. Send request to Python AI service

        AiMatchingResponse aiResponse;

        try {

            String jsonPayload = objectMapper.writeValueAsString(aiRequest);

            System.out.println("JSON SENT TO AI SERVICE:");
            System.out.println(jsonPayload);

            // Create Java HTTP client
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            // Create HTTP request
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/v1/matching/rank"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            jsonPayload,
                            StandardCharsets.UTF_8
                    ))
                    .build();

            // Send request
            HttpResponse<String> httpResponse = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            System.out.println("AI SERVICE STATUS: "
                    + httpResponse.statusCode());

            System.out.println("AI SERVICE RESPONSE:");
            System.out.println(httpResponse.body());

            // Check HTTP status
            if (httpResponse.statusCode() < 200
                    || httpResponse.statusCode() >= 300) {

                throw new IllegalStateException(
                        "AI service returned HTTP "
                        + httpResponse.statusCode()
                        + ": "
                        + httpResponse.body()
                );
            }

            // Convert Python JSON response into Java DTO
            aiResponse = objectMapper.readValue(
                    httpResponse.body(),
                    AiMatchingResponse.class
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "AI service request was interrupted.",
                    exception
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to communicate with Volunteer AI service.",
                    exception
            );
        }

        if (aiResponse == null || aiResponse.getResults() == null) {

            throw new IllegalStateException(
                    "Invalid response received from Volunteer AI service"
            );
        }

        // 9. Save AI results in PostgreSQL
        List<VolunteerMatchResult> savedResults = new ArrayList<>();

        int rank = 1;

        for (AiMatchingResult aiResult : aiResponse.getResults()) {

            VolunteerProfile profile =
                    volunteerMap.get(aiResult.getVolunteer_id());

            // Ignore unknown volunteer IDs
            if (profile == null) {
                continue;
            }

            if (aiResult.getBge_score() == null
                    || !Double.isFinite(aiResult.getBge_score())) {
                throw new IllegalStateException(
                        "AI service returned an invalid matching score");
            }

            VolunteerMatchResult matchResult =
                    new VolunteerMatchResult();

            matchResult.setRequirement(requirement);
            matchResult.setVolunteerProfile(profile);

            matchResult.setMatchScore(
                    BigDecimal.valueOf(aiResult.getBge_score())
            );

            matchResult.setRecommendationReason(
                    "AI recommendation based on requirement description, "
                    + "volunteer skills and experience."
            );

            matchResult.setModelName(aiResponse.getModel());
            matchResult.setModelVersion("BAAI/bge-reranker-base");

            matchResult.setRankPosition(rank++);

            matchResult.setRecommendationStatus(
                    RecommendationStatus.NEW
            );

            savedResults.add(matchResult);
        }

        if (savedResults.isEmpty()) {
            throw new IllegalStateException(
                    "AI service did not return any valid volunteer recommendations");
        }

        matchResultRepository.saveAll(savedResults);

        // 10. Prepare response
        return mapToResponse(requirement, aiResponse, savedResults);
    }

    // BUILD REQUIREMENT DESCRIPTION FOR AI

    private String buildRequirementDescription(
            VolunteerRequirement requirement) {

        StringBuilder description = new StringBuilder();

        description.append("Volunteer Requirement: ")
                .append(requirement.getTitle());

        description.append(". Description: ")
                .append(requirement.getDescription() != null
                        ? requirement.getDescription()
                        : "");

        description.append(". Location: ")
                .append(requirement.getLocation());

        description.append(". Urgency: ")
                .append(requirement.getUrgency());

        description.append(". Required Skills: ");

        requirement.getRequiredSkills().forEach(skill -> {

            description.append(skill.getSkill().getSkillName());

            description.append(
                    Boolean.TRUE.equals(skill.getMandatory())
                            ? " (Mandatory)"
                            : " (Optional)"
            );

            description.append(", ");
        });

        return description.toString();
    }

    // MAP AI RESULTS TO API RESPONSE

    private VolunteerMatchResponse mapToResponse(
            VolunteerRequirement requirement,
            AiMatchingResponse aiResponse,
            List<VolunteerMatchResult> savedResults) {

        VolunteerMatchResponse response =
                new VolunteerMatchResponse();

        response.setRequirementId(requirement.getId());
        response.setRequirementTitle(requirement.getTitle());
        response.setModelName(aiResponse.getModel());

        List<VolunteerMatchResponse.MatchedVolunteer> matchedVolunteers =
                new ArrayList<>();

        for (VolunteerMatchResult result : savedResults) {

            VolunteerProfile profile = result.getVolunteerProfile();

            VolunteerMatchResponse.MatchedVolunteer matched =
                    new VolunteerMatchResponse.MatchedVolunteer();

            matched.setVolunteerProfileId(profile.getId());
            matched.setUserId(profile.getUser().getId());
            matched.setVolunteerName(profile.getUser().getName());
            matched.setCity(profile.getCity());

            matched.setAvailabilityStatus(
                    profile.getAvailabilityStatus().name()
            );

            matched.setExperienceYears(profile.getExperienceYears());

            matched.setSkills(
                    profile.getSkills().stream()
                            .map(ps -> ps.getSkill().getSkillName())
                            .collect(Collectors.toList())
            );

            matched.setMatchScore(result.getMatchScore());
            matched.setRankPosition(result.getRankPosition());

            matched.setRecommendationReason(
                    result.getRecommendationReason()
            );

            matchedVolunteers.add(matched);
        }

        response.setRecommendedVolunteers(matchedVolunteers);

        return response;
    }
    
 // GET SAVED AI RECOMMENDATIONS

    public VolunteerMatchResponse getMatches(
            Long requirementId,
            Long requesterUserId) {

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        VolunteerRequirement requirement =
                requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Volunteer requirement not found"));

        // Only ADMIN or approved NGO can access recommendations
        if (requester.getRole() != Role.ADMIN) {

            if (requester.getRole() != Role.NGO || !requester.isApproved()) {
                throw new IllegalArgumentException(
                        "Only approved NGOs and Admin can view recommendations");
            }

            if (!requirement.getNgo().getId().equals(requesterUserId)) {
                throw new IllegalArgumentException(
                        "You are not authorized to view these recommendations");
            }
        }

        List<VolunteerMatchResult> results =
                matchResultRepository
                        .findByRequirement_IdOrderByRankPositionAsc(requirementId);

        // Build response
        VolunteerMatchResponse response =
                new VolunteerMatchResponse();

        response.setRequirementId(requirement.getId());
        response.setRequirementTitle(requirement.getTitle());

        if (!results.isEmpty()) {
            response.setModelName(results.get(0).getModelName());
        }

        List<VolunteerMatchResponse.MatchedVolunteer> matchedVolunteers =
                new ArrayList<>();

        for (VolunteerMatchResult result : results) {

            VolunteerProfile profile = result.getVolunteerProfile();

            VolunteerMatchResponse.MatchedVolunteer matched =
                    new VolunteerMatchResponse.MatchedVolunteer();

            matched.setVolunteerProfileId(profile.getId());
            matched.setUserId(profile.getUser().getId());
            matched.setVolunteerName(profile.getUser().getName());
            matched.setCity(profile.getCity());

            matched.setAvailabilityStatus(
                    profile.getAvailabilityStatus().name());

            matched.setExperienceYears(profile.getExperienceYears());

            matched.setSkills(
                    profile.getSkills().stream()
                            .map(ps -> ps.getSkill().getSkillName())
                            .collect(Collectors.toList()));

            matched.setMatchScore(result.getMatchScore());
            matched.setRankPosition(result.getRankPosition());

            matched.setRecommendationReason(
                    result.getRecommendationReason());

            matchedVolunteers.add(matched);
        }

        response.setRecommendedVolunteers(matchedVolunteers);

        return response;
    }


    // UPDATE RECOMMENDATION STATUS

    public void updateRecommendationStatus(
            Long matchId,
            Long requesterUserId,
            RecommendationStatus status) {

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        VolunteerMatchResult match =
                matchResultRepository.findById(matchId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Recommendation not found"));

        VolunteerRequirement requirement = match.getRequirement();

        // Only ADMIN or approved NGO can update status
        if (requester.getRole() != Role.ADMIN) {

            if (requester.getRole() != Role.NGO || !requester.isApproved()) {
                throw new IllegalArgumentException(
                        "Only approved NGOs and Admin can update recommendation status");
            }

            if (!requirement.getNgo().getId().equals(requesterUserId)) {
                throw new IllegalArgumentException(
                        "You are not authorized to update this recommendation");
            }
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Recommendation status is required");
        }

        match.setRecommendationStatus(status);

        matchResultRepository.save(match);
    }
    
}