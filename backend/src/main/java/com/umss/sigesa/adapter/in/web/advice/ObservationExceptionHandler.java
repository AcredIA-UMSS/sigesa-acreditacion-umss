package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.exception.EvidenceNotInProcessException;
import com.umss.sigesa.domain.exception.ForbiddenObservationRoleException;
import com.umss.sigesa.domain.exception.InvalidObservationStateException;
import com.umss.sigesa.domain.exception.ObservationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.umss.sigesa.adapter.in.web")
public class ObservationExceptionHandler {

    @ExceptionHandler(ObservationNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(ObservationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "OBSERVATION_NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(EvidenceNotFoundException.class)
    public ResponseEntity<Map<String, String>> evidenceMissing(EvidenceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "EVIDENCE_NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(EvidenceNotInProcessException.class)
    public ResponseEntity<Map<String, String>> evidenceScope(EvidenceNotInProcessException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "EVIDENCE_NOT_IN_PROCESS", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidObservationStateException.class)
    public ResponseEntity<Map<String, String>> invalidState(InvalidObservationStateException ex) {
        HttpStatus status = "OBSERVATION_INCOMPLETE".equals(ex.getCode())
                ? HttpStatus.UNPROCESSABLE_ENTITY
                : HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(Map.of("error", ex.getCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenObservationRoleException.class)
    public ResponseEntity<Map<String, String>> forbidden(ForbiddenObservationRoleException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "FORBIDDEN_ROLE", "message", ex.getMessage()));
    }
}
