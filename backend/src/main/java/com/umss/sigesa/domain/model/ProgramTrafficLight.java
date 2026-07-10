package com.umss.sigesa.domain.model;

public record ProgramTrafficLight(
        String programId,
        String name,
        String status,
        Integer criticalObservations
) {}
