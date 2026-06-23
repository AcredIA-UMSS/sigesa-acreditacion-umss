package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.InvalidCredentialsException;
import com.umss.sigesa.domain.exception.InvalidEmailDomainException;
import com.umss.sigesa.domain.exception.InvalidScopeException;
import com.umss.sigesa.domain.exception.RoleNotAssignedException;
import com.umss.sigesa.domain.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(InvalidScopeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidScope(InvalidScopeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "INVALID_SCOPE", "message", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "USER_NOT_FOUND", "message", ex.getMessage()));
    }
}
