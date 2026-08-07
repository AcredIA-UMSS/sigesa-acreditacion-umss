package com.umss.sigesa.application.port.out;

import java.util.UUID;

public interface SubphaseWorkflowPort {

    boolean hasBlockingEvidence(UUID subphaseId);
}
