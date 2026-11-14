package com.umss.sigesa.adapter.out.evidence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3EvidenceBlobStorageAdapter — PR-IMPL-041 / #51")
class S3EvidenceBlobStorageAdapterTest {

    @Mock
    private S3Client s3Client;

    @Test
    @DisplayName("store: PUT object y retorna key opaca (no path absoluto)")
    void store_putsObjectAndReturnsOpaqueKey() {
        S3EvidenceBlobStorageAdapter adapter = new S3EvidenceBlobStorageAdapter(s3Client, "sigesa-evidences");
        UUID evidenceId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);

        String key = adapter.store(evidenceId, 1, "%PDF".getBytes(), "acta final.pdf");

        assertEquals("evidences/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/v1/acta_final.pdf", key);
        assertFalse(key.startsWith("/") || key.contains(":\\"));
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("sigesa-evidences", captor.getValue().bucket());
        assertEquals(key, captor.getValue().key());
    }

    @Test
    @DisplayName("restore: PUT object en storageKey existente")
    void restore_putsObjectAtExistingKey() {
        S3EvidenceBlobStorageAdapter adapter = new S3EvidenceBlobStorageAdapter(s3Client, "sigesa-evidences");
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);

        adapter.restore("evidences/id/v1/guia.pdf", "%PDF".getBytes());

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("sigesa-evidences", captor.getValue().bucket());
        assertEquals("evidences/id/v1/guia.pdf", captor.getValue().key());
    }

    @Test
    @DisplayName("delete: DELETE object por storageKey")
    void delete_removesObject() {
        S3EvidenceBlobStorageAdapter adapter = new S3EvidenceBlobStorageAdapter(s3Client, "sigesa-evidences");
        adapter.delete("evidences/id/v1/doc.pdf");
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals("evidences/id/v1/doc.pdf", captor.getValue().key());
    }

    @Test
    @DisplayName("retrieve: GET object bytes")
    void retrieve_returnsBytes() {
        S3EvidenceBlobStorageAdapter adapter = new S3EvidenceBlobStorageAdapter(s3Client, "sigesa-evidences");
        var responseBytes = software.amazon.awssdk.core.ResponseBytes.fromByteArray(
                software.amazon.awssdk.services.s3.model.GetObjectResponse.builder().build(),
                "hello".getBytes());
        when(s3Client.getObjectAsBytes(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class)))
                .thenReturn(responseBytes);

        var found = adapter.retrieve("evidences/id/v1/doc.pdf");
        assertEquals("hello", new String(found.orElseThrow()));
    }
}
