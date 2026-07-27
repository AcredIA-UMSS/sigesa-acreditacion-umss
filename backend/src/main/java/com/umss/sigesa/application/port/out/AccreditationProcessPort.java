package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AccreditationProcess;

import java.util.UUID;

public interface AccreditationProcessPort {
    boolean existsActiveProcessByCareer(UUID careerId);
    AccreditationProcess save(AccreditationProcess process);
}