---
id: FSD-UC-042
nombre: Reacreditación y vencimiento de vigencia
estado: Pendiente
release: v1.1
actor_principal: "[JD]"
actores_secundarios: "[TD], [P]"
modulo: MOD-PUBLIC
reglas: FSD-BR-10
design_doc: —
pr_impl: —
ultima_actualizacion: "2026-11-15"
relacionados: FSD-UC-016, FSD-UC-003
---

# FSD-UC-042 — Reacreditación y vencimiento de vigencia

## Contexto

El certificado público (UC-016) expone `validUntil`. No hay aviso a DUEA ni proceso de **reacreditación** cuando la vigencia vence o está por vencer. `AccreditationProcess.priorProcessId` ya está previsto en el modelo (ADR-0005) y no se usa.

| Campo | Valor |
|-------|-------|
| **Pantalla (objetivo)** | `/admin/vigencias` (JD) · aviso en semáforo UC-013 |
| **API (objetivo)** | `GET /api/v1/programs/validity` · `POST /api/v1/processes/{id}/reaccreditation` |
| **No incluye** | Rediseñar el PDF del certificado. Cambiar reglas de publicación. |

## Flujo principal

1. [JD] consulta programas publicados con `validUntil`.
2. El sistema clasifica: `VIGENTE` (> 180 días), `POR_VENCER` (≤ 180 días), `VENCIDO`.
3. Semáforo institucional (UC-013) muestra alerta `POR_VENCER`/`VENCIDO` sin sustituir ROJO/AMARILLO/VERDE de avance.
4. [JD] inicia reacreditación: clona plantilla vigente a un **nuevo** proceso con `priorProcessId` = proceso cerrado/archivado.
5. El portal público (UC-016) sigue mostrando el certificado vigente hasta que [JD] publique el nuevo; un certificado vencido muestra estado `VENCIDO` sin filtrar observaciones internas.

## Excepciones

| Condición | Respuesta |
|-----------|-----------|
| Reacreditar proceso que no está `CLOSED` | `409 PROCESS_NOT_CLOSED` |
| Programa sin certificado publicado | aparece en listado como `SIN_CERTIFICADO`, no como `VENCIDO` |
| [CC] inicia reacreditación | `403` |

## Criterios de aceptación

```gherkin
# language: es
@FSD-UC-042 @TC-REACCRED
Característica: Vigencia y reacreditación

  Escenario: Jefatura ve vencimientos
    Dado programas publicados con validUntil en el pasado y en menos de 180 días
    Cuando [JD] abre vigencias
    Entonces distingue VENCIDO y POR_VENCER

  Escenario: Nuevo proceso enlazado al anterior
    Dado un proceso CLOSED de una carrera acreditada
    Cuando [JD] inicia reacreditación
    Entonces existe un proceso ACTIVE con priorProcessId igual al cerrado
    Y el certificado público anterior no se borra

  Escenario: Coordinador no dispara reacreditación
    Dado un [CC] autenticado
    Cuando invoca POST reaccreditation
    Entonces recibe 403
```
