package com.umss.sigesa.adapter.out.dossier;

import com.umss.sigesa.application.port.out.DossierExportPort;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.service.process.ProcessEnrichmentHelper;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.IndicatorEvidenceLink;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Genera el ZIP jerárquico del Dossier Digital con:
 * <ul>
 *   <li>Índice maestro HTML5 con CSS embebido, nombres de negocio de proceso y carrera, y enlaces relativos locales a las evidencias.</li>
 *   <li>Verificación de integridad: SHA-256 de cada archivo y re-lectura del ZIP al finalizar.</li>
 *   <li>Archivo de manifiesto con checksums para validación offline.</li>
 * </ul>
 */
@Component
public class ZipDossierExportStreamingAdapter implements DossierExportPort {

    private static final Logger log = LoggerFactory.getLogger(ZipDossierExportStreamingAdapter.class);

    private final ProcessQueryPort processQueryPort;
    private final NormativeHierarchyQueryPort normativeHierarchyQueryPort;
    private final DocumentAssetPort documentAssetPort;
    private final EvidenceBlobStoragePort blobStorage;
    private final ProgramCatalogPort programCatalogPort;
    private final TemplatePort templatePort;

    @Autowired
    public ZipDossierExportStreamingAdapter(ProcessQueryPort processQueryPort,
                                            NormativeHierarchyQueryPort normativeHierarchyQueryPort,
                                            DocumentAssetPort documentAssetPort,
                                            EvidenceBlobStoragePort blobStorage,
                                            ProgramCatalogPort programCatalogPort,
                                            TemplatePort templatePort) {
        this.processQueryPort = processQueryPort;
        this.normativeHierarchyQueryPort = normativeHierarchyQueryPort;
        this.documentAssetPort = documentAssetPort;
        this.blobStorage = blobStorage;
        this.programCatalogPort = programCatalogPort;
        this.templatePort = templatePort;
    }

    // Constructor de compatibilidad para pruebas unitarias sin dependencias de catálogo
    public ZipDossierExportStreamingAdapter(ProcessQueryPort processQueryPort,
                                            NormativeHierarchyQueryPort normativeHierarchyQueryPort,
                                            DocumentAssetPort documentAssetPort,
                                            EvidenceBlobStoragePort blobStorage) {
        this(processQueryPort, normativeHierarchyQueryPort, documentAssetPort, blobStorage, null, null);
    }

