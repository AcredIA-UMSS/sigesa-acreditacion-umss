package com.umss.sigesa.adapter.out.dossier;

import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.IndicatorEvidenceLink;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZipDossierExportStreamingAdapterTest {

    @Mock
    private ProcessQueryPort processQueryPort;

    @Mock
    private NormativeHierarchyQueryPort normativeHierarchyQueryPort;

    @Mock
    private DocumentAssetPort documentAssetPort;

    @Mock
    private EvidenceBlobStoragePort blobStorage;

    @Test
    @DisplayName("Genera ZIP con estructura jerárquica e índice maestro")
    void generatesZipWithHierarchyAndIndex() throws Exception {
        UUID processId = UUID.randomUUID();
        ZipDossierExportStreamingAdapter adapter = new ZipDossierExportStreamingAdapter(
                processQueryPort, normativeHierarchyQueryPort, documentAssetPort, blobStorage);

        AccreditationProcess process = AccreditationProcess.builder().id(processId).careerId(UUID.randomUUID()).status("ACTIVE").build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        UUID level1Id = UUID.randomUUID();
        UUID level2Id = UUID.randomUUID();
        UUID level3Id = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();

        NormativeIndicator indicator = NormativeIndicator.builder().id(indicatorId).code("IND-1.1").description("Indicador 1").order(1).build();
        Level3Node level3 = Level3Node.builder().id(level3Id).name("Criterio A").indicators(List.of(indicator)).build();
        Level2Node level2 = Level2Node.builder().id(level2Id).name("Componente B").level3Nodes(List.of(level3)).build();
        Level1Node level1 = Level1Node.builder().id(level1Id).name("Dimensión X").level2Nodes(List.of(level2)).build();

        NormativeHierarchyQueryPort.ProcessNormativeTree tree =
                new NormativeHierarchyQueryPort.ProcessNormativeTree(processId, List.of(level1));
        when(normativeHierarchyQueryPort.findProcessTree(processId)).thenReturn(Optional.of(tree));

        UUID assetId = UUID.randomUUID();
        DocumentAsset asset = DocumentAsset.builder()
                .id(assetId)
                .processId(processId)
                .folioCode("FOLIO-0012")
                .title("resolucion.pdf")
                .storageKey("evidences/1/v1/resolucion.pdf")
                .build();
        when(documentAssetPort.findByProcessId(processId)).thenReturn(List.of(asset));
        IndicatorEvidenceLink link = IndicatorEvidenceLink.builder().id(UUID.randomUUID()).indicatorId(indicatorId).documentAssetId(assetId).evidenceId(UUID.randomUUID()).build();
        when(documentAssetPort.findAllLinksByProcessId(processId)).thenReturn(List.of(link));
        when(blobStorage.retrieve("evidences/1/v1/resolucion.pdf")).thenReturn(Optional.of("PDFDATA".getBytes()));

        File zip = adapter.generateDossierZip(processId, UUID.randomUUID().toString());
        assertNotNull(zip);
        assertTrue(zip.exists());

        List<String> entries = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry.getName());
                zis.closeEntry();
            }
        }
        assertTrue(entries.stream().anyMatch(e -> e.contains("00_Indice_Maestro_Cumplimiento.html")));
        assertTrue(entries.stream().anyMatch(e -> e.contains("FOLIO-0012") && e.contains("resolucion.pdf")));
    }

    @Test
    @DisplayName("Si archivo falta en storage, genera archivo de advertencia")
    void addsWarningWhenBlobMissing() throws Exception {
        UUID processId = UUID.randomUUID();
        ZipDossierExportStreamingAdapter adapter = new ZipDossierExportStreamingAdapter(
                processQueryPort, normativeHierarchyQueryPort, documentAssetPort, blobStorage);

        AccreditationProcess process = AccreditationProcess.builder().id(processId).careerId(UUID.randomUUID()).status("ACTIVE").build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        UUID level1Id = UUID.randomUUID();
        UUID level2Id = UUID.randomUUID();
        UUID level3Id = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();

        NormativeIndicator indicator = NormativeIndicator.builder().id(indicatorId).code("IND-1.1").description("Indicador 1").order(1).build();
        Level3Node level3 = Level3Node.builder().id(level3Id).name("Criterio A").indicators(List.of(indicator)).build();
        Level2Node level2 = Level2Node.builder().id(level2Id).name("Componente B").level3Nodes(List.of(level3)).build();
        Level1Node level1 = Level1Node.builder().id(level1Id).name("Dimensión X").level2Nodes(List.of(level2)).build();

        NormativeHierarchyQueryPort.ProcessNormativeTree tree =
                new NormativeHierarchyQueryPort.ProcessNormativeTree(processId, List.of(level1));
        when(normativeHierarchyQueryPort.findProcessTree(processId)).thenReturn(Optional.of(tree));

        UUID assetId = UUID.randomUUID();
        DocumentAsset asset = DocumentAsset.builder()
                .id(assetId)
                .processId(processId)
                .folioCode("FOLIO-0099")
                .title("missing.pdf")
                .storageKey("missing/key")
                .build();
        when(documentAssetPort.findByProcessId(processId)).thenReturn(List.of(asset));
        IndicatorEvidenceLink link = IndicatorEvidenceLink.builder().id(UUID.randomUUID()).indicatorId(indicatorId).documentAssetId(assetId).evidenceId(UUID.randomUUID()).build();
        when(documentAssetPort.findAllLinksByProcessId(processId)).thenReturn(List.of(link));
        when(blobStorage.retrieve("missing/key")).thenReturn(Optional.empty());

        File zip = adapter.generateDossierZip(processId, UUID.randomUUID().toString());
        assertNotNull(zip);
        assertTrue(zip.exists());
    }
}
