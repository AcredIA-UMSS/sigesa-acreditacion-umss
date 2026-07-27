package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class ReportJobNotFoundException extends RuntimeException {

    public ReportJobNotFoundException(UUID jobId) {
        super("Report job not found: " + jobId);
    }
}
