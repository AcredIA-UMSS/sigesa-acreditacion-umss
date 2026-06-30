---
producto: "SIGESA / AcredIA"
documento: LFSD ⚡ (Lean Functional Specification Document)
version: v1.0 (Vivo)
fecha_inicio_implementacion: "2026-05-16"
status: vivo
audiencia: dual (humanos + agentes IA)
baseline_ref: "docs/baseline/04_fsd/FSD.md"
ultima_actualizacion: "2026-06-23"
---

# Especificación Funcional Viva (LFSD ⚡) — SIGESA

> **Qué es:** Índice ágil de requerimientos funcionales durante la implementación.  
> **Atención agentes IA:** Para implementar un feature, **no** adivinen el flujo. Naveguen al archivo del Caso de Uso (`FSD-UC-NNN`) enlazado en la tabla inferior.  
> **Baseline congelado (no editar):** [`docs/baseline/04_fsd/FSD.md`](../baseline/04_fsd/FSD.md) · Dorada v1.0 · 2026-05-16

---

## 1. Referencias globales invariables

| Referencia | Ruta viva |
|------------|-----------|
| Glosario del dominio | [`glosario.md`](glosario.md) · `context/03_domain_glossary.md` (si existe en repo) |
| Reglas de negocio globales | [`reglas_negocio.md`](reglas_negocio.md) |
| Catálogo API completo | [`api_contracts.md`](api_contracts.md) |
| DTP (contrato técnico vivo) | [`DTP.md`](DTP.md) |
| Máquina de estados Indicador | `PENDIENTE` → `SUBIDO` ↔ `OBSERVADO` / `APROBADO` · [`diagramas/D-STA-001-indicador.mmd`](diagramas/D-STA-001-indicador.mmd) · [`diagramas/FSD-UC-006_008_009_estados_indicador.mmd`](diagramas/FSD-UC-006_008_009_estados_indicador.mmd) |
| Modelo documental AI-SDLC | [`../MODELO_DOCUMENTAL_IMPLEMENTACION.md`](../MODELO_DOCUMENTAL_IMPLEMENTACION.md) |

**Artefactos descompuestos (solo lectura / histórico):** [`docs/baseline/04_fsd/`](../baseline/04_fsd/) — fuente de extracción del baseline.

---

## 2. Índice de casos de uso (módulos funcionales)

| ID | Nombre | Actor | Release | Estado | FSD | Design Doc | PR-IMPL |
|----|--------|-------|---------|--------|-----|------------|---------|
| `FSD-UC-001` | Autenticación y sesión | Interno | v1.0 | **Hecho** | [Ver detalle](uc/FSD-UC-001.md) | [`DD-UC-001`](../design/DD-UC-001.md) | [`PR-IMPL-001`](../prompts/impl/PR-IMPL-001.md) |
| `FSD-UC-002` | Gestión de usuarios [JD] | [JD] | v1.0 | **Hecho** | [Ver detalle](uc/FSD-UC-002.md) | [`DD-UC-002`](../design/DD-UC-002.md) | [`PR-IMPL-002`](../prompts/impl/PR-IMPL-002.md) |
| `FSD-UC-003` | Plantillas y Proceso CEUB/ARCU-SUR | [JD] | v1.0 | **En Curso** | [Ver detalle](uc/FSD-UC-003.md) | [`DD-UC-003`](../design/DD-UC-003.md) | [`PR-IMPL-003`](../prompts/impl/PR-IMPL-003.md) |
| `FSD-UC-004` | Cargar Evidencia | [CC] | v1.0 | **En Curso** | [Ver detalle](uc/FSD-UC-004.md) | [`DD-UC-004`](../design/DD-UC-004.md) | [`PR-IMPL-006`](../prompts/impl/PR-IMPL-006.md) |
| `FSD-UC-005` | Versionado y bloqueo de borrado | [CC], [TD] | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-005.md) | — | — |
| `FSD-UC-006` | Subsanar Evidencia | [CC] | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-006.md) | — | — |
| `FSD-UC-007` | Buscar Evidencia | [CC], [TD] | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-007.md) | — | — |
| `FSD-UC-008` | Rechazar Indicador | [TD] | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-008.md) | — | — |
| `FSD-UC-009` | Aprobar Indicador | [TD] | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-009.md) | — | — |
| `FSD-UC-010` | Avanzar/cerrar Fase | [TD] | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-010.md) | — | — |
| `FSD-UC-011` | Dashboard [CC] y observaciones | [CC] | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-011.md) | — | — |
| `FSD-UC-012` | Bandeja auditoría [TD] | [TD] | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-012.md) | — | — |
| `FSD-UC-013` | Panel semáforo [JD] | [JD] | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-013.md) | — | — |
| `FSD-UC-014` | Reporte ejecutivo PDF | [JD] | v1.0 | **En Curso** | [Ver detalle](uc/FSD-UC-014.md) | [`DD-UC-014`](../design/DD-UC-014.md) | [`PR-IMPL-005`](../prompts/impl/PR-IMPL-005.md) |
| `FSD-UC-015` | Notificaciones y alertas | Sistema | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-015.md) | — | — |
| `FSD-UC-016` | Portal público | [P] | v1.1 | Pendiente | [Ver detalle](uc/FSD-UC-016.md) | — | — |
| `FSD-UC-017` | Bitácora de auditoría | [JD] | v1.0 | Pendiente | [Ver detalle](uc/FSD-UC-017.md) | — | — |
| `FSD-UC-018` | Importación masiva | [CC] | v1.1 | Pendiente | [Ver detalle](uc/FSD-UC-018.md) | — | — |

