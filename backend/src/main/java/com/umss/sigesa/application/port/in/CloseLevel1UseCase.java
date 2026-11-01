package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Level1CompleteResult;

import java.util.UUID;

public interface CloseLevel1UseCase {

    Level1CompleteResult close(UUID processId, UUID level1Id, UUID actorId, String actorRole);
}
