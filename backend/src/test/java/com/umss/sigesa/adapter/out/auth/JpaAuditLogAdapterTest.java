package com.umss.sigesa.adapter.out.auth;

import com.umss.sigesa.adapter.out.persistance.AuditLogJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AuditLogJpaEntity;
import com.umss.sigesa.domain.model.AuditLogEntry;
import com.umss.sigesa.domain.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JpaAuditLogAdapterTest {

    @Test
    @DisplayName("logLogin saves immutable audit entity with LOGIN action")
    void testLogLogin() {
        AuditLogJpaRepository repo = mock(AuditLogJpaRepository.class);
        com.umss.sigesa.application.port.out.UserRepositoryPort userRepo = mock(com.umss.sigesa.application.port.out.UserRepositoryPort.class);
        JpaAuditLogAdapter adapter = new JpaAuditLogAdapter(repo, userRepo);

        UUID userId = UUID.randomUUID();
        Email email = Email.of("user@umss.edu.bo");

        adapter.logLogin(userId, email);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(repo).save(captor.capture());

        AuditLogJpaEntity saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAction()).isEqualTo("LOGIN");
        assertThat(saved.getActorEmail()).isEqualTo("user@umss.edu.bo");
        assertThat(saved.getEntityType()).isEqualTo("USER");
        assertThat(saved.getEntityId()).isEqualTo(userId.toString());
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("logEvidenceUploaded and logDeleteDenied append without errors")
    void testEvidenceAuditLogs() {
        AuditLogJpaRepository repo = mock(AuditLogJpaRepository.class);
        com.umss.sigesa.application.port.out.UserRepositoryPort userRepo = mock(com.umss.sigesa.application.port.out.UserRepositoryPort.class);
        JpaAuditLogAdapter adapter = new JpaAuditLogAdapter(repo, userRepo);

        UUID actorId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();

        adapter.logEvidenceUploaded(actorId, evidenceId, indicatorId);
        adapter.logDeleteDenied(actorId, evidenceId);

        verify(repo, times(2)).save(any(AuditLogJpaEntity.class));
    }

    @Test
    @DisplayName("streamAuditLogsCsv writes valid CSV stream")
    void testStreamAuditLogsCsv() {
        AuditLogJpaRepository repo = mock(AuditLogJpaRepository.class);
        com.umss.sigesa.application.port.out.UserRepositoryPort userRepo = mock(com.umss.sigesa.application.port.out.UserRepositoryPort.class);
        JpaAuditLogAdapter adapter = new JpaAuditLogAdapter(repo, userRepo);

        AuditLogJpaEntity entity = AuditLogJpaEntity.builder()
                .id(UUID.randomUUID())
                .actorId(UUID.randomUUID())
                .actorEmail("jd@umss.edu.bo")
                .actorRole("JD")
                .action("STAGE_CLOSED")
                .entityType("STAGE")
                .entityId("STAGE-01")
                .payloadDiff("Cierre de compuerta E2")
                .ipAddress("192.168.1.100")
                .createdAt(LocalDateTime.of(2026, 10, 18, 12, 0, 0))
                .build();

        when(repo.findAllFiltered(any(), any(), any(), any(), any()))
                .thenReturn(List.of(entity));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        adapter.streamAuditLogsCsv(null, null, null, null, null, out);

        String csv = out.toString();
        assertThat(csv).contains("id,created_at,actor_email,actor_role,action,entity_type,entity_id,ip_address,payload_diff");
        assertThat(csv).contains("jd@umss.edu.bo");
        assertThat(csv).contains("STAGE_CLOSED");
        assertThat(csv).contains("Cierre de compuerta E2");
    }

    @Test
    @DisplayName("findAuditLogs maps entity page to domain model page")
    void testFindAuditLogs() {
        AuditLogJpaRepository repo = mock(AuditLogJpaRepository.class);
        com.umss.sigesa.application.port.out.UserRepositoryPort userRepo = mock(com.umss.sigesa.application.port.out.UserRepositoryPort.class);
        JpaAuditLogAdapter adapter = new JpaAuditLogAdapter(repo, userRepo);

        AuditLogJpaEntity entity = AuditLogJpaEntity.builder()
                .id(UUID.randomUUID())
                .actorEmail("actor@umss.edu.bo")
                .action("LOGIN")
                .entityType("USER")
                .entityId("ID-1")
                .createdAt(LocalDateTime.now())
                .build();

        when(repo.findFiltered(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));

        Page<AuditLogEntry> result = adapter.findAuditLogs(null, null, null, null, null, PageRequest.of(0, 20));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAction()).isEqualTo("LOGIN");
    }
}
