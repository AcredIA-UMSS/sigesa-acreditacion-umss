package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

final class EvidenceControlScopeSupport {

    private EvidenceControlScopeSupport() {
    }

    static String normalizedRole(AssistantAuthContext auth) {
        if (auth == null || auth.role() == null) {
            return "";
        }
        return auth.role().trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Resolves program filter for list queries.
     * Empty list = all programs (JD/TD). CC is always constrained to JWT scope.
     */
    static List<UUID> resolveProgramFilter(AssistantAuthContext auth, UUID requestedProgramId) {
        String role = normalizedRole(auth);
        List<UUID> scope = auth.programScope() == null ? List.of() : auth.programScope();

        if ("CC".equals(role)) {
            if (requestedProgramId != null) {
                if (!scope.contains(requestedProgramId)) {
                    throw new ProgramScopeDeniedException();
                }
                return List.of(requestedProgramId);
            }
            return scope;
        }

        if ("JD".equals(role) || "TD".equals(role)) {
            if (requestedProgramId != null) {
                return List.of(requestedProgramId);
            }
            return List.of();
        }

        throw new ProgramScopeDeniedException();
    }

    static void assertIndicatorInScope(AssistantAuthContext auth, UUID programId) {
        String role = normalizedRole(auth);
        if ("JD".equals(role) || "TD".equals(role)) {
            return;
        }
        if (!"CC".equals(role)) {
            throw new ProgramScopeDeniedException();
        }
        List<UUID> scope = auth.programScope() == null ? List.of() : auth.programScope();
        if (programId == null || !scope.contains(programId)) {
            throw new ProgramScopeDeniedException();
        }
    }
}
