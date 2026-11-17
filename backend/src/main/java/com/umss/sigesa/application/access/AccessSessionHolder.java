package com.umss.sigesa.application.access;

import java.util.UUID;

/**
 * Sesión de ingreso del hilo actual. La fija el login o el filtro JWT y la lee la bitácora.
 */
public final class AccessSessionHolder {

    public static final String REQUEST_ATTRIBUTE = "sigesa.accessSessionId";
    public static final String ACTOR_EMAIL_ATTRIBUTE = "sigesa.actorEmail";
    public static final String AUDITED_ATTRIBUTE = "sigesa.accessAudited";

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private AccessSessionHolder() {
    }

    public static void bind(UUID sessionId) {
        if (sessionId != null) {
            CURRENT.set(sessionId);
        }
    }

    public static UUID current() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
