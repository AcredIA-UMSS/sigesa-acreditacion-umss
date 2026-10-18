package com.umss.sigesa.adapter.out.report;

import com.umss.sigesa.application.port.out.ReportArtifactStoragePort;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Optional;
import java.util.UUID;

public class S3ReportArtifactStorageAdapter implements ReportArtifactStoragePort {

    private final S3Client s3Client;
    private final String bucket;

    public S3ReportArtifactStorageAdapter(S3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public String store(UUID jobId, byte[] pdfContent) {
        String key = "reports/" + jobId + ".pdf";
        s3Client.putObject(
                PutObjectRequest.builder().bucket(bucket).key(key).build(),
                RequestBody.fromBytes(pdfContent == null ? new byte[0] : pdfContent));
        return key;
    }

    @Override
    public Optional<byte[]> retrieve(String artifactKey) {
        if (artifactKey == null || artifactKey.isBlank()) {
            return Optional.empty();
        }
        try {
            ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(artifactKey).build());
            return Optional.ofNullable(bytes.asByteArray());
        } catch (NoSuchKeyException ex) {
            return Optional.empty();
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return Optional.empty();
            }
            throw new IllegalStateException("Unable to read report artifact: " + artifactKey, ex);
        }
    }
}
