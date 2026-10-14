package com.umss.sigesa.domain.model;

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
public class PrimarySurveyBatch {

    private UUID id;
    private UUID processId;
    private String audience;
    private int responsesCount;
    private UUID documentAssetId;
    private LocalDateTime registeredAt;
}
