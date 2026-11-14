package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.TechnicianInboxResponseDto;
import com.umss.sigesa.application.model.dashboard.TechnicianInboxItem;
import com.umss.sigesa.application.port.in.GetTechnicianInboxUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TechnicianInboxControllerTest {

    @Test
    @DisplayName("getInbox returns OK with mapped DTO page")
    void testGetInboxSuccess() {
        GetTechnicianInboxUseCase useCase = mock(GetTechnicianInboxUseCase.class);
        TechnicianInboxController controller = new TechnicianInboxController(useCase);

        UUID programId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();

        TechnicianInboxItem item = TechnicianInboxItem.builder()
                .indicatorId(indicatorId)
                .indicatorCode("IND-101")
                .indicatorDescription("Test indicator")
                .status("SUBSANADO")
                .dimensionName("Dimensión 1")
                .criterionName("Criterio 1")
                .subcriterionName("Subcriterio 1")
                .pathHierarchy("D1 > C1 > S1")
                .programId(programId)
                .programName("Carrera Informática")
                .activeObservationsCount(0)
                .updatedAt(LocalDateTime.now())
                .build();

        Page<TechnicianInboxItem> page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);
        when(useCase.getInbox(eq(programId), any(), eq("SUBSANADO"), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<TechnicianInboxResponseDto>> response = controller.getInbox(
                programId, null, "SUBSANADO", PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);

        TechnicianInboxResponseDto dto = response.getBody().getContent().get(0);
        assertThat(dto.getIndicatorId()).isEqualTo(indicatorId);
        assertThat(dto.getIndicatorCode()).isEqualTo("IND-101");
        assertThat(dto.getStatus()).isEqualTo("SUBSANADO");
        assertThat(dto.getPathHierarchy()).isEqualTo("D1 > C1 > S1");
        assertThat(dto.getEvidenceId()).isNull();
    }

    @Test
    @DisplayName("getInbox returns OK with empty page when no indicators match criteria")
    void testGetInboxEmpty() {
        GetTechnicianInboxUseCase useCase = mock(GetTechnicianInboxUseCase.class);
        TechnicianInboxController controller = new TechnicianInboxController(useCase);

        when(useCase.getInbox(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        ResponseEntity<Page<TechnicianInboxResponseDto>> response = controller.getInbox(
                null, null, null, PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isZero();
    }
}
