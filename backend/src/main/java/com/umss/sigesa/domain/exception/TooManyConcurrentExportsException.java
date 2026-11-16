package com.umss.sigesa.domain.exception;

public class TooManyConcurrentExportsException extends RuntimeException {
    private final int retryAfterSeconds;

    public TooManyConcurrentExportsException(String message) {
        this(message, 60);
    }

    public TooManyConcurrentExportsException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
