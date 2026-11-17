package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.domain.model.AuditLogEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponseDto {

    private UUID id;
    private UUID actorId;
    private String actorEmail;
    private String actorRole;
    private String action;
    private String entityType;
    private String entityId;
    private String payloadDiff;
    private String ipAddress;
    private LocalDateTime createdAt;

    public static AuditLogResponseDto from(AuditLogEntry entry) {
        return AuditLogResponseDto.builder()
                .id(entry.getId())
                .actorId(entry.getActorId())
                .actorEmail(entry.getActorEmail())
                .actorRole(entry.getActorRole())
                .action(entry.getAction())
                .entityType(entry.getEntityType())
                .entityId(entry.getEntityId())
                .payloadDiff(entry.getPayloadDiff())
                .ipAddress(entry.getIpAddress())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
