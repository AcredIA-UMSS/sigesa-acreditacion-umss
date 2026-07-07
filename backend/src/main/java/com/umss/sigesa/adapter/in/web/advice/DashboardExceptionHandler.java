package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.InvalidJobStateException;
import com.umss.sigesa.domain.exception.JobNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class DashboardExceptionHandler {

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleJobNotFound(JobNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "JOB_NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidJobStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJobState(InvalidJobStateException ex) {
        if (ex.getMessage().contains("Access denied")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "ACCESS_DENIED", "message", "You do not have permissions for this program or job."));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "JOB_NOT_READY", "message", ex.getMessage()));
    }
}
