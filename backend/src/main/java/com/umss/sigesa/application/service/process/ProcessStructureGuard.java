package com.umss.sigesa.application.service.process;

import com.umss.sigesa.domain.exception.ProcessNotEditableException;
import com.umss.sigesa.domain.model.AccreditationProcess;

public class ProcessStructureGuard {

    public void ensureProcessActive(AccreditationProcess process) {
        if (process == null || !"ACTIVE".equals(process.getStatus())) {
            throw new ProcessNotEditableException(
                    "La estructura del proceso solo puede editarse mientras el proceso está ACTIVE.");
        }
    }
}
