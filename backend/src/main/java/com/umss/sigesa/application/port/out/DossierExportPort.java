package com.umss.sigesa.application.port.out;

import java.util.UUID;
import java.util.function.Consumer;

public interface DossierExportPort {
    /**
     * Genera el archivo ZIP del dossier en streaming directo hacia el storage configurado
     * sin crear archivos temporales locales ni retener el ZIP completo en memoria heap.
     *
     * @param processId identificador del proceso de acreditación
     * @param jobId identificador del job de exportación
     * @return storageKey del archivo ZIP generado en el storage
     */
    String generateDossierZip(UUID processId, String jobId);

    default String generateDossierZip(UUID processId, String jobId, Consumer<Integer> progressListener) {
        return generateDossierZip(processId, jobId);
    }
}
