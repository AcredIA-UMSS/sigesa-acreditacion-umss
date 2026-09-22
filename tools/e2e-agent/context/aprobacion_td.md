# Sección: Revisión TD — aprobar indicador / cerrar dimensión

## Rol

- Solo TD ve acciones de revisión (`canReviewEvidence` en detalle de proceso)
- td@umss.edu.bo / TecnicoDemo2026!

## Rutas

- Detalle proceso: `/procesos/{uuid}` — árbol «Estructura del proceso»
- Por indicador: bloque «Revisión técnica del indicador», botones «Rechazar» y «Aprobar»
- Cierre nivel 1: pie de dimensión — «Cerrar dimensión» o «Cerrar nivel 1» + `window.confirm`

## Precondiciones (críticas para E2E)

1. **Aprobar:** indicador con ≥1 evidencia y estado SUBIDO/SUBSANADO (no OBSERVADO abierto). Ejecutar antes caso CC evidencia o usar seed con evidencia.
2. **Cerrar dimensión:** todos los indicadores del subárbol APROBADO — suele ser escenario largo; preferir test separado o marcar como `@slow` / skip hasta pipeline de datos.

## Proceso seed

- Preferir Ingeniería de Sistemas ACTIVE `950e8400-e29b-41d4-a716-446655440020` (backend)
- Alternativa demo ADM-EMP `8d38cabf-02f5-4d62-86e8-4aae588c4f9c` si existe en tu BD local

## Navegación árbol

- Dimensiones nivel 1 suelen ser `getByRole('button', { name: /…/ })` expandibles
- Expandir hasta el indicador objetivo antes de buscar «Aprobar»

## Negativo (caso 1.3 plan)

- Indicador sin evidencias → texto «No hay evidencias cargadas», sin botón «Aprobar»
