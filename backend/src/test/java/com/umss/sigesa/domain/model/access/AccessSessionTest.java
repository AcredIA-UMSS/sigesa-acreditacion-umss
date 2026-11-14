package com.umss.sigesa.domain.model.access;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AccessSessionTest {

    @Test
    void closeFromActiveSetsEndedAtAndDoesNotReopen() {
        LocalDateTime started = LocalDateTime.of(2026, 11, 11, 8, 0);
        AccessSession open = new AccessSession(
                UUID.randomUUID(), UUID.randomUUID(), "Ana Pérez", "ana@umss.edu.bo", "TD",
                started, null, null, "127.0.0.1", "agent", AccessSessionStatus.ACTIVE);

        AccessSession closed = open.close(AccessSessionEndReason.LOGOUT, started.plusMinutes(12));

        assertEquals(AccessSessionStatus.CLOSED, closed.status());
        assertEquals(AccessSessionEndReason.LOGOUT, closed.endReason());
        assertEquals(started.plusMinutes(12), closed.endedAt());
        assertSame(closed, closed.close(AccessSessionEndReason.EXPIRED, started.plusHours(1)));
    }

    @Test
    void failedSessionStaysFailed() {
        AccessSession failed = new AccessSession(
                UUID.randomUUID(), null, null, "nadie@umss.edu.bo", null,
                LocalDateTime.now(), LocalDateTime.now(), AccessSessionEndReason.FAILED,
                "10.0.0.8", null, AccessSessionStatus.FAILED);

        assertSame(failed, failed.close(AccessSessionEndReason.LOGOUT, LocalDateTime.now()));
    }
}
