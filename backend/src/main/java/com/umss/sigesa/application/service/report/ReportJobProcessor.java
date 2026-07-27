package com.umss.sigesa.application.service.report;

import java.util.UUID;

public interface ReportJobProcessor {

    void enqueue(UUID jobId);
}