**Leyenda de estado:** `Pendiente` · `En Curso` · `Implementado` · `Verificado` · `Obsoleto`

---

## 3. Reglas críticas del sistema (hard constraints)

1. **Append-Only:** Prohibido el borrado físico (`DELETE`) de Evidencias aprobadas. Intentos → `409 EVIDENCE_IMMUTABLE` + `AUDIT_DELETE_DENIED` (FSD-BR-02, FSD-BR-15).
2. **Cierre de Fase:** `COUNT(indicadores_fase) == COUNT(indicadores WHERE estado = APROBADO)`, excluyendo indicadores marcados explícitamente como **N/A**. Si no se cumple → `409 FASE_CIERRE_BLOQUEADO` (FSD-BR-07).
3. **Aislamiento de rol:** El Coordinador [CC] solo accede a información de **su propia carrera** (FSD-BR-09).
4. **Separación de dictamen:** Solo [TD] aprueba/rechaza Indicador; dictamen final institucional solo humano (FSD-BR-04, FSD-BR-11).
5. **Correo institucional:** Cuentas solo `@umss.edu.bo` (FSD-BR-12).

---

## 4. Actores (referencia rápida)

| Actor | Responsabilidad |
|-------|-----------------|
| [CC] | Carga y subsana Evidencia de su carrera |
| [TD] | Revisa, aprueba/rechaza Indicador; cierra Fase |
| [JD] | Usuarios, plantillas, semáforo, PDF, publicación |
| [P] | Consulta pública (solo publicados) |

---

## 5. Trazabilidad viva → implementación

Cadena obligatoria (ver [`MODELO_DOCUMENTAL_IMPLEMENTACION.md`](../MODELO_DOCUMENTAL_IMPLEMENTACION.md)):

`Código → PR-IMPL-NNN → DD-UC-NNN → FSD-UC-NNN → DTP / PRD vivo`

| Task Spec Kit | FSD-UC | Release |
|---------------|--------|---------|
| T-001 Modelo Proceso/Fase/Indicador | UC-003 | v1.0-rc |
| T-002 Auth + RBAC | UC-001, UC-002 | v1.0-rc |
| T-003 Upload + versionado | UC-004, UC-005 | v1.0-rc |
| T-004 State machine Indicador | UC-008, UC-009, UC-010 | v1.0-rc |
| T-005 Observaciones + subsanación | UC-006, UC-008 | v1.0-rc |
| T-006 Búsqueda indexada | UC-007 | v1.0 |
| T-007 Dashboards + semáforo | UC-011, UC-012, UC-013 | v1.0 |
| T-008 Notificaciones outbox | UC-015 | v1.0 |
| T-009 Reporte PDF | UC-014 | v1.0 |
| T-010 Bitácora append-only | UC-017 | v1.0 |
| T-011 Portal público | UC-016 | v1.1 |
| T-012 Importación CSV | UC-018 | v1.1 |

---

## 6. Registro de cambios (capa viva)

| Fecha | Cambio |
|-------|--------|
| 2026-06-15 | Transformación FSD clásico → **LFSD ⚡**; 18 UC atomizados en [`uc/`](uc/) |
| 2026-06-15 | UC-003 marcado **En Curso** (scaffolding Gestión de Fases en código) |
| 2026-06-23 | Separación trazabilidad 1:1: `DD-UC-001`/`DD-UC-002`; prompts `PR-IMPL-001`/`PR-IMPL-002` (histórico `PR-IMPL-004`) |
