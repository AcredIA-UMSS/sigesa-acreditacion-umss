package com.umss.sigesa.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ProcessStatsResponseDto {
    private long total;
    private Map<String, Long> byStatus;
    private Map<String, Long> byOperationalMode;
    private long frozenCount;
}