    @Override
    public File generateDossierZip(UUID processId, String jobId) {
        try {
            AccreditationProcess process = processQueryPort.findDetailById(processId)
                    .orElseThrow(() -> new IllegalStateException("Proceso no encontrado: " + processId));

            // 1. Resolver nombres de negocio para Carrera
            UUID careerId = process.getCareerId();
            String careerName = "N/D";
            String careerCode = "";
            if (careerId != null) {
                if (programCatalogPort != null) {
                    Optional<ProgramCatalogPort.ProgramEntry> programOpt = programCatalogPort.findById(careerId);
                    if (programOpt.isPresent()) {
                        ProgramCatalogPort.ProgramEntry prog = programOpt.get();
                        if (prog.name() != null && !prog.name().isBlank()) {
                            careerName = prog.name();
                        } else {
                            careerName = careerId.toString();
                        }
                        if (prog.code() != null && !prog.code().isBlank()) {
                            careerCode = prog.code();
                        }
                    } else {
                        careerName = careerId.toString();
                    }
                } else {
                    careerName = careerId.toString();
                }
            }
            String careerDisplayName = careerCode.isBlank()
                    ? careerName
                    : (careerName.contains(careerCode) ? careerName : careerName + " (" + careerCode + ")");

            // 2. Resolver nombres de negocio para Proceso / Plantilla
            String templateName = "";
            String templateType = "";
            if (process.getTemplateId() != null && templatePort != null) {
                Optional<Template> templateOpt = templatePort.findMetadataById(process.getTemplateId());
                if (templateOpt.isPresent()) {
                    Template tmpl = templateOpt.get();
                    if (tmpl.getName() != null && !tmpl.getName().isBlank()) {
                        templateName = tmpl.getName();
                    }
                    if (tmpl.getType() != null && !tmpl.getType().isBlank()) {
                        templateType = tmpl.getType();
                    }
                }
            }

            String processCode = processId.toString().substring(0, 8);
            String processBusinessName;
            if (!templateName.isBlank()) {
                processBusinessName = templateName;
            } else if (!templateType.isBlank()) {
                processBusinessName = "Acreditación " + templateType;
            } else {
                processBusinessName = "Proceso " + processCode;
            }

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String careerSlug = sanitize(careerCode.isBlank() ? careerName : careerCode);
            String processSlug = sanitize(!templateName.isBlank() ? templateName : (!templateType.isBlank() ? templateType : processCode));
            String zipName = "Dossier_" + careerSlug + "_" + processSlug + "_" + dateStr + ".zip";

            File tempDir = Files.createTempDirectory("dossier-export-").toFile();
            File tempFile = new File(tempDir, zipName);
            tempFile.deleteOnExit();
            tempDir.deleteOnExit();

            List<Level1Node> level1Nodes = normativeHierarchyQueryPort.findProcessTree(processId)
                    .map(tree -> new ArrayList<>(tree.level1Nodes()))
                    .orElseGet(ArrayList::new);
            ProcessEnrichmentHelper.sortNormativeTree(level1Nodes);

            List<DocumentAsset> assets = documentAssetPort.findByProcessId(processId);
            List<IndicatorEvidenceLink> allLinks = documentAssetPort.findAllLinksByProcessId(processId);

            Map<UUID, List<DocumentAsset>> docsByIndicator = new HashMap<>();
            for (IndicatorEvidenceLink link : allLinks) {
                DocumentAsset asset = assets.stream()
                        .filter(a -> a.getId().equals(link.getDocumentAssetId()))
                        .findFirst().orElse(null);
                if (asset == null) continue;
                docsByIndicator.computeIfAbsent(link.getIndicatorId(), k -> new ArrayList<>()).add(asset);
            }

            // Pre-recuperar datos binarios de almacenamiento (exactamente 1 lectura por DocumentAsset)
            Map<UUID, byte[]> blobByAssetId = new HashMap<>();
            Map<UUID, Boolean> blobAvailableByAssetId = new HashMap<>();
            for (DocumentAsset asset : assets) {
                if (asset.getStorageKey() != null) {
                    Optional<byte[]> dataOpt = blobStorage.retrieve(asset.getStorageKey());
                    if (dataOpt.isPresent()) {
                        blobByAssetId.put(asset.getId(), dataOpt.get());
                        blobAvailableByAssetId.put(asset.getId(), true);
                    } else {
                        blobAvailableByAssetId.put(asset.getId(), false);
                    }
                } else {
                    blobAvailableByAssetId.put(asset.getId(), false);
                }
            }

            // Mapear rutas de entrada relativas para cada vínculo (indicatorId_docId)
            Map<String, String> entryPathsByLink = new HashMap<>();
            int dimCount = 0, compCount = 0, critCount = 0, indCount = 0, evCount = 0, missingCount = 0;

            for (Level1Node l1 : level1Nodes) {
                dimCount++;
                String dimName = l1.getName() == null ? "Dimension" : l1.getName();
                String dimPath = "Dimension_" + sanitize(dimName);
                if (l1.getLevel2Nodes() != null) {
                    for (Level2Node l2 : l1.getLevel2Nodes()) {
                        compCount++;
                        String areaName = l2.getName() == null ? "Area" : l2.getName();
                        String areaPath = dimPath + "/Componente_" + sanitize(areaName);
                        if (l2.getLevel3Nodes() != null) {
                            for (Level3Node l3 : l2.getLevel3Nodes()) {
                                critCount++;
                                String critName = l3.getName() == null ? "Criterio" : l3.getName();
                                String critPath = areaPath + "/Criterio_" + sanitize(critName);
                                if (l3.getIndicators() != null) {
                                    for (NormativeIndicator ind : l3.getIndicators()) {
                                        indCount++;
                                        String indName = ind.getCode() == null ? ind.getDescription() : ind.getCode();
                                        String indPath = critPath + "/Indicador_" + sanitize(indName);
                                        List<DocumentAsset> docs = docsByIndicator.getOrDefault(ind.getId(), List.of());
                                        evCount += docs.size();
                                        Set<String> seen = new HashSet<>();
                                        for (DocumentAsset doc : docs) {
                                            String fileName = buildFileName(doc);
                                            if (!seen.add(fileName)) {
                                                fileName = makeUnique(fileName);
                                            }
                                            String entryPath = indPath + "/" + fileName;
                                            entryPathsByLink.put(ind.getId() + "_" + doc.getId(), entryPath);
                                            if (!blobAvailableByAssetId.getOrDefault(doc.getId(), false)) {
                                                missingCount++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Manifiest de checksums SHA-256 (declarado fuera del try para poder usarlo en verifyZipIntegrity)
            Map<String, String> checksums = new LinkedHashMap<>();

            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // 1. Índice maestro HTML mejorado con nombres de negocio y enlaces relativos navegables
                String indexHtml = buildIndexHtml(process, processId, careerDisplayName, processBusinessName,
                        level1Nodes, docsByIndicator, entryPathsByLink, blobAvailableByAssetId, blobByAssetId,
                        dimCount, compCount, critCount, indCount, evCount, missingCount);
                addZipEntryString(zos, "00_Indice_Maestro_Cumplimiento.html", indexHtml);

                // 2. Resumen ejecutivo (texto plano)
                String execSummary = buildExecutiveSummary(process, processId, careerDisplayName, processBusinessName,
                        dimCount, compCount, critCount, indCount, evCount, missingCount);
                addZipEntryString(zos, "00_Resumen_Ejecutivo.txt", execSummary);

                // 3. Archivos de evidencias con verificación de integridad
                for (Level1Node level1 : level1Nodes) {
                    if (level1.getLevel2Nodes() != null) {
                        for (Level2Node level2 : level1.getLevel2Nodes()) {
                            if (level2.getLevel3Nodes() != null) {
                                for (Level3Node level3 : level2.getLevel3Nodes()) {
                                    if (level3.getIndicators() != null) {
                                        for (NormativeIndicator indicator : level3.getIndicators()) {
                                            String indName = indicator.getCode() == null
                                                    ? indicator.getDescription()
                                                    : indicator.getCode();
                                            String indPath = "Dimension_" + sanitize(level1.getName() == null ? "Dimension" : level1.getName())
                                                    + "/Componente_" + sanitize(level2.getName() == null ? "Area" : level2.getName())
                                                    + "/Criterio_" + sanitize(level3.getName() == null ? "Criterio" : level3.getName())
                                                    + "/Indicador_" + sanitize(indName);
                                            List<DocumentAsset> docs = docsByIndicator.getOrDefault(indicator.getId(), List.of());
                                            if (docs.isEmpty()) {
                                                // Entry vacío para indicar indicador sin evidencias
                                                addZipEntryString(zos, indPath + "/_SIN_EVIDENCIAS.txt",
                                                        "Este indicador no tiene evidencias cargadas.\n");
                                            } else {
                                                for (DocumentAsset doc : docs) {
                                                    String linkKey = indicator.getId() + "_" + doc.getId();
                                                    String entryPath = entryPathsByLink.get(linkKey);
                                                    byte[] data = blobByAssetId.get(doc.getId());
                                                    if (data != null) {
                                                        if (data.length == 0) {
                                                            log.warn("[generateDossierZip] Blob vacío para job {} documento {} (key={})",
                                                                    jobId, doc.getId(), doc.getStorageKey());
                                                            addZipEntryString(zos, entryPath + "_VACIO.txt",
                                                                    "El archivo original está vacío en el almacenamiento.\n"
                                                                            + "Folio: " + (doc.getFolioCode() == null ? "N/D" : doc.getFolioCode())
                                                                            + "\nStorageKey: " + doc.getStorageKey() + "\n");
                                                        } else {
                                                            String sha256 = sha256Hex(data);
                                                            checksums.put(entryPath, sha256);
                                                            addZipEntry(zos, entryPath, data, doc);
                                                        }
                                                    } else {
                                                        addZipEntryString(zos, entryPath + "_NO_DISPONIBLE.txt",
                                                                buildWarningContent(doc));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Manifiesto de checksums SHA-256
                String manifest = buildManifest(checksums, processId, careerDisplayName, processBusinessName, jobId);
                addZipEntryString(zos, "00_Manifiest_Checksums.txt", manifest);
            }

            // 5. Verificación de integridad: re-abrir el ZIP y leer todas las entradas
            verifyZipIntegrity(tempFile, checksums);

            log.info("[generateDossierZip] ZIP generado correctamente para job {}: {} entradas, {} checksums",
                    jobId, checksums.size(), checksums.size());
            return tempFile;
        } catch (Exception e) {
            log.error("[generateDossierZip] Error generando dossier para job {}: {}", jobId, e.getMessage(), e);
            throw new IllegalStateException("Error generating dossier zip", e);
        }
    }

    /* ------------------------------------------------------------------ *
     *  Construcción del índice HTML5 mejorado                            *
     * ------------------------------------------------------------------ */

    private String buildIndexHtml(AccreditationProcess process, UUID processId,
                                  String careerDisplayName, String processBusinessName,
                                  List<Level1Node> level1Nodes,
                                  Map<UUID, List<DocumentAsset>> docsByIndicator,
                                  Map<String, String> entryPathsByLink,
                                  Map<UUID, Boolean> blobAvailableByAssetId,
                                  Map<UUID, byte[]> blobByAssetId,
                                  int dimCount, int compCount, int critCount, int indCount, int evCount, int missingCount) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='es'>\n");
        html.append("<head>\n");
        html.append("  <meta charset='UTF-8'>\n");
        html.append("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("  <title>Índice Maestro de Cumplimiento - ").append(escape(careerDisplayName)).append(" - ").append(escape(processBusinessName)).append("</title>\n");
        html.append("  <style>\n");
        html.append("    :root {\n");
        html.append("      --primary-50: #E8F1FF;\n");
        html.append("      --primary-100: #D6E3FF;\n");
        html.append("      --primary-200: #A9C7FF;\n");
        html.append("      --primary-300: #7CA2E1;\n");
        html.append("      --primary-400: #375F9A;\n");
        html.append("      --primary-500: #1B4780;\n");
        html.append("      --primary-600: #003770;\n");
        html.append("      --primary-700: #002249;\n");
        html.append("      --primary-800: #001B3D;\n");
        html.append("      --secondary-50: #FFECE8;\n");
        html.append("      --secondary-300: #FFB4AB;\n");
        html.append("      --secondary-500: #E80F16;\n");
        html.append("      --secondary-700: #BA1A1A;\n");
        html.append("      --gray-50: #F8F9FA;\n");
        html.append("      --gray-100: #F3F4F5;\n");
        html.append("      --gray-200: #EDEEEF;\n");
        html.append("      --gray-300: #E7E8E9;\n");
        html.append("      --gray-400: #E1E3E4;\n");
        html.append("      --gray-500: #C3C6D1;\n");
        html.append("      --gray-600: #9396A1;\n");
        html.append("      --gray-700: #737781;\n");
        html.append("      --gray-800: #434750;\n");
        html.append("      --gray-900: #191C1D;\n");
        html.append("      --success: #198754;\n");
        html.append("      --warning: #FFC107;\n");
        html.append("      --danger: #DC3545;\n");
        html.append("      --font-sans: \"Inter\", -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;\n");
        html.append("      --font-mono: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;\n");
        html.append("    }\n");
        html.append("    * { box-sizing: border-box; margin: 0; padding: 0; }\n");
        html.append("    body { font-family: var(--font-sans); background: #FFFFFF; color: var(--gray-900); line-height: 1.5; margin: 0; padding: 24px; }\n");
        html.append("    h1, h2, h3, h4, h5, h6 { font-weight: 600; color: var(--primary-800); }\n");
        html.append("    h1 { font-size: 1.875rem; }\n");
        html.append("    h2 { font-size: 1.5rem; margin-top: 1.5rem; }\n");
        html.append("    h3 { font-size: 1.25rem; margin-top: 1.25rem; }\n");
        html.append("    h4 { font-size: 1.125rem; margin-top: 1rem; }\n");
        html.append("    h5 { font-size: 1rem; margin-top: 0.875rem; }\n");
        html.append("    p { margin: 0.5rem 0; color: var(--gray-700); }\n");
        html.append("    a { color: var(--primary-600); text-decoration: none; font-weight: 500; }\n");
        html.append("    a:hover { text-decoration: underline; color: var(--primary-700); }\n");
        html.append("    .file-link { color: var(--primary-600); word-break: break-all; }\n");
        html.append("    .file-link:hover { text-decoration: underline; }\n");

        /* Header */
        html.append("    .header { background: linear-gradient(135deg, var(--primary-600) 0%, var(--primary-500) 100%); color: #fff; padding: 24px 32px; border-radius: 12px 12px 0 0; margin-bottom: 24px; }\n");
        html.append("    .header h1 { margin: 0 0 8px 0; font-size: 1.875rem; color: #fff; }\n");
        html.append("    .header .subtitle { opacity: 0.92; font-size: 0.95rem; font-weight: 400; }\n");

        /* Offline notice banner */
        html.append("    .notice-banner { background: var(--primary-50); border: 1px solid var(--primary-200); border-left: 4px solid var(--primary-500); border-radius: 8px; padding: 14px 18px; margin-bottom: 24px; font-size: 0.875rem; color: var(--primary-800); line-height: 1.4; }\n");

        /* Meta chips */
        html.append("    .meta { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 24px; }\n");
        html.append("    .meta .item { background: var(--primary-50); color: var(--primary-700); padding: 6px 12px; border-radius: 6px; font-size: 0.8rem; font-weight: 500; border: 1px solid var(--primary-200); }\n");

        /* Stats cards */
        html.append("    .stats { display: flex; flex-wrap: wrap; gap: 12px; margin: 24px 0; }\n");
        html.append("    .stat-card { background: var(--gray-50); border: 1px solid var(--gray-200); border-radius: 8px; padding: 14px 16px; min-width: 120px; text-align: center; }\n");
        html.append("    .stat-card .num { display: block; font-size: 1.875rem; font-weight: 700; color: var(--primary-600); }\n");
        html.append("    .stat-card .lbl { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px; color: var(--gray-600); }\n");
        html.append("    .stat-card .lbl-missing { color: var(--danger); }\n");

        /* Table of contents */
        html.append("    .toc { margin: 24px 0; background: var(--gray-50); border: 1px solid var(--gray-200); border-radius: 8px; padding: 16px 20px; }\n");
        html.append("    .toc h2 { border-bottom: 2px solid var(--primary-600); padding-bottom: 6px; margin: 0 0 12px 0; font-size: 1.25rem; }\n");
        html.append("    .toc ul { list-style: none; padding: 0; }\n");
        html.append("    .toc li { margin: 6px 0; }\n");
        html.append("    .toc a { color: var(--primary-700); }\n");

        /* Hierarchy */
        html.append("    .dimension { margin: 30px 0; page-break-inside: avoid; border: 1px solid var(--gray-200); border-radius: 10px; overflow: hidden; }\n");
        html.append("    .dimension > h2 { background: var(--primary-50); color: var(--primary-800); border-left: 4px solid var(--primary-600); padding: 12px 16px; margin: 0; font-size: 1.25rem; }\n");
        html.append("    .component { margin: 16px 0 0 20px; padding-left: 12px; border-left: 2px solid var(--gray-300); }\n");
        html.append("    .component h3 { color: var(--primary-700); margin: 8px 0; font-size: 1.125rem; }\n");
        html.append("    .criterion { margin: 12px 0 0 30px; padding-left: 12px; border-left: 2px solid var(--primary-200); }\n");
        html.append("    .criterion h4 { color: var(--primary-600); margin: 6px 0; font-size: 1rem; }\n");
        html.append("    .indicator { margin: 10px 0 0 40px; padding-left: 12px; border-left: 2px solid var(--primary-300); padding-bottom: 8px; }\n");
        html.append("    .indicator h5 { color: var(--primary-500); margin: 4px 0; font-size: 0.875rem; }\n");

        /* Evidence table */
        html.append("    table.evidence { width: 100%; border-collapse: collapse; margin: 8px 0; font-size: 0.875rem; }\n");
        html.append("    table.evidence th { background: var(--gray-100); border: 1px solid var(--gray-300); padding: 8px 10px; text-align: left; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px; color: var(--gray-700); }\n");
        html.append("    table.evidence td { border: 1px solid var(--gray-300); padding: 8px 10px; vertical-align: middle; }\n");
        html.append("    table.evidence tr:nth-child(even) td { background: var(--gray-50); }\n");

        /* Badges */
        html.append("    .badge { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 0.75rem; font-weight: 600; white-space: nowrap; }\n");
        html.append("    .badge-ok { background: #dcfce7; color: #166534; }\n");
        html.append("    .badge-missing { background: #fee2e2; color: #991b1b; }\n");
        html.append("    .no-evidences { color: var(--gray-600); font-style: italic; font-size: 0.875rem; margin: 6px 0; }\n");

        /* Missing section */
        html.append("    .missing-section { background: var(--secondary-50); border: 1px solid var(--secondary-300); border-radius: 8px; padding: 16px; margin: 24px 0; }\n");
        html.append("    .missing-section h3 { color: var(--secondary-700); margin-top: 0; }\n");
        html.append("    .missing-section ul { margin: 8px 0 0 20px; font-size: 0.875rem; }\n");
        html.append("    .missing-section li { margin: 4px 0; }\n");

        /* Manifest */
        html.append("    .manifest { background: var(--primary-50); border: 1px solid var(--primary-200); border-radius: 8px; padding: 16px; margin: 24px 0; font-size: 0.75rem; }\n");
        html.append("    .manifest h3 { color: var(--primary-800); margin-top: 0; font-size: 0.875rem; }\n");
        html.append("    .manifest pre { margin: 8px 0 0 0; white-space: pre-wrap; word-break: break-all; font-family: var(--font-mono); }\n");

        /* Footer */
        html.append("    .footer { margin-top: 40px; border-top: 1px solid var(--gray-300); padding-top: 16px; font-size: 0.75rem; color: var(--gray-600); }\n");

        /* Print */
        html.append("    @media print { body { padding: 0; } .header { background: var(--primary-600); -webkit-print-color-adjust: exact; }\n");
        html.append("    .no-print { display: none; } }\n");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Cabecera
        html.append("  <div class='header'>\n");
        html.append("    <h1>Índice Maestro de Cumplimiento</h1>\n");
        html.append("    <div class='subtitle'>Dossier Digital de Acreditación &mdash; ")
                .append(escape(careerDisplayName)).append(" &mdash; ")
                .append(escape(processBusinessName)).append("</div>\n");
        html.append("  </div>\n");

        // Aviso de revisión fuera de línea
        html.append("  <div class='notice-banner'>\n");
        html.append("    <strong>Aviso para revisión local:</strong> Los archivos de evidencias en las tablas inferiores están vinculados mediante rutas relativas locales. Para que los enlaces abran directamente los documentos en su navegador o visor predeterminado, asegúrese de haber extraído/descomprimido el archivo ZIP en una carpeta local.\n");
        html.append("  </div>\n");

        // Metadatos del proceso
        html.append("  <div class='meta'>\n");
        html.append("    <div class='item'><strong>Proceso:</strong> ").append(escape(processBusinessName)).append("</div>\n");
        html.append("    <div class='item'><strong>Carrera:</strong> ").append(escape(careerDisplayName)).append("</div>\n");
        html.append("    <div class='item'><strong>ID Proceso:</strong> ").append(escape(processId.toString())).append("</div>\n");
        html.append("    <div class='item'><strong>Estado:</strong> ")
                .append(escape(process.getStatus() != null ? process.getStatus() : "N/D")).append("</div>\n");
        html.append("    <div class='item'><strong>Modalidad:</strong> ")
                .append(escape(process.getOperationalMode() != null ? process.getOperationalMode().name() : "N/D")).append("</div>\n");
        html.append("    <div class='item'><strong>Generado:</strong> ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("</div>\n");
        html.append("  </div>\n");

        // Estadísticas
        html.append("  <div class='stats'>\n");
        html.append("    <div class='stat-card'><span class='num'>").append(dimCount).append("</span><span class='lbl'>Dimensiones</span></div>\n");
        html.append("    <div class='stat-card'><span class='num'>").append(compCount).append("</span><span class='lbl'>Componentes</span></div>\n");
        html.append("    <div class='stat-card'><span class='num'>").append(critCount).append("</span><span class='lbl'>Criterios</span></div>\n");
        html.append("    <div class='stat-card'><span class='num'>").append(indCount).append("</span><span class='lbl'>Indicadores</span></div>\n");
        html.append("    <div class='stat-card'><span class='num'>").append(evCount).append("</span><span class='lbl'>Evidencias</span></div>\n");
        html.append("    <div class='stat-card'><span class='num'>").append(missingCount).append("</span><span class='lbl lbl-missing'>Faltantes</span></div>\n");
        html.append("  </div>\n");

        // Tabla de contenidos
        if (!level1Nodes.isEmpty()) {
            html.append("  <div class='toc'>\n");
            html.append("    <h2>Tabla de Contenidos</h2>\n");
            html.append("    <ul>\n");
            for (Level1Node l1 : level1Nodes) {
                String dimName = l1.getName() == null ? "Dimension" : l1.getName();
                String anchor = "dim-" + sanitize(dimName);
                html.append("      <li><a href='#").append(anchor).append("'>").append(escape(dimName)).append("</a></li>\n");
            }
            if (missingCount > 0) {
                html.append("      <li><a href='#missing'>Evidencias no disponibles (").append(missingCount).append(")</a></li>\n");
            }
            html.append("      <li><a href='#manifest'>Manifiest de checksums (SHA-256)</a></li>\n");
            html.append("    </ul>\n");
            html.append("  </div>\n");
        }

        // Detalle jerárquico
        for (Level1Node level1 : level1Nodes) {
            String dimName = level1.getName() == null ? "Dimension" : level1.getName();
            String anchor = "dim-" + sanitize(dimName);
            html.append("  <div class='dimension' id='").append(anchor).append("'>\n");
            html.append("    <h2>").append(escape(dimName)).append("</h2>\n");
            if (level1.getLevel2Nodes() != null) {
                for (Level2Node level2 : level1.getLevel2Nodes()) {
                    String areaName = level2.getName() == null ? "Area" : level2.getName();
                    html.append("    <div class='component'>\n");
                    html.append("      <h3>").append(escape(areaName)).append("</h3>\n");
                    if (level2.getLevel3Nodes() != null) {
                        for (Level3Node level3 : level2.getLevel3Nodes()) {
                            String critName = level3.getName() == null ? "Criterio" : level3.getName();
                            html.append("      <div class='criterion'>\n");
                            html.append("        <h4>").append(escape(critName)).append("</h4>\n");
                            if (level3.getIndicators() != null) {
                                for (NormativeIndicator indicator : level3.getIndicators()) {
                                    String indName = indicator.getCode() == null
                                            ? indicator.getDescription()
                                            : indicator.getCode();
                                    String anchorInd = "ind-" + escape(indicator.getCode() != null ? indicator.getCode() : indicator.getId().toString());
                                    html.append("        <div class='indicator' id='").append(anchorInd).append("'>\n");
                                    html.append("          <h5>").append(escape(indName)).append("</h5>\n");
                                    List<DocumentAsset> docs = docsByIndicator.getOrDefault(indicator.getId(), List.of());
                                    if (docs.isEmpty()) {
                                        html.append("          <p class='no-evidences'>&mdash; Sin evidencias cargadas.</p>\n");
                                    } else {
                                        html.append("          <table class='evidence'>\n");
                                        html.append("            <thead><tr><th style='width: 20%;'>Folio</th><th style='width: 50%;'>Archivo</th><th style='width: 15%;'>Tamaño</th><th style='width: 15%;'>Estado</th></tr></thead>\n");
                                        html.append("            <tbody>\n");
                                        for (DocumentAsset doc : docs) {
                                            String linkKey = indicator.getId() + "_" + doc.getId();
                                            String entryPath = entryPathsByLink.get(linkKey);
                                            boolean available = blobAvailableByAssetId.getOrDefault(doc.getId(), false);
                                            byte[] data = blobByAssetId.get(doc.getId());
                                            String folio = doc.getFolioCode() == null ? "N/D" : doc.getFolioCode();
                                            String title = doc.getTitle() == null ? "documento" : doc.getTitle();
                                            long size = (data != null) ? (long) data.length : 0L;

                                            html.append("              <tr>\n");
                                            html.append("                <td><strong>").append(escape(folio)).append("</strong></td>\n");

                                            if (available && data != null && data.length > 0) {
                                                String href = entryPath != null ? entryPath : "";
                                                html.append("                <td><a class='file-link' href='").append(escape(href))
                                                        .append("' target='_blank'>").append(escape(title)).append("</a></td>\n");
                                                html.append("                <td>").append(formatSize(size)).append("</td>\n");
                                                html.append("                <td><span class='badge badge-ok'>Disponible</span></td>\n");
                                            } else if (available && (data == null || data.length == 0)) {
                                                String href = (entryPath != null ? entryPath : "") + "_VACIO.txt";
                                                html.append("                <td><a class='file-link' href='").append(escape(href))
                                                        .append("' target='_blank' style='color: var(--danger);'>")
                                                        .append(escape(title)).append(" (Vacío)</a></td>\n");
                                                html.append("                <td>0 B</td>\n");
                                                html.append("                <td><span class='badge badge-missing'>Vacío</span></td>\n");
                                            } else {
                                                String href = (entryPath != null ? entryPath : "") + "_NO_DISPONIBLE.txt";
                                                html.append("                <td><a class='file-link' href='").append(escape(href))
                                                        .append("' target='_blank' style='color: var(--danger);'>")
                                                        .append(escape(title)).append(" (No disponible)</a></td>\n");
                                                html.append("                <td>0 B</td>\n");
                                                html.append("                <td><span class='badge badge-missing'>No disponible</span></td>\n");
                                            }
                                            html.append("              </tr>\n");
                                        }
                                        html.append("            </tbody>\n");
                                        html.append("          </table>\n");
                                    }
                                    html.append("        </div>\n");
                                }
                            }
                            html.append("      </div>\n");
                        }
                    }
                    html.append("    </div>\n");
                }
            }
            html.append("  </div>\n");
        }

        // Sección de faltantes
        if (missingCount > 0) {
            html.append("  <div class='missing-section' id='missing'>\n");
            html.append("    <h3>Evidencias No Disponibles (").append(missingCount).append(")</h3>\n");
            html.append("    <p>Los siguientes archivos no se pudieron recuperar del almacenamiento y se incluyeron como advertencia de texto dentro del ZIP:</p>\n");
            html.append("    <ul>\n");
            for (Level1Node level1 : level1Nodes) {
                if (level1.getLevel2Nodes() != null) {
                    for (Level2Node level2 : level1.getLevel2Nodes()) {
                        if (level2.getLevel3Nodes() != null) {
                            for (Level3Node level3 : level2.getLevel3Nodes()) {
                                if (level3.getIndicators() != null) {
                                    for (NormativeIndicator indicator : level3.getIndicators()) {
                                        List<DocumentAsset> docs = docsByIndicator.getOrDefault(indicator.getId(), List.of());
                                        for (DocumentAsset doc : docs) {
                                            if (!blobAvailableByAssetId.getOrDefault(doc.getId(), false)) {
                                                String linkKey = indicator.getId() + "_" + doc.getId();
                                                String entryPath = entryPathsByLink.get(linkKey);
                                                String href = (entryPath != null ? entryPath : "") + "_NO_DISPONIBLE.txt";
                                                html.append("      <li><strong>Folio: ").append(escape(doc.getFolioCode() == null ? "N/D" : doc.getFolioCode()))
                                                        .append("</strong> &mdash; <a href='").append(escape(href)).append("' target='_blank'>")
                                                        .append(escape(doc.getTitle() == null ? "documento" : doc.getTitle())).append("</a>")
                                                        .append(" (Indicador: ").append(escape(indicator.getCode() == null ? indicator.getDescription() : indicator.getCode()))
                                                        .append(" &mdash; Clave: ").append(escape(doc.getStorageKey())).append(")</li>\n");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            html.append("    </ul>\n");
            html.append("  </div>\n");
        }

        // Manifiest de checksums
        html.append("  <div class='manifest' id='manifest'>\n");
        html.append("    <h3>Manifiest de Integridad (SHA-256)</h3>\n");
        html.append("    <p>Cada archivo incluido en el ZIP tiene un checksum SHA-256 registrado en el archivo `00_Manifiest_Checksums.txt` para validar que no hubo corrupción durante la generación o descarga.</p>\n");
        html.append("  </div>\n");

        // Pie de página
        html.append("  <div class='footer'>\n");
        html.append("    Generado por SIGESA Dossier Export Service &mdash; Universidad Mayor de San Simón\n");
        html.append("  </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }

    private String buildExecutiveSummary(AccreditationProcess process, UUID processId,
                                         String careerDisplayName, String processBusinessName,
                                         int dimCount, int compCount, int critCount, int indCount, int evCount, int missingCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("RESUMEN EJECUTIVO - DOSSIER DIGITAL\n");
        sb.append("====================================\n\n");
        sb.append("Proceso:    ").append(processBusinessName).append("\n");
        sb.append("ID Proceso: ").append(processId).append("\n");
        sb.append("Carrera:    ").append(careerDisplayName).append("\n");
        sb.append("Estado:     ").append(process.getStatus() != null ? process.getStatus() : "N/D").append("\n");
        sb.append("Modalidad:  ").append(process.getOperationalMode() != null ? process.getOperationalMode() : "N/D").append("\n");
        sb.append("Generado:   ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");
        sb.append("ESTADÍSTICAS\n");
        sb.append("-----------\n");
        sb.append("Dimensiones: ").append(dimCount).append("\n");
        sb.append("Componentes: ").append(compCount).append("\n");
        sb.append("Criterios:   ").append(critCount).append("\n");
        sb.append("Indicadores: ").append(indCount).append("\n");
        sb.append("Evidencias:  ").append(evCount).append("\n");
        sb.append("Faltantes:   ").append(missingCount).append("\n\n");
        sb.append("ESTRUCTURA DEL ZIP\n");
        sb.append("-----------------\n");
        sb.append("00_Indice_Maestro_Cumplimiento.html  - Índice navegable con enlaces relativos a las evidencias locales\n");
        sb.append("00_Resumen_Ejecutivo.txt             - Este archivo\n");
        sb.append("00_Manifiest_Checksums.txt           - Checksums SHA-256 para validación de integridad\n");
        sb.append("Dimension_*/Componente_*/Criterio_*/Indicador_*/  - Jerarquía normativa con archivos foliados\n\n");
        sb.append("NOTA DE USO:\n");
        sb.append("Para que los enlaces del índice HTML abran correctamente las evidencias locales,\n");
        sb.append("descomprima el archivo ZIP por completo en su disco duro antes de abrirlo.\n");
        return sb.toString();
    }

    private String buildManifest(Map<String, String> checksums, UUID processId,
                                 String careerDisplayName, String processBusinessName, String jobId) {
        StringBuilder sb = new StringBuilder();
        sb.append("MANIFIEST DE INTEGRIDAD - SHA-256\n");
        sb.append("==================================\n\n");
        sb.append("Proceso:   ").append(processBusinessName).append(" (ID: ").append(processId).append(")\n");
        sb.append("Carrera:   ").append(careerDisplayName).append("\n");
        sb.append("Job:       ").append(jobId).append("\n");
        sb.append("Generado:  ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        sb.append("Total archivos con checksum: ").append(checksums.size()).append("\n\n");
        sb.append("ARCHIVO\tSHA-256\n");
        sb.append("-------\t-------\n");
        checksums.forEach((path, hash) -> sb.append(path).append("\t").append(hash).append("\n"));
        sb.append("\n---\n");
        sb.append("Para validar: sha256sum -c 00_Manifiest_Checksums.txt (descomprimir primero)\n");
        return sb.toString();
    }

    private String buildFileName(DocumentAsset doc) {
        String originalName = doc.getTitle() == null ? "documento" : doc.getTitle();
        String ext = getExtension(originalName);
        String base = doc.getFolioCode() == null ? UUID.randomUUID().toString().substring(0, 8) : doc.getFolioCode();
        String fileName = base + "_" + sanitize(originalName);
        if (fileName.toLowerCase().endsWith(ext.toLowerCase()) && ext.length() > 0) {
            // keep as is
        } else if (!ext.isEmpty()) {
            if (!fileName.toLowerCase().endsWith(ext.toLowerCase())) {
                fileName = fileName.replaceAll("\\.[^.]+$", "") + ext;
            }
        }
        return fileName;
    }

    private String makeUnique(String fileName) {
        String ext = getExtension(fileName);
        String base = ext.isEmpty() ? fileName : fileName.substring(0, fileName.length() - ext.length());
        return base + "_" + UUID.randomUUID().toString().substring(0, 4) + ext;
    }

    private String buildWarningContent(DocumentAsset doc) {
        return "DOCUMENTO NO DISPONIBLE\n"
                + "======================\n"
                + "Folio: " + (doc.getFolioCode() == null ? "N/D" : doc.getFolioCode()) + "\n"
                + "Archivo: " + (doc.getTitle() == null ? "documento" : doc.getTitle()) + "\n"
                + "StorageKey: " + doc.getStorageKey() + "\n"
                + "El archivo original no se pudo recuperar del almacenamiento.\n";
    }

    /* ------------------------------------------------------------------ *
     *  Escritura de entradas ZIP con verificación                        *
     * ------------------------------------------------------------------ */

    private void addZipEntry(ZipOutputStream zos, String path, byte[] data, DocumentAsset doc) throws IOException {
        ZipEntry entry = new ZipEntry(normalizePath(path));
        entry.setSize(data.length);
        entry.setCompressedSize(-1); // deja que ZIP calcule
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
        log.debug("[addZipEntry] Entrada escrita: {} ({} bytes)", path, data.length);
    }

    private void addZipEntryString(ZipOutputStream zos, String path, String content) throws IOException {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        ZipEntry entry = new ZipEntry(normalizePath(path));
        entry.setSize(data.length);
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }

    /* ------------------------------------------------------------------ *
     *  Verificación de integridad del ZIP generado                       *
     * ------------------------------------------------------------------ */

    private void verifyZipIntegrity(File zipFile, Map<String, String> expectedChecksums) throws IOException {
        int totalEntries = 0;
        int readableEntries = 0;
        List<String> corruptEntries = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                totalEntries++;
                String entryName = entry.getName();
                long expectedSize = entry.getSize();
                long readBytes = 0;

                // Leer toda la entrada para verificar que no esté corrupta
                byte[] buffer = new byte[8192];
                int n;
                while ((n = zis.read(buffer)) != -1) {
                    readBytes += n;
                }

                // Si el entry tiene tamaño declarado, verificar que coincida
                if (expectedSize >= 0 && readBytes != expectedSize) {
                    corruptEntries.add(entryName + " (esperado " + expectedSize + " bytes, leídos " + readBytes + ")");
                } else {
                    readableEntries++;
                }

                zis.closeEntry();
            }
        }

        log.info("[verifyZipIntegrity] Entradas totales: {}, legibles: {}, corruptas: {}",
                totalEntries, readableEntries, corruptEntries.size());

        if (!corruptEntries.isEmpty()) {
            StringBuilder sb = new StringBuilder("Entradas ZIP corruptas detectadas:\n");
            for (String c : corruptEntries) {
                sb.append("  - ").append(c).append("\n");
            }
            throw new IllegalStateException(sb.toString());
        }
    }

    /* ------------------------------------------------------------------ *
     *  Utilidades                                                        *
     * ------------------------------------------------------------------ */

    private String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private String sanitize(String name) {
        if (name == null) return "SinNombre";
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String cleaned = withoutAccents.replaceAll("[^A-Za-z0-9._-]", "_");
        cleaned = cleaned.replaceAll("[/\\\\:*?\"<>|]", "_");
        if (cleaned.isBlank()) return "SinNombre";
        return cleaned;
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        if (idx == -1 || idx == name.length() - 1) return "";
        return name.substring(idx);
    }

    private String normalizePath(String path) {
        return path.replace("\\", "/");
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String[] units = {"B", "KB", "MB", "GB"};
        double size = bytes / Math.pow(1024, exp);
        return String.format("%.1f %s", size, units[exp]);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
