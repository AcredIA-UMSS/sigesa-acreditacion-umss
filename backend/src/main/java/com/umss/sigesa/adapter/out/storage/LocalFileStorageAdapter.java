package com.umss.sigesa.adapter.out.storage;

import com.umss.sigesa.application.port.out.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path storageDirectory;

    public LocalFileStorageAdapter(@Value("${sigesa.evidence.storage-dir:uploads/evidences}") String storageDir) {
        this.storageDirectory = Paths.get(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento de evidencias", e);
        }
    }

    @Override
    public StorageResult store(String filename, byte[] fileBytes) {
        String contentHash = calculateSha256(fileBytes);

        String extension = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = filename.substring(dotIndex);
        }
        String storageKey = UUID.randomUUID().toString() + extension;

        Path targetPath = this.storageDirectory.resolve(storageKey);
        try {
            Files.write(targetPath, fileBytes);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar archivo en disco local", e);
        }

        return new StorageResult(storageKey, contentHash);
    }

    private String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algoritmo no disponible", e);
        }
    }
}
