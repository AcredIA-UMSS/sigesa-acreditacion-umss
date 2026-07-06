package com.umss.sigesa.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListProgramsUseCase {

    record ProgramSummary(UUID id, String code, String name) {}

    List<ProgramSummary> list();
}
