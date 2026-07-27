package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import com.umss.sigesa.domain.model.ExecutiveReportSnapshot;

public interface ExecutiveDataPort {

    ExecutiveReportSnapshot fetchSnapshot(ExecutiveReportFilters filters);
}
