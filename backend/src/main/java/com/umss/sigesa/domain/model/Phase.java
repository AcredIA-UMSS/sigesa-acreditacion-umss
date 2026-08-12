package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Phase {
    private UUID id;
    private String name;
    private Integer order;
    private String description;
    @Builder.Default
    private List<Subphase> subphases = new ArrayList<>();
}
