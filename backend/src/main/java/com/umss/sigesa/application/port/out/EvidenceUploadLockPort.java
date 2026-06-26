package com.umss.sigesa.application.port.out;

import java.util.UUID;

public interface EvidenceUploadLockPort {

    boolean tryAcquire(UUID indicatorId);

    void release(UUID indicatorId);
}
