package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.ReportAccessDeniedException;
import com.umss.sigesa.domain.exception.ReportJobNotFoundException;
import com.umss.sigesa.domain.exception.ReportNotReadyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ReportExceptionHandler {

    @ExceptionHandler(ReportJobNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ReportJobNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "REPORT_JOB_NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(ReportAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(ReportAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "FORBIDDEN_ROLE", "message", "Acceso denegado al reporte"));
    }

    @ExceptionHandler(ReportNotReadyException.class)
    public ResponseEntity<Map<String, String>> handleNotReady(ReportNotReadyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "REPORT_NOT_READY", "message", ex.getMessage()));
    }
}
