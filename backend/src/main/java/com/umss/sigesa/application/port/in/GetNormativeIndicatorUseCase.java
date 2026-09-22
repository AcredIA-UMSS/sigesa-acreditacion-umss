package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.process.NormativeIndicatorDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;

import java.util.UUID;

public interface GetNormativeIndicatorUseCase {

    NormativeIndicatorDetail getById(UUID indicatorId, ProcessQueryContext ctx);
}
