package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.exception.TemplateNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ProcessExceptionHandler {

    @ExceptionHandler(ProcessAlreadyActiveException.class)
    public ResponseEntity<Map<String, String>> handleProcessAlreadyActive(ProcessAlreadyActiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT) // 409
                .body(Map.of("error", "PROCESS_ALREADY_ACTIVE", "message", ex.getMessage()));
    }

    @ExceptionHandler(TemplateNotValidException.class)
    public ResponseEntity<Map<String, String>> handleTemplateNotValid(TemplateNotValidException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY) // 422
                .body(Map.of("error", "TEMPLATE_NOT_VALID", "message", ex.getMessage()));
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTemplateNotFound(TemplateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "TEMPLATE_NOT_FOUND", "message", ex.getMessage()));
    }
}