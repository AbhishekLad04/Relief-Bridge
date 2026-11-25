package com.reliefbridge.auth.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SecurityErrorHandler securityErrorHandler;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            SecurityErrorHandler securityErrorHandler) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.securityErrorHandler = securityErrorHandler;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No JWT: continue to Spring Security.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            // Validate JWT signature and expiration.
            Claims claims = jwtService.extractAllClaims(token);

            Long userId = Long.parseLong(claims.getSubject());

            String role = claims.get("role", String.class);

            Integer tokenVersion =
                    claims.get("tokenVersion", Integer.class);

            if (role == null || role.isBlank()) {
                throw new BadCredentialsException("Invalid JWT role.");
            }

            if (tokenVersion == null) {
                throw new BadCredentialsException("Invalid JWT token version.");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load current user from database.
                User user = userRepository.findById(userId)
                        .orElseThrow(() ->
                                new BadCredentialsException("Invalid JWT user."));

                // Verify role.
                if (!user.getRole().name().equals(role)) {
                    throw new BadCredentialsException("JWT role mismatch.");
                }

                // Verify token version.
                if (user.getTokenVersion() != tokenVersion) {
                    throw new BadCredentialsException(
                            "JWT has been invalidated.");
                }

                // Create authentication.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority("ROLE_" + role)
                                )
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception ex) {

            SecurityContextHolder.clearContext();

            securityErrorHandler.commence(
                    request,
                    response,
                    new BadCredentialsException(
                            "Invalid or expired JWT token.")
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}