package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessSummary;

import java.util.List;

public interface ListProcessesUseCase {

    List<ProcessSummary> list(ProcessQueryContext ctx);
}
