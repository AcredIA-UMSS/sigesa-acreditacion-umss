package com.umss.sigesa.application.service.process;

import com.umss.sigesa.domain.exception.ProcessNotFoundException;

import java.util.List;
import java.util.UUID;

public final class ProcessAccessPolicy {

    private ProcessAccessPolicy() {
    }

    public static boolean canAccess(String role, UUID careerId, List<UUID> programScope) {
        if ("JD".equals(role) || "TD".equals(role)) {
            return true;
        }
        if ("CC".equals(role)) {
            return programScope != null && programScope.contains(careerId);
        }
        return false;
    }

    public static void assertCanAccess(String role, UUID careerId, List<UUID> programScope, UUID processId) {
        if (!canAccess(role, careerId, programScope)) {
            throw new ProcessNotFoundException(processId);
        }
    }
}
