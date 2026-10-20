package com.umss.sigesa.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ProcessStatsResponseDto {
    private int total;
    private Map<String, Integer> byStatus;
    private Map<String, Integer> byOperationalMode;
    private int frozenCount;
}
