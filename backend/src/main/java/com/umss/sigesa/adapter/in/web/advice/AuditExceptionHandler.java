package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.adapter.in.web.dto.ApiErrorResponse;
import com.umss.sigesa.domain.exception.InvalidDateRangeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.umss.sigesa.adapter.in.web")
public class AuditExceptionHandler {

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidDateRange(InvalidDateRangeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of("INVALID_DATE_RANGE", ex.getMessage()));
    }
}
