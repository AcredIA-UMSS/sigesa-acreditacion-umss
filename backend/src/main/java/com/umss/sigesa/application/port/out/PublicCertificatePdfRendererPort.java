package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.PublicProgramSnapshot;

public interface PublicCertificatePdfRendererPort {

    byte[] render(PublicProgramSnapshot snapshot);
}
