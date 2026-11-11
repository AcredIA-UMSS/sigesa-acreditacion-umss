package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class S3EvidenceBlobStorageAdapter implements EvidenceBlobStoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3EvidenceBlobStorageAdapter.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3EvidenceBlobStorageAdapter(S3Client s3Client, String bucket) {
        this(s3Client, null, bucket);
    }

    public S3EvidenceBlobStorageAdapter(S3Client s3Client, S3Presigner s3Presigner, String bucket) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
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

    @Override
    public Optional<InputStream> openInputStream(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(storageKey).build()));
        } catch (NoSuchKeyException ex) {
            return Optional.empty();
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return Optional.empty();
            }
            throw new IllegalStateException("Unable to open evidence blob stream: " + storageKey, ex);
        }
    }

    @Override
    public OutputStream createOutputStream(String storageKey, String contentType) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("storageKey is required for output stream");
        }
        return new S3MultipartOutputStream(s3Client, bucket, storageKey, contentType);
    }

    @Override
    public Optional<String> generatePresignedDownloadUrl(String storageKey, Duration duration) {
        if (s3Presigner == null || storageKey == null || storageKey.isBlank()) {
            return Optional.empty();
        }
        try {
            String filename = storageKey.contains("/")
                    ? storageKey.substring(storageKey.lastIndexOf('/') + 1)
                    : storageKey;
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .responseContentDisposition("attachment; filename=\"" + filename + "\"")
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();
            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            return Optional.of(presigned.url().toString());
        } catch (Exception ex) {
            log.warn("Error generando presigned URL para {}: {}", storageKey, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void purgeExpiredDossierExports(Duration maxAge) {
        try {
            Instant threshold = Instant.now().minus(maxAge);
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix("dossier-exports/")
                    .build();
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            List<ObjectIdentifier> toDelete = listResponse.contents().stream()
                    .filter(obj -> obj.lastModified() != null && obj.lastModified().isBefore(threshold))
                    .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                    .toList();

            if (!toDelete.isEmpty()) {
                s3Client.deleteObjects(DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder().objects(toDelete).build())
                        .build());
                log.info("Purgadas {} exportaciones de dossier expiradas de S3", toDelete.size());
            }
        } catch (Exception ex) {
            log.warn("Error purgando exportaciones expiradas en S3: {}", ex.getMessage());
        }
    }

    static String objectKey(UUID evidenceId, int version, String originalFilename) {
        String safeName = originalFilename == null ? "blob" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "evidences/" + evidenceId + "/v" + version + "/" + safeName;
    }

    /**
     * Implementa un OutputStream que sube fragmentos de 5 MB vía S3 Multipart Upload
     * o hace un solo PutObject si el contenido total es menor a 5 MB.
     */
    private static class S3MultipartOutputStream extends OutputStream {

        private static final int PART_SIZE = 5 * 1024 * 1024; // 5 MB mínimo para S3 multipart

        private final S3Client s3Client;
        private final String bucket;
        private final String storageKey;
        private final String contentType;

        private final ByteArrayOutputStream currentPartBuffer = new ByteArrayOutputStream(PART_SIZE);
        private final List<CompletedPart> completedParts = new ArrayList<>();
        private String uploadId = null;
        private int partNumber = 1;
        private boolean closed = false;
        private boolean aborted = false;

        public synchronized void abort() {
            if (closed) return;
            aborted = true;
            abortMultipart();
        }

        public S3MultipartOutputStream(S3Client s3Client, String bucket, String storageKey, String contentType) {
            this.s3Client = s3Client;
            this.bucket = bucket;
            this.storageKey = storageKey;
            this.contentType = contentType != null ? contentType : "application/octet-stream";
        }

        @Override
        public synchronized void write(int b) throws IOException {
            if (closed) {
                throw new IOException("Stream already closed");
            }
            currentPartBuffer.write(b);
            if (currentPartBuffer.size() >= PART_SIZE) {
                flushPart();
            }
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) throws IOException {
            if (closed) {
                throw new IOException("Stream already closed");
            }
            int remaining = len;
            int currentOffset = off;
            while (remaining > 0) {
                int spaceInCurrent = PART_SIZE - currentPartBuffer.size();
                int toWrite = Math.min(remaining, spaceInCurrent);
                currentPartBuffer.write(b, currentOffset, toWrite);
                remaining -= toWrite;
                currentOffset += toWrite;
                if (currentPartBuffer.size() >= PART_SIZE) {
                    flushPart();
                }
            }
        }

        private void flushPart() {
            if (uploadId == null) {
                CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(storageKey)
                        .contentType(contentType)
                        .build();
                CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(createRequest);
                uploadId = createResponse.uploadId();
            }
            byte[] partBytes = currentPartBuffer.toByteArray();
            currentPartBuffer.reset();
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .contentLength((long) partBytes.length)
                    .build();
            UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, RequestBody.fromBytes(partBytes));
            completedParts.add(CompletedPart.builder()
                    .partNumber(partNumber)
                    .eTag(uploadPartResponse.eTag())
                    .build());
            partNumber++;
        }

        @Override
        public synchronized void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            if (aborted) {
                abortMultipart();
                return;
            }
            try {
                if (uploadId == null) {
                    byte[] bytes = currentPartBuffer.toByteArray();
                    PutObjectRequest putRequest = PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(storageKey)
                            .contentType(contentType)
                            .contentLength((long) bytes.length)
                            .build();
                    s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));
                } else {
                    if (currentPartBuffer.size() > 0) {
                        byte[] partBytes = currentPartBuffer.toByteArray();
                        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                                .bucket(bucket)
                                .key(storageKey)
                                .uploadId(uploadId)
                                .partNumber(partNumber)
                                .contentLength((long) partBytes.length)
                                .build();
                        UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, RequestBody.fromBytes(partBytes));
                        completedParts.add(CompletedPart.builder()
                                .partNumber(partNumber)
                                .eTag(uploadPartResponse.eTag())
                                .build());
                    }
                    CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                            .parts(completedParts)
                            .build();
                    CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                            .bucket(bucket)
                            .key(storageKey)
                            .uploadId(uploadId)
                            .multipartUpload(completedMultipartUpload)
                            .build();
                    s3Client.completeMultipartUpload(completeRequest);
                }
            } catch (Exception ex) {
                abortMultipart();
                throw new IOException("Error completing S3 multipart upload for " + storageKey, ex);
            }
        }

        private void abortMultipart() {
            if (uploadId != null) {
                try {
                    s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                            .bucket(bucket)
                            .key(storageKey)
                            .uploadId(uploadId)
                            .build());
                } catch (Exception ignored) {}
            }
        }
    }
}
