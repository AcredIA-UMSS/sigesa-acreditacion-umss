package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ObservationSummary;
import com.umss.sigesa.domain.model.ReportFormat;

import java.io.File;
import java.util.stream.Stream;

public interface ReportGeneratorPort {
    boolean supports(ReportFormat format);
    File generateReport(Stream<ObservationSummary> dataStream, ReportFormat format);
}
