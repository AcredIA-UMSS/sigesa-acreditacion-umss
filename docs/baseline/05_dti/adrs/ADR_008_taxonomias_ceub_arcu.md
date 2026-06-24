# ADR_008: Taxonomías CEUB/ARCU-SUR en base de datos

| Campo | Valor |
|-------|-------|
| **Canónico** | [ADR-0008](../../adr/ADR-0008-taxonomies-ceub-arcu-sur-database.md) |
| **Origen equipo** | `team/aylenGonzales/09_dti/adr/ADR-005.md` |

## Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0005` |
| Título | Taxonomías de fases e indicadores CEUB/ARCU-SUR como configuración en BD, no hardcodeadas en código |
| Fecha | 16/05/2026 |
| Autor(es) | Equipo AcredIA |
| Estado | **Aceptada** |
| Alcance | MOD-04 — Gestión de fases y procesos; diferenciador competitivo central del producto |
| Stakeholders consultados | Tech Lead AcredIA · Jefa DUEA · CEUB (normativa oficial consultada) |

---

## 1. Contexto

El diferenciador competitivo central de AcredIA / SIGESA es ser el **único sistema diseñado nativamente para las normativas bolivianas CEUB y ARCU-SUR** (MRD §6.2, BRD §3.2). Esto significa que las fases de acreditación, los criterios de evaluación y los indicadores requeridos por cada organismo acreditador deben estar integrados en el sistema desde el primer día — sin que el usuario deba configurarlos manualmente.

La decisión crítica es **dónde y cómo** se almacenan estas taxonomías normativas:

- El proceso CEUB tiene 5 fases (Autoevaluación → Documentación → Visita de pares → Informe externo → Resolución final) con indicadores específicos por cada tipo de carrera.
- El proceso ARCU-SUR tiene sus propias dimensiones, componentes e indicadores, y solo puede iniciarse si hay acreditación CEUB vigente (RB-01, RBN-13).
- Las normativas **cambian**: el CEUB actualiza sus estándares periódicamente. El riesgo de cambio normativo es medio-alto (RF-05, BRD §16).
- El sistema debe soportar **múltiples versiones** de la normativa (p. ej., la carrera X se acredita bajo CEUB 2021 mientras la carrera Y inicia con CEUB 2025).

**Restricciones relevantes:**
- La arquitectura debe permitir actualizar taxonomías sin redeploy del sistema (BRD §16 riesgo de cambio normativo, FSD RF-05).
- Los coordinadores de carrera tienen nivel técnico bajo; la configuración de fases e indicadores no puede ser responsabilidad de los usuarios finales.
- El sistema debe preconfigurar las fases desde el día 1 (MRD-N-07, PRD-REQ-010).

**Fuerzas en tensión:**
- **Flexibilidad vs. integridad normativa**: si las taxonomías son editables por cualquier usuario, el cumplimiento normativo queda en riesgo; si son inmutables en código, cada cambio del CEUB requiere un redeploy.
- **Time-to-market vs. completitud**: cargar las taxonomías iniciales requiere trabajo de configuración con la DUEA, que puede retrasar el despliegue si no está bien planificado.

---

## 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| **A. Taxonomías en BD (tablas de configuración), editables solo por [JD] con trazabilidad** | Actualización sin redeploy; soporte a múltiples versiones normativas; editable por [JD] con log de auditoría; carga inicial via seed SQL o panel de admin | Requiere diseño cuidadoso del modelo (versión de normativa + relación proceso–versión); riesgo de corrupción si [JD] edita incorrectamente | $0 adicional |
| **B. Taxonomías hardcodeadas en el código fuente (constantes/enums)** | Ningún usuario puede corromper las fases; control total en el repositorio de código | Cada cambio normativo requiere un redeploy del sistema; no soporta múltiples versiones de la normativa en paralelo; elimina el diferenciador "arquitectura modular para actualizaciones normativas" | $0 pero alto costo de mantenimiento |
| **C. Taxonomías en archivos de configuración externos (JSON/YAML en disco)** | Actualización sin redeploy (recargar config); versionables en git | Sin interfaz de administración; requiere acceso SSH para actualizar; sin trazabilidad de quién cambió qué; riesgo de inconsistencia entre configuración y BD | $0 pero complejidad operativa |
| **D. Taxonomías como microservicio de configuración externo** | Separación de responsabilidades; escalable; versionado independiente | Sobredimensionado para el piloto UMSS; agrega latencia de red en cada consulta de fases; infraestructura adicional incompatible con la restricción de stack mínimo | Alto costo de tiempo en v1.0 |

---

## 3. Decisión

> **Elegimos la alternativa A: taxonomías de fases e indicadores CEUB y ARCU-SUR almacenadas en tablas de configuración en PostgreSQL, editables exclusivamente por [JD] con registro obligatorio en LOG_AUDITORIA, y cargadas inicialmente via seed SQL revisado por la DUEA.**

Esta decisión materializa directamente el diferenciador competitivo del producto: las normativas están integradas en la capa de datos desde el día 1, no en el código. Cuando el CEUB actualice sus estándares, la Jefatura DUEA puede actualizar las taxonomías desde el panel de administración sin involucrar al equipo de desarrollo — lo que es un argumento de venta concreto frente a DEVA (UAJMS) y sistemas generalistas (MRD §6.2).

El modelo incluye una entidad `VERSION_NORMATIVA` que permite tener múltiples versiones activas en paralelo: una carrera que inició su proceso bajo CEUB-2021 mantiene sus fases e indicadores originales aunque el sistema ya tenga cargado CEUB-2025 para nuevos procesos.

