package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.IndicatorAnalysisNotFoundException;
import com.umss.sigesa.domain.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class EvaluationExceptionHandler {

    @ExceptionHandler(IndicatorAnalysisNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAnalysisNotFound(IndicatorAnalysisNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "ANALYSIS_NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "VALIDATION_ERROR", "message", ex.getMessage()));
    }
}
