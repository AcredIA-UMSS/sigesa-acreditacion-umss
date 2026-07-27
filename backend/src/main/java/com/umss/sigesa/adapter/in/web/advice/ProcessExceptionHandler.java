package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.umss.sigesa.adapter.in.web")
public class ProcessExceptionHandler {

    @ExceptionHandler(ProcessAlreadyActiveException.class)
    public ResponseEntity<Map<String, String>> handleProcessAlreadyActive(ProcessAlreadyActiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "PROCESS_ALREADY_ACTIVE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTemplateNotFound(TemplateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "TEMPLATE_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }
}
