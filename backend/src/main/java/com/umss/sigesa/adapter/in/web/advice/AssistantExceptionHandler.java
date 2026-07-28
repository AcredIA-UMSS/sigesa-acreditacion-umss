package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.AssistantCompletionException;
import com.umss.sigesa.domain.exception.AssistantUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AssistantExceptionHandler {

    @ExceptionHandler(AssistantUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleUnavailable(AssistantUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "ASSISTANT_UNAVAILABLE", "message", ex.getMessage()));
    }

    @ExceptionHandler(AssistantCompletionException.class)
    public ResponseEntity<Map<String, String>> handleCompletion(AssistantCompletionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "ASSISTANT_COMPLETION_FAILED", "message", ex.getMessage()));
    }
}
