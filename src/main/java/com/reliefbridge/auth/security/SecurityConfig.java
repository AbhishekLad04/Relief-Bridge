package com.reliefbridge.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityErrorHandler securityErrorHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            SecurityErrorHandler securityErrorHandler) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityErrorHandler = securityErrorHandler;
    }

    // Password encryption using BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Main Spring Security configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

            // Disable CSRF because authentication uses JWT
            .csrf(csrf -> csrf.disable())

            // Stateless authentication
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Centralized JSON security error responses
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(securityErrorHandler)
                    .accessDeniedHandler(securityErrorHandler)
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth

                    // =====================================
                    // 1. PUBLIC ENDPOINTS
                    // =====================================

                    .requestMatchers(
                            "/api/auth/register",
                            "/api/auth/login"
                    ).permitAll()

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/disasters"
                    ).permitAll()

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/campaigns",
                            "/api/campaigns/*"
                    ).permitAll()

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/heatmap"
                    ).permitAll()

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/updates/*/file"
                    ).permitAll()


                    // =====================================
                    // 2. ADMIN-ONLY ENDPOINTS
                    // =====================================

                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")

                 // =====================================
                 // DISASTER MANAGEMENT SECURITY
                 // =====================================

                 // Logged-in users can view their own reports
                 .requestMatchers(
                         HttpMethod.GET,
                         "/api/disasters/my"
                 ).authenticated()

                 // Public disaster list
                 .requestMatchers(
                         HttpMethod.GET,
                         "/api/disasters"
                 ).permitAll()

                 // Public disaster details
                 .requestMatchers(
                         HttpMethod.GET,
                         "/api/disasters/*"
                 ).permitAll()

                 // ADMIN, NGO and CITIZEN can report disasters
                 .requestMatchers(
                         HttpMethod.POST,
                         "/api/disasters"
                 ).hasAnyRole("ADMIN", "NGO", "CITIZEN")

                 // Authenticated users can attempt updates
                 // Ownership and report status are checked in DisasterService
                 .requestMatchers(
                         HttpMethod.PUT,
                         "/api/disasters/*"
                 ).authenticated()

                 // ADMIN-only verification and rejection
                 .requestMatchers(
                         HttpMethod.PATCH,
                         "/api/disasters/*/verify",
                         "/api/disasters/*/reject"
                 ).hasRole("ADMIN")

                 // ADMIN-only deletion
                 .requestMatchers(
                         HttpMethod.DELETE,
                         "/api/disasters/*"
                 ).hasRole("ADMIN")


                    // =====================================
                    // 3. ADMIN + NGO ENDPOINTS
                    // =====================================

                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/campaigns"
                    ).hasAnyRole("ADMIN", "NGO")

                    .requestMatchers(
                            "/api/ngo/**"
                    ).hasAnyRole("ADMIN", "NGO")

                    .requestMatchers(
                            HttpMethod.PATCH,
                            "/api/campaigns/**"
                    ).hasAnyRole("ADMIN", "NGO")

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/campaigns/*/pledges"
                    ).hasAnyRole("ADMIN", "NGO")

                    .requestMatchers(
                            HttpMethod.PATCH,
                            "/api/pledges/*/receive"
                    ).hasAnyRole("ADMIN", "NGO")

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/campaigns/*/applications"
                    ).hasAnyRole("ADMIN", "NGO")

                    .requestMatchers(
                            HttpMethod.PATCH,
                            "/api/applications/*/assign",
                            "/api/applications/*/reject"
                    ).hasAnyRole("ADMIN", "NGO")

                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/campaigns/*/updates"
                    ).hasAnyRole("ADMIN", "NGO")


                    // =====================================
                    // 4. CITIZEN-ONLY ENDPOINTS
                    // =====================================

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/pledges/mine"
                    ).hasRole("CITIZEN")

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/applications/mine"
                    ).hasRole("CITIZEN")

                    .requestMatchers(
                            HttpMethod.PATCH,
                            "/api/users/me/capabilities"
                    ).hasRole("CITIZEN")

                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/campaigns/*/pledges"
                    ).hasRole("CITIZEN")

                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/campaigns/*/apply"
                    ).hasRole("CITIZEN")

                    .requestMatchers(
                            HttpMethod.PATCH,
                            "/api/applications/*/done"
                    ).hasRole("CITIZEN")


                    // =====================================
                    // 5. USER PROFILE AND PASSWORD
                    // =====================================

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/users/me"
                    ).authenticated()

                    .requestMatchers(
                            HttpMethod.PATCH,
                            "/api/users/me/password"
                    ).authenticated()


                    // =====================================
                    // 6. AUTHENTICATED USERS
                    // =====================================

                    .requestMatchers(
                            "/api/notifications/**"
                    ).authenticated()

                    
                    // =====================================
                    // 7. DEFAULT SECURITY
                    // =====================================

                    .anyRequest().authenticated()
            )

            // Add JWT filter
            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}