package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.EvidenceAuditDetails;
import com.umss.sigesa.domain.model.Role;

import java.util.UUID;

public interface AuditLogPort {

    void logLogin(UUID userId, Email email);

    /**
     * Intento de login fallido. No recibe la contraseña. La implementación debe
     * persistir el evento en una transacción independiente del fallo de negocio.
     */
    void logLoginFailure(String attemptedEmail);

    void logLogout(UUID userId, Email email);

    /**
     * Un JWT emitido por el sistema llegó vencido. No recibe el token.
     * Debe persistir aunque la petición no quede autenticada.
     */
    void logSessionExpired(UUID userId, String email);

    void logUserRegistered(UUID userId, Email email, Role role);

    void logUserDeactivated(UUID userId, Email email);

    void logUserActivated(UUID userId, Email email);

    void logReportRequested(UUID requesterId, UUID jobId);

    void logEvidenceUploaded(UUID uploadedBy, UUID evidenceId, EvidenceAuditDetails details);
}
