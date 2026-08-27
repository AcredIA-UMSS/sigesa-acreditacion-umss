package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.SubphaseWorkflowPort;
import com.umss.sigesa.domain.exception.InvalidSubphaseStateException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.SubphaseState;
import com.umss.sigesa.domain.model.SubphaseTransitionResult;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class SubphaseTransitionHelper {

    public static final Set<SubphaseState> REVIEWABLE_STATES =
            EnumSet.of(SubphaseState.SUBIDO, SubphaseState.SUBSANADO);

    private final SubphaseWorkflowPort subphaseWorkflowPort;

    public SubphaseTransitionHelper(SubphaseWorkflowPort subphaseWorkflowPort) {
        this.subphaseWorkflowPort = subphaseWorkflowPort;
    }

    public SubphaseTransitionResult transition(
            UUID subphaseId,
            SubphaseState targetState,
            Set<SubphaseState> allowedFrom) {
        if (!subphaseWorkflowPort.exists(subphaseId)) {
            throw new ProcessNotFoundException("Subfase no encontrada: " + subphaseId);
        }
        SubphaseState current = subphaseWorkflowPort.getCurrentState(subphaseId);
        if (!allowedFrom.contains(current)) {
            throw new InvalidSubphaseStateException(
                    "Subfase " + subphaseId + " en estado " + current
                            + "; se requiere " + allowedFrom);
        }
        subphaseWorkflowPort.updateState(subphaseId, targetState);
        return new SubphaseTransitionResult(subphaseId, current, targetState);
    }
}
