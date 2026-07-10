package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.ForbiddenProgramScopeException;
import com.umss.sigesa.domain.exception.InvalidFileFormatException;
import com.umss.sigesa.domain.exception.MaxFileSizeExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class EvidenceExceptionHandler {

    @ExceptionHandler(EvidenceUnclassifiedException.class)
    public ResponseEntity<Map<String, String>> handleEvidenceUnclassified(EvidenceUnclassifiedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("code", "EVIDENCE_UNCLASSIFIED", "message", ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenProgramScopeException.class)
    public ResponseEntity<Map<String, String>> handleForbiddenProgramScope(ForbiddenProgramScopeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("code", "FORBIDDEN_SCOPE", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFileFormat(InvalidFileFormatException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("code", "INVALID_FILE_FORMAT", "message", ex.getMessage()));
    }

    @ExceptionHandler(MaxFileSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxFileSizeExceeded(MaxFileSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("code", "PAYLOAD_TOO_LARGE", "message", ex.getMessage()));
    }
}
