package com.umss.sigesa.adapter.out.catalog;

import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.config.DevSeedData;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StaticProgramCatalogAdapter implements ProgramCatalogPort {

    @Override
    public List<ProgramEntry> findAll() {
        return List.of(
                new ProgramEntry(
                        DevSeedData.PROGRAM_INF_SIS,
                        "INF-SIS",
                        "Ingeniería de Sistemas (demo UMSS)"
                ),
                new ProgramEntry(
                        DevSeedData.PROGRAM_CEUB,
                        "CEUB",
                        "Coordinación CEUB (demo)"
                ),
                new ProgramEntry(
                        DevSeedData.PROGRAM_ARCUSUR,
                        "ARCU-SUR",
                        "Coordinación ARCU-SUR (demo)"
                )
        );
    }
}
