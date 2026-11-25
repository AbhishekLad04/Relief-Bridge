package com.reliefbridge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;

@Component
public class AdminBootstrap implements CommandLineRunner {


private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;

@Value("${app.admin.name}")
private String adminName;

@Value("${app.admin.email}")
private String adminEmail;

@Value("${app.admin.password}")
private String adminPassword;

public AdminBootstrap(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder) {

    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
}

@Override
@Transactional
public void run(String... args) {

    String normalizedEmail = adminEmail.trim().toLowerCase();

    // Check whether an ADMIN already exists with this email
    if (userRepository.existsByEmail(normalizedEmail)) {

        User existingUser = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow();

        if (existingUser.getRole() != Role.ADMIN) {
            throw new IllegalStateException(
                "Configured ADMIN email already belongs to a non-ADMIN user."
            );
        }

        System.out.println("ADMIN account already exists.");

        return;
    }

    // Validate configured credentials
    if (adminPassword == null || adminPassword.isBlank()) {
        throw new IllegalStateException(
            "ADMIN_PASSWORD environment variable is required."
        );
    }

    if (adminPassword.length() < 12) {
        throw new IllegalStateException(
            "ADMIN password must contain at least 12 characters."
        );
    }

    // Create initial ADMIN
    User admin = new User();

    admin.setName(adminName);
    admin.setEmail(normalizedEmail);
    admin.setPassword(passwordEncoder.encode(adminPassword));

    admin.setRole(Role.ADMIN);

    admin.setDonor(false);
    admin.setVolunteer(false);
    admin.setSkills(null);

    admin.setOrgName(null);
    admin.setRegNo(null);

    admin.setApproved(true);

    userRepository.save(admin);

    System.out.println("Initial ADMIN account created successfully.");
}


}
