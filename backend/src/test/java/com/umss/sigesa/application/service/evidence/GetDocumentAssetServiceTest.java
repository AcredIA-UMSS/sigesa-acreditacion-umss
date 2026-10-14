package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.DocumentAssetNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDocumentAssetService — M7")
class GetDocumentAssetServiceTest {

    @Mock
    private DocumentAssetPort documentAssetPort;
    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    @InjectMocks
    private GetDocumentAssetService service;

    @Test
    @DisplayName("Devuelve metadatos y linkedIndicatorIds")
    void get_success() {
        UUID userId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        DocumentAsset asset = DocumentAsset.create(
                processId, "doc.pdf", "key", "abcd1234ffff", userId, LocalDateTime.now());

        when(documentAssetPort.findById(asset.getId())).thenReturn(Optional.of(asset));
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));
        when(documentAssetPort.findLinkedIndicatorIds(asset.getId())).thenReturn(List.of(indicatorId));

        var view = service.get(asset.getId(), userId, List.of("CC"));

        assertEquals(asset.getFolioCode(), view.folioCode());
        assertEquals(List.of(indicatorId), view.linkedIndicatorIds());
        assertEquals(processId, view.processId());
    }

    @Test
    @DisplayName("Asset inexistente")
    void get_notFound() {
        UUID id = UUID.randomUUID();
        when(documentAssetPort.findById(id)).thenReturn(Optional.empty());
        assertThrows(DocumentAssetNotFoundException.class,
                () -> service.get(id, UUID.randomUUID(), List.of("CC")));
    }

    @Test
    @DisplayName("CC fuera de alcance")
    void get_scopeDenied() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        DocumentAsset asset = DocumentAsset.create(
                processId, "doc.pdf", "key", "abcd1234ffff", userId, LocalDateTime.now());
        when(documentAssetPort.findById(asset.getId())).thenReturn(Optional.of(asset));
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(UUID.randomUUID()).status("ACTIVE").build()));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());

        assertThrows(ProgramScopeDeniedException.class,
                () -> service.get(asset.getId(), userId, List.of("CC")));
    }
}
