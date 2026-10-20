package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessStats;
import com.umss.sigesa.application.port.in.GetProcessStatsUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.model.OperationalMode;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class GetProcessStatsService implements GetProcessStatsUseCase {

    private static final String DEFAULT_MODE = OperationalMode.ACTIVE.name();

    private final ProcessQueryPort processQueryPort;

    public GetProcessStatsService(ProcessQueryPort processQueryPort) {
        this.processQueryPort = processQueryPort;
    }

    @Override
    public ProcessStats stats(ProcessQueryContext ctx) {
        if (isCareerScoped(ctx.role()) && (ctx.programScope() == null || ctx.programScope().isEmpty())) {
            return new ProcessStats(0, Map.of(), Map.of(), 0);
        }

        List<ProcessQueryPort.ProcessListItem> items = isCareerScoped(ctx.role())
                ? processQueryPort.findSummaryItemsByCareerIds(ctx.programScope())
                : processQueryPort.findAllSummaryItems();

        Map<String, Long> byStatus = items.stream()
                .collect(Collectors.groupingBy(
                        item -> blankToUnknown(item.status()),
                        TreeMap::new,
                        Collectors.counting()));
        Map<String, Long> byOperationalMode = items.stream()
                .collect(Collectors.groupingBy(
                        item -> blankToDefaultMode(item.operationalMode()),
                        TreeMap::new,
                        Collectors.counting()));
        long frozenCount = byOperationalMode.getOrDefault(OperationalMode.AUDIT_READONLY.name(), 0L);

        return new ProcessStats(items.size(), byStatus, byOperationalMode, frozenCount);
    }

    private static boolean isCareerScoped(String role) {
        return "CC".equals(role) || "EE".equals(role) || "CAE_MEMBER".equals(role);
    }

    private static String blankToUnknown(String value) {
        return (value == null || value.isBlank()) ? "UNKNOWN" : value;
    }

    private static String blankToDefaultMode(String value) {
        return (value == null || value.isBlank()) ? DEFAULT_MODE : value;
    }
}
