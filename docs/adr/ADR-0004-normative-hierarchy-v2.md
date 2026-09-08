# ADR-0004: Jerarquía normativa multinivel (release 2.0.0)

| Campo | Valor |
|-------|-------|
| **ID canónico** | ADR-0004 |
| **Estado** | **Aceptada** |
| **Fecha** | 2026-09-08 |
| **Release** | `2.0.0` |
| **Alcance** | MOD-PROCESS · MOD-EVIDENCE · MOD-WORKFLOW · MOD-DASH |
| **Trazabilidad** | FSD.md §2–§7 · [`glosario.md`](../product/glosario.md) §2 · UC-003, 004, 006–010, 019, 021, 022 |
| **Supersede documental** | Pivot v1.1 (Proceso→Fase→Subfase→Evidencia) — obsoleto desde 2.0.0 |

## Contexto

### Problema

El piloto v1.x modeló la acreditación como **Proceso → Fase → Subfase → Evidencia**. Ese diseño simplificó la implementación inicial (2026-08) pero **no refleja** la taxonomía real de los sistemas evaluadores:

| Sistema | Nivel 1 | Nivel 2 | Nivel 3 | Hoja verificable |
|---------|---------|---------|---------|------------------|
| **CEUB** | Área | Variable | Sub-variable | Indicador |
| **ARCU-SUR** | Dimensión | Componente | Criterio | Indicador |

Institucionalmente, DUEA-UMSS necesita:

1. Plantillas y procesos alineados a la normativa CEUB/ARCU-SUR (no a un cronograma genérico de “fases”).
2. **Ponderación** por indicador para reportes ejecutivos y semáforos.
3. **Código normativo** por indicador para trazabilidad externa (evaluadores, auditorías).
4. Evidencias asociadas al **Indicador**, no a un contenedor intermedio ambiguo (“subfase”).

El código en `main` (2026-09) implementa Fase/Subfase (`phases`, `subphases`, `/subphases/*`). Existen restos de un modelo Indicador legacy (`indicator_id` nullable en `evidence`, tablas de reporte V2) nunca integrados al workflow v1.1.

### Drivers

- Release **2.0.0** declarado por cambio estructural mayor del proyecto.
- FSD vivos actualizados (2026-09-08) con jerarquía N1→N2→N3→Indicador→Evidencia.
- Riesgo de deuda: seguir extendiendo Fase/Subfase aleja el producto de la normativa real.

## Alternativas consideradas

| # | Alternativa | Pros | Contras | Veredicto |
|---|-------------|------|---------|-----------|
| **A** | **Jerarquía completa v2.0** (N1→N2→N3→Indicador) | Alineación normativa; ponderación nativa; extensible | Migración BD/API/UI costosa | **Elegida** |
| **B** | Renombrar Fase→N1 y Subfase→Indicador sin N2/N3 | Migración más rápida | Sigue sin reflejar CEUB/ARCU-SUR; re-trabajo posterior | Rechazada |
| **C** | Mantener Fase/Subfase con metadatos “tipo dimensión” | Cero migración | Confusión semántica; deuda permanente | Rechazada |
| **D** | Dos modelos paralelos (cronograma + taxonomía) | Separa plazos de normativa | Duplicación; sync complejo | Rechazada |

## Decisión

### 1. Modelo de dominio objetivo (v2.0)

```
AccreditationProcess (por carrera + gestión)
  └── EvaluatorModel: CEUB | ARCU-SUR  (desde plantilla)
        └── Level1Node  (Área | Dimensión)
              └── Level2Node  (Variable | Componente)
                    └── Level3Node  (Sub-variable | Criterio)
                          └── Indicator  (code, description, weight, referenceUrl, status)
                                └── Evidence  (archivo / enlace, versionada)
```

**Unidad de workflow:** `Indicator` (estados `PENDIENTE` → `SUBIDO` ↔ `OBSERVADO` / `APROBADO`).

**Unidad de cierre agregado:** `Level1Node` pasa a `COMPLETADO` cuando **todos los indicadores** de su subárbol están `APROBADO` (FSD-UC-010, FSD-BR-07).

**Contenedor operativo:** `AccreditationProcess` instancia el árbol clonado desde `AccreditationTemplate` al crear el proceso (FSD-UC-003). La plantilla define el mismo árbol en entidades `Template*`.

### 2. Obsolescencia v1.x

| Entidad v1.x | Estado en v2.0 |
|--------------|----------------|
| `Phase` / `TemplatePhase` | **Obsoleto** — reemplazado por `Level1Node` |
| `Subphase` / `TemplateSubphase` | **Obsoleto** — reemplazado por árbol N2/N3 + `Indicator` |
| `SubphaseObservation` | **Obsoleto** — reemplazado por `IndicatorObservation` |
| API `/subphases/*` | **Obsoleto** — reemplazado por `/indicators/*` |
| Códigos `FASE_CIERRE_BLOQUEADO`, `SUBPHASE_HAS_EVIDENCE` | **Obsoleto** — `NIVEL1_CIERRE_BLOQUEADO`, `INDICATOR_HAS_EVIDENCE` |

