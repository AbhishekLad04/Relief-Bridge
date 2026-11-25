package com.reliefbridge.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.reliefbridge.exception.EmailAlreadyExistsException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

// 400 - Bad Request
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, Object>> handleBadRequest(
        IllegalArgumentException ex) {

    return buildResponse(
        HttpStatus.BAD_REQUEST,
        ex.getMessage()
    );
}

// 401 - Unauthorized
@ExceptionHandler(UsernameNotFoundException.class)
public ResponseEntity<Map<String, Object>> handleUserNotFound(
        UsernameNotFoundException ex) {

    return buildResponse(
        HttpStatus.UNAUTHORIZED,
        ex.getMessage()
    );
}

// 403 - Forbidden
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<Map<String, Object>> handleAccessDenied(
        AccessDeniedException ex) {

    return buildResponse(
        HttpStatus.FORBIDDEN,
        ex.getMessage()
    );
}

// 400 - Validation errors
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> handleValidationErrors(
        MethodArgumentNotValidException ex) {

    Map<String, String> errors = new LinkedHashMap<>();

    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
        errors.put(error.getField(), error.getDefaultMessage());
    }

    Map<String, Object> response = new LinkedHashMap<>();

    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Validation Failed");
    response.put("messages", errors);
    response.put("timestamp", LocalDateTime.now());

    return ResponseEntity.badRequest().body(response);
}

// 404 - Resource not found
@ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
public ResponseEntity<Map<String, Object>> handleResourceNotFound(
        org.springframework.web.servlet.resource.NoResourceFoundException ex) {

    return buildResponse(
        HttpStatus.NOT_FOUND,
        "Requested resource was not found."
    );
}

// 500 - Internal server error
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleGeneralException(
        Exception ex) {

    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred."
    );
}

private ResponseEntity<Map<String, Object>> buildResponse(
        HttpStatus status,
        String message) {

    Map<String, Object> response = new LinkedHashMap<>();

    response.put("status", status.value());
    response.put("error", status.getReasonPhrase());
    response.put("message", message);
    response.put("timestamp", LocalDateTime.now());

    return ResponseEntity.status(status).body(response);
}

@ExceptionHandler(ResponseStatusException.class)
public ResponseEntity<Map<String, Object>> handleResponseStatusException(
        ResponseStatusException ex) {

    int status = ex.getStatusCode().value();

    String message = ex.getReason() != null
            ? ex.getReason()
            : "Request failed.";

    Map<String, Object> response = new HashMap<>();
    response.put("status", status);
    response.put("error", status == 409 ? "Conflict" : "Bad Request");
    response.put("message", message);
    response.put("timestamp", LocalDateTime.now());

    return ResponseEntity.status(status).body(response);
}

@ExceptionHandler(EmailAlreadyExistsException.class)
public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(
        EmailAlreadyExistsException ex) {

    Map<String, Object> response = new HashMap<>();

    response.put("status", 409);
    response.put("error", "Conflict");
    response.put("message", ex.getMessage());
    response.put("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
}

}
