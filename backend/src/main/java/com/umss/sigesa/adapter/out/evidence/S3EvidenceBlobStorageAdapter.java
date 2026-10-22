package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Optional;
import java.util.UUID;

public class S3EvidenceBlobStorageAdapter implements EvidenceBlobStoragePort {

    private final S3Client s3Client;
    private final String bucket;

    public S3EvidenceBlobStorageAdapter(S3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public String store(UUID evidenceId, int version, byte[] content, String originalFilename) {
        String key = objectKey(evidenceId, version, originalFilename);
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).build(),
                    RequestBody.fromBytes(content == null ? new byte[0] : content));
            return key;
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to store evidence blob in object storage", ex);
        }
    }

    @Override
    public void restore(String storageKey, byte[] content) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("storageKey is required to restore a blob");
        }
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(storageKey).build(),
                    RequestBody.fromBytes(content == null ? new byte[0] : content));
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to restore evidence blob in object storage", ex);
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(storageKey).build());
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to delete evidence blob: " + storageKey, ex);
        }
    }

    @Override
    public Optional<byte[]> retrieve(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return Optional.empty();
        }
        try {
            ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(storageKey).build());
            return Optional.ofNullable(bytes.asByteArray());
        } catch (NoSuchKeyException ex) {
            return Optional.empty();
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return Optional.empty();
            }
            throw new IllegalStateException("Unable to read evidence blob: " + storageKey, ex);
        }
    }

    static String objectKey(UUID evidenceId, int version, String originalFilename) {
        String safeName = originalFilename == null ? "blob" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "evidences/" + evidenceId + "/v" + version + "/" + safeName;
    }
}
