package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.AuditEventNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.umss.sigesa.adapter.in.web")
public class AuditExceptionHandler {

    @ExceptionHandler(AuditEventNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(AuditEventNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "AUDIT_EVENT_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }
}
