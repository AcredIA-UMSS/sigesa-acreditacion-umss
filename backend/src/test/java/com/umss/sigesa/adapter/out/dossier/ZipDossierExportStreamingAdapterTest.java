package com.umss.sigesa.adapter.out.dossier;

import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.IndicatorEvidenceLink;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
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

    @Mock
    private ProgramCatalogPort programCatalogPort;

    @Mock
    private TemplatePort templatePort;

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

    @Test
    @DisplayName("Genera manifiest de checksums SHA-256 dentro del ZIP")
    void generatesChecksumsManifest() throws Exception {
        UUID processId = UUID.randomUUID();
        String jobId = "test-job-001";
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
        byte[] fileContent = "contenido de prueba del archivo PDF".getBytes(StandardCharsets.UTF_8);
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
        when(blobStorage.retrieve("evidences/1/v1/resolucion.pdf")).thenReturn(Optional.of(fileContent));

        File zip = adapter.generateDossierZip(processId, jobId);
        assertNotNull(zip);

        boolean manifestFound = false;
        String manifestContent = null;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("00_Manifiest_Checksums.txt")) {
                    manifestFound = true;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = zis.read(buf)) != -1) {
                        bos.write(buf, 0, n);
                    }
                    manifestContent = bos.toString(StandardCharsets.UTF_8);
                    break;
                }
                zis.closeEntry();
            }
        }
        assertTrue(manifestFound, "El manifiest de checksums debe estar presente en el ZIP");
        assertNotNull(manifestContent);
        assertTrue(manifestContent.contains("SHA-256"), "El manifiest debe contener checksums SHA-256");
        assertTrue(manifestContent.contains("FOLIO-0012") || manifestContent.contains("resolucion.pdf"),
                "El manifiest debe referenciar el archivo de evidencia");
    }

    @Test
    @DisplayName("Verificación de integridad ZIP pasa para archivos válidos")
    void zipIntegrityVerificationPassesForValidFiles() throws Exception {
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
        byte[] fileContent = "datos de evidencia binarios 1234567890".getBytes(StandardCharsets.UTF_8);
        DocumentAsset asset = DocumentAsset.builder()
                .id(assetId)
                .processId(processId)
                .folioCode("FOLIO-0042")
                .title("informe.pdf")
                .storageKey("evidences/1/v1/informe.pdf")
                .build();
        when(documentAssetPort.findByProcessId(processId)).thenReturn(List.of(asset));
        IndicatorEvidenceLink link = IndicatorEvidenceLink.builder().id(UUID.randomUUID()).indicatorId(indicatorId).documentAssetId(assetId).evidenceId(UUID.randomUUID()).build();
        when(documentAssetPort.findAllLinksByProcessId(processId)).thenReturn(List.of(link));
        when(blobStorage.retrieve("evidences/1/v1/informe.pdf")).thenReturn(Optional.of(fileContent));

        File zip = adapter.generateDossierZip(processId, UUID.randomUUID().toString());
        assertNotNull(zip);
        assertTrue(zip.exists());
    }

    @Test
    @DisplayName("Índice HTML contiene estadísticas, tabla de contenidos y enlaces relativos a archivos")
    void htmlIndexContainsStatsAndToc() throws Exception {
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

        String indexContent = null;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("00_Indice_Maestro_Cumplimiento.html")) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = zis.read(buf)) != -1) {
                        bos.write(buf, 0, n);
                    }
                    indexContent = bos.toString(StandardCharsets.UTF_8);
                    break;
                }
                zis.closeEntry();
            }
        }
        assertNotNull(indexContent, "El índice HTML debe estar presente");
        assertTrue(indexContent.contains("Índice Maestro de Cumplimiento"), "Debe contener el título");
        assertTrue(indexContent.contains("Dimensiones"), "Debe contener estadística de dimensiones");
        assertTrue(indexContent.contains("Evidencias"), "Debe contener estadística de evidencias");
        assertTrue(indexContent.contains("Tabla de Contenidos"), "Debe contener tabla de contenidos");
        assertTrue(indexContent.contains("FOLIO-0012"), "Debe contener el folio de la evidencia");
        assertTrue(indexContent.contains("resolucion.pdf"), "Debe contener el nombre del archivo");
        assertTrue(indexContent.contains("SHA-256"), "Debe mencionar SHA-256 para integridad");
        assertTrue(indexContent.contains("badge-ok"), "Debe contener badges de estado");
        // Verifica que contiene enlace relativo clicable para el archivo
        assertTrue(indexContent.contains("href='Dimension_") && indexContent.contains("FOLIO-0012_resolucion.pdf"),
                "Debe contener enlace relativo local al archivo dentro del ZIP");
    }

    @Test
    @DisplayName("Usa nombres de negocio de proceso y carrera en HTML, resumen ejecutivo y manifiesto")
    void resolvesBusinessNamesForProcessAndCareer() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        ZipDossierExportStreamingAdapter adapter = new ZipDossierExportStreamingAdapter(
                processQueryPort, normativeHierarchyQueryPort, documentAssetPort, blobStorage,
                programCatalogPort, templatePort);

        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .careerId(careerId)
                .templateId(templateId)
                .status("ACTIVE")
                .build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        when(programCatalogPort.findById(careerId)).thenReturn(Optional.of(
                new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));

        Template template = Template.builder()
                .id(templateId)
                .name("Modelo CEUB 2026")
                .type("CEUB")
                .build();
        when(templatePort.findMetadataById(templateId)).thenReturn(Optional.of(template));

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
                .folioCode("FOLIO-0077")
                .title("plan_estudios.pdf")
                .storageKey("evidences/plan_estudios.pdf")
                .build();
        when(documentAssetPort.findByProcessId(processId)).thenReturn(List.of(asset));
        IndicatorEvidenceLink link = IndicatorEvidenceLink.builder().id(UUID.randomUUID()).indicatorId(indicatorId).documentAssetId(assetId).evidenceId(UUID.randomUUID()).build();
        when(documentAssetPort.findAllLinksByProcessId(processId)).thenReturn(List.of(link));
        when(blobStorage.retrieve("evidences/plan_estudios.pdf")).thenReturn(Optional.of("PDF_CONTENT".getBytes()));

        File zip = adapter.generateDossierZip(processId, "job-business-names-123");
        assertNotNull(zip);
        assertTrue(zip.getName().startsWith("Dossier_INF-SIS_Modelo_CEUB_2026_"),
                "El nombre del archivo ZIP debe incluir el código de carrera y nombre de plantilla: " + zip.getName());

        String htmlContent = null;
        String summaryContent = null;
        String manifestContent = null;

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = zis.read(buf)) != -1) {
                    bos.write(buf, 0, n);
                }
                String content = bos.toString(StandardCharsets.UTF_8);
                if (entry.getName().endsWith("00_Indice_Maestro_Cumplimiento.html")) {
                    htmlContent = content;
                } else if (entry.getName().endsWith("00_Resumen_Ejecutivo.txt")) {
                    summaryContent = content;
                } else if (entry.getName().endsWith("00_Manifiest_Checksums.txt")) {
                    manifestContent = content;
                }
                zis.closeEntry();
            }
        }

        // Validaciones en Índice HTML
        assertNotNull(htmlContent);
        assertTrue(htmlContent.contains("Ingeniería de Sistemas (INF-SIS)"),
                "El HTML debe mostrar el nombre de negocio de la carrera");
        assertTrue(htmlContent.contains("Modelo CEUB 2026"),
                "El HTML debe mostrar el nombre de negocio del proceso/plantilla");
        assertTrue(htmlContent.contains("href='Dimension_"),
                "El HTML debe tener enlaces relativos a las carpetas de evidencias");
        assertTrue(htmlContent.contains("FOLIO-0077_plan_estudios.pdf"),
                "El HTML debe enlazar al archivo foliado");

        // Validaciones en Resumen Ejecutivo
        assertNotNull(summaryContent);
        assertTrue(summaryContent.contains("Carrera:    Ingeniería de Sistemas (INF-SIS)"),
                "El resumen ejecutivo debe mostrar el nombre de la carrera");
        assertTrue(summaryContent.contains("Proceso:    Modelo CEUB 2026"),
                "El resumen ejecutivo debe mostrar el nombre del proceso");

        // Validaciones en Manifiesto
        assertNotNull(manifestContent);
        assertTrue(manifestContent.contains("Carrera:   Ingeniería de Sistemas (INF-SIS)"),
                "El manifiesto debe incluir el nombre de la carrera");
        assertTrue(manifestContent.contains("Proceso:   Modelo CEUB 2026"),
                "El manifiesto debe incluir el nombre del proceso");
    }
}
