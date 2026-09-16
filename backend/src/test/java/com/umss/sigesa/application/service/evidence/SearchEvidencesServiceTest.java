package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.out.EvidenceSearchQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.EvidenceSearchCriteria;
import com.umss.sigesa.domain.model.EvidenceSearchPage;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchEvidencesServiceTest {

    @Mock
    private EvidenceSearchQueryPort searchQueryPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    @InjectMocks
    private SearchEvidencesService service;

    @Test
    void shouldSearchWithoutProgramFilterForTechnician() {
        UUID requesterId = UUID.randomUUID();
        EvidenceSearchCriteria criteria = new EvidenceSearchCriteria(
                UUID.randomUUID(), null, null, null, null, "plan", 2026, 0, 20);
        EvidenceSearchPage page = new EvidenceSearchPage(List.of(), 0, 0, 20);
        when(searchQueryPort.search(criteria, null)).thenReturn(page);

        EvidenceSearchPage result = service.search(criteria, requesterId, List.of("TD"));

        assertThat(result.total()).isZero();
        verify(assignmentRepository, never()).findActiveByUserId(any());
        verify(searchQueryPort).search(criteria, null);
    }

    @Test
    void shouldSearchWithinAssignedProgramsForCoordinator() {
        UUID requesterId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        EvidenceSearchCriteria criteria = new EvidenceSearchCriteria(
                UUID.randomUUID(), null, null, null, null, null, null, 0, 10);
        when(assignmentRepository.findActiveByUserId(requesterId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), requesterId, programId, LocalDateTime.now(), null)));
        when(searchQueryPort.search(eq(criteria), eq(List.of(programId))))
                .thenReturn(new EvidenceSearchPage(List.of(), 0, 0, 20));

        service.search(criteria, requesterId, List.of("CC"));

        verify(searchQueryPort).search(eq(criteria), eq(List.of(programId)));
    }

    @Test
    void shouldThrowWhenCoordinatorHasNoAssignedPrograms() {
        UUID requesterId = UUID.randomUUID();
        EvidenceSearchCriteria criteria = new EvidenceSearchCriteria(
                UUID.randomUUID(), null, null, null, null, "q", null, 0, 10);
        when(assignmentRepository.findActiveByUserId(requesterId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.search(criteria, requesterId, List.of("CC")))
                .isInstanceOf(ProgramScopeDeniedException.class);
        verify(searchQueryPort, never()).search(any(), any());
    }

    @Test
    void shouldTreatJdAsUnrestrictedLikeTechnician() {
        EvidenceSearchCriteria criteria = new EvidenceSearchCriteria(
                UUID.randomUUID(), null, null, null, null, "", null, 0, 5);
        when(searchQueryPort.search(any(), isNull())).thenReturn(new EvidenceSearchPage(List.of(), 0, 0, 20));

        service.search(criteria, UUID.randomUUID(), List.of("JD"));

        verify(assignmentRepository, never()).findActiveByUserId(any());
    }
}
