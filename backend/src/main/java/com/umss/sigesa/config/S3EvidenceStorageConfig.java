package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.evidence.S3EvidenceBlobStorageAdapter;
import com.umss.sigesa.adapter.out.report.S3ReportArtifactStorageAdapter;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ReportArtifactStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "sigesa.evidence.storage", havingValue = "s3")
public class S3EvidenceStorageConfig {

    private static final Logger log = LoggerFactory.getLogger(S3EvidenceStorageConfig.class);

    @Bean
    S3Client evidenceS3Client(
            @Value("${sigesa.evidence.s3.endpoint}") String endpoint,
            @Value("${sigesa.evidence.s3.region:us-east-1}") String region,
            @Value("${sigesa.evidence.s3.access-key}") String accessKey,
            @Value("${sigesa.evidence.s3.secret-key}") String secretKey,
            @Value("${sigesa.evidence.s3.path-style:true}") boolean pathStyle) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyle)
                        .build())
                .build();
    }

    @Bean
    EvidenceBlobStoragePort evidenceBlobStoragePort(
            S3Client evidenceS3Client,
            @Value("${sigesa.evidence.s3.bucket}") String bucket) {
        return new S3EvidenceBlobStorageAdapter(evidenceS3Client, bucket);
    }

    @Bean
    ReportArtifactStoragePort reportArtifactStoragePort(
            S3Client evidenceS3Client,
            @Value("${sigesa.evidence.s3.bucket}") String bucket) {
        return new S3ReportArtifactStorageAdapter(evidenceS3Client, bucket);
    }

    @Bean
    ApplicationRunner evidenceS3BucketInitializer(
            S3Client evidenceS3Client,
            @Value("${sigesa.evidence.s3.bucket}") String bucket) {
        return args -> ensureBucket(evidenceS3Client, bucket);
    }

    static void ensureBucket(S3Client s3Client, String bucket) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("Bucket S3/MinIO creado: {}", bucket);
            } else {
                throw ex;
            }
        }
    }
}
