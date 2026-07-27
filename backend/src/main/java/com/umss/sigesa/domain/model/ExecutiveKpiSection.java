package com.umss.sigesa.domain.model;

import java.util.List;

public record ExecutiveKpiSection(
        Integer totalPrograms,
        Double averageGlobalProgress,
        Integer criticalObservations,
        Integer alertPrograms,
        List<ProgramTrafficLight> programTrafficLights
) {}
