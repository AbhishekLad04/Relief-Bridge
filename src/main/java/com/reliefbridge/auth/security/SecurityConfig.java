package com.reliefbridge.auth.security;

import java.util.List;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

    // CORS Configuration for React Frontend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Main Spring Security configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(securityErrorHandler)
                    .accessDeniedHandler(securityErrorHandler)
            )
            .authorizeHttpRequests(auth -> auth

                    // =====================================
                    // 1. PUBLIC ENDPOINTS
                    // =====================================
                    .requestMatchers(
                            "/api/auth/register",
                            "/api/auth/login"
                    ).permitAll()

                    // Public Disaster Endpoints
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/disasters",
                            "/api/disasters/*"
                    ).permitAll()

                    // Public Campaign Endpoints
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/campaigns",
                            "/api/campaigns/*",
                            "/api/campaigns/*/financials"
                    ).permitAll()

                    // Public Donation & Payment Endpoints
                    .requestMatchers(
                            "/api/donations/order",
                            "/api/donations/verify",
                            "/api/donations/mock-signature",
                            "/api/donations/stats"
                    ).permitAll()

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/donations/reference/*",
                            "/api/donations/receipt/*",
                            "/api/donations/campaign/*"
                    ).permitAll()

                    // Public Fund Utilization Transparency Endpoints
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/fund-utilizations/campaign/*/verified",
                            "/api/fund-utilizations/campaign/*/categories"
                    ).permitAll()

                    // Public Skills list
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/volunteer/skills"
                    ).permitAll()


                    // =====================================
                    // 2. ADMIN-ONLY ENDPOINTS
                    // =====================================
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    .requestMatchers(
                            HttpMethod.PATCH,
                            "/api/disasters/*/verify",
                            "/api/disasters/*/reject"
                    ).hasRole("ADMIN")

                    .requestMatchers(
                            HttpMethod.DELETE,
                            "/api/disasters/*"
                    ).hasRole("ADMIN")

                    .requestMatchers(
                            HttpMethod.PATCH,
                            "/api/fund-utilizations/*/verify"
                    ).hasRole("ADMIN")

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/fund-utilizations/pending",
                            "/api/donations/admin/all"
                    ).hasRole("ADMIN")


                    // =====================================
                    // 3. NGO + ADMIN ENDPOINTS
                    // =====================================
                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/campaigns",
                            "/api/fund-utilizations"
                    ).hasAnyRole("ADMIN", "NGO")

                    .requestMatchers(
                            HttpMethod.PUT,
                            "/api/campaigns/*"
                    ).hasAnyRole("ADMIN", "NGO")

                    .requestMatchers(
                            HttpMethod.PATCH,
                            "/api/campaigns/*/status"
                    ).hasAnyRole("ADMIN", "NGO")

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/campaigns/my",
                            "/api/fund-utilizations/my",
                            "/api/donations/ngo/my",
                            "/api/fund-utilizations/campaign/*/all"
                    ).hasAnyRole("ADMIN", "NGO")


                    // =====================================
                    // 4. CITIZEN ENDPOINTS
                    // =====================================
                    .requestMatchers(
                            HttpMethod.PATCH,
                            "/api/users/me/capabilities"
                    ).hasRole("CITIZEN")

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/donations/my"
                    ).hasAnyRole("CITIZEN", "ADMIN")


                    // =====================================
                    // 5. DISASTERS REPORTING & MANAGEMENT
                    // =====================================
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/disasters/my"
                    ).authenticated()

                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/disasters"
                    ).hasAnyRole("ADMIN", "NGO", "CITIZEN")

                    .requestMatchers(
                            HttpMethod.PUT,
                            "/api/disasters/*"
                    ).authenticated()


                    // =====================================
                    // 6. VOLUNTEER MODULE SECURITY
                    // =====================================
                    .requestMatchers("/api/volunteer/**").authenticated()


                    // =====================================
                    // 7. USER PROFILE & NOTIFICATIONS
                    // =====================================
                    .requestMatchers(
                            "/api/users/me",
                            "/api/users/me/password",
                            "/api/notifications/**"
                    ).authenticated()


                    // =====================================
                    // 8. DEFAULT SECURITY
                    // =====================================
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
