package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.DuplicateActiveAssignmentException;
import com.umss.sigesa.domain.exception.DuplicateEmailException;
import com.umss.sigesa.domain.exception.InvalidCredentialsException;
import com.umss.sigesa.domain.exception.InvalidEmailDomainException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.InvalidScopeException;
import com.umss.sigesa.domain.exception.RoleNotAssignedException;
import com.umss.sigesa.domain.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.umss.sigesa.adapter.in.web")
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "AUTH_INVALID_CREDENTIALS",
                        "message", InvalidCredentialsException.GENERIC_MESSAGE
                ));
    }

    @ExceptionHandler(RoleNotAssignedException.class)
    public ResponseEntity<Map<String, String>> handleRoleNotAssigned(RoleNotAssignedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "ACCESS_DENIED", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidEmailDomainException.class)
    public ResponseEntity<Map<String, String>> handleInvalidEmail(InvalidEmailDomainException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "INVALID_EMAIL_DOMAIN", "message", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "EMAIL_ALREADY_REGISTERED", "message", DuplicateEmailException.MESSAGE));
    }

    @ExceptionHandler(DuplicateActiveAssignmentException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateAssignment(DuplicateActiveAssignmentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "ASSIGNMENT_ALREADY_ACTIVE", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidScopeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidScope(InvalidScopeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "INVALID_SCOPE", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRole(InvalidRoleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "INVALID_ROLE", "message", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "USER_NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Solicitud inválida.")
                .orElse("Solicitud inválida.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "VALIDATION_ERROR", "message", message));
    }
}
