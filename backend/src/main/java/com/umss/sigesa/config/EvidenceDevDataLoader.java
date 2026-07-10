package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.EvidenceJpaRepository;
import com.umss.sigesa.adapter.out.persistance.EvidenceVersionJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evidencias de demostración para desarrollo local (búsqueda, dashboard, workflow TD).
 */
@Component
@Profile("!prod")
@Order(260)
public class EvidenceDevDataLoader implements ApplicationRunner {

    private static final byte[] DEMO_PDF_BYTES = """
            %PDF-1.4
            1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
            2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj
            3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 300 144] >> endobj
            xref
            0 4
            trailer << /Size 4 /Root 1 0 R >>
            startxref
            200
            %%EOF
            """.strip().getBytes();

    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository versionRepository;
    private final AppUserJpaRepository userRepository;
    private final Path storageDirectory;

    public EvidenceDevDataLoader(EvidenceJpaRepository evidenceRepository,
                                 EvidenceVersionJpaRepository versionRepository,
                                 AppUserJpaRepository userRepository,
                                 @Value("${sigesa.evidence.storage-dir:uploads/evidences}") String storageDir) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.storageDirectory = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Files.createDirectories(storageDirectory);

        UUID coordinatorId = userRepository.findByEmail(AuthDataLoader.SEED_CC_EMAIL)
                .map(AppUserEntity::getId)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000099"));

        LocalDateTime base = LocalDateTime.of(2026, 6, 1, 10, 0);

        seedEvidence(
                DevSeedData.EVIDENCE_LABS,
                DevSeedData.EVIDENCE_LABS_V1,
                DevSeedData.INDICATOR_102,
                coordinatorId,
                "Inventario fotográfico de laboratorios de cómputo — periodo 2026-1.",
                DevSeedData.DEMO_EVIDENCE_STORAGE_LABS,
                base.minusDays(10)
        );

        seedEvidence(
                DevSeedData.EVIDENCE_PLAN,
                DevSeedData.EVIDENCE_PLAN_V1,
                DevSeedData.INDICATOR_201,
                coordinatorId,
                "Resolución CSU del plan de estudios vigente (PDF escaneado).",
                DevSeedData.DEMO_EVIDENCE_STORAGE_PLAN,
                base.minusDays(5)
        );

        seedEvidence(
                DevSeedData.EVIDENCE_BIBLIO,
                DevSeedData.EVIDENCE_BIBLIO_V1,
                DevSeedData.INDICATOR_305,
                coordinatorId,
                "Reporte de gestión de recursos bibliográficos digitales.",
                DevSeedData.DEMO_EVIDENCE_STORAGE_BIBLIO,
                base.minusDays(15)
        );
    }

    private void seedEvidence(UUID evidenceId,
                              UUID versionId,
                              UUID indicatorId,
                              UUID createdBy,
                              String description,
                              String storageKey,
                              LocalDateTime createdAt) throws IOException {
        if (evidenceRepository.existsById(evidenceId)) {
            return;
        }

        Path target = storageDirectory.resolve(storageKey);
        if (!Files.exists(target)) {
            Files.write(target, DEMO_PDF_BYTES);
        }

        String contentHash = sha256(DEMO_PDF_BYTES);

        EvidenceEntity evidence = new EvidenceEntity();
        evidence.setId(evidenceId);
        evidence.setIndicatorId(indicatorId);
        evidence.setLatestVersionId(versionId);
        evidence.setCreatedAt(createdAt);
        evidenceRepository.save(evidence);

        EvidenceVersionEntity version = new EvidenceVersionEntity();
        version.setId(versionId);
        version.setEvidenceId(evidenceId);
        version.setVersionNumber(1);
        version.setDescription(description);
        version.setStorageKey(storageKey);
        version.setContentHash(contentHash);
        version.setObservationId(null);
        version.setCreatedBy(createdBy);
        version.setCreatedAt(createdAt);
        versionRepository.save(version);
    }

    private static String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hex = new StringBuilder();
            for (byte value : hash) {
                String part = Integer.toHexString(0xff & value);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