El código legacy permanece en `main` hasta completar la migración; **nuevas features** deben implementarse contra el modelo v2.0.

### 3. Estrategia de migración de datos (fases)

| Fase | Acción | Entregable |
|------|--------|------------|
| **M0 — Documental** | FSD, glosario, ADR-0004, modelo_datos, reglas_negocio | ✅ 2026-09-08 |
| **M1 — Esquema** | Flyway V14 tablas v2; columnas legacy read-only | ✅ DDL V14 |
| **M2 — Migración 1:1** | `phases` → `level1_nodes`; `subphases` → `indicators` bajo N1; N2/N3 **placeholder** auto-generados (“General”) | ✅ Flyway V15 |
| **M3 — API dual** | Endpoints v2 `/indicators/*`; deprecación `/subphases/*` | OpenAPI + Orval |
| **M4 — UI** | Árbol multinivel en `/procesos/{id}` y `/admin/plantillas` | Frontend |
| **M5 — Limpieza** | Drop tablas/columnas legacy tras ventana de compatibilidad | ADR de retiro |

**Regla M2:** si un proceso legacy no tiene N2/N3, el migrador crea nodos placeholder `order=1` para no perder FKs; [JD] puede refinir estructura vía UC-022 post-migración.

### 4. Mapeo legacy → v2.0 (referencia)

| Origen (v1.x) | Destino (v2.0) | Notas |
|---------------|----------------|-------|
| `phases.*` | `level1_nodes.*` | `status` ABIERTA/COMPLETADA se conserva |
| `subphases.name` | `indicators.description` | |
| `subphases.reference_url` | `indicators.reference_url` | |
| `subphases.requirements` | `indicators.description` o campo `guidance` | Unificar en descripción ampliada |
| — | `indicators.code` | Generar `LEG-{subphase_id_prefix}` si falta |
| — | `indicators.weight` | Default `1.0`; normalizar post-migración |
| `subphase_observation` | `indicator_observation` | FK a indicador migrado |
| `evidence.subphase_id` | `evidence.indicator_id` | Reactivar FK NOT NULL en v2 |

### 5. Contratos API objetivo (resumen)

| Operación | Endpoint v2.0 | UC |
|-----------|---------------|-----|
| Cargar evidencia | `POST /api/v1/indicators/{id}/evidences` | UC-004 |
| Rechazar | `POST /api/v1/indicators/{id}/reject` | UC-008 |
| Aprobar | `POST /api/v1/indicators/{id}/approve` | UC-009 |
| Cerrar Nivel 1 | `POST /api/v1/level1-nodes/{id}/complete` | UC-010 |
| Árbol proceso | `GET /api/v1/processes/{id}` → `level1Nodes[]…indicators[]` | UC-019 |

### 6. Nomenclatura UI

La UI **debe** mostrar el alias del sistema evaluador (tabla en [`glosario.md`](../product/glosario.md) §2.2), no solo “Nivel 1/2/3”, salvo en documentación técnica interna.

## Consecuencias

### Positivas

- Alineación con normativa CEUB y ARCU-SUR real.
- Ponderación y código de indicador habilitan reportes (UC-013, UC-014) con semántica correcta.
- Workflow granular por indicador; cierre por Dimensión/Área institucionalmente reconocible.
- Reutilización parcial de tablas/columnas `indicator_*` legacy del esquema inicial.

### Negativas / Riesgos

- **Breaking change** en API y UI; requiere coordinación frontend/backend y ventana de compatibilidad dual.
- Migración M2 con placeholders N2/N3 puede requerir curación manual por [JD].
- Tests, MCP assistant y dashboards deben actualizarse (UC-024 tools aún referencian subfase).
- Diagramas `.mmd` y DD-UC-003/004/008–010/019/021/022 pendientes de actualización.

### Neutras

- `AccreditationProcess`, auth, usuarios, append-only de evidencias y RBAC por carrera **no cambian** de principio.
- H2 en tests puede mantener esquema simplificado hasta Testcontainers (ADR-0002).

## Cumplimiento

| Artefacto | Acción requerida |
|-----------|------------------|
| [`modelo_datos.md`](../product/modelo_datos.md) | Actualizado v2.0 |
| [`reglas_negocio.md`](../product/reglas_negocio.md) | BR-01, 04, 05, 07, 17, 19, 22 actualizados; BR-24, BR-25 añadidos |
| [`DTP.md`](../product/DTP.md) | Sincronizar § modelo y endpoints (siguiente paso) |
| [`api_contracts.md`](../product/api_contracts.md) | Sincronizar (siguiente paso) |
| DD-UC-003, 004, 008–010, 019, 021, 022 | Revisión obligatoria antes de implementar M1 |
| Flyway V14+ | Diseño en PR de implementación M1 |

## Referencias

- [`docs/product/FSD.md`](../product/FSD.md) — LFSD v2.0
- [`docs/product/glosario.md`](../product/glosario.md) — §2 Jerarquía normativa
- Migraciones actuales: `backend/src/main/resources/db/migration/V5__*` … `V13__*` (Fase/Subfase)
