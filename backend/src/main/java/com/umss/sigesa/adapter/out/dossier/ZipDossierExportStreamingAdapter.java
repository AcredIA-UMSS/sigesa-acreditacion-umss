package com.umss.sigesa.adapter.out.dossier;

import com.umss.sigesa.application.port.out.DossierExportPort;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Genera el ZIP jerárquico del Dossier Digital en streaming directo hacia el storage
 * configurado (sin crear archivos temporales en disco efímero ni retener el ZIP completo en memoria heap).
 * <ul>
 *   <li>Streaming directo sobre EvidenceBlobStoragePort.createOutputStream</li>
 *   <li>Descarga paralela acotada de blobs de evidencia (8 hilos) con backpressure</li>
 *   <li>Índice maestro HTML5 responsivo y resumen ejecutivo</li>
 *   <li>Manifiesto de checksums SHA-256 para auditoría offline</li>
 * </ul>
 */
@Component
public class ZipDossierExportStreamingAdapter implements DossierExportPort {

    private static final Logger log = LoggerFactory.getLogger(ZipDossierExportStreamingAdapter.class);

    private final ProcessQueryPort processQueryPort;
    private final NormativeHierarchyQueryPort normativeHierarchyQueryPort;
    private final DocumentAssetPort documentAssetPort;
    private final EvidenceBlobStoragePort blobStorage;

    public ZipDossierExportStreamingAdapter(ProcessQueryPort processQueryPort,
                                            NormativeHierarchyQueryPort normativeHierarchyQueryPort,
                                            DocumentAssetPort documentAssetPort,
                                            EvidenceBlobStoragePort blobStorage) {
        this.processQueryPort = processQueryPort;
        this.normativeHierarchyQueryPort = normativeHierarchyQueryPort;
        this.documentAssetPort = documentAssetPort;
        this.blobStorage = blobStorage;
    }

    private record EvidenceTask(String entryPath, DocumentAsset doc) {}
    private record EmptyIndicatorEntry(String entryPath) {}

    private record DownloadResult(
            String entryPath,
            DocumentAsset doc,
            byte[] data,
            boolean isError
    ) {
        private static final DownloadResult POISON_PILL = new DownloadResult(null, null, null, false);
    }

    @Override
    public String generateDossierZip(UUID processId, String jobId) {
        return generateDossierZip(processId, jobId, p -> {});
    }

