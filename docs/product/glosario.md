# Glosario FSD — SIGESA / AcredIA

## Control de versión

| Campo | Valor |
|-------|-------|
| **Versión** | v2.0 (jerarquía normativa multinivel) |
| **Release** | `2.0.0` |
| **Timestamp** | `2026-09-08T00:00:00-04:00` |
| **Glosario canónico del repo** | [`context/03_domain_glossary.md`](../../context/03_domain_glossary.md) |
| **Estado máquina indicador** | [`diagramas/FSD-UC-006_008_009_estados_indicador.mmd`](diagramas/FSD-UC-006_008_009_estados_indicador.mmd) *(renombrar desde `estados_subfase` en migración v2)* |

> Vista **funcional** para lectores del FSD. Desde **release 2.0.0** el modelo operativo es la **jerarquía normativa CEUB/ARCU-SUR** con evidencias en el **Indicador**. El modelo v1.x **Proceso → Fase → Subfase → Evidencia** queda **obsoleto** (implementación legacy hasta migración de código).

---

## 1. Actores

| Símbolo | Nombre (ES) | Código (EN) | Responsabilidad resumida |
|---------|-------------|-------------|--------------------------|
| [CC] | Coordinador de Carrera | `ProgramCoordinator` | Carga y subsana Evidencia de su carrera |
| [TD] | Técnico DUEA | `DueaTechnician` | Valida, aprueba/rechaza **Indicadores**; bandeja global |
| [JD] | Jefatura DUEA | `DueaAdministrator` | Configuración, semáforo, reportes, publicación |
| [P] | Público | `Public` | Consulta portal sin login; solo contenido publicado |
| [EE] | Evaluador externo | `ExternalEvaluator` | Revisión documental **solo lectura** de la carrera asignada (FSD-UC-019); sin carga ni dictamen |

---

## 2. Jerarquía normativa (v2.0)

### 2.1 Árbol canónico

```
[Modelo / Sistema Evaluador]  (CEUB | ARCU-SUR)
       │
       └── [Nivel 1]
              │
              └── [Nivel 2]
                     │
                     └── [Nivel 3]
                            │
                            └── [Indicador]  (código, descripción, ponderación)
                                   │
                                   └── [Evidencias asociadas]  (archivos / enlaces)
```

**Contenedor de ejecución:** un `AccreditationProcess` (Proceso de acreditación de una carrera) **instancia** el árbol completo clonado desde una plantilla publicada.

**Cadena obligatoria (v2.0):**

`Proceso → Modelo evaluador → Nivel 1 → Nivel 2 → Nivel 3 → Indicador → Evidencia`

### 2.2 Nomenclatura por sistema evaluador

| Nivel genérico | EN (código) | ARCU-SUR (ES) | CEUB (ES) |
|----------------|-------------|---------------|-----------|
| Raíz | `EvaluatorModel` | Sistema evaluador ARCU-SUR | Sistema evaluador CEUB |
| Nivel 1 | `Level1Node` | Dimensión | Área |
| Nivel 2 | `Level2Node` | Componente | Variable |
| Nivel 3 | `Level3Node` | Criterio | Sub-variable |
| Hoja verificable | `Indicator` | Indicador | Indicador |
| Prueba documental | `Evidence` | Evidencia | Evidencia |

> En especificaciones y UI usar el **nombre del sistema evaluador** cuando el contexto sea CEUB o ARCU-SUR; usar **Nivel 1/2/3** cuando la regla aplique a ambos.

### 2.3 Atributos clave del Indicador

| Atributo | EN | Obl. | Descripción |
|----------|-----|------|-------------|
| Código | `code` | Sí | Identificador normativo único dentro del Nivel 3 |
| Descripción | `description` | Sí | Enunciado del indicador |
| Ponderación | `weight` | Sí | Peso numérico (suma ponderada definida por normativa/plantilla) |
| Orden | `order` | Sí | Orden de presentación dentro del Nivel 3 |
| Enlace de referencia | `referenceUrl` | Cond. | URL HTTPS a guía o documento normativo |

### 2.4 Evidencias asociadas

| Término | EN | Definición FSD |
|---------|-----|----------------|
| Evidencia | `Evidence` | Prueba documental versionada, **siempre** ligada a un **Indicador** |
| Evidencia (enlace) | `EvidenceLink` | URL externa como prueba (alternativa o complemento a archivo) |

