package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import com.umss.sigesa.domain.model.ExecutiveReportSnapshot;

/**
 * Puerto de lectura compartido entre MOD-DASH (FSD-UC-013) y MOD-REPORT (FSD-UC-014).
 * UC-013 implementará la proyección de semáforos; UC-014 la consume vía {@link ExecutiveDataPort}.
 */
public interface ExecutiveDashboardQueryPort {

    ExecutiveReportSnapshot fetchExecutiveSnapshot(ExecutiveReportFilters filters);
}
