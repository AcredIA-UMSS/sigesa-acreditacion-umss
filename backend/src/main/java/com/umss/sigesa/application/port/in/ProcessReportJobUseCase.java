package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface ProcessReportJobUseCase {

    void process(UUID jobId);
}
