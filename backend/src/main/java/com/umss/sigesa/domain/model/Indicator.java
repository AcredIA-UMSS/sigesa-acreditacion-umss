package com.umss.sigesa.domain.model;

import java.util.UUID;

public class Indicator {

    private final UUID id;
    private final UUID programId;
    private final UUID criterionId;
    private final UUID phaseId;

    public Indicator(UUID id, UUID programId, UUID criterionId, UUID phaseId) {
        this.id = id;
        this.programId = programId;
        this.criterionId = criterionId;
        this.phaseId = phaseId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProgramId() {
        return programId;
    }

    public UUID getCriterionId() {
        return criterionId;
    }

    public UUID getPhaseId() {
        return phaseId;
    }
}
