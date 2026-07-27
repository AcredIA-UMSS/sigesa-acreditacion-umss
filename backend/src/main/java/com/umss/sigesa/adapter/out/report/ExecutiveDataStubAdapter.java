package com.umss.sigesa.adapter.out.report;

import com.umss.sigesa.application.port.out.ExecutiveDataPort;
import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import com.umss.sigesa.domain.model.ExecutiveReportSnapshot;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class ExecutiveDataStubAdapter implements ExecutiveDataPort {

    @Override
    public ExecutiveReportSnapshot fetchSnapshot(ExecutiveReportFilters filters) {
        List<ExecutiveReportSnapshot.ProgramSummary> programs = buildPrograms(filters);
        return new ExecutiveReportSnapshot(LocalDateTime.now(), filters, programs);
    }

    private List<ExecutiveReportSnapshot.ProgramSummary> buildPrograms(ExecutiveReportFilters filters) {
        if (filters.programId() != null) {
            return List.of(new ExecutiveReportSnapshot.ProgramSummary(
                    filters.programId(),
                    "Carrera " + filters.programId(),
                    "YELLOW",
                    42,
                    28
            ));
        }
        if (filters.facultyId() != null) {
            return List.of(
                    new ExecutiveReportSnapshot.ProgramSummary(
                            UUID.randomUUID(),
                            "Programa Facultad A",
                            "GREEN",
                            30,
                            25
                    ),
                    new ExecutiveReportSnapshot.ProgramSummary(
                            UUID.randomUUID(),
                            "Programa Facultad B",
                            "RED",
                            18,
                            6
                    )
            );
        }
        if (programsForYear(filters.managementYear()).isEmpty()) {
            return Collections.emptyList();
        }
        return programsForYear(filters.managementYear());
    }

    private List<ExecutiveReportSnapshot.ProgramSummary> programsForYear(int year) {
        return List.of(new ExecutiveReportSnapshot.ProgramSummary(
                UUID.randomUUID(),
                "Resumen institucional " + year,
                "YELLOW",
                120,
                78
        ));
    }
}
