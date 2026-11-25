package com.reliefbridge.auth.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class SecurityErrorHandler
implements AuthenticationEntryPoint, AccessDeniedHandler {

private final ObjectMapper objectMapper;

public SecurityErrorHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
}

// 401 - Authentication failure
@Override
public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        org.springframework.security.core.AuthenticationException exception)
        throws IOException {

    writeErrorResponse(
        response,
        HttpStatus.UNAUTHORIZED,
        "Unauthorized",
        "Authentication required. Please provide a valid JWT token."
    );
}

// 403 - Authorization failure
@Override
public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException exception)
        throws IOException {

    writeErrorResponse(
        response,
        HttpStatus.FORBIDDEN,
        "Forbidden",
        "You do not have permission to access this resource."
    );
}

private void writeErrorResponse(
        HttpServletResponse response,
        HttpStatus status,
        String error,
        String message)
        throws IOException {

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    var body = new java.util.LinkedHashMap<String, Object>();

    body.put("status", status.value());
    body.put("error", error);
    body.put("message", message);
    body.put("timestamp", LocalDateTime.now());

    objectMapper.writeValue(response.getOutputStream(), body);
}

}
