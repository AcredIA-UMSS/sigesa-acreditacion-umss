---
id: DD-UC-008
titulo: "Rechazar Indicador"
producto: "SIGESA / AcredIA"
grupo: "G2"
fsd_uc:
  - "FSD-UC-008"
prd_refs:
  - "PRD-REQ-008"
prompts:
  - "PR-IMPL-008"
release: "v1.0"
status: borrador
fecha: "19/08/2026"
autores:
  - "Tech Lead"
---

# Design Doc DD-UC-008 — Rechazar Indicador

## 1. Objetivo y contexto
- **Qué resuelve este feature**: Permite a un Director Técnico [TD] rechazar un indicador que esté en estado `SUBIDO` o `SUBSANADO` registrando una justificación formal de al menos 20 caracteres, lo que crea una observación e indica al Coordinador [CC] que debe subsanar la evidencia.
- **Caso(s) de uso del FSD**: `FSD-UC-008` (Rechazar Indicador), [Ver detalle](docs/product/uc/FSD-UC-008.md).

## 2. Diseño
- **Endpoint**: `POST /api/v1/indicators/{indicatorId}/reject`
- **Body**: `{ "justification": "..." }`
- **Unidad de Evaluación**: La revisión y rechazo se aplica al **Indicador como unidad completa**, considerando todo su conjunto de evidencias adjuntas. El dictamen se confirma mediante una ventana Pop-up que requiere una justificación formal.
- **Enrutamiento Híbrido de Filtrado (`filter_indicators`)**:
  - Permite a los evaluadores [TD] / [JD] filtrar los indicadores pendientes mediante enrutamiento híbrido:
    - **Escenario 1 (SQL directo):** Filtro por ID o estado explícito directamente en PostgreSQL sin invocar IA.
    - **Escenario 2 (AI Tool Calling):** Con AI Toggle activado, el asistente decodifica la consulta semántica e invoca `filter_indicators`.
    - **Escenario 3 (Fuera de Alcance):** Retorna rechazo inmediato.
    - **Escenario 4 (AI Toggle OFF):** Retorna `null` / lista vacía por defecto si no es Escenario 1.
- **Lógica**:
  1. Validar rol TD o JD.
  2. Verificar estado actual del indicador (SUBIDO, SUBSANADO o PENDIENTE).
  3. Validar justificación >= 15 caracteres en el modal emergente.
  4. Persistir `ObservationEntity` con estado `PENDIENTE_SUBSANACION`.
  5. Insertar transición a `OBSERVADO` en `indicator_state_history`.
  6. Encolar notificación al CC.