    @Override
    public String generateDossierZip(UUID processId, String jobId, Consumer<Integer> progressListener) {
        String storageKey = null;
        try {
            AccreditationProcess process = processQueryPort.findDetailById(processId)
                    .orElseThrow(() -> new IllegalStateException("Proceso no encontrado: " + processId));
            String careerIdStr = process.getCareerId() != null ? process.getCareerId().toString() : "N/D";
            String processCode = processId.toString().substring(0, 8);
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String zipName = sanitize(careerIdStr) + "_" + sanitize(processCode) + "_" + dateStr + ".zip";
            storageKey = "dossier-exports/" + jobId + "/" + zipName;

            Optional<NormativeHierarchyQueryPort.ProcessNormativeTree> treeOpt =
                    normativeHierarchyQueryPort.findProcessTree(processId);
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

            int dimCount = 0, compCount = 0, critCount = 0, indCount = 0;
            List<EvidenceTask> tasks = new ArrayList<>();
            List<EmptyIndicatorEntry> emptyEntries = new ArrayList<>();

            if (treeOpt.isPresent()) {
                for (Level1Node level1 : treeOpt.get().level1Nodes()) {
                    dimCount++;
                    String dimName = level1.getName() == null ? "Dimension" : level1.getName();
                    String dimPath = "Dimension_" + sanitize(dimName);
                    for (Level2Node level2 : level1.getLevel2Nodes()) {
                        compCount++;
                        String areaName = level2.getName() == null ? "Area" : level2.getName();
                        String areaPath = dimPath + "/Componente_" + sanitize(areaName);
                        for (Level3Node level3 : level2.getLevel3Nodes()) {
                            critCount++;
                            String critName = level3.getName() == null ? "Criterio" : level3.getName();
                            String critPath = areaPath + "/Criterio_" + sanitize(critName);
                            for (NormativeIndicator indicator : level3.getIndicators()) {
                                indCount++;
                                String indName = indicator.getCode() == null
                                        ? indicator.getDescription()
                                         : indicator.getCode();
                                String indPath = critPath + "/Indicador_" + sanitize(indName);
                                List<DocumentAsset> docs = docsByIndicator.getOrDefault(indicator.getId(), List.of());
                                if (docs.isEmpty()) {
                                    emptyEntries.add(new EmptyIndicatorEntry(indPath + "/_SIN_EVIDENCIAS.txt"));
                                } else {
                                    Set<String> seen = new HashSet<>();
                                    for (DocumentAsset doc : docs) {
                                        String fileName = buildFileName(doc);
                                        if (!seen.add(fileName)) {
                                            fileName = makeUnique(fileName);
                                        }
                                        String entryPath = indPath + "/" + fileName;
                                        tasks.add(new EvidenceTask(entryPath, doc));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            int evCount = tasks.size();
            int missingCount = 0;
            Map<String, String> checksums = new LinkedHashMap<>();
            Set<String> missingStorageKeys = new HashSet<>();
            Map<String, Long> docSizes = new HashMap<>();
            Set<String> addedZipPaths = new HashSet<>();

            // Streaming directo hacia el storage configurado
            try (OutputStream rawOut = blobStorage.createOutputStream(storageKey, "application/zip");
                 BufferedOutputStream bos = new BufferedOutputStream(rawOut);
                 ZipOutputStream zos = new ZipOutputStream(bos)) {

                // 1. Entradas para indicadores sin evidencias
                for (EmptyIndicatorEntry empty : emptyEntries) {
                    addZipEntryString(zos, empty.entryPath(), "Este indicador no tiene evidencias cargadas.\n", addedZipPaths);
                }

                // 2. Descarga paralela acotada de evidencias (8 hilos) con backpressure
                if (!tasks.isEmpty()) {
                    int parallelism = Math.min(8, Math.max(1, tasks.size()));
                    ExecutorService fetchPool = Executors.newFixedThreadPool(parallelism);
                    BlockingQueue<DownloadResult> queue = new ArrayBlockingQueue<>(16);
                    AtomicBoolean cancelled = new AtomicBoolean(false);

                    Thread producer = Thread.ofVirtual().name("dossier-fetch-" + jobId).start(() -> {
                        try {
                            List<CompletableFuture<Void>> futures = new ArrayList<>();
                            for (EvidenceTask task : tasks) {
                                if (cancelled.get() || Thread.currentThread().isInterrupted()) break;
                                CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                                    if (cancelled.get() || Thread.currentThread().isInterrupted()) return;
                                    try {
                                        Optional<byte[]> dataOpt = blobStorage.retrieve(task.doc().getStorageKey());
                                        queue.put(new DownloadResult(task.entryPath(), task.doc(), dataOpt.orElse(null), false));
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    } catch (Exception ex) {
                                        log.warn("[generateDossierZip] Error recuperando evidencia {}: {}",
                                                task.doc().getStorageKey(), ex.getMessage());
                                        try {
                                            queue.put(new DownloadResult(task.entryPath(), task.doc(), null, true));
                                        } catch (InterruptedException ie) {
                                            Thread.currentThread().interrupt();
                                        }
                                    }
                                }, fetchPool);
                                futures.add(f);
                            }
                            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                        } catch (Exception ignored) {
                        } finally {
                            try {
                                queue.offer(DownloadResult.POISON_PILL, 2, TimeUnit.SECONDS);
                            } catch (InterruptedException ignored) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    });

                    try {
                        int processedTasks = 0;
                        int lastReportedPercent = 10;
                        if (progressListener != null) {
                            progressListener.accept(10);
                        }

                        DownloadResult result;
                        while ((result = queue.take()) != DownloadResult.POISON_PILL) {
                            processedTasks++;
                            if (progressListener != null && evCount > 0) {
                                int currentPercent = 10 + (int) ((80.0 * processedTasks) / evCount);
                                if (currentPercent >= lastReportedPercent + 5 && currentPercent <= 90) {
                                    lastReportedPercent = currentPercent;
                                    progressListener.accept(currentPercent);
                                }
                            }

                            if (result.isError() || result.data() == null) {
                                missingCount++;
                                missingStorageKeys.add(result.doc().getStorageKey());
                                addZipEntryString(zos, result.entryPath() + "_ERROR_DOCUMENTO_NO_DISPONIBLE.txt",
                                        buildWarningContent(result.doc()), addedZipPaths);
                            } else if (result.data().length == 0) {
                                missingCount++;
                                missingStorageKeys.add(result.doc().getStorageKey());
                                addZipEntryString(zos, result.entryPath() + "_VACIO.txt",
                                        "El archivo original está vacío en el almacenamiento.\n"
                                                + "Folio: " + (result.doc().getFolioCode() == null ? "N/D" : result.doc().getFolioCode())
                                                + "\nStorageKey: " + result.doc().getStorageKey() + "\n", addedZipPaths);
                            } else {
                                String sha256 = sha256Hex(result.data());
                                checksums.put(result.entryPath(), sha256);
                                docSizes.put(result.doc().getStorageKey(), (long) result.data().length);
                                addZipEntry(zos, result.entryPath(), result.data(), addedZipPaths);
                            }
                        }
                    } catch (Exception loopEx) {
                        cancelled.set(true);
                        fetchPool.shutdownNow();
                        producer.interrupt();
                        throw loopEx;
                    } finally {
                        fetchPool.shutdownNow();
                    }
                }

                // 3. Índice maestro HTML5
                String indexHtml = buildIndexHtml(process, processId, careerIdStr, treeOpt,
                        docsByIndicator, dimCount, compCount, critCount, indCount, evCount, missingCount,
                        missingStorageKeys, docSizes);
                addZipEntryString(zos, "00_Indice_Maestro_Cumplimiento.html", indexHtml, addedZipPaths);

                // 4. Resumen ejecutivo (texto plano)
                String execSummary = buildExecutiveSummary(process, processId, careerIdStr,
                        dimCount, compCount, critCount, indCount, evCount, missingCount);
                addZipEntryString(zos, "00_Resumen_Ejecutivo.txt", execSummary, addedZipPaths);

                // 5. Manifiesto de checksums SHA-256
                String manifest = buildManifest(checksums, processId, jobId);
                addZipEntryString(zos, "00_Manifiest_Checksums.txt", manifest, addedZipPaths);
            }

            log.info("[generateDossierZip] ZIP generado en streaming directo sin archivos temporales para job {}: {} checksums, storageKey={}",
                    jobId, checksums.size(), storageKey);
            return storageKey;
        } catch (Exception e) {
            log.error("[generateDossierZip] Error generando dossier para job {}: {}", jobId, e.getMessage(), e);
            if (storageKey != null) {
                try {
                    blobStorage.delete(storageKey);
                } catch (Exception delEx) {
                    log.warn("[generateDossierZip] Error purgando storageKey parcial {}: {}", storageKey, delEx.getMessage());
                }
            }
            throw new IllegalStateException("Error generating dossier zip", e);
        }
    }

    /* ------------------------------------------------------------------ *
     *  Construcción del índice HTML5 responsivo                          *
     * ------------------------------------------------------------------ */

    private String buildIndexHtml(AccreditationProcess process, UUID processId, String careerIdStr,
                                  Optional<NormativeHierarchyQueryPort.ProcessNormativeTree> treeOpt,
                                  Map<UUID, List<DocumentAsset>> docsByIndicator,
                                  int dimCount, int compCount, int critCount, int indCount, int evCount, int missingCount,
                                  Set<String> missingStorageKeys, Map<String, Long> docSizes) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='es'>\n");
        html.append("<head>\n");
        html.append("  <meta charset='UTF-8'>\n");
        html.append("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("  <title>Índice Maestro de Cumplimiento - Dossier Digital SIGESA</title>\n");
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
        html.append("      --secondary-300: #FFB4A8;\n");
        html.append("      --secondary-500: #E80F16;\n");
        html.append("      --secondary-700: #930006;\n");
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
        html.append("    a { color: var(--primary-600); text-decoration: none; }\n");
        html.append("    a:hover { text-decoration: underline; }\n");
        html.append("    .header { background: linear-gradient(135deg, var(--primary-600) 0%, var(--primary-500) 100%); color: #fff; padding: 24px 32px; border-radius: 12px 12px 0 0; margin-bottom: 24px; }\n");
        html.append("    .header h1 { margin: 0 0 8px 0; font-size: 1.875rem; color: #fff; }\n");
        html.append("    .header .subtitle { opacity: 0.9; font-size: 0.875rem; }\n");
        html.append("    .meta { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 24px; }\n");
        html.append("    .meta .item { background: var(--primary-50); color: var(--primary-700); padding: 6px 12px; border-radius: 6px; font-size: 0.75rem; font-weight: 500; border: 1px solid var(--primary-200); }\n");
        html.append("    .stats { display: flex; flex-wrap: wrap; gap: 12px; margin: 24px 0; }\n");
        html.append("    .stat-card { background: var(--gray-50); border: 1px solid var(--gray-200); border-radius: 8px; padding: 14px 16px; min-width: 120px; text-align: center; }\n");
        html.append("    .stat-card .num { display: block; font-size: 1.875rem; font-weight: 700; color: var(--primary-600); }\n");
        html.append("    .stat-card .lbl { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px; color: var(--gray-600); }\n");
        html.append("    .stat-card .lbl-missing { color: var(--danger); }\n");
        html.append("    .toc { margin: 24px 0; }\n");
        html.append("    .toc h2 { border-bottom: 2px solid var(--primary-600); padding-bottom: 6px; margin-bottom: 12px; }\n");
        html.append("    .toc ul { list-style: none; padding: 0; }\n");
        html.append("    .toc li { margin: 6px 0; }\n");
        html.append("    .toc a { color: var(--primary-700); }\n");
        html.append("    .dimension { margin: 30px 0; page-break-inside: avoid; border: 1px solid var(--gray-200); border-radius: 10px; overflow: hidden; }\n");
        html.append("    .dimension > h2 { background: var(--primary-50); color: var(--primary-800); border-left: 4px solid var(--primary-600); padding: 12px 16px; margin: 0; font-size: 1.25rem; }\n");
        html.append("    .component { margin: 16px 0 0 20px; padding-left: 12px; border-left: 2px solid var(--gray-300); }\n");
        html.append("    .component h3 { color: var(--primary-700); margin: 8px 0; font-size: 1.125rem; }\n");
        html.append("    .criterion { margin: 12px 0 0 30px; padding-left: 12px; border-left: 2px solid var(--primary-200); }\n");
        html.append("    .criterion h4 { color: var(--primary-600); margin: 6px 0; font-size: 1rem; }\n");
        html.append("    .indicator { margin: 10px 0 0 40px; padding-left: 12px; border-left: 2px solid var(--primary-300); }\n");
        html.append("    .indicator h5 { color: var(--primary-500); margin: 4px 0; font-size: 0.875rem; }\n");
        html.append("    table.evidence { width: 100%; border-collapse: collapse; margin: 8px 0; font-size: 0.875rem; }\n");
        html.append("    table.evidence th { background: var(--gray-100); border: 1px solid var(--gray-300); padding: 8px 10px; text-align: left; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px; color: var(--gray-700); }\n");
        html.append("    table.evidence td { border: 1px solid var(--gray-300); padding: 8px 10px; vertical-align: top; }\n");
        html.append("    table.evidence tr:nth-child(even) td { background: var(--gray-50); }\n");
        html.append("    .badge { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 0.75rem; font-weight: 600; }\n");
        html.append("    .badge-ok { background: #dcfce7; color: #166534; }\n");
        html.append("    .badge-missing { background: #fee2e2; color: #991b1b; }\n");
        html.append("    .no-evidences { color: var(--gray-600); font-style: italic; font-size: 0.875rem; }\n");
        html.append("    .missing-section { background: var(--secondary-50); border: 1px solid var(--secondary-300); border-radius: 8px; padding: 16px; margin: 24px 0; }\n");
        html.append("    .missing-section h3 { color: var(--secondary-700); margin-top: 0; }\n");
        html.append("    .manifest { background: var(--primary-50); border: 1px solid var(--primary-200); border-radius: 8px; padding: 16px; margin: 24px 0; font-size: 0.75rem; }\n");
        html.append("    .manifest h3 { color: var(--primary-800); margin-top: 0; font-size: 0.875rem; }\n");
        html.append("    .manifest pre { margin: 8px 0 0 0; white-space: pre-wrap; word-break: break-all; font-family: var(--font-mono); }\n");
        html.append("    .footer { margin-top: 40px; border-top: 1px solid var(--gray-300); padding-top: 16px; font-size: 0.75rem; color: var(--gray-600); }\n");
        html.append("    @media print { body { padding: 0; } .header { background: var(--primary-600); -webkit-print-color-adjust: exact; }\n");
        html.append("    .no-print { display: none; } }\n");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Cabecera
        html.append("  <div class='header'>\n");
        html.append("    <h1>Índice Maestro de Cumplimiento</h1>\n");
        html.append("    <div class='subtitle'>Dossier Digital de Acreditación &mdash; SIGESA</div>\n");
        html.append("  </div>\n");

        // Metadatos del proceso
        html.append("  <div class='meta'>\n");
        html.append("    <div class='item'><strong>Proceso:</strong> ").append(escape(processId.toString())).append("</div>\n");
        html.append("    <div class='item'><strong>Carrera:</strong> ").append(escape(careerIdStr)).append("</div>\n");
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
        if (treeOpt.isPresent()) {
            html.append("  <div class='toc'>\n");
            html.append("    <h2>Tabla de Contenidos</h2>\n");
            html.append("    <ul>\n");
            for (Level1Node l1 : treeOpt.get().level1Nodes()) {
                String dimName = l1.getName() == null ? "Dimension" : l1.getName();
                String anchor = "dim-" + sanitize(dimName);
                html.append("      <li><a href='#").append(anchor).append("'>").append(escape(dimName)).append("</a></li>\n");
            }
            html.append("    </ul>\n");
            html.append("    <li><a href='#missing'>Evidencias no disponibles</a></li>\n");
            html.append("    <li><a href='#manifest'>Manifiest de checksums</a></li>\n");
            html.append("  </div>\n");
        }

        // Detalle jerárquico
        if (treeOpt.isPresent()) {
            for (Level1Node level1 : treeOpt.get().level1Nodes()) {
                String dimName = level1.getName() == null ? "Dimension" : level1.getName();
                String anchor = "dim-" + sanitize(dimName);
                html.append("  <div class='dimension' id='").append(anchor).append("'>\n");
                html.append("    <h2>").append(escape(dimName)).append("</h2>\n");
                for (Level2Node level2 : level1.getLevel2Nodes()) {
                    String areaName = level2.getName() == null ? "Area" : level2.getName();
                    html.append("    <div class='component'>\n");
                    html.append("      <h3>").append(escape(areaName)).append("</h3>\n");
                    for (Level3Node level3 : level2.getLevel3Nodes()) {
                        String critName = level3.getName() == null ? "Criterio" : level3.getName();
                        html.append("      <div class='criterion'>\n");
                        html.append("        <h4>").append(escape(critName)).append("</h4>\n");
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
                                html.append("            <thead><tr><th>Folio</th><th>Archivo</th><th>Tamaño</th><th>Estado</th></tr></thead>\n");
                                html.append("            <tbody>\n");
                                for (DocumentAsset doc : docs) {
                                    boolean missing = missingStorageKeys.contains(doc.getStorageKey());
                                    String folio = doc.getFolioCode() == null ? "N/D" : doc.getFolioCode();
                                    String title = doc.getTitle() == null ? "documento" : doc.getTitle();
                                    long size = docSizes.getOrDefault(doc.getStorageKey(), 0L);
                                    boolean available = !missing && size > 0;
                                    html.append("              <tr>\n");
                                    html.append("                <td><strong>").append(escape(folio)).append("</strong></td>\n");
                                    html.append("                <td>").append(escape(title)).append("</td>\n");
                                    html.append("                <td>").append(formatSize(size)).append("</td>\n");
                                    html.append("                <td><span class='badge ").append(available ? "badge-ok" : "badge-missing")
                                            .append("'>").append(available ? "Disponible" : "No disponible").append("</span></td>\n");
                                    html.append("              </tr>\n");
                                }
                                html.append("            </tbody>\n");
                                html.append("          </table>\n");
                            }
                            html.append("        </div>\n");
                        }
                        html.append("      </div>\n");
                    }
                    html.append("    </div>\n");
                }
                html.append("  </div>\n");
            }
        }

        // Sección de faltantes
        if (missingCount > 0) {
            html.append("  <div class='missing-section' id='missing'>\n");
            html.append("    <h3>Evidencias No Disponibles (").append(missingCount).append(")</h3>\n");
            html.append("    <p>Los siguientes archivos no se pudieron recuperar del almacenamiento y se incluyeron como advertencia de texto dentro del ZIP:</p>\n");
            if (treeOpt.isPresent()) {
                for (Level1Node level1 : treeOpt.get().level1Nodes()) {
                    for (Level2Node level2 : level1.getLevel2Nodes()) {
                        for (Level3Node level3 : level2.getLevel3Nodes()) {
                            for (NormativeIndicator indicator : level3.getIndicators()) {
                                List<DocumentAsset> docs = docsByIndicator.getOrDefault(indicator.getId(), List.of());
                                for (DocumentAsset doc : docs) {
                                    if (missingStorageKeys.contains(doc.getStorageKey())) {
                                        html.append("    <p>- Folio: ").append(escape(doc.getFolioCode() == null ? "N/D" : doc.getFolioCode()))
                                                .append(" | Archivo: ").append(escape(doc.getTitle() == null ? "documento" : doc.getTitle()))
                                                .append(" | Indicador: ").append(escape(indicator.getCode() == null ? indicator.getDescription() : indicator.getCode()))
                                                .append(" | Clave: ").append(escape(doc.getStorageKey())).append("</p>\n");
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

    private String buildExecutiveSummary(AccreditationProcess process, UUID processId, String careerIdStr,
                                         int dimCount, int compCount, int critCount, int indCount, int evCount, int missingCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("RESUMEN EJECUTIVO - DOSSIER DIGITAL\n");
        sb.append("====================================\n\n");
        sb.append("Proceso: ").append(processId).append("\n");
        sb.append("Carrera: ").append(careerIdStr).append("\n");
        sb.append("Estado: ").append(process.getStatus() != null ? process.getStatus() : "N/D").append("\n");
        sb.append("Modalidad: ").append(process.getOperationalMode() != null ? process.getOperationalMode() : "N/D").append("\n");
        sb.append("Generado: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");
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
        sb.append("00_Indice_Maestro_Cumplimiento.html  - Índice navegable con enlaces relativos\n");
        sb.append("00_Resumen_Ejecutivo.txt             - Este archivo\n");
        sb.append("00_Manifiest_Checksums.txt           - Checksums SHA-256 para validación de integridad\n");
        sb.append("Dimension_*/Componente_*/Criterio_*/Indicador_*/  - Jerarquía normativa con archivos\n");
        return sb.toString();
    }

    private String buildManifest(Map<String, String> checksums, UUID processId, String jobId) {
        StringBuilder sb = new StringBuilder();
        sb.append("MANIFIEST DE INTEGRIDAD - SHA-256\n");
        sb.append("==================================\n\n");
        sb.append("Proceso: ").append(processId).append("\n");
        sb.append("Job:     ").append(jobId).append("\n");
        sb.append("Generado: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
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
        if (fileName.toLowerCase().endsWith(ext.toLowerCase()) && !ext.isEmpty()) {
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

    private String ensureUniqueZipPath(String path, Set<String> addedPaths) {
        String normalized = normalizePath(path);
        if (addedPaths.add(normalized)) {
            return normalized;
        }
        String ext = getExtension(normalized);
        String base = ext.isEmpty() ? normalized : normalized.substring(0, normalized.length() - ext.length());
        int counter = 1;
        String candidate;
        do {
            candidate = base + "_" + counter + ext;
            counter++;
        } while (!addedPaths.add(candidate));
        return candidate;
    }

    private void addZipEntry(ZipOutputStream zos, String path, byte[] data, Set<String> addedZipPaths) throws IOException {
        String uniquePath = ensureUniqueZipPath(path, addedZipPaths);
        ZipEntry entry = new ZipEntry(uniquePath);
        entry.setSize(data.length);
        entry.setCompressedSize(-1);
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
        log.debug("[addZipEntry] Entrada escrita: {} ({} bytes)", uniquePath, data.length);
    }

    private void addZipEntryString(ZipOutputStream zos, String path, String content, Set<String> addedZipPaths) throws IOException {
        String uniquePath = ensureUniqueZipPath(path, addedZipPaths);
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        ZipEntry entry = new ZipEntry(uniquePath);
        entry.setSize(data.length);
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }

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
