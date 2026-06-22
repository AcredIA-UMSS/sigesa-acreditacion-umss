# Glosario FSD — SIGESA / AcredIA

## Control de versión

| Campo | Valor |
|-------|-------|
| **Versión** | Dorada v1.0 |
| **Timestamp** | `2026-05-16T18:30:00-04:00` |
| **Glosario canónico del repo** | [`context/03_domain_glossary.md`](../../context/03_domain_glossary.md) |
| **Estado máquina** | `team/alexAlvarez/docs/context/04_state_machine.md` |

> Vista **funcional** para lectores del FSD. Ante conflicto terminológico, prevalece el glosario canónico en `context/03_domain_glossary.md`.

---

## 1. Actores

| Símbolo | Nombre (ES) | Código (EN) | Responsabilidad resumida |
|---------|-------------|-------------|--------------------------|
| [CC] | Coordinador de Carrera | `ProgramCoordinator` | Carga y subsana Evidencia de su carrera |
| [TD] | Técnico DUEA | `DueaTechnician` | Valida, aprueba/rechaza Indicadores; bandeja global |
| [JD] | Jefatura DUEA | `DueaAdministrator` | Configuración, semáforo, reportes, publicación |
| [P] | Público | `Public` | Consulta portal sin login; solo contenido publicado |
| [EE] | Evaluador externo | `ExternalEvaluator` | Rol reservado normativa (futuro); no operativo v1.0 piloto |

---

## 2. Jerarquía normativa

| Término (ES) | EN | Definición FSD |
|--------------|-----|----------------|
| Proceso | `AccreditationProcess` | Ciclo CEUB o ARCU-SUR de una carrera en una gestión |
| Fase | `Phase` | Etapa mayor del proceso (autoevaluación, subsanación, etc.) |
| Dimensión | `EvaluationDimension` | Agrupación del marco de evaluación |
| Criterio | `EvaluationCriterion` | Requisito evaluable al que se vincula la Evidencia |
| Indicador | `Indicator` | Elemento verificable con estados y Evidencia asociada |
| Evidencia | `Evidence` | Prueba documental normativa versionada (**no** usar "archivo" genérico) |

**Cadena obligatoria:** Fase → Dimensión → Criterio → Indicador → Evidencia.

**Prohibido:** "Etapa" o "Step" para Fase; "File" para Evidencia en especificaciones.

---

## 3. Estados del Indicador

| Estado | Significado |
|--------|-------------|
| `PENDIENTE` | Sin Evidencia |
| `SUBIDO` | Evidencia en revisión [TD] |
| `OBSERVADO` | Rechazado; observación activa |
| `SUBSANADO` | Nueva versión enviada |
| `APROBADO` | Validación [TD] completa |

---

## 4. Modalidades y plantillas

| Término | Valor |
|---------|-------|
| CEUB | Acreditación nacional (Bolivia) |
| ARCU-SUR | Acreditación regional Sur |
| Plantilla | `AccreditationTemplate` versionada; activada por [JD] |

---

## 5. Identificadores de especificación

| Prefijo | Documento |
|---------|-----------|
| `FSD-UC-*` | Caso de uso — [`casos_uso.md`](casos_uso.md) |
| `FSD-BR-*` | Regla de negocio — [`reglas_negocio.md`](reglas_negocio.md) |
| `PRD-US-*` | User story — [`docs/03_prd/PRD.md`](../03_prd/PRD.md) |
| `API-*` | Endpoint lógico — [`api_contracts.md`](api_contracts.md) |
| `MOD-*` | Boundary funcional / servicio lógico — AUTH, PROCESS, EVIDENCE, WORKFLOW, DASH, NOTIFY, REPORT, PUBLIC, AUDIT, OPS |

---

## 6. Códigos de error API (dominio)

| Código | Significado |
|--------|-------------|
| `EVIDENCE_IMMUTABLE` | Intento de borrar Evidencia aprobada |
| `EVIDENCE_UNCLASSIFIED` | Carga sin Indicador/Criterio |
| `JUSTIFICATION_REQUIRED` | Rechazo sin texto |
| `FASE_CIERRE_BLOQUEADO` | Cierre con indicadores pendientes |
| `PROCESS_ALREADY_ACTIVE` | Segundo proceso activo misma carrera/tipo |
| `FORBIDDEN_ROLE` | Rol no autorizado para la transición |
| `FORBIDDEN_SCOPE` | [CC] accede a otra carrera |
| `INVALID_EMAIL_DOMAIN` | Email no @umss.edu.bo |

---

## 7. Reglas estrictas para IA

1. **PROHIBIDO** usar "File" o "archivo" genérico cuando el contexto es normativo → usar **Evidencia** / `Evidence`.
2. **OBLIGATORIO** consultar este glosario y `context/03_domain_glossary.md` antes de generar esquemas, APIs o código.
3. **OBLIGATORIO** respetar la máquina de estados; no exponer `status` editable desde el cliente.
4. Término no listado → solicitar definición al usuario antes de modelar.

---

## 8. Referencias cruzadas

| Artefacto | Ruta |
|-----------|------|
| Casos de uso | [`casos_uso.md`](casos_uso.md) |
| Gherkin | [`gherkin.md`](gherkin.md) |
| Modelo datos (funcional) | [`modelo_datos.md`](modelo_datos.md) |
| Modelo datos (físico) | [`docs/05_dti/modelo_datos.md`](../05_dti/modelo_datos.md) |
| FSD maestro | [`FSD.md`](FSD.md) |

---

## Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| Dorada v1.0 | 2026-05-16 | Glosario FSD derivado del canónico + términos técnicos API/estados |
