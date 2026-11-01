package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.AuditLogQueryPort;
import com.umss.sigesa.domain.model.AuditAction;
import com.umss.sigesa.domain.model.AuditEvent;
import com.umss.sigesa.domain.model.AuditLogQuery;
import com.umss.sigesa.domain.model.AuditResourceType;
import com.umss.sigesa.domain.model.AuditResult;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.EvidenceAuditDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(AuditLogPersistenceIT.FixedClockConfig.class)
class AuditLogPersistenceIT {

    static final Instant FIXED = Instant.parse("2026-10-29T15:00:00Z");

    @Autowired
    private AuditLogPort auditLogPort;

    @Autowired
    private AuditLogQueryPort queryPort;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("un login exitoso guarda actor, acción y hora UTC")
    void successfulLoginIsStoredInUtc() {
        UUID actorId = UUID.randomUUID();
        auditLogPort.logLogin(actorId, Email.of("jd.audit@umss.edu.bo"));

        AuditEvent event = findByActor(actorId).getContent().get(0);
        assertEquals(AuditAction.LOGIN, event.action());
        assertEquals(AuditResult.SUCCESS, event.result());
        assertEquals("jd.audit@umss.edu.bo", event.actorEmail());
        assertEquals(FIXED, event.occurredAt());
        assertNull(event.payload());
    }

    @Test
    @DisplayName("un login fallido no conserva contraseñas ni correos que no son institucionales")
    void failedLoginDropsSecrets() {
        auditLogPort.logLoginFailure("s3cret-password");
        auditLogPort.logLoginFailure("ghost.audit@umss.edu.bo");

        Page<AuditEvent> failures = queryPort.find(new AuditLogQuery(
                null, null, Set.of(AuditAction.LOGIN), null, null, AuditResult.FAILURE, null, null),
                PageRequest.of(0, 50));

        assertTrue(failures.getContent().stream().anyMatch(event ->
                "ghost.audit@umss.edu.bo".equals(event.actorEmail())
                        && event.occurredAt().equals(FIXED)
                        && event.payload() == null));
        assertTrue(failures.getContent().stream().noneMatch(event ->
                containsSecret(event, "s3cret-password")));
        assertTrue(failures.getContent().stream().anyMatch(event -> event.actorEmail() == null));
    }

    @Test
    @DisplayName("una carga de evidencia guarda la evidencia y el indicador")
    void evidenceUploadStoresResourceAndIndicator() {
        UUID actorId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        auditLogPort.logEvidenceUploaded(actorId, evidenceId, new EvidenceAuditDetails(
                indicatorId, UUID.randomUUID(), null, "../informe final.pdf"));

        Page<AuditEvent> page = queryPort.find(new AuditLogQuery(
                actorId, null, Set.of(AuditAction.EVIDENCE_UPLOADED), AuditResourceType.EVIDENCE,
                evidenceId, AuditResult.SUCCESS, null, null), PageRequest.of(0, 20));

        assertEquals(1, page.getTotalElements());
        AuditEvent event = page.getContent().get(0);
        assertEquals(evidenceId, event.resourceId());
        assertTrue(event.payload().contains(indicatorId.toString()));
        assertTrue(event.payload().contains("informe final.pdf"));
        assertFalse(event.payload().contains("../"));
        assertFalse(event.payload().contains("password"));
        assertEquals(FIXED, event.occurredAt());
    }

    @Test
    @DisplayName("el evento de éxito se revierte con la transacción de negocio")
    void successEventRollsBackWithBusinessTransaction() {
        UUID actorId = UUID.randomUUID();
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        assertThrows(IllegalStateException.class, () -> template.executeWithoutResult(status -> {
            auditLogPort.logLogin(actorId, Email.of("rollback.audit@umss.edu.bo"));
            throw new IllegalStateException("rollback");
        }));

        assertEquals(0, findByActor(actorId).getTotalElements());
    }

    @Test
    @DisplayName("el login fallido se conserva aunque la transacción exterior se revierta")
    void loginFailureSurvivesOuterRollback() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        assertThrows(IllegalStateException.class, () -> template.executeWithoutResult(status -> {
            auditLogPort.logLoginFailure("survives.audit@umss.edu.bo");
            throw new IllegalStateException("rollback");
        }));

        Page<AuditEvent> page = queryPort.find(new AuditLogQuery(
                null, "survives.audit@umss.edu.bo", Set.of(AuditAction.LOGIN), null, null,
                AuditResult.FAILURE, null, null), PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
    }

    private Page<AuditEvent> findByActor(UUID actorId) {
        return queryPort.find(new AuditLogQuery(
                actorId, null, Set.of(), null, null, null, null, null), PageRequest.of(0, 20));
    }

    private static boolean containsSecret(AuditEvent event, String secret) {
        return (event.actorEmail() != null && event.actorEmail().contains(secret))
                || (event.payload() != null && event.payload().contains(secret))
                || (event.description() != null && event.description().contains(secret));
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedAuditClock() {
            return Clock.fixed(FIXED, ZoneOffset.UTC);
        }
    }
}
