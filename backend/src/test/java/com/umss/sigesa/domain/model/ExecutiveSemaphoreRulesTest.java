package com.umss.sigesa.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExecutiveSemaphoreRulesTest {

    @Test
    @DisplayName("Carrera sin indicadores queda sin dato")
    void sinIndicadoresEsSinDato() {
        assertEquals(ExecutiveSemaphoreRules.SIN_DATO, ExecutiveSemaphoreRules.evaluate(0, 0, 0, 0));
    }

    @Test
    @DisplayName("Indicador crítico vencido deja la carrera en rojo aunque el avance sea alto")
    void vencidoEsRojo() {
        assertEquals(ExecutiveSemaphoreRules.ROJO, ExecutiveSemaphoreRules.evaluate(10, 95.0, 1, 1));
    }

    @Test
    @DisplayName("Avance bajo o muchas observaciones abiertas es rojo")
    void avanceBajoEsRojo() {
        assertEquals(ExecutiveSemaphoreRules.ROJO, ExecutiveSemaphoreRules.evaluate(10, 40.0, 0, 0));
        assertEquals(ExecutiveSemaphoreRules.ROJO, ExecutiveSemaphoreRules.evaluate(20, 90.0, 11, 0));
    }

    @Test
    @DisplayName("Avance intermedio u observaciones abiertas es amarillo")
    void avanceIntermedioEsAmarillo() {
        assertEquals(ExecutiveSemaphoreRules.AMARILLO, ExecutiveSemaphoreRules.evaluate(10, 68.5, 0, 0));
        assertEquals(ExecutiveSemaphoreRules.AMARILLO, ExecutiveSemaphoreRules.evaluate(10, 90.0, 2, 0));
    }

    @Test
    @DisplayName("Avance alto y sin observaciones es verde")
    void avanceAltoEsVerde() {
        assertEquals(ExecutiveSemaphoreRules.VERDE, ExecutiveSemaphoreRules.evaluate(10, 80.0, 0, 0));
    }

    @Test
    @DisplayName("El armado agrupa colores y deja en gris a la carrera sin datos")
    void assemblerSeparaSinDato() {
        ExecutiveKpiSection section = ExecutiveSemaphoreAssembler.assemble(List.of(
                new CareerSemaphoreInput("a", "Sistemas", "FCT", 10, 40.0, 1, 2),
                new CareerSemaphoreInput("b", "Civil", "FCT", 0, 0, 0, 0),
                new CareerSemaphoreInput("c", "Medicina", "FCMM", 8, 90.0, 0, 0)));

        assertEquals(2, section.totalPrograms());
        assertEquals(1, section.alertPrograms());
        assertEquals(1, section.criticalObservations());
        assertEquals(ExecutiveSemaphoreRules.ROJO, section.programTrafficLights().get(0).status());
        assertEquals("FCT", section.programTrafficLights().get(0).faculty());
        assertEquals(ExecutiveSemaphoreRules.SIN_DATO, section.programTrafficLights().get(1).status());
        assertEquals(ExecutiveSemaphoreRules.VERDE, section.programTrafficLights().get(2).status());
    }
}
