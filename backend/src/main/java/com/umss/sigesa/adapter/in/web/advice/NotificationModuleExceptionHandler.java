package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.NotificationModuleUnavailableException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.umss.sigesa.adapter.in.web")
public class NotificationModuleExceptionHandler {

    @ExceptionHandler(NotificationModuleUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleUnavailable(NotificationModuleUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "NOTIFICATION_MODULE_UNAVAILABLE", "message", ex.getMessage()));
    }
}
