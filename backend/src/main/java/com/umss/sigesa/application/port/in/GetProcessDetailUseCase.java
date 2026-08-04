package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;

import java.util.UUID;

public interface GetProcessDetailUseCase {

    EnrichedProcessDetail getDetail(UUID processId, ProcessQueryContext ctx);
}
