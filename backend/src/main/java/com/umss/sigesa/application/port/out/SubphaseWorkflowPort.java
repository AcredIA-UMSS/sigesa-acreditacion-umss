package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.SubphaseState;

import java.util.UUID;

public interface SubphaseWorkflowPort {

    boolean hasBlockingEvidence(UUID subphaseId);

    boolean exists(UUID subphaseId);

    SubphaseState getCurrentState(UUID subphaseId);

    void updateState(UUID subphaseId, SubphaseState newState);
}
