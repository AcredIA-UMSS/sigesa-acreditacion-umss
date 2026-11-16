package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.CoverageMatrix;

public interface CoverageMatrixPdfPort {

    byte[] render(CoverageMatrix matrix);
}
