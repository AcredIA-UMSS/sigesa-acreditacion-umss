package com.umss.sigesa.adapter.in.async;

import com.umss.sigesa.application.port.in.ProcessReportJobUseCase;
import com.umss.sigesa.application.service.report.ReportJobProcessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReportJobAsyncDispatcher implements ReportJobProcessor {

    private final ProcessReportJobUseCase processReportJobUseCase;

    public ReportJobAsyncDispatcher(ProcessReportJobUseCase processReportJobUseCase) {
        this.processReportJobUseCase = processReportJobUseCase;
    }

    @Override
    @Async("reportJobExecutor")
    public void enqueue(UUID jobId) {
        processReportJobUseCase.process(jobId);
    }
}
