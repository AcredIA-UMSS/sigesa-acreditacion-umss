package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAsset {

    private UUID id;
    private UUID processId;
    private String folioCode;
    private String title;
    private String storageKey;
    private String contentHash;
    private boolean institutional;
    private UUID uploadedBy;
    private LocalDateTime createdAt;

    public static DocumentAsset create(
            UUID processId,
            String title,
            String storageKey,
            String contentHash,
            UUID uploadedBy,
            LocalDateTime createdAt) {
        return DocumentAsset.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .folioCode(folioCode(contentHash, createdAt.toLocalDate()))
                .title(title)
                .storageKey(storageKey)
                .contentHash(contentHash)
                .institutional(false)
                .uploadedBy(uploadedBy)
                .createdAt(createdAt)
                .build();
    }

    public static String folioCode(String contentHash, LocalDate date) {
        String ymd = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String hex = contentHash == null ? "" : contentHash.toLowerCase(Locale.ROOT);
        String eight = hex.length() >= 8 ? hex.substring(0, 8) : (hex + "00000000").substring(0, 8);
        return "SIGESA-" + ymd + "-" + eight;
    }
}
