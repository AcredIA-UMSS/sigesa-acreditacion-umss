package com.umss.sigesa.domain.model;

import java.util.ArrayList;
import java.util.List;

public final class ExecutiveSemaphoreAssembler {

    private ExecutiveSemaphoreAssembler() {
    }

    public static ExecutiveKpiSection assemble(List<CareerSemaphoreInput> careers) {
        List<ProgramTrafficLight> lights = new ArrayList<>();
        double progressSum = 0.0;
        int withData = 0;
        int alerts = 0;
        int pendingSum = 0;

        for (CareerSemaphoreInput career : careers) {
            String status = ExecutiveSemaphoreRules.evaluate(
                    career.totalIndicators(),
                    career.progressPercentage(),
                    career.pendingObservations(),
                    career.overdueObservations());
            if (!ExecutiveSemaphoreRules.SIN_DATO.equals(status)) {
                withData++;
                progressSum += career.progressPercentage();
            }
            if (ExecutiveSemaphoreRules.ROJO.equals(status) || ExecutiveSemaphoreRules.AMARILLO.equals(status)) {
                alerts++;
            }
            pendingSum += career.pendingObservations();
            Double completion = career.totalIndicators() <= 0 ? null : career.progressPercentage();
            lights.add(new ProgramTrafficLight(
                    career.programId(),
                    career.name(),
                    status,
                    career.pendingObservations(),
                    career.faculty(),
                    completion));
        }

        double average = withData == 0 ? 0.0 : Math.round((progressSum / withData) * 100.0) / 100.0;
        return new ExecutiveKpiSection(withData, average, pendingSum, alerts, lights);
    }
}
