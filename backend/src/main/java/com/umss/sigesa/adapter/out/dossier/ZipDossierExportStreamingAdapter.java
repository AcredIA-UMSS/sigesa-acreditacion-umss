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
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipDossierExportStreamingAdapter implements DossierExportPort {

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

    @Override
    public File generateDossierZip(UUID processId, String jobId) {
        try {
            AccreditationProcess process = processQueryPort.findDetailById(processId).orElse(null);
            String careerName = process != null && process.getCareerId() != null ? process.getCareerId().toString() : "Carrera";
            String processCode = processId.toString().substring(0, 8);
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String zipName = "Dossier_" + sanitize(careerName) + "_" + sanitize(processCode) + "_" + dateStr + ".zip";
            File tempFile = Files.createTempFile("dossier-", ".zip").toFile();
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                Optional<NormativeHierarchyQueryPort.ProcessNormativeTree> treeOpt = normativeHierarchyQueryPort.findProcessTree(processId);
                List<DocumentAsset> assets = documentAssetPort.findByProcessId(processId);
                List<IndicatorEvidenceLink> allLinks = documentAssetPort.findAllLinksByProcessId(processId);

                Map<UUID, List<DocumentAsset>> docsByIndicator = new HashMap<>();
                for (IndicatorEvidenceLink link : allLinks) {
                    DocumentAsset asset = assets.stream().filter(a -> a.getId().equals(link.getDocumentAssetId())).findFirst().orElse(null);
                    if (asset == null) continue;
                    docsByIndicator.computeIfAbsent(link.getIndicatorId(), k -> new ArrayList<>()).add(asset);
                }

                StringBuilder indexHtml = new StringBuilder();
                indexHtml.append("<html><head><meta charset='UTF-8'><title>Indice Maestro</title></head><body>");
                indexHtml.append("<h1>Índice Maestro de Cumplimiento</h1>");
                indexHtml.append("<p>Proceso: ").append(escape(processId.toString())).append("</p>");

                if (treeOpt.isPresent()) {
                    for (Level1Node level1 : treeOpt.get().level1Nodes()) {
                        String dimName = level1.getName() == null ? "Dimension" : level1.getName();
                        String dimPath = "Dimension_" + sanitize(dimName);
                        indexHtml.append("<h2>").append(escape(dimName)).append("</h2>");
                        for (Level2Node level2 : level1.getLevel2Nodes()) {
                            String areaName = level2.getName() == null ? "Area" : level2.getName();
                            String areaPath = dimPath + "/Componente_" + sanitize(areaName);
                            for (Level3Node level3 : level2.getLevel3Nodes()) {
                                String critName = level3.getName() == null ? "Criterio" : level3.getName();
                                String critPath = areaPath + "/Criterio_" + sanitize(critName);
                                for (NormativeIndicator indicator : level3.getIndicators()) {
                                    String indName = indicator.getCode() == null ? indicator.getDescription() : indicator.getCode();
                                    String indPath = critPath + "/Indicador_" + sanitize(indName);
                                    indexHtml.append("<h3>").append(escape(indName)).append("</h3><ul>");
                                    List<DocumentAsset> docs = docsByIndicator.getOrDefault(indicator.getId(), List.of());
                                    if (docs.isEmpty()) {
                                        indexHtml.append("<li>(Sin evidencias)</li>");
                                    } else {
                                        Set<String> seen = new HashSet<>();
                                        for (DocumentAsset doc : docs) {
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
                                            if (!seen.add(fileName)) {
                                                // make unique
                                                fileName = base + "_" + UUID.randomUUID().toString().substring(0, 4) + "_" + sanitize(originalName);
                                                if (!ext.isEmpty() && !fileName.toLowerCase().endsWith(ext.toLowerCase())) {
                                                    fileName = fileName.replaceAll("\\.[^.]+$", "") + ext;
                                                }
                                            }
                                            String entryPath = indPath + "/" + fileName;
                                            addZipEntry(zos, entryPath, doc);
                                            indexHtml.append("<li><a href='").append(relativeLink(indPath, entryPath)).append("'>").append(escape(fileName)).append("</a> (folio: ").append(escape(doc.getFolioCode() == null ? "" : doc.getFolioCode())).append(")</li>");
                                        }
                                    }
                                    indexHtml.append("</ul>");
                                }
                            }
                        }
                    }
                }

                indexHtml.append("</body></html>");
                addZipEntryString(zos, "00_Indice_Maestro_Cumplimiento.html", indexHtml.toString());
                addZipEntryString(zos, "00_Indice_Maestro_Cumplimiento.pdf", "Índice maestro (resumen). PDF generado en versión básica.");
            }

            return tempFile;
        } catch (Exception e) {
            throw new IllegalStateException("Error generating dossier zip", e);
        }
    }

    private void addZipEntry(ZipOutputStream zos, String path, DocumentAsset doc) throws IOException {
        ZipEntry entry = new ZipEntry(normalizePath(path));
        zos.putNextEntry(entry);
        try {
            Optional<byte[]> data = blobStorage.retrieve(doc.getStorageKey());
            if (data.isPresent()) {
                zos.write(data.get());
            } else {
                String warning = "DOCUMENTO NO DISPONIBLE\nFOLIO: " + (doc.getFolioCode() == null ? "" : doc.getFolioCode()) + "\nSTORAGE_KEY: " + doc.getStorageKey();
                zos.write(warning.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            String warning = "[FOLIO]_" + (doc.getFolioCode() == null ? "ERROR" : doc.getFolioCode()) + "_ERROR_DOCUMENTO_NO_DISPONIBLE.txt\n" + ex.getMessage();
            zos.write(warning.getBytes(StandardCharsets.UTF_8));
        }
        zos.closeEntry();
    }

    private void addZipEntryString(ZipOutputStream zos, String path, String content) throws IOException {
        ZipEntry entry = new ZipEntry(normalizePath(path));
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String sanitize(String name) {
        if (name == null) return "SinNombre";
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String cleaned = withoutAccents.replaceAll("[^A-Za-z0-9._-]", "_");
        cleaned = cleaned.replaceAll("[/\\\\:\\*\\?\"<>|]", "_");
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
        // ensure forward slashes
        return path.replace("\\", "/");
    }

    private String relativeLink(String fromDir, String toPath) {
        // simple relative link
        String[] fromParts = fromDir.split("/");
        String[] toParts = toPath.split("/");
        int common = 0;
        int min = Math.min(fromParts.length, toParts.length);
        for (int i = 0; i < min; i++) {
            if (fromParts[i].equals(toParts[i])) {
                common++;
            } else {
                break;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = common; i < fromParts.length; i++) {
            sb.append("../");
        }
        for (int i = common; i < toParts.length; i++) {
            if (i > common) sb.append("/");
            sb.append(toParts[i]);
        }
        if (sb.length() == 0) {
            return toPath;
        }
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
