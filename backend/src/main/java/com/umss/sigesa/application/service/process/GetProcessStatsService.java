package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessStats;
import com.umss.sigesa.application.port.in.GetProcessStatsUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort.ProcessStatsRow;
import com.umss.sigesa.domain.model.OperationalMode;
import com.umss.sigesa.domain.model.ProcessStatus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GetProcessStatsService implements GetProcessStatsUseCase {

    private static final List<String> STATUS_KEYS = List.of(
            ProcessStatus.ACTIVE.name(),
            ProcessStatus.CLOSED.name());
    private static final List<String> MODE_KEYS = List.of(
            OperationalMode.ACTIVE.name(),
            OperationalMode.AUDIT_READONLY.name(),
            OperationalMode.IMPROVEMENT_MONITORING.name());

    private final ProcessQueryPort processQueryPort;

    public GetProcessStatsService(ProcessQueryPort processQueryPort) {
        this.processQueryPort = processQueryPort;
    }

    @Override
    public ProcessStats getStats(ProcessQueryContext ctx) {
        if (isCareerScoped(ctx.role()) && isEmptyScope(ctx.programScope())) {
            return emptyStats();
        }
        List<ProcessStatsRow> rows = isCareerScoped(ctx.role())
                ? processQueryPort.findStatsRowsByCareerIds(ctx.programScope())
                : processQueryPort.findAllStatsRows();
        return aggregate(rows);
    }

    private static ProcessStats aggregate(List<ProcessStatsRow> rows) {
        Map<String, Integer> byStatus = zeroCounts(STATUS_KEYS);
        Map<String, Integer> byOperationalMode = zeroCounts(MODE_KEYS);
        for (ProcessStatsRow row : rows) {
            if (!countsTowardPopulation(row, byStatus, byOperationalMode)) {
                continue;
            }
            byStatus.put(row.status(), byStatus.get(row.status()) + 1);
            byOperationalMode.put(row.operationalMode(), byOperationalMode.get(row.operationalMode()) + 1);
        }
        int total = byStatus.values().stream().mapToInt(Integer::intValue).sum();
        int frozenCount = byOperationalMode.get(OperationalMode.AUDIT_READONLY.name());
        return new ProcessStats(
                total,
                Collections.unmodifiableMap(byStatus),
                Collections.unmodifiableMap(byOperationalMode),
                frozenCount);
    }

    private static boolean countsTowardPopulation(ProcessStatsRow row,
                                                  Map<String, Integer> byStatus,
                                                  Map<String, Integer> byOperationalMode) {
        if (row == null || row.status() == null || ProcessStatus.ARCHIVED.name().equals(row.status())) {
            return false;
        }
        return byStatus.containsKey(row.status()) && byOperationalMode.containsKey(row.operationalMode());
    }

    private static ProcessStats emptyStats() {
        return new ProcessStats(0, fixedCounts(STATUS_KEYS), fixedCounts(MODE_KEYS), 0);
    }

    private static Map<String, Integer> fixedCounts(List<String> keys) {
        return Collections.unmodifiableMap(zeroCounts(keys));
    }

    private static Map<String, Integer> zeroCounts(List<String> keys) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String key : keys) {
            counts.put(key, 0);
        }
        return counts;
    }

    private static boolean isCareerScoped(String role) {
        return "CC".equals(role) || "EE".equals(role) || "CAE_MEMBER".equals(role);
    }

    private static boolean isEmptyScope(List<UUID> programScope) {
        return programScope == null || programScope.isEmpty();
    }
}
