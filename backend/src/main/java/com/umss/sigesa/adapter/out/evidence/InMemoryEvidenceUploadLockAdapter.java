package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.application.port.out.EvidenceUploadLockPort;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Solo tests unitarios que instancian el adaptador a mano. El runtime usa
 * {@link JdbcEvidenceUploadLockAdapter} (no hay {@code @Component} aquí).
 */
public class InMemoryEvidenceUploadLockAdapter implements EvidenceUploadLockPort {

    private final ConcurrentHashMap<UUID, UUID> locks = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(UUID indicatorId) {
        return locks.putIfAbsent(indicatorId, indicatorId) == null;
    }

    @Override
    public void release(UUID indicatorId) {
        locks.remove(indicatorId);
    }
}
