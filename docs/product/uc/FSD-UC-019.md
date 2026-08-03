---
id: FSD-UC-019
nombre: Revisión documental evaluador externo [EE]
estado: En Curso
release: v1.1
actor_principal: "[EE]"
trazabilidad_prd: PRD-US-026
modulo: MOD-REVIEW
reglas: FSD-BR-09, FSD-BR-19
ultima_actualizacion: "2026-08-03"
design_doc: DD-UC-019
pr_impl: PR-IMPL-014
---

# FSD-UC-019 — Revisión documental evaluador externo [EE]

## Contexto

| Campo | Valor |
|-------|-------|
| **Design Doc** | [`DD-UC-019`](../../design/DD-UC-019.md) |
| **Prompt impl** | [`PR-IMPL-014`](../../prompts/impl/PR-IMPL-014.md) |
| **Trazabilidad** | PRD-REQ-029 · PRD-US-026 · BRD-REQ-001 · MRD-N-09 |
| **Precondiciones** | Cuenta [EE] activa; carrera asignada vía `user_program_assignment` |
| **Pantalla** | `/dashboard` (vista solo lectura) |

## Flujo principal

1. [JD] registra usuario con rol [EE] y carrera asignada (FSD-UC-002).
2. [EE] inicia sesión con correo `@umss.edu.bo` (FSD-BR-12).
3. Sistema emite JWT con `role=EE` y `programScope[]` de la carrera asignada.
4. [EE] accede al panel de control de la carrera asignada: KPIs, fases, observaciones e historial documental.
5. [EE] consulta detalle de observaciones (`GET /dashboards/coordinator/details`) acotado a su carrera.
6. Sistema registra accesos sensibles en bitácora (UC-017, stub v1.0).

## Excepciones y flujos alternos

| ID | Condición | Comportamiento |
|----|-----------|----------------|
| A1 | [EE] sin carrera asignada | `403 ACCESS_DENIED`; sin datos de otras carreras |
| A2 | [EE] intenta carga/subsanación de Evidencia | `403 FORBIDDEN_ROLE` (FSD-BR-19) |
| A3 | [EE] intenta aprobar/rechazar Indicador | `403 FORBIDDEN_ROLE` (FSD-BR-04) |
| A4 | [EE] intenta exportar reportes o administrar usuarios | `403 FORBIDDEN_ROLE` |
| A5 | [EE] intenta acceder a otra carrera | `403` / lista vacía por scope (FSD-BR-09) |

## Postcondiciones

[EE] visualiza documentación de acreditación de **una sola carrera** asignada, sin mutaciones de estado ni evidencias.

## Datos

| Dirección | Campos |
|-----------|--------|
| Entrada (login) | `email`, `password` |
| Salida (JWT) | `accessToken`, `role=EE`, `programScope[]` |
| Lectura dashboard | KPIs coordinador reutilizados en modo solo lectura |

## Diagramas

- [Contexto C4](../diagramas/diag-06-c4-contexto-sistema.mmd) — actor [EE]
- [Dashboard compuesto](../diagramas/MAR-SEQ-004-dashboard-drilldown.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-026 @FSD-UC-019 @TC-EE-01
Característica: Revisión documental evaluador externo [EE]

  Escenario: Inicio de sesión [EE] con carrera asignada
    Dado un evaluador externo con cuenta activa y carrera asignada
    Cuando inicia sesión con credenciales válidas
    Entonces el sistema crea una sesión autenticada con rol [EE]
    Y redirige al panel de revisión documental de su carrera

  Escenario: Consulta solo lectura de documentación
    Dado un [EE] autenticado con carrera INF-SIS asignada
    Cuando abre el panel de control
    Entonces ve KPIs, fases y observaciones de INF-SIS
    Y no puede cargar ni modificar Evidencias

  Escenario: Bloqueo de mutación por [EE]
    Dado un [EE] autenticado
    Cuando intenta cargar Evidencia en un Indicador
    Entonces el sistema responde 403 FORBIDDEN_ROLE
    Y no altera el estado del Indicador

  Escenario: Aislamiento entre carreras
    Dado un [EE] asignado únicamente a la carrera CEUB
    Cuando consulta el dashboard
    Entonces no ve datos de otras carreras
```
