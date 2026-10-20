package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessStats;

public interface GetProcessStatsUseCase {

    ProcessStats stats(ProcessQueryContext ctx);
}
