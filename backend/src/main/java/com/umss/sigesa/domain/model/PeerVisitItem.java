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
public class PeerVisitItem {

    private UUID id;
    private PeerVisitItemType type;
    private String title;
    private LocalDateTime scheduledAt;
    private String location;
    private String notes;
    private boolean done;
    private int order;
}
