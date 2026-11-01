package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StageGateEvaluationResponseDto {
    private boolean pass;
    private List<String> failedRules;
    private String summary;
}