La restricción de edición solo por [JD] con log de auditoría garantiza la integridad normativa frente al riesgo de corrupción accidental.

---

## 4. Consecuencias

### 4.1 Positivas
- Los cambios normativos del CEUB o ARCU-SUR se implementan desde el panel de administración [JD] sin redeploy ni intervención del equipo de desarrollo.
- El soporte a múltiples versiones de normativa en paralelo protege la integridad de los procesos históricos.
- El seed SQL inicial (revisado con la DUEA y documentado en `db/seeds/ceub_2025.sql` y `arcu_sur_2023.sql`) es la fuente canónica de la normativa cargada en el sistema.
- La ventaja competitiva "normativa nativa" es demostrable técnicamente ante otros usuarios del CEUB (MRD §6.3 ventaja competitiva sostenible).

### 4.2 Negativas / costos
- El modelo de datos es más complejo: requiere la entidad `VERSION_NORMATIVA` y la relación `PROCESO_ACREDITACION.version_normativa_id`.
- La carga inicial del seed SQL requiere trabajo coordinado con la DUEA para validar que las fases e indicadores están correctamente transcritos desde la normativa oficial — estimado: 3–5 días de trabajo conjunto.
- Si [JD] edita incorrectamente una taxonomía activa, puede afectar procesos en curso. Mitigación: el panel de admin muestra un warning si la versión tiene procesos activos asociados y requiere confirmación explícita.

### 4.3 Neutras / observables
- La regla RBN-13 (`una carrera ARCU-SUR solo puede iniciar si tiene acreditación CEUB vigente`) se implementa como validación en la capa de aplicación al crear un proceso, no como constraint de BD — esto facilita el testing y los cambios futuros.
- Las taxonomías iniciales se referencian en el FSD como SA-04: "Las normativas CEUB y ARCU-SUR no sufrirán cambios estructurales durante la implementación de v1.0" — esto da margen para cargar el seed sin preocupación inmediata de obsolescencia.

---

## 5. Impacto en el sistema

- **Código**: `T-06` (FSD) — implementar módulo de gestión de fases con cierre condicional. Se añade la entidad `VERSION_NORMATIVA` al modelo de datos (FSD §6.1). El endpoint `POST /procesos` valida la existencia de la versión normativa seleccionada y la regla RBN-13.
- **Operaciones**: el directorio `db/seeds/` en el repositorio contiene los archivos SQL de carga inicial. El proceso de onboarding con la DUEA incluye una sesión de validación del seed antes del despliegue piloto.
- **Seguridad**: solo el rol [JD] tiene acceso al panel de administración de taxonomías. Las ediciones generan eventos en `LOG_AUDITORIA` (ADR-0002). Los procesos activos no pueden cambiar su `version_normativa_id` una vez iniciados.
- **Equipo**: la sesión de validación del seed con la DUEA requiere disponibilidad de un técnico DUEA y el Tech Lead durante 3–5 días hábiles.
- **Costo**: $0 de infraestructura. ~15–20 horas de trabajo de configuración y validación con la DUEA.

---

## 6. Plan de reversión

**Señales de que la decisión fue incorrecta:**
- [JD] corrompe accidentalmente una taxonomía activa y no hay forma de recuperar la versión anterior desde el log.
- La complejidad del modelo de versiones normativas supera la capacidad del equipo de mantenimiento.

**Costo estimado de revertir:** bajo. Las taxonomías en BD pueden exportarse a JSON/YAML para pasar a la alternativa C, o hardcodearse para la alternativa B. El modelo de datos no cambiaría significativamente — solo se eliminaría la edición en runtime.

**Plan B:** Agregar versionado inmutable de taxonomías con `COPY ON WRITE`: cuando [JD] edita una versión normativa, el sistema crea automáticamente una nueva versión y archiva la anterior en lugar de modificarla in-place.

---

## 7. Validación

- **Funcional**: la Jefa DUEA puede actualizar una fase de la normativa CEUB desde el panel de admin sin intervención técnica, y el cambio no afecta procesos en estado `ACTIVO` iniciados con la versión anterior.
- **Seed validado**: antes del despliegue piloto, la DUEA confirma por escrito que el seed SQL refleja fielmente las fases e indicadores vigentes de CEUB 2025 y ARCU-SUR 2023.
- **RBN-13 verificado**: test de integración que valida que `POST /procesos` con tipo ARCU-SUR falla si la carrera no tiene proceso CEUB en estado `ACREDITADO`.
- **Responsable**: Tech Lead AcredIA (modelo de datos) + Jefa DUEA (validación de contenido normativo).
- **Plazo**: seed validado y cargado en el ambiente piloto Q3 2026, 2 semanas antes del inicio del piloto cerrado.

---

## 8. Referencias

- FSD v1.0 AcredIA/SIGESA — §5 (RBN-13), §4 FSD-UC-003 y FSD-UC-004, SA-04, RF-05, T-06.
- PRD v1.0 — PRD-REQ-010.
- BRD v2.0 — BR-007, RB-01, RB-05, §16 (Riesgo de cambios normativos).
- MRD v1.0 — MRD-N-07, §6.2 Posicionamiento y §6.3 Ventaja competitiva sostenible.
- Normativa CEUB vigente: documentación oficial proporcionada por la DUEA-UMSS.
- Normativa ARCU-SUR/MERCOSUR Educativo: documentación oficial RANA.

---

## 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 16/05/2026 | Equipo AcredIA | Propuesta inicial — decisión derivada de RF-05 (FSD) y diferenciador competitivo MRD §6.2 |
| 2 | 16/05/2026 | Equipo AcredIA | Aceptada tras validación con Jefa DUEA sobre proceso de actualización normativa |