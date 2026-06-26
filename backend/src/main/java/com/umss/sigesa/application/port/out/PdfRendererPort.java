package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ExecutiveReportSnapshot;

public interface PdfRendererPort {

    byte[] render(ExecutiveReportSnapshot snapshot);
}
