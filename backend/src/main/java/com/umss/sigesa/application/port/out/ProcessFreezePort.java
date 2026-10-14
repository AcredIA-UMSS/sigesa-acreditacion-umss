package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ProcessFreeze;

import java.util.Optional;
import java.util.UUID;

public interface ProcessFreezePort {

    Optional<ProcessFreeze> findByProcessId(UUID processId);

    ProcessFreeze save(ProcessFreeze freeze);
}
