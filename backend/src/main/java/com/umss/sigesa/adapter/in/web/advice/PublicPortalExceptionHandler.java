package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.adapter.in.web.dto.ApiErrorResponse;
import com.umss.sigesa.domain.exception.PublicResourceNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.umss.sigesa.adapter.in.web")
@Order(0)
public class PublicPortalExceptionHandler {

    @ExceptionHandler(PublicResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(PublicResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("NOT_FOUND", PublicResourceNotFoundException.CLIENT_MESSAGE));
    }
}
