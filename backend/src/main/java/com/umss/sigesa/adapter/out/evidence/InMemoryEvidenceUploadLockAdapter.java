package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.application.port.out.EvidenceUploadLockPort;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
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
