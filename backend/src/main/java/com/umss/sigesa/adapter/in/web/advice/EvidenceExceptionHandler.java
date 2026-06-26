package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.EvidencePayloadTooLargeException;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.IndicatorNotUploadableException;
import com.umss.sigesa.domain.exception.InvalidEvidenceFormatException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.UploadInProgressException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class EvidenceExceptionHandler {

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
}
