package com.umss.sigesa.config;

import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Seeder de almacenamiento de evidencias para entornos de desarrollo y QA.
 * Asegura que todos los blobs referenciados en document_assets y evidence_version
 * existan en el sistema de almacenamiento configurado (S3/MinIO o disco local).
 */
@Component
@Profile("!prod & !test")
@Order(115)
public class EvidenceStorageSeedDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EvidenceStorageSeedDataLoader.class);

    private final EvidenceBlobStoragePort blobStorage;
    private final EntityManager entityManager;
    private final String configuredStoragePath;

    public EvidenceStorageSeedDataLoader(
            EvidenceBlobStoragePort blobStorage,
            EntityManager entityManager,
            @Value("${sigesa.evidence.storage-path:./data/evidences}") String configuredStoragePath) {
        this.blobStorage = blobStorage;
        this.entityManager = entityManager;
        this.configuredStoragePath = configuredStoragePath;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            seedDocumentAssetBlobs();
            seedEvidenceVersionBlobs();
            syncLocalFilesIfPresent();
        } catch (Exception ex) {
            log.warn("Fallo durante el seeding de almacenamiento de evidencias: {}", ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void seedDocumentAssetBlobs() {
        try {
            List<Object[]> assets = entityManager.createNativeQuery(
                    "SELECT id, storage_key, title, folio_code FROM document_assets WHERE storage_key IS NOT NULL")
                    .getResultList();

            for (Object[] row : assets) {
                String key = (String) row[1];
                if (key == null || key.isBlank() || key.startsWith("external-url:")) {
                    continue;
                }
                if (blobStorage.retrieve(key).isPresent()) {
                    continue;
                }
                String title = row[2] != null ? row[2].toString() : "Documento de Evidencia";
                String folio = row[3] != null ? row[3].toString() : "SIGESA-EVD";
                byte[] content = resolveOrGenerateContent(key, title, folio);
                blobStorage.restore(key, content);
                log.info("Blob restaurado en almacenamiento para document_asset {}: {}", row[0], key);
            }
        } catch (Exception ex) {
            log.warn("No se pudieron sembrar blobs de document_assets: {}", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void seedEvidenceVersionBlobs() {
        try {
            List<Object[]> versions = entityManager.createNativeQuery(
                    "SELECT id, storage_key, description, original_filename FROM evidence_version WHERE storage_key IS NOT NULL")
                    .getResultList();

            for (Object[] row : versions) {
                String key = (String) row[1];
                if (key == null || key.isBlank() || key.startsWith("external-url:")) {
                    continue;
                }
                if (blobStorage.retrieve(key).isPresent()) {
                    continue;
                }
                String desc = row[2] != null ? row[2].toString() : "Versión de evidencia";
                String origName = row[3] != null ? row[3].toString() : key;
                byte[] content = resolveOrGenerateContent(key, desc, origName);
                blobStorage.restore(key, content);
                log.info("Blob restaurado en almacenamiento para evidence_version {}: {}", row[0], key);
            }
        } catch (Exception ex) {
            log.warn("No se pudieron sembrar blobs de evidence_version: {}", ex.getMessage());
        }
    }

    private byte[] resolveOrGenerateContent(String key, String titleOrDesc, String secondaryInfo) {
        byte[] fileBytes = tryReadFileFromDisk(key);
        if (fileBytes != null && fileBytes.length > 0) {
            return fileBytes;
        }
        return generateSyntheticContent(key, titleOrDesc, secondaryInfo);
    }

    private byte[] tryReadFileFromDisk(String key) {
        String cleanName = Path.of(key).getFileName().toString();
        List<Path> candidateRoots = List.of(
                Path.of(configuredStoragePath),
                Path.of("./data/evidences"),
                Path.of("backend/data/evidences"),
                Path.of("/app/data/evidences")
        );

        for (Path root : candidateRoots) {
            Path directPath = root.resolve(key).normalize();
            if (Files.isRegularFile(directPath)) {
                try {
                    return Files.readAllBytes(directPath);
                } catch (IOException ignored) {}
            }
            Path cleanPath = root.resolve(cleanName).normalize();
            if (Files.isRegularFile(cleanPath)) {
                try {
                    return Files.readAllBytes(cleanPath);
                } catch (IOException ignored) {}
            }
        }
        return null;
    }

    private void syncLocalFilesIfPresent() {
        List<Path> candidateRoots = List.of(
                Path.of(configuredStoragePath),
                Path.of("./data/evidences"),
                Path.of("backend/data/evidences"),
                Path.of("/app/data/evidences")
        );

        for (Path root : candidateRoots) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(root)) {
                stream.filter(Files::isRegularFile).forEach(path -> {
                    String filename = path.getFileName().toString();
                    if (filename.startsWith(".")) return;
                    try {
                        if (blobStorage.retrieve(filename).isEmpty()) {
                            byte[] bytes = Files.readAllBytes(path);
                            blobStorage.restore(filename, bytes);
                            log.info("Archivo local sincronizado a almacenamiento: {}", filename);
                        }
                    } catch (Exception ex) {
                        log.debug("No se pudo sincronizar archivo local {}: {}", filename, ex.getMessage());
                    }
                });
            } catch (IOException ex) {
                log.debug("Error leyendo directorio de evidencias {}: {}", root, ex.getMessage());
            }
        }
    }

    private byte[] generateSyntheticContent(String key, String title, String folio) {
        String lowerKey = key.toLowerCase();
        if (lowerKey.endsWith(".pdf")) {
            return createSamplePdf(title, folio, key);
        } else if (lowerKey.endsWith(".csv")) {
            return ("id,indicador,estado,fecha\n1," + title.replace(",", ";") + ",VERIFICADO,2026-11-03\n")
                    .getBytes(StandardCharsets.UTF_8);
        } else {
            return ("UNIVERSIDAD MAYOR DE SAN SIMON - SIGESA\n"
                    + "Folio/Referencia: " + folio + "\n"
                    + "Documento: " + title + "\n"
                    + "Archivo: " + key + "\n"
                    + "Estado: Evidencia verificada para fines de acreditacion universitaria.\n")
                    .getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] createSamplePdf(String title, String folio, String filename) {
        String cleanTitle = title.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ .,:_-]", "");
        String streamContent = "BT /F1 16 Tf 50 720 Td (UNIVERSIDAD MAYOR DE SAN SIMON) Tj\n"
                + "/F1 12 Tf 0 -30 Td (SIGESA - Expediente Digital de Acreditacion) Tj\n"
                + "0 -25 Td (Folio Oficial: " + folio + ") Tj\n"
                + "0 -25 Td (Archivo: " + filename + ") Tj\n"
                + "0 -25 Td (Documento: " + cleanTitle + ") Tj\n"
                + "0 -40 Td (Certificacion: Evidencia verificada para el comite de evaluadores externos.) Tj ET";

        String pdf = "%PDF-1.4\n"
                + "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
                + "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
                + "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n"
                + "4 0 obj << /Length " + streamContent.getBytes(StandardCharsets.UTF_8).length + " >>\n"
                + "stream\n"
                + streamContent + "\n"
                + "endstream\n"
                + "endobj\n"
                + "5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
                + "xref\n"
                + "0 6\n"
                + "0000000000 65535 f \n"
                + "0000000010 00000 n \n"
                + "0000000056 00000 n \n"
                + "0000000112 00000 n \n"
                + "0000000245 00000 n \n"
                + "0000000370 00000 n \n"
                + "trailer << /Size 6 /Root 1 0 R >>\n"
                + "startxref\n"
                + "444\n"
                + "%%EOF";
        return pdf.getBytes(StandardCharsets.UTF_8);
    }
}
