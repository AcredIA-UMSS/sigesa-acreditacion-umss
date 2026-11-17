package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PendingIndicatorResponseDto {
    private UUID indicatorId;
    private String code;
    private String name;
    private String status;
    private Integer order;
}
