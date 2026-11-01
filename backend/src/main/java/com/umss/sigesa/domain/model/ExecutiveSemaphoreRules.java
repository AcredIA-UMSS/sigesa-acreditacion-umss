package com.umss.sigesa.domain.model;

/**
 * Reglas del semáforo institucional (FSD-UC-013).
 * Sin indicadores: SIN_DATO. Observación vencida, avance bajo o muchas observaciones: ROJO.
 */
public final class ExecutiveSemaphoreRules {

    public static final String VERDE = "VERDE";
    public static final String AMARILLO = "AMARILLO";
    public static final String ROJO = "ROJO";
    public static final String SIN_DATO = "SIN_DATO";

    private ExecutiveSemaphoreRules() {
    }

    public static String evaluate(
            int totalIndicators,
            double progressPercentage,
            int pendingObservations,
            int overdueObservations) {
        if (totalIndicators <= 0) {
            return SIN_DATO;
        }
        if (overdueObservations > 0 || progressPercentage < 50.0 || pendingObservations > 10) {
            return ROJO;
        }
        if (progressPercentage < 80.0 || pendingObservations > 0) {
            return AMARILLO;
        }
        return VERDE;
    }
}
