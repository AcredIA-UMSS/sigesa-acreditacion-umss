package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.EvidenceLifecycleQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.EvidenceImmutableException;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.EvidenceVersionHistoryItem;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceLifecycleServiceTest {

    private static final UUID EVIDENCE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PROGRAM_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID USER_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID VERSION_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    @Mock
    private EvidenceLifecycleQueryPort lifecycleQueryPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private AuditLogPort auditLogPort;

    private ListEvidenceVersionsService listService;
    private AttemptDeleteEvidenceService deleteService;

    @BeforeEach
    void setUp() {
        listService = new ListEvidenceVersionsService(lifecycleQueryPort, assignmentRepository);
        deleteService = new AttemptDeleteEvidenceService(lifecycleQueryPort, assignmentRepository, auditLogPort);
    }

    @Test
    void listVersions_marksCurrentForTd() {
        when(lifecycleQueryPort.findContext(EVIDENCE_ID))
                .thenReturn(Optional.of(new EvidenceLifecycleQueryPort.EvidenceContext(
                        EVIDENCE_ID, PROGRAM_ID, VERSION_ID)));
        when(lifecycleQueryPort.listVersions(EVIDENCE_ID, VERSION_ID))
                .thenReturn(List.of(new EvidenceVersionHistoryItem(
                        VERSION_ID, 1, null, null, "desc", "hash", "doc.pdf", USER_ID,
                        LocalDateTime.now(), true, true)));

        List<EvidenceVersionHistoryItem> result = listService.list(
                EVIDENCE_ID, USER_ID, List.of("TD"));

        assertEquals(1, result.size());
        assertTrue(result.getFirst().current());
    }

    @Test
    void listVersions_deniesCcOutsideScope() {
        when(lifecycleQueryPort.findContext(EVIDENCE_ID))
                .thenReturn(Optional.of(new EvidenceLifecycleQueryPort.EvidenceContext(
                        EVIDENCE_ID, PROGRAM_ID, VERSION_ID)));
        when(assignmentRepository.findActiveByUserId(USER_ID)).thenReturn(List.of());

        assertThrows(ProgramScopeDeniedException.class, () ->
                listService.list(EVIDENCE_ID, USER_ID, List.of("CC")));
    }

    @Test
    void listVersions_allowsCcInScope() {
        when(lifecycleQueryPort.findContext(EVIDENCE_ID))
                .thenReturn(Optional.of(new EvidenceLifecycleQueryPort.EvidenceContext(
                        EVIDENCE_ID, PROGRAM_ID, VERSION_ID)));
        when(assignmentRepository.findActiveByUserId(USER_ID))
                .thenReturn(List.of(new UserProgramAssignment(
                        UUID.randomUUID(), USER_ID, PROGRAM_ID, LocalDateTime.now(), null)));
        when(lifecycleQueryPort.listVersions(EVIDENCE_ID, VERSION_ID)).thenReturn(List.of());

        listService.list(EVIDENCE_ID, USER_ID, List.of("CC"));
    }

    @Test
    void delete_alwaysImmutableAndAudited() {
        when(lifecycleQueryPort.findContext(EVIDENCE_ID))
                .thenReturn(Optional.of(new EvidenceLifecycleQueryPort.EvidenceContext(
                        EVIDENCE_ID, PROGRAM_ID, VERSION_ID)));

        assertThrows(EvidenceImmutableException.class, () ->
                deleteService.attemptDelete(EVIDENCE_ID, USER_ID, List.of("JD")));

        verify(auditLogPort).logDeleteDenied(USER_ID, EVIDENCE_ID);
    }

    @Test
    void listVersions_notFound() {
        when(lifecycleQueryPort.findContext(EVIDENCE_ID)).thenReturn(Optional.empty());

        assertThrows(EvidenceNotFoundException.class, () ->
                listService.list(EVIDENCE_ID, USER_ID, List.of("TD")));
    }
}
