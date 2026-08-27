package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EvidenceSearchPageResponseDto {
    private List<EvidenceSearchHitResponseDto> items;
    private long total;
    private int page;
    private int size;
}