**Prohibido:** usar "File" genérico cuando el contexto es normativo → **Evidencia** / `Evidence`.  
**Prohibido:** usar "Etapa" o "Step" para niveles normativos.  
**Obsoleto v1.x:** Fase (`Phase`), Subfase (`Subphase`) — no usar en nuevas especificaciones v2.0.

---

## 3. Estados del Indicador (unidad de workflow)

| Estado | Significado |
|--------|-------------|
| `PENDIENTE` | Sin Evidencia cargada |
| `SUBIDO` | Evidencia en revisión [TD] |
| `OBSERVADO` | Rechazado; observación OPEN activa |
| `SUBSANADO` | Nueva versión enviada tras observación |
| `APROBADO` | Validación [TD] completa |

> El estado se deriva del workflow del **indicador** (evidencias + observaciones + transiciones TD). No es editable desde el cliente.

### 3.1 Cierre de Nivel 1

Un **Nivel 1** (Dimensión/Área) pasa a `COMPLETADO` cuando **todos los Indicadores** de su subárbol están en `APROBADO`. Ver FSD-UC-010.

---

## 4. Modalidades y plantillas

| Término | Valor |
|---------|-------|
| CEUB | Acreditación nacional (Bolivia) |
| ARCU-SUR | Acreditación regional Sur |
| Plantilla | `AccreditationTemplate` versionada; activada por [JD]; clona **Nivel 1 → Nivel 2 → Nivel 3 → Indicador** |
| Proceso | `AccreditationProcess` — instancia operativa del árbol para una carrera y gestión |

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
| `EVIDENCE_UNCLASSIFIED` | Carga sin `indicatorId` o descripción/archivo |
| `JUSTIFICATION_REQUIRED` | Rechazo de indicador sin texto suficiente |
| `NIVEL1_CIERRE_BLOQUEADO` | Cierre de Nivel 1 con indicadores pendientes *(v2; legacy: `FASE_CIERRE_BLOQUEADO`)* |
| `INDICATOR_HAS_EVIDENCE` | Eliminación de indicador con evidencias/workflow *(v2; legacy: `SUBPHASE_HAS_EVIDENCE`)* |
| `PROCESS_ALREADY_ACTIVE` | Segundo proceso activo misma carrera/tipo |
| `FORBIDDEN_ROLE` | Rol no autorizado para la transición |
| `FORBIDDEN_SCOPE` | [CC] accede a otra carrera |
| `INVALID_EMAIL_DOMAIN` | Email no @umss.edu.bo |

---

## 7. Reglas estrictas para IA

1. **PROHIBIDO** usar Fase/Subfase en nuevas especificaciones **v2.0**; usar la jerarquía §2.
2. **OBLIGATORIO** modelar carga, revisión y cierre sobre **Indicador**, no sobre niveles 1–3 directamente.
3. **OBLIGATORIO** respetar la máquina de estados del indicador; no exponer `status` editable desde el cliente.
4. **OBLIGATORIO** distinguir nomenclatura ARCU-SUR vs CEUB en UI y reportes (tabla §2.2).
5. Al referenciar código legacy v1.x, marcar explícitamente **«implementación pendiente de migración»**.

---

## 8. Migración v1.x → v2.0 (referencia)

| Concepto v1.x (obsoleto) | Equivalente v2.0 | Notas |
|--------------------------|------------------|-------|
| Fase | Nivel 1 | Unidad de cierre agregado (UC-010) |
| Subfase | Indicador *(aprox.)* | v1 tenía 2 niveles; v2 expande a 3 niveles + indicador |
| `subphaseId` en evidencia | `indicatorId` | FK objetivo en API v2 |
| Observación de subfase | Observación de indicador | Misma semántica de subsanación |

---

## 9. Referencias cruzadas

| Artefacto | Ruta |
|-----------|------|
| Modelo datos (funcional) | [`modelo_datos.md`](modelo_datos.md) |
| FSD maestro | [`FSD.md`](FSD.md) |
| Baseline histórico (no editar) | [`docs/baseline/04_fsd/`](../baseline/04_fsd/) |

---

## Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| v2.0 | 2026-09-08 | Release 2.0.0: jerarquía Modelo→N1→N2→N3→Indicador→Evidencia; retiro Fase/Subfase del modelo funcional |
| v1.1 | 2026-08-27 | Pivot a Proceso→Fase→Subfase→Evidencia (obsoleto en v2.0) |
| Dorada v1.0 | 2026-05-16 | Glosario FSD derivado del canónico + términos técnicos API/estados |
