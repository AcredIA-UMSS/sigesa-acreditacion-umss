package com.umss.sigesa.application.port.out;

public interface FileStoragePort {
    StorageResult store(String filename, byte[] fileBytes);

    record StorageResult(String storageKey, String contentHash) {}
}
