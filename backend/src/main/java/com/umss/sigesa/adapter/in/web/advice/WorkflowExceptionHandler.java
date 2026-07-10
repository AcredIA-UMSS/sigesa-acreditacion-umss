package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.ForbiddenProgramScopeException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class WorkflowExceptionHandler {

    @ExceptionHandler(InvalidIndicatorStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidState(InvalidIndicatorStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "INVALID_STATE", "message", ex.getMessage()));
    }

    @ExceptionHandler(JustificationRequiredException.class)
    public ResponseEntity<Map<String, String>> handleJustificationRequired(JustificationRequiredException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "JUSTIFICATION_REQUIRED", "message", ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenProgramScopeException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenProgramScopeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "FORBIDDEN_ROLE", "message", ex.getMessage()));
    }
}
