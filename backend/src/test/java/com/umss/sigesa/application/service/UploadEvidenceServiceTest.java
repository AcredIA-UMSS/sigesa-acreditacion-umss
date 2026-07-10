package com.umss.sigesa.application.service;

import com.umss.sigesa.adapter.in.web.dto.EvidenceResponse;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.FileStoragePort;
import com.umss.sigesa.application.port.out.IndicatorStateHistoryPort;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.ForbiddenProgramScopeException;
import com.umss.sigesa.domain.exception.InvalidFileFormatException;
import com.umss.sigesa.domain.exception.MaxFileSizeExceededException;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadEvidenceService Unit Tests")
class UploadEvidenceServiceTest {

    @Mock
    private EvidenceRepositoryPort evidenceRepository;

    @Mock
    private FileStoragePort fileStorage;

    @Mock
    private IndicatorStateHistoryPort indicatorStateHistory;

    private UploadEvidenceService service;

    private UUID indicatorId;
    private UUID criterionId;
    private UUID programId;
    private AuthenticatedIdentity coordinatorIdentity;

    @BeforeEach
    void setUp() {
        service = new UploadEvidenceService(evidenceRepository, fileStorage, indicatorStateHistory);
        indicatorId = UUID.randomUUID();
        criterionId = UUID.randomUUID();
        programId = UUID.randomUUID();

        coordinatorIdentity = new AuthenticatedIdentity(
                UUID.randomUUID(),
                Email.of("cc@umss.edu.bo"),
                Role.CC,
                List.of(programId)
        );
    }

    @Test
    @DisplayName("FSD-BR-01: Carga de evidencia rechazada si no hay indicador o criterio asociado")
    void upload_withoutIndicatorOrCriterion_shouldThrowException() {
        byte[] content = "test content".getBytes();
        assertThrows(EvidenceUnclassifiedException.class, () ->
                service.upload(null, criterionId, "Desc", "file.pdf", content, "application/pdf", coordinatorIdentity)
        );

        assertThrows(EvidenceUnclassifiedException.class, () ->
                service.upload(indicatorId, null, "Desc", "file.pdf", content, "application/pdf", coordinatorIdentity)
        );
    }

    @Test
    @DisplayName("FSD-BR-03: Solo Coordinador de Carrera [CC] puede cargar evidencias")
    void upload_nonCcRole_shouldThrowException() {
        AuthenticatedIdentity technicianIdentity = new AuthenticatedIdentity(
                UUID.randomUUID(),
                Email.of("td@umss.edu.bo"),
                Role.TD,
                List.of(programId)
        );
        byte[] content = "test content".getBytes();

        assertThrows(ForbiddenProgramScopeException.class, () ->
                service.upload(indicatorId, criterionId, "Desc", "file.pdf", content, "application/pdf", technicianIdentity)
        );
    }

    @Test
    @DisplayName("FSD-BR-09: Coordinador [CC] sólo puede cargar evidencias a su propia carrera")
    void upload_ccOutsideProgramScope_shouldThrowException() {
        UUID otherProgramId = UUID.randomUUID();
        when(evidenceRepository.findProgramIdForIndicator(indicatorId)).thenReturn(otherProgramId);

        byte[] content = "test content".getBytes();

        assertThrows(ForbiddenProgramScopeException.class, () ->
                service.upload(indicatorId, criterionId, "Desc", "file.pdf", content, "application/pdf", coordinatorIdentity)
        );
    }

    @Test
    @DisplayName("Carga exitosa: valida tamaño, formato, guarda binario, registra historia y retorna versión 1")
    void upload_successful() {
        byte[] content = "test content".getBytes();
        when(evidenceRepository.findProgramIdForIndicator(indicatorId)).thenReturn(programId);
        when(fileStorage.store(eq("file.pdf"), eq(content))).thenReturn(
                new FileStoragePort.StorageResult("storage-key-123", "sha256-hash-xyz")
        );

        EvidenceResponse response = service.upload(
                indicatorId,
                criterionId,
                "Valid description",
                "file.pdf",
                content,
                "application/pdf",
                coordinatorIdentity
        );

        assertNotNull(response);
        assertEquals(1, response.version());
        assertEquals("sha256-hash-xyz", response.contentHash());
        assertEquals("EvidenceUploaded", response.event());

        verify(evidenceRepository).save(any(), any());
        verify(indicatorStateHistory).recordTransition(eq(indicatorId), eq("PENDIENTE"), eq("SUBIDO"), eq(coordinatorIdentity.userId()), eq(Role.CC));
    }

    @Test
    @DisplayName("Validación: Archivo excede el límite de 5MB")
    void upload_exceedsMaxSize_shouldThrowException() {
        byte[] largeContent = new byte[5 * 1024 * 1024 + 1];
        when(evidenceRepository.findProgramIdForIndicator(indicatorId)).thenReturn(programId);

        assertThrows(MaxFileSizeExceededException.class, () ->
                service.upload(indicatorId, criterionId, "Desc", "file.pdf", largeContent, "application/pdf", coordinatorIdentity)
        );
    }

    @Test
    @DisplayName("Validación: Formato de archivo no permitido")
    void upload_invalidFormat_shouldThrowException() {
        byte[] content = "test content".getBytes();
        when(evidenceRepository.findProgramIdForIndicator(indicatorId)).thenReturn(programId);

        assertThrows(InvalidFileFormatException.class, () ->
                service.upload(indicatorId, criterionId, "Desc", "file.exe", content, "application/octet-stream", coordinatorIdentity)
        );
    }
}
