package com.umss.sigesa.application.port.out;

import java.io.File;
import java.util.UUID;

public interface DossierExportPort {
    File generateDossierZip(UUID processId, String jobId);
}
