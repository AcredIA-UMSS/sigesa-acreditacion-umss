package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.adapter.in.web.dto.ApiErrorResponse;
import com.umss.sigesa.domain.exception.NewsNotFoundException;
import com.umss.sigesa.domain.exception.NewsNotPublishedException;
import com.umss.sigesa.domain.exception.NewsPublishedConflictException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.umss.sigesa.adapter.in.web")
@Order(0)
public class NewsExceptionHandler {

    @ExceptionHandler(NewsNotPublishedException.class)
    public ResponseEntity<ApiErrorResponse> handleNotPublished(NewsNotPublishedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of("NEWS_NOT_PUBLISHED", NewsNotPublishedException.CLIENT_MESSAGE));
    }

    @ExceptionHandler(NewsPublishedConflictException.class)
    public ResponseEntity<ApiErrorResponse> handlePublishedDelete(NewsPublishedConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of("NEWS_PUBLISHED", NewsPublishedConflictException.CLIENT_MESSAGE));
    }

    @ExceptionHandler(NewsNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NewsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("NEWS_NOT_FOUND", NewsNotFoundException.CLIENT_MESSAGE));
    }
}
