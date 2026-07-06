package com.umss.sigesa.adapter.out.catalog;

import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.config.AuthDataLoader;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class StaticProgramCatalogAdapter implements ProgramCatalogPort {

    private static final UUID CEUB_PROGRAM_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
    private static final UUID ARCUSUR_PROGRAM_ID = UUID.fromString("770e8400-e29b-41d4-a716-446655440002");

    @Override
    public List<ProgramEntry> findAll() {
        return List.of(
                new ProgramEntry(
                        AuthDataLoader.SEED_PROGRAM_ID,
                        "INF-SIS",
                        "Ingeniería de Sistemas (demo UMSS)"
                ),
                new ProgramEntry(CEUB_PROGRAM_ID, "CEUB", "Coordinación CEUB (demo)"),
                new ProgramEntry(ARCUSUR_PROGRAM_ID, "ARCU-SUR", "Coordinación ARCU-SUR (demo)")
        );
    }
}
