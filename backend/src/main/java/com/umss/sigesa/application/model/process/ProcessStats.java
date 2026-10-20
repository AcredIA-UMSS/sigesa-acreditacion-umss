package com.umss.sigesa.application.model.process;

import java.util.Map;

public record ProcessStats(
        int total,
        Map<String, Integer> byStatus,
        Map<String, Integer> byOperationalMode,
        int frozenCount
) {
}
