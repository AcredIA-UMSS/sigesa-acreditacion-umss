package com.umss.sigesa.application.model.process;

import java.util.Map;

public record ProcessStats(
        long total,
        Map<String, Long> byStatus,
        Map<String, Long> byOperationalMode,
        long frozenCount
) {
}
