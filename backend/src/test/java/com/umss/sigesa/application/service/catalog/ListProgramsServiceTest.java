package com.umss.sigesa.application.service.catalog;

import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProgramsServiceTest {

    @Mock
    private ProgramCatalogPort programCatalogPort;

    @InjectMocks
    private ListProgramsService service;

    @Test
    void shouldMapCatalogEntriesToSummaries() {
        UUID id = UUID.randomUUID();
        when(programCatalogPort.search("ing")).thenReturn(List.of(
                new ProgramCatalogPort.ProgramEntry(id, "INF-SIS", "Ingeniería de Sistemas")));

        var result = service.list("ing");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().code()).isEqualTo("INF-SIS");
        assertThat(result.getFirst().name()).isEqualTo("Ingeniería de Sistemas");
        verify(programCatalogPort).search("ing");
    }

    @Test
    void shouldReturnEmptyListWhenCatalogHasNoMatches() {
        when(programCatalogPort.search(null)).thenReturn(List.of());

        assertThat(service.list(null)).isEmpty();
    }
}
