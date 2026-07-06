package com.umss.sigesa.application.port.out;

import java.util.List;
import java.util.UUID;

public interface ProgramCatalogPort {

    record ProgramEntry(UUID id, String code, String name) {}

    List<ProgramEntry> findAll();
}
