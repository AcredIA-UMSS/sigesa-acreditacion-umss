package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.application.port.out.EvidenceUploadLockPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Lock de upload atómico en Postgres/H2 (PK {@code indicator_id}). Visible a todas las réplicas.
 */
@Component
public class JdbcEvidenceUploadLockAdapter implements EvidenceUploadLockPort {

    static final Duration STALE_AFTER = Duration.ofMinutes(5);

    private final JdbcTemplate jdbcTemplate;

    public JdbcEvidenceUploadLockAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryAcquire(UUID indicatorId) {
        Instant staleBefore = Instant.now().minus(STALE_AFTER);
        jdbcTemplate.update(
                "DELETE FROM evidence_upload_lock WHERE indicator_id = ? AND acquired_at < ?",
                indicatorId,
                Timestamp.from(staleBefore));
        try {
            int inserted = jdbcTemplate.update(
                    "INSERT INTO evidence_upload_lock (indicator_id, acquired_at) VALUES (?, ?)",
                    indicatorId,
                    Timestamp.from(Instant.now()));
            return inserted == 1;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(UUID indicatorId) {
        jdbcTemplate.update("DELETE FROM evidence_upload_lock WHERE indicator_id = ?", indicatorId);
    }
}
