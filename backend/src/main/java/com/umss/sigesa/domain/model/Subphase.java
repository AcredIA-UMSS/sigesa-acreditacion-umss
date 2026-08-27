package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.umss.sigesa.domain.model.SubphaseState;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subphase {
    private UUID id;
    private String name;
    private Integer order;
    private String referenceUrl;
    private String description;
    private String requirements;
    private SubphaseState status;
}