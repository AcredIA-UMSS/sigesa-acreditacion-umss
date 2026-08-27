# Glosario FSD — SIGESA / AcredIA

## Control de versión

| Campo | Valor |
|-------|-------|
| **Versión** | v1.1 (modelo subfase-centrado) |
| **Timestamp** | `2026-08-27T20:00:00-04:00` |
| **Glosario canónico del repo** | [`context/03_domain_glossary.md`](../../context/03_domain_glossary.md) |
| **Estado máquina subfase** | [`diagramas/FSD-UC-006_008_009_estados_subfase.mmd`](diagramas/FSD-UC-006_008_009_estados_subfase.mmd) |

> Vista **funcional** para lectores del FSD. Desde v1.1 el modelo operativo es **Proceso → Fase → Subfase → Evidencia**. Las taxonomías legacy (Dimensión, Criterio, Indicador) quedan **fuera de alcance** del piloto v1.0.

---

## 1. Actores

| Símbolo | Nombre (ES) | Código (EN) | Responsabilidad resumida |
|---------|-------------|-------------|--------------------------|
| [CC] | Coordinador de Carrera | `ProgramCoordinator` | Carga y subsana Evidencia de su carrera |
| [TD] | Técnico DUEA | `DueaTechnician` | Valida, aprueba/rechaza **Subfases**; bandeja global |
| [JD] | Jefatura DUEA | `DueaAdministrator` | Configuración, semáforo, reportes, publicación |
| [P] | Público | `Public` | Consulta portal sin login; solo contenido publicado |
| [EE] | Evaluador externo | `ExternalEvaluator` | Revisión documental **solo lectura** de la carrera asignada (FSD-UC-019); sin carga ni dictamen |

---

## 2. Jerarquía operativa (v1.1)

| Término (ES) | EN | Definición FSD |
|--------------|-----|----------------|
| Proceso | `AccreditationProcess` | Ciclo CEUB o ARCU-SUR de una carrera en una gestión |
| Fase | `Phase` | Etapa mayor del proceso (autoevaluación, subsanación, etc.) |
| Subfase | `Subphase` | Unidad verificable con requisitos, evidencias y workflow de revisión |
| Evidencia | `Evidence` | Prueba documental normativa versionada, **siempre** ligada a una Subfase |

**Cadena obligatoria:** Proceso → Fase → Subfase → Evidencia.

**Prohibido:** "Etapa" o "Step" para Fase; "File" para Evidencia en especificaciones.

**Fuera de alcance v1.0:** Dimensión, Criterio, Indicador (taxonomía normativa legacy no utilizada en el piloto).

---

## 3. Estados de la Subfase

| Estado | Significado |
|--------|-------------|
| `PENDIENTE` | Sin Evidencia cargada |
| `SUBIDO` | Evidencia en revisión [TD] |
| `OBSERVADO` | Rechazada; observación OPEN activa |
| `SUBSANADO` | Nueva versión enviada tras observación |
| `APROBADO` | Validación [TD] completa |

> El estado se deriva del workflow de la subfase (evidencias + observaciones + transiciones TD). No es editable desde el cliente.

---

## 4. Modalidades y plantillas

| Término | Valor |
|---------|-------|
| CEUB | Acreditación nacional (Bolivia) |
| ARCU-SUR | Acreditación regional Sur |
| Plantilla | `AccreditationTemplate` versionada; activada por [JD]; clona **Fase → Subfase** |

---

## 5. Identificadores de especificación

| Prefijo | Documento |
|---------|-----------|
| `FSD-UC-*` | Caso de uso — [`uc/`](uc/) |
| `FSD-BR-*` | Regla de negocio — [`reglas_negocio.md`](reglas_negocio.md) |
| `PRD-US-*` | User story — [`03_prd/PRD.md`](03_prd/PRD.md) |
| `API-*` | Endpoint lógico — [`api_contracts.md`](api_contracts.md) |
| `MOD-*` | Boundary funcional — AUTH, PROCESS, EVIDENCE, WORKFLOW, DASH, NOTIFY, REPORT, PUBLIC, AUDIT, ASSISTANT |

---

## 6. Códigos de error API (dominio)

| Código | Significado |
|--------|-------------|
| `EVIDENCE_IMMUTABLE` | Intento de borrar Evidencia aprobada |
| `EVIDENCE_UNCLASSIFIED` | Carga sin `subphaseId` o descripción/archivo |
| `JUSTIFICATION_REQUIRED` | Rechazo de subfase sin texto suficiente |
| `FASE_CIERRE_BLOQUEADO` | Cierre con subfases pendientes |
| `SUBPHASE_HAS_EVIDENCE` | Eliminación de subfase con evidencias/workflow |
| `PROCESS_ALREADY_ACTIVE` | Segundo proceso activo misma carrera/tipo |
| `FORBIDDEN_ROLE` | Rol no autorizado para la transición |
| `FORBIDDEN_SCOPE` | [CC] accede a otra carrera |
| `INVALID_EMAIL_DOMAIN` | Email no @umss.edu.bo |

---

## 7. Reglas estrictas para IA

1. **PROHIBIDO** usar "File" o "archivo" genérico cuando el contexto es normativo → usar **Evidencia** / `Evidence`.
2. **PROHIBIDO** introducir Dimensión, Criterio o Indicador en nuevas especificaciones del piloto v1.0.
3. **OBLIGATORIO** modelar carga, revisión y cierre sobre **Subfase**, no sobre entidades taxonómicas legacy.
4. **OBLIGATORIO** respetar la máquina de estados de subfase; no exponer `status` editable desde el cliente.

---

## 8. Referencias cruzadas

| Artefacto | Ruta |
|-----------|------|
| Modelo datos (funcional) | [`modelo_datos.md`](modelo_datos.md) |
| FSD maestro | [`FSD.md`](FSD.md) |
| Baseline histórico (no editar) | [`docs/baseline/04_fsd/`](../baseline/04_fsd/) |

---

## Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| v1.1 | 2026-08-27 | Pivot a Proceso→Fase→Subfase→Evidencia; retiro Dimensión/Criterio/Indicador del alcance v1.0 |
| Dorada v1.0 | 2026-05-16 | Glosario FSD derivado del canónico + términos técnicos API/estados |
