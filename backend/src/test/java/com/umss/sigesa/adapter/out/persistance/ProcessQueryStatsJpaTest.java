package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.ProcessPersistenceMapper;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort.ProcessStatsRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({ProcessQueryJpaAdapter.class, ProcessPersistenceMapper.class})
class ProcessQueryStatsJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProcessQueryPort processQueryPort;

    @Test
    void findAllStatsRows_includesAuditReadonlyAndExcludesArchived() {
        entityManager.persist(process("ACTIVE", "AUDIT_READONLY"));
        entityManager.persist(process("ARCHIVED", "ACTIVE"));
        entityManager.flush();
        entityManager.clear();

        List<ProcessStatsRow> rows = processQueryPort.findAllStatsRows();

        assertEquals(1, rows.size());
        assertEquals("ACTIVE", rows.getFirst().status());
        assertEquals("AUDIT_READONLY", rows.getFirst().operationalMode());
        assertTrue(rows.stream().noneMatch(row -> "ARCHIVED".equals(row.status())));
    }

    private static AccreditationProcessJpaEntity process(String status, String operationalMode) {
        return AccreditationProcessJpaEntity.builder()
                .careerId(UUID.randomUUID())
                .templateId(UUID.randomUUID())
                .status(status)
                .operationalMode(operationalMode)
                .startDate(LocalDateTime.now())
                .build();
    }
}
