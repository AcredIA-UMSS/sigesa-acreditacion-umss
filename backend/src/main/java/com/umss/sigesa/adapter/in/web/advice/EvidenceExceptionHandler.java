package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.EvidenceImmutableException;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.exception.EvidencePayloadTooLargeException;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.ForbiddenProgramScopeException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.IndicatorNotUploadableException;
import com.umss.sigesa.domain.exception.InvalidEvidenceFormatException;
import com.umss.sigesa.domain.exception.InvalidFileFormatException;
import com.umss.sigesa.domain.exception.MaxFileSizeExceededException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.InvalidSubphaseStateException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.IndicatorNotLinkedException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.exception.UploadInProgressException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class EvidenceExceptionHandler {

    @ExceptionHandler(EvidenceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEvidenceNotFound(EvidenceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "EVIDENCE_NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(EvidenceImmutableException.class)
    public ResponseEntity<Map<String, String>> handleImmutable(EvidenceImmutableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "EVIDENCE_IMMUTABLE", "message", ex.getMessage()));
    }

    @ExceptionHandler(EvidenceUnclassifiedException.class)
    public ResponseEntity<Map<String, String>> handleUnclassified(EvidenceUnclassifiedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "EVIDENCE_UNCLASSIFIED", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidEvidenceFormatException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFormat(InvalidEvidenceFormatException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "INVALID_EVIDENCE_FORMAT", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFileFormat(InvalidFileFormatException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "INVALID_FILE_FORMAT", "message", ex.getMessage()));
    }

    @ExceptionHandler(IndicatorNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IndicatorNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "INDICATOR_NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(IndicatorNotUploadableException.class)
    public ResponseEntity<Map<String, String>> handleNotUploadable(IndicatorNotUploadableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "INDICATOR_NOT_UPLOADABLE", "message", ex.getMessage()));
    }

    @ExceptionHandler(ProgramScopeDeniedException.class)
    public ResponseEntity<Map<String, String>> handleScope(ProgramScopeDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "PROGRAM_SCOPE_DENIED", "message", ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenProgramScopeException.class)
    public ResponseEntity<Map<String, String>> handleForbiddenProgramScope(ForbiddenProgramScopeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "FORBIDDEN_SCOPE", "message", ex.getMessage()));
    }

    @ExceptionHandler(JustificationRequiredException.class)
    public ResponseEntity<Map<String, String>> handleJustificationRequired(JustificationRequiredException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "JUSTIFICATION_REQUIRED", "message", ex.getMessage()));
    }

    @ExceptionHandler(EvidenceRequiredException.class)
    public ResponseEntity<Map<String, String>> handleEvidenceRequired(EvidenceRequiredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "EVIDENCE_REQUIRED", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidSubphaseStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidSubphaseState(InvalidSubphaseStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "INVALID_STATE", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidIndicatorStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidIndicatorState(InvalidIndicatorStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "INVALID_STATE", "message", ex.getMessage()));
    }

    @ExceptionHandler(IndicatorNotLinkedException.class)
    public ResponseEntity<Map<String, String>> handleIndicatorNotLinked(IndicatorNotLinkedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "INDICATOR_NOT_LINKED", "message", ex.getMessage()));
    }

    @ExceptionHandler(SubsanationNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleSubsanationNotAllowed(SubsanationNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "SUBSANATION_NOT_ALLOWED", "message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "INVALID_STATE", "message", ex.getMessage()));
    }

    @ExceptionHandler(UploadInProgressException.class)
    public ResponseEntity<Map<String, String>> handleUploadLock(UploadInProgressException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "UPLOAD_IN_PROGRESS", "message", ex.getMessage()));
    }

    @ExceptionHandler(EvidencePayloadTooLargeException.class)
    public ResponseEntity<Map<String, String>> handleTooLarge(EvidencePayloadTooLargeException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("error", "PAYLOAD_TOO_LARGE", "message", ex.getMessage()));
    }

    @ExceptionHandler(MaxFileSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxFileSizeExceeded(MaxFileSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("error", "PAYLOAD_TOO_LARGE", "message", ex.getMessage()));
    }
}
