package com.umss.sigesa.application.service.dashboard;

import com.umss.sigesa.application.model.dashboard.TechnicianInboxItem;
import com.umss.sigesa.application.port.out.TechnicianInboxQueryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetTechnicianInboxServiceTest {

    @Test
    @DisplayName("getInbox delegates to queryPort and caps max page size to 50")
    void testGetInboxCappedPageSize() {
        TechnicianInboxQueryPort queryPort = mock(TechnicianInboxQueryPort.class);
        GetTechnicianInboxService service = new GetTechnicianInboxService(queryPort);

        UUID programId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        String status = "SUBIDO";
        Pageable excessivePageable = PageRequest.of(0, 100);

        TechnicianInboxItem item = TechnicianInboxItem.builder()
                .indicatorId(UUID.randomUUID())
                .indicatorCode("IND-1.1.1")
                .indicatorDescription("Descripción de prueba")
                .status("SUBIDO")
                .dimensionName("Dimensión 1")
                .pathHierarchy("D1 > C1 > S1")
                .programId(programId)
                .activeObservationsCount(1)
                .updatedAt(LocalDateTime.now())
                .build();

        when(queryPort.findInboxItems(eq(programId), eq(level1Id), eq(status), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 50), 1));

        Page<TechnicianInboxItem> result = service.getInbox(programId, level1Id, status, excessivePageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIndicatorCode()).isEqualTo("IND-1.1.1");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(queryPort).findInboxItems(eq(programId), eq(level1Id), eq(status), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
    }

    @Test
    @DisplayName("getInbox preserves page size when within limit and handles unpaged requests")
    void testGetInboxPreservesAcceptableSize() {
        TechnicianInboxQueryPort queryPort = mock(TechnicianInboxQueryPort.class);
        GetTechnicianInboxService service = new GetTechnicianInboxService(queryPort);

        when(queryPort.findInboxItems(any(), any(), any(), any()))
                .thenReturn(Page.empty());

        Pageable reasonable = PageRequest.of(2, 25);
        service.getInbox(null, null, null, reasonable);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(queryPort).findInboxItems(eq(null), eq(null), eq(null), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(25);

        // Edge case: unpaged
        service.getInbox(null, null, null, Pageable.unpaged());
        verify(queryPort).findInboxItems(eq(null), eq(null), eq(null), eq(Pageable.unpaged()));
    }
}
