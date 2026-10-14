package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.ProcessFreezePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.exception.ProcessFrozenException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.OperationalMode;
import com.umss.sigesa.domain.model.ProcessFreeze;
import com.umss.sigesa.domain.model.StageDeliverableCode;

import java.util.UUID;

public class ProcessFreezeGuard {

    private final ProcessFreezePort freezePort;
    private final ProcessQueryPort processQueryPort;

    public ProcessFreezeGuard(ProcessFreezePort freezePort, ProcessQueryPort processQueryPort) {
        this.freezePort = freezePort;
        this.processQueryPort = processQueryPort;
    }

    public boolean isActiveAuditReadonly(UUID processId) {
        return freezePort.findByProcessId(processId)
                .map(ProcessFreeze::isActiveAuditReadonly)
                .orElse(false);
    }

    public void assertWritable(UUID processId) {
        if (isActiveAuditReadonly(processId)) {
            throw new ProcessFrozenException();
        }
        if (isImprovementMonitoring(processId)) {
            throw new ProcessFrozenException(
                    "En monitoreo de mejoras solo se permite actualizar avance del plan y generar E7.");
        }
    }

    public void assertPlanWritable(UUID processId) {
        if (isActiveAuditReadonly(processId)) {
            throw new ProcessFrozenException();
        }
    }

    public void assertGenerateWritable(UUID processId, StageDeliverableCode code) {
        if (StageDeliverableCode.isE4(code)) {
            assertWritable(processId);
            return;
        }
        if (StageDeliverableCode.isE7(code)) {
            if (isActiveAuditReadonly(processId)) {
                throw new ProcessFrozenException();
            }
            return;
        }
        assertWritable(processId);
    }

    private boolean isImprovementMonitoring(UUID processId) {
        return processQueryPort.findDetailById(processId)
                .map(AccreditationProcess::getOperationalMode)
                .filter(mode -> mode == OperationalMode.IMPROVEMENT_MONITORING)
                .isPresent();
    }
}
