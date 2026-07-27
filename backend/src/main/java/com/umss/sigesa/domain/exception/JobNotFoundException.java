package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(UUID jobId) {
        super("Export job not found with id: " + jobId);
    }
}
