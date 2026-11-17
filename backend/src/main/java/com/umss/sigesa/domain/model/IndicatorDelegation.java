package com.umss.sigesa.domain.model;

import com.umss.sigesa.domain.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorDelegation {

    public static final String PERMISSION_EVIDENCE_AND_FODA = "EVIDENCE_AND_FODA";

    private UUID id;
    private UUID processId;
    private UUID userId;
    private UUID level1Id;
    private UUID level2Id;
    private UUID indicatorId;
    private String permission;
    private LocalDateTime createdAt;

    public static IndicatorDelegation create(
            UUID processId,
            UUID userId,
            UUID level1Id,
            UUID level2Id,
            UUID indicatorId,
            LocalDateTime now) {
        int scopes = countNonNull(level1Id) + countNonNull(level2Id) + countNonNull(indicatorId);
        if (scopes != 1) {
            throw new ValidationException("La delegación exige exactamente un alcance (N1, N2 o indicador).");
        }
        return IndicatorDelegation.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .userId(userId)
                .level1Id(level1Id)
                .level2Id(level2Id)
                .indicatorId(indicatorId)
                .permission(PERMISSION_EVIDENCE_AND_FODA)
                .createdAt(now)
                .build();
    }

    public boolean matches(UUID targetIndicatorId, UUID targetLevel2Id, UUID targetLevel1Id) {
        if (indicatorId != null) {
            return indicatorId.equals(targetIndicatorId);
        }
        if (level2Id != null) {
            return level2Id.equals(targetLevel2Id);
        }
        return level1Id != null && level1Id.equals(targetLevel1Id);
    }

    private static int countNonNull(UUID value) {
        return value == null ? 0 : 1;
    }
}
