package com.umss.sigesa.adapter.out.evidence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DisplayName("JdbcEvidenceUploadLockAdapter — lock atómico #51")
class JdbcEvidenceUploadLockAdapterIT {

    @Autowired
    private JdbcEvidenceUploadLockAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Segundo acquire del mismo indicador falla hasta release")
    void tryAcquire_isExclusiveUntilRelease() {
        UUID indicatorId = UUID.randomUUID();
        assertTrue(adapter.tryAcquire(indicatorId));
        assertFalse(adapter.tryAcquire(indicatorId));
        adapter.release(indicatorId);
        assertTrue(adapter.tryAcquire(indicatorId));
        adapter.release(indicatorId);
    }

    @Test
    @DisplayName("Lock vencido (>5 min) se puede re-adquirir")
    void tryAcquire_replacesStaleLock() {
        UUID indicatorId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO evidence_upload_lock (indicator_id, acquired_at) VALUES (?, ?)",
                indicatorId,
                Timestamp.from(Instant.now().minus(JdbcEvidenceUploadLockAdapter.STALE_AFTER.plusMinutes(1))));
        assertTrue(adapter.tryAcquire(indicatorId));
        adapter.release(indicatorId);
    }
}
