package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.adapter.out.persistance.entity.AuditLogJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessActionSummaryTest {

    @Test
    void labelsCreateUpdateAndDeleteAndSkipsReads() {
        UUID sessionId = UUID.randomUUID();
        List<String> labels = AccessActionSummary.bySession(List.of(
                entry(sessionId, "LOGIN", "USER"),
                entry(sessionId, "READ", "PROCESOS"),
                entry(sessionId, "CREATE", "EVIDENCIAS"),
                entry(sessionId, "EVIDENCE_UPLOADED", "EVIDENCE"),
                entry(sessionId, "UPDATE", "PROCESOS"),
                entry(sessionId, "DELETE", "USUARIOS"),
                entry(sessionId, "LOGOUT", "SESSION")
        )).get(sessionId);

        assertEquals(List.of("Crear evidencia", "Modificar proceso", "Eliminar usuario"), labels);
    }

    @Test
    void sessionWithoutElementChangesStaysEmpty() {
        UUID sessionId = UUID.randomUUID();
        assertTrue(AccessActionSummary.bySession(List.of(entry(sessionId, "READ", "SISTEMA"))).isEmpty());
        assertNull(AccessActionSummary.labelOf(entry(sessionId, "LOGIN", "USER")));
    }

    private static AuditLogJpaEntity entry(UUID sessionId, String action, String entityType) {
        return AuditLogJpaEntity.builder()
                .id(UUID.randomUUID())
                .actorEmail("jd@umss.edu.bo")
                .action(action)
                .entityType(entityType)
                .entityId("1")
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
