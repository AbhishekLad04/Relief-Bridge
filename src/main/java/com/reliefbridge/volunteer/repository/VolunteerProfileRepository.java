package com.reliefbridge.volunteer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.volunteer.entity.VolunteerProfile;
import com.reliefbridge.volunteer.enums.AvailabilityStatus;

@Repository
public interface VolunteerProfileRepository
        extends JpaRepository<VolunteerProfile, Long> {

    boolean existsByUser_Id(Long userId);

    Optional<VolunteerProfile> findByUser_Id(Long userId);

    @Query("""
        SELECT DISTINCT vp
        FROM VolunteerProfile vp
        JOIN FETCH vp.user u
        LEFT JOIN FETCH vp.skills ps
        LEFT JOIN FETCH ps.skill
        WHERE vp.availabilityStatus = :status
        AND u.isVolunteer = true
        AND u.role = :role
        """)
    List<VolunteerProfile> findEligibleVolunteers(
            @Param("status") AvailabilityStatus status,
            @Param("role") Role role
    );
}