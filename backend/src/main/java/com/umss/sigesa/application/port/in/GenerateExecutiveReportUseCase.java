package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ExecutiveReportFilters;

import java.util.UUID;

public interface GenerateExecutiveReportUseCase {

    UUID generate(ExecutiveReportFilters filters, UUID requesterId);
}
