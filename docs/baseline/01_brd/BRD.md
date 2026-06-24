# Business Requirements Document (BRD) — SIGESA / AcredIA

## Control de versión del documento

| Campo | Valor |
|-------|-------|
| **Versión** | **Dorada v2.2** |
| **Última actualización (timestamp)** | `2026-05-16T16:30:00-04:00` |
| **Resumen de cambios (esta versión)** | Cierre trazabilidad Q-01…Q-04: **BRD-REQ-026** (paneles [CC]/[TD]); piloto auth local + Adapter LDAP v1.1; umbral progreso **5 MB**; responsive lectura Must v1.0 / carga móvil v1.1; §14.3 alcance piloto F2 (plantilla). |
| **Resumen de cambios (Dorada v2.1)** | Aclaración: sistema de automatización de acreditación (no ERP); F5 en dominio acreditación. |
| **Resumen de cambios (Dorada v2.0)** | Auditoría skill+plantilla: 25 REQ, roadmap, subsanación, VoC, decisiones pendientes. |
| **Resumen de cambios (Dorada v1.0)** | Consolidación canónica desde `team/*/docs/`; 12 OBJ, 18 REQ, 14 RB, append-only, trazabilidad inicial. |
| **Estado de auditoría** | **Apto para transición a MRD** — ver §27 Checklist de auditoría documental. |

> **Versión Dorada** — Documento canónico de negocio consolidado desde los aportes del equipo en `team/*/docs/` y alineado a `context/03_domain_glossary.md`.

---

## 0. Metadatos

| Campo | Valor |
|-------|-------|
| Producto | SIGESA / AcredIA — Sistema de gestión y seguimiento de acreditaciones (UMSS) |
| Ámbito | `docs/01_brd/` (canónico) |
| Versión | **Dorada v2.2** |
| Fecha | 16/05/2026 |
| Timestamp última edición | `2026-05-16T15:24:02-04:00` |
| Sponsor de negocio | Jefatura DUEA — Dirección Universitaria de Evaluación y Acreditación, UMSS |
| Stakeholders | Jefatura DUEA · Técnicos DUEA · Coordinadores de Carrera · Decanatos · Rectorado/Vicerrectorado · CEUB · ARCU-SUR · Evaluadores externos · Público |
| Autores | Equipo AcredIA (consolidación documental) |
| Revisores | Docente + 1 grupo par *(pendiente)* |
| Estado | Borrador para validación institucional |
| Glosario de referencia | `context/03_domain_glossary.md` |
| Fuentes consolidadas | `team/borisAngulo/docs/01_brd/BRD_v2.md` · `team/aylenGonzales/01_brd/BRD_v2_aylen.md` · `team/alexAlvarez/docs/01_brd/BRD.md` · `team/Marlene/01_brd/BRD.md` · `docs/BRD_v1.md` |
| Cadena documental | BRD → MRD → PRD → FSD → DTI / NFR |
| Prompts utilizados | Consolidación asistida por IA bajo skill `sigesa-generacion-documentos-negocio` |

---

## 1. Resumen ejecutivo

**Problema:** La UMSS gestiona evaluación y acreditación de carreras (**CEUB**, **ARCU-SUR**) con herramientas dispersas (hojas de cálculo, correo, almacenamiento removible, mensajería informal). La evidencia más fuerte del dolor es la **pérdida de trazabilidad y tiempo** en localizar la versión válida de una **Evidencia** normativa (referencia de campo: **20+ minutos** por sesión de búsqueda) y el **riesgo de incumplimiento de plazos** por falta de visibilidad centralizada.

**Propuesta:** **SIGESA** es un **sistema de automatización** del ciclo de evaluación y acreditación de carreras: centraliza el **Proceso** por carrera y facultad, modela **Fases** normativas y la taxonomía **Dimensión → Criterio → Indicador**, y automatiza carga, validación, alertas, observaciones y reportes de **Evidencia** entre **[CC]**, **[TD]** y **[JD]**, con portal de transparencia para **[P]**. **No es un ERP** ni un reemplazo de sistemas académicos o administrativos de la UMSS.

**Valor esperado (metas de negocio):**

| Dimensión | Meta |
|-----------|------|
| Eficiencia | Localización de Evidencia: de 20+ min a **≤ 2 min** |
| Riesgo | **0** incidentes de pérdida documental por gestión en procesos piloto |
| Gobernanza | Reporte ejecutivo en **≤ 5 min** (P95) |
| Adopción | **≥ 80 %** de actores clave activos a los 3 meses post go-live |
| Trazabilidad | **100 %** de fases activas con cadena Proceso→Indicador→Evidencia |

**Llamada a la acción:** Validar alcance MoSCoW; designar responsable DUEA para datos maestros y normativa; autorizar ventana de piloto; aprobar **canal oficial** de Evidencia mediante acto institucional; medir línea base de KPIs antes del lanzamiento.

---

## 2. Contexto del negocio

- **Organización:** Universidad Mayor de San Simón (UMSS), Cochabamba, Bolivia.
- **Unidad impactada:** Dirección Universitaria de Evaluación y Acreditación (DUEA) y, transversalmente, facultades, carreras y unidades que proveen Evidencias.
- **Procesos afectados:** ciclo de evaluación y acreditación de carreras (autoevaluación, documentación, visita de pares, informe externo, resolución final); seguimiento de observaciones; reporting a autoridades; consulta pública de estado.
- **Estrategia vinculada:** fortalecer calidad académica, transparencia y capacidad de auditoría institucional frente a CEUB/ARCU-SUR; reducir dependencia de conocimiento tácito y canales informales.

### 2.1 Naturaleza del producto (qué es / qué no es)

| SIGESA **es** | SIGESA **no es** |
|---------------|------------------|
| Sistema de **automatización** del proceso de acreditación CEUB/ARCU-SUR | Un **ERP** universitario ni una suite administrativa integral |
| Orquestador de **workflow** documental: **Evidencia**, observaciones, fases, alertas, reportes | Un sustituto de **SIIS**, tesorería, RRHH u otros sistemas de gestión institucional |
| Herramienta de **cumplimiento y trazabilidad** para la DUEA y las carreras | Una plataforma de ranking internacional (QS/THE) ni un data warehouse corporativo |

**Alcance del proyecto:** automatizar tareas repetitivas y propensas a error del ciclo de acreditación (búsqueda, versionado, notificaciones, consolidación de estado). Los datos maestros (facultades, carreras, usuarios) se **parametrizan** en SIGESA; no se pretende gestionar en esta solución la operación académica ni financiera de la universidad.

**Evolución post go-live (F5):** ampliar **automatización dentro del dominio acreditación** (exportaciones, planes de mejora, asistencia IA explicable, portal, certificados — capacidades P2/P3 del BRD). **No** implica convertir SIGESA en ERP ni integrar en tiempo real toda la stack institucional como módulo central.

---

## 3. Problema y oportunidad de negocio

### 3.1 Problema

La acreditación exige Evidencias por **Criterio** e **Indicador**, plazos e hitos coordinados entre muchos actores. Hoy la información vive repartida entre documentos físicos, hojas de cálculo no integradas, correo y canales informales, lo que produce **duplicidad**, **poca trazabilidad** (*amnesia de versiones*) y **decisiones tardías** por ausencia de vista consolidada.

**Capas del dolor (validadas en investigación de campo, feb–mar 2026):**

| Capa | Síntoma | Consecuencia |
|------|---------|--------------|
| Operativa | 20+ min por búsqueda de «versión final»; cargas pesadas sin retroalimentación | Retrabajo pre-auditoría |
| Estratégica | Jefatura sin semáforo confiable; reportes manuales | Decisiones con información incompleta |
| Coordinativa | Sin confirmación de recepción/validación; observaciones dispersas | Incumplimiento de plazos |

### 3.2 Oportunidad

- **Eficiencia:** recuperación estimada de **15–20 h/mes/técnico** (a validar en piloto con registro de tiempos).
- **Riesgo:** menor probabilidad de observaciones por deficiencia documental; carpeta de auditoría exportable.
- **Transparencia:** consulta pública de estado sin fricción administrativa.
- **Diferenciación:** cumplimiento nativo CEUB/ARCU-SUR frente a plataformas globales mal adaptadas al contexto boliviano.

### 3.3 Evidencia de Continuous Discovery

| Elemento | Estado |
|----------|--------|
| Documento Discovery | `docs/discovery/discovery_v0.1.md` — vinculación formal pendiente si no existe |
| Entrevistas | Perfiles [JD], [TD], [CC]; detalle en anexo Discovery |
| Hipótesis validada | Panel de baja curva para jefatura + repositorio versionado para técnicos reduce fricción y mejora cumplimiento de plazos |
| Artefactos M2 UI/UX | Wireframes/journeys — enlazar cuando estén publicados |
| Cadencia Discovery | Semanal o quincenal durante iteración (por acordar con DUEA) |

### 3.4 Voz del Cliente (investigación UX, feb–mar 2026)

> Citas y métricas **no inventadas**: provienen de bitácoras y pruebas referenciadas en `team/aylenGonzales/01_brd/BRD_v2_aylen.md` y `docs/BRD_v1.md`.

| Señal | Evidencia | Implicancia de negocio |
|-------|-----------|------------------------|
| Dolor de búsqueda | «20+ minutos» por sesión localizando la versión final | BRD-OBJ-01, BRD-REQ-020 |
| Amnesia de versiones | Duplicidad y conflicto entre correo/Drive | BRD-REQ-006, BRD-REQ-007 |
| Incertidumbre coordinador | Sin confirmación de recepción/validación | BRD-REQ-008, BRD-REQ-011 |
| Carga cognitiva jefatura | Dependencia de memoria del equipo para saber cumplimiento | BRD-REQ-010, BRD-OBJ-05 |
| Validación prototipo | Core tasks **96,66 %** éxito; CSAT **8,67/10** | BRD-KPI-09, BRD-KPI-10 |
| Mejora UX por perfil | Coordinador y técnico: percepción **2/5 → 5/5** (cualitativo, iteración Hi-Fi) | BRD-OBJ-12, BRD-RSK-09–11 |

---

## 4. Usuarios objetivo / Personas clave

> Nomenclatura estricta según glosario: **[CC]**, **[TD]**, **[JD]**, **[P]**. No usar «Cliente», «Super Administrador» ni «archivo» para la prueba normativa (usar **Evidencia**).

### 4.1 [JD] Jefatura DUEA — Persona principal estratégica

| Atributo | Valor |
|----------|-------|
| Contexto | Supervisa todas las carreras en acreditación; necesita estado unificado |
| Jobs-to-be-done | Ver avance global; aprobar/rechazar cierres de fase; configurar plantillas; generar reportes ejecutivos |
| Dolores | Reportes informales; sin semáforo de riesgo; dependencia de Excel/correo |
| Ganancia esperada | Control, plazos visibles, trazabilidad, base para auditoría |

### 4.2 [TD] Técnico DUEA — Persona principal operativa

| Atributo | Valor |
|----------|-------|
| Contexto | Auditoría global de Evidencias e indicadores |
| Jobs-to-be-done | Validar Evidencias; aprobar/rechazar indicadores con justificación; autorizar avance de fase; registrar observaciones |
| Dolores | Búsqueda entre canales; versiones contradictorias; backlog pre-auditoría |
| Ganancia esperada | Bandeja de revisión, trazabilidad, menos idas y vueltas informales |

### 4.3 [CC] Coordinador de Carrera

| Atributo | Valor |
|----------|-------|
| Contexto | Gestiona autoevaluación y Evidencias de su **AcademicProgram** |
| Jobs-to-be-done | Cargar y corregir Evidencias; responder observaciones; cumplir cronograma |
| Dolores | Dispersión; sin confirmación de recepción; plazos inamovibles |
| Ganancia esperada | Flujo guiado, alertas, panel de progreso de su carrera |

### 4.4 [P] Público — Portal de transparencia

| Atributo | Valor |
|----------|-------|
| Contexto | Consulta anónima de información publicada oficialmente |
| Jobs-to-be-done | Verificar estado de acreditación; descargar certificados publicados |
| Restricción | Solo contenido explícitamente autorizado por [JD] |

### 4.5 Actores complementarios

| Código | Actor | Notas |
|--------|-------|-------|
| [JC] | Jefe de Carrera | Puede fusionarse con [CC] según resolución DUEA |
| [EE] | Evaluador externo / par académico | Visibilidad acotada en fases de evaluación externa |

---

## 5. Propuesta de valor

| Eje | Contenido |
|-----|-----------|
| **Para quién** | DUEA, equipos de carrera y autoridades UMSS en procesos CEUB/ARCU-SUR |
| **Que necesita** | Gestionar ciclo de acreditación con Evidencias clasificadas, versionadas y auditables |
| **Nuestra propuesta es** | SIGESA: **sistema de automatización** de fases, indicadores, alertas y reportes con normativa local nativa (no ERP) |
| **Que aporta** | Fuente única de verdad; historial inmutable post-aprobación; semáforos; observaciones formales; reportes ≤ 5 min |
| **A diferencia de** | Excel + correo + mensajería + repositorios pasivos |
| **Diferencial** | Taxonomía CEUB/ARCU-SUR integrada; append-only; máquina de estados con roles humanos explícitos |

---

## 6. Panorama competitivo (resumen)

| Alternativa | Tipo | Fortaleza | Debilidad |
|-------------|------|-----------|-----------|
| Proceso actual (Excel, correo, físico) | *do-nothing* | Costo inmediato bajo | Sin trazabilidad ni alertas |
| DEVA UAJMS | Directo parcial | Referente boliviano | Repositorio pasivo, no gestiona ciclo activo |
| QS / THE / AACSB | Indirecto | Benchmark global | Bajo encaje normativo local |
| DMS genérico (Drive, carpetas) | Indirecto | Almacenamiento conocido | No modela Fases ni RACI DUEA–carrera |
| SIGESA (objetivo) | Directo | Flujo y Evidencias alineados UMSS | Requiere piloto y gobernanza |

> Análisis profundo: `docs/02_mrd/MRD.md`.

---

## 7. Business Model Canvas

> Cada bloque incluye **≥ 3 elementos** (criterio plantilla BRD).

| Bloque | Elementos |
|--------|-----------|
| **1. Segmentos** | [TD] Técnicos DUEA · [CC] Coordinadores (12 facultades UMSS) · [JD] y autoridades (Rectorado, decanatos) · [P] estudiantes/egresados/empleadores |
| **2. Propuesta de valor** | Fuente única de verdad · Versionado append-only · Semáforos y alertas · Cumplimiento nativo CEUB/ARCU-SUR |
| **3. Canales** | Plataforma web con correo UMSS · Capacitación presencial/virtual · Portal público de transparencia |
| **4. Relación** | Observaciones en flujo formal · Soporte piloto DUEA–AcredIA · Mejora continua por comité operativo |
| **5. Ingresos** | Presupuesto institucional UMSS · Sin licencia por estudiante en v1 · Posible extensión a otras universidades CEUB *(explorar en MRD)* |
| **6. Recursos** | SIGESA (producto) · Taxonomías normativas · Datos maestros facultades/carreras · Infraestructura TI UMSS |
| **7. Actividades** | Parametrización de procesos · Validación [TD] · Alertas y reportes · Actualización normativa versionada |
| **8. Socios** | CEUB · ARCU-SUR · Ministerio de Educación (marco) · Proveedor cloud/TI |
| **9. Costos** | Desarrollo inicial (CAPEX) · Hosting y soporte (OPEX) · Capacitación y change management · Operación comités de gobernanza |

---

## 8. Métricas clave de éxito (North Star + apoyo)

| ID | KPI | North Star | Línea base | Meta | Horizonte | Fuente |
|----|-----|:----------:|------------|------|-----------|--------|
| BRD-KPI-01 | Tiempo de localización de Evidencia | **Sí** | 20+ min | ≤ 2 min | Q4 2026 | Logs + encuesta |
| BRD-KPI-02 | % procesos activos con Evidencias críticas al día | No | Por medir | ≥ 80 % | Piloto | SIGESA + DUEA |
| BRD-KPI-03 | Cumplimiento de hitos de Fase en fecha | No | Por medir | +20 pp vs. base | Piloto | Plan vs. real |
| BRD-KPI-04 | Tiempo generación reporte ejecutivo (P95) | No | Horas/días | ≤ 5 min | Q4 2026 | Logs |
| BRD-KPI-05 | Adopción actores clave (% activos/registrados) | No | 0 % | ≥ 80 % | Mes +3 | SIGESA |
| BRD-KPI-06 | Incidentes pérdida/versión incorrecta | No | Recurrente | 0 críticos | Q1 2027 | DUEA |
| BRD-KPI-07 | % fases con trazabilidad completa | No | 0 % | 100 % | Q2 2027 | SIGESA |
| BRD-KPI-08 | Satisfacción sponsor [JD] (Likert 1–5) | No | Por medir | ≥ 4,0 | Post-piloto | Encuesta |
| BRD-KPI-09 | CSAT post-tarea (prototipo ref. 8,67/10) | No | Por medir | ≥ 8,0 | Piloto UX | Encuesta |
| BRD-KPI-10 | Tasa éxito core tasks | No | Por medir | ≥ 95 % | Piloto | UAT |

---

## 9. Objetivos de negocio (SMART)

| ID | Objetivo | Métrica | Línea base | Meta | Horizonte |
|----|----------|---------|------------|------|-----------|
| BRD-OBJ-01 | Reducir tiempo de localización de Evidencia | Minutos/consulta | 20+ min | ≤ 2 min | Q4 2026 |
| BRD-OBJ-02 | Eliminar pérdida de Evidencia en procesos SIGESA | Incidentes/gestión | Recurrente | 0 | Q1 2027 |
| BRD-OBJ-03 | Mejorar cumplimiento de hitos de Fase | % hitos a tiempo | Por medir | +20 pp vs. base | Fin piloto |
| BRD-OBJ-04 | Trazabilidad completa Proceso→Indicador→Evidencia | % fases completas | 0 % | 100 % | Q2 2027 |
| BRD-OBJ-05 | Autonomía [JD] para estado consolidado | Minutos hasta vista | Por medir | ≤ 2 min | Q4 2026 |
| BRD-OBJ-06 | Automatizar reporte ejecutivo estándar | P95 generación | Horas/días | ≤ 5 min | Q4 2026 |
| BRD-OBJ-07 | Adopción activa actores clave | % MAU/registrados | 0 % | ≥ 80 % | Mes +3 |
| BRD-OBJ-08 | Metadatos completos en Evidencias versionadas | % con autor/fecha | Por medir | ≥ 95 % | Fin piloto |
| BRD-OBJ-09 | Uso mensual reporte ejecutivo por carrera piloto | Generaciones/mes | 0 | ≥ 1/carrera | Durante piloto |
| BRD-OBJ-10 | Cero accesos indebidos confirmados | Incidentes críticos | Por medir | 0 | Continuo |
| BRD-OBJ-11 | Canal oficial: Evidencias piloto solo por SIGESA | % evidencias vía sistema | Por medir | ≥ 95 % | Fin piloto |
| BRD-OBJ-12 | Cumplimiento WCAG 2.2 AA en componentes críticos | Incumplimientos AA | Por medir | 0 críticos | Release acordado |

---

## 10. Stakeholders y matriz RACI

| Stakeholder | Interés | R/A/C/I |
|-------------|---------|---------|
| Sponsor (Vicerrectoría / [JD] delegada) | Estratégico, presupuesto | **A** |
| [JD] Jefatura DUEA | Políticas, publicación portal, priorización | **A**/R |
| [TD] Técnico DUEA | Validación, observaciones, reportes | **R** |
| [CC] Coordinador de Carrera | Carga y subsanación Evidencias | **R** |
| TI / Infraestructura UMSS | Despliegue, seguridad, respaldos | **R** (infra) |
| CEUB / ARCU-SUR | Marco normativo | C |
| AcredIA (desarrollo) | Entrega producto | **R** (producto) |
| [P] / comunidad | Transparencia | I |

### RACI por tema

| Tema | [JD] | [TD] | [CC] | TI | [P] |
|------|:----:|:----:|:----:|:--:|:---:|
| Alcance y priorización | A | C | C | I | I |
| Validación normativa Evidencia | C | R | C | I | I |
| Carga y subsanación | I | C | R | I | I |
| Configuración institucional | A/R | R | I | C | I |
| Reportes ejecutivos | A | R | C | I | I |
| Publicación portal | A | R | C | C | I |
| Consulta pública | I | I | I | I | R |

---

## 11. Requerimientos de negocio (MoSCoW)

> Sin prescripción tecnológica (sin stack, frameworks ni lenguajes). Cada ítem es verificable en UAT de negocio. La validación normativa de indicadores es responsabilidad de **[TD]**; no se omiten pasos de auditoría DUEA.

| ID | Requerimiento | Prioridad | Justificación | Métrica de aceptación |
|----|---------------|-----------|---------------|------------------------|
| BRD-REQ-001 | Autenticación y autorización por roles ([JD], [TD], [CC], [EE], [P]) | Must | Gobierno del proceso y separación de deberes | 100 % acciones sensibles con rol válido |
| BRD-REQ-002 | Gestión de **Proceso** por carrera y facultad (CEUB/ARCU-SUR) | Must | Núcleo operativo normativo | 100 % procesos piloto con tipo explícito |
| BRD-REQ-003 | **Fases** configurables por [JD]; cierre según reglas agregadas | Must | Control institucional del ciclo | Solo roles autorizados crean/cierran fases |
| BRD-REQ-004 | Taxonomía **Dimensión → Criterio → Indicador** parametrizable | Must | Alineación glosario; sin Evidencia huérfana | 0 Evidencias sin clasificación en producción |
| BRD-REQ-005 | Carga de **Evidencia** solo por [CC] (o rol delegado); [TD] valida, no sustituye carga | Must | Integridad y responsabilidad de carrera | 0 cargas en nombre de [CC] por [TD] sin delegación formal |
| BRD-REQ-006 | **Versionado** con autor, fecha e historial consultable | Must | Elimina amnesia de versiones | ≥ 95 % con metadatos completos en piloto |
| BRD-REQ-007 | **Append-only**: sin eliminación física de Evidencia aprobada | Must | Auditoría CEUB/ARCU-SUR | 0 violaciones; subsanación solo por nueva versión |
| BRD-REQ-008 | **Observaciones** [TD]→[CC] con **subsanación** enlazada a la observación | Must | Canal formal vs. correo/WhatsApp | 100 % observaciones piloto en sistema; cada cierre con Evidencia subsanatoria |
| BRD-REQ-009 | **Máquina de estados** indicador/fase; [TD] autoriza avance cuando corresponde | Must | No saltar validación DUEA | 100 % transiciones ilegales rechazadas en prueba |
| BRD-REQ-010 | Panel **semáforo** por carrera/facultad para [JD] | Must | Decisión sin compilación manual | Vista consolidada ≤ 2 min |
| BRD-REQ-011 | **Alertas** automáticas (plazos, rechazos, vencimientos) | Must | Mitiga principal riesgo de retraso | ≥ 90 % alertas críticas a tiempo; eventos críticos ≤ 15 min |
| BRD-REQ-012 | **Reporte ejecutivo** PDF ≤ 2 clics / ≤ 5 min (P95) | Must | Rendición de cuentas | UAT [JD] piloto superado |
| BRD-REQ-013 | Un **Proceso** activo por tipo/carrera/periodo | Must | Integridad de ciclo | 0 dobles procesos activos |
| BRD-REQ-014 | **Cronograma** coherente; sin cierre con tareas pendientes | Must | Plazos institucionales | 100 % cierres inválidos rechazados |
| BRD-REQ-015 | **Bitácora de auditoría** inmutable de acciones relevantes | Should | Trazabilidad externa | 100 % acciones críticas con actor y timestamp |
| BRD-REQ-016 | Portal **[P]** solo contenido publicado por [JD] | Should | Transparencia controlada | 0 borradores públicos |
| BRD-REQ-017 | Exportación PDF/Excel por carrera, facultad, periodo | Could | Análisis decanal | ≥ 1 formato consolidado en iteración acordada |
| BRD-REQ-018 | IA **explicable** y supervisada (sin dictámenes automáticos) | Could | Reducir consultas repetitivas con ética | 100 % sugerencias con origen auditable |
| BRD-REQ-019 | **Carga masiva / planilla** para [CC] donde el volumen lo exija | Must | Eficiencia operativa (boris BR-004) | Importación sin reingreso manual total en escenario piloto definido |
| BRD-REQ-020 | **Búsqueda** de Evidencia por título, carrera, facultad, gestión | Must | KPI localización ≤ 2 min | UAT búsqueda representativa ≤ 2 min |
| BRD-REQ-021 | **Respaldo** automático diario verificable (datos + Evidencias) | Must | Continuidad ante auditoría | Confirmación diaria a [JD]/TI; RPO/RTO acordados |
| BRD-REQ-022 | **Planes de mejora** vinculados al Proceso (crear, seguir, cerrar) | Should | Core task investigación UX | ≥ 1 plan gestionado punta a punta en piloto |
| BRD-REQ-023 | Emisión/descarga de **certificados** de acreditación publicados | Could | Reduce trámite papel | Certificado descargable cuando [JD] publique |
| BRD-REQ-024 | **Chatbot informacional** (FAQ normativa aprobada por DUEA) | Could | Desvío consultas repetitivas | Respuestas solo de contenido aprobado |
| BRD-REQ-025 | Retroalimentación en cargas largas y mensajes de error accionables | Should | Hallazgos UX prototipo | 100 % cargas > **5 MB** con progreso visible; mensajes validados con [TD]/[CC] |
| BRD-REQ-026 | **Paneles operativos** [CC] y **bandeja** [TD]: visibilidad del avance por Fase, observaciones pendientes y estado del Proceso sin herramientas paralelas | Must | Control operativo y gerencial (cierra trazabilidad MRD-N-06 → PRD-REQ-012); apoya BRD-OBJ-03, BRD-OBJ-09 | [CC] accede a observaciones pendientes en ≤ 3 clics; [TD] filtra bandeja representativa en ≤ 2 min |

### 11.1 Resumen MoSCoW

| Prioridad | Cantidad | IDs |
|-----------|----------|-----|
| Must | 18 | 001–014, 019–021, **026** |
| Should | 4 | 015, 016, 022, 025 |
| Could | 4 | 017, 018, 023, 024 |

### 11.2 Priorización por release (P1–P3, visión producto)

| Nivel | Capacidades | Reqs. BRD |
|-------|-------------|-----------|
| **P1 — Crítico** | Auth, fases, Evidencia, versionado, observaciones, estados, paneles [CC]/[TD] | 001–009, 019, **026** |
| **P2 — Importante** | Panel [JD], alertas, búsqueda, auditoría, respaldos, responsive lectura ampliada | 010–015, 020–021, 025 |
| **P3 — Valioso** | Reportes amplios, portal, certificados, IA, chatbot, planes mejora | 016–018, 022–024, 017 |

---

## 12. Reglas de negocio y políticas

| ID | Regla | Tipo | Origen |
|----|-------|------|--------|
| BRD-RB-01 | Proceso asociado obligatoriamente a carrera y facultad | Negocio | Gobierno datos UMSS |
| BRD-RB-02 | Máximo un Proceso activo del mismo tipo (CEUB/ARCU-SUR) por carrera y periodo | Negocio | BRD-REQ-013 |
| BRD-RB-03 | Proceso registra: tipo, organismo, gestión (año), fechas inicio/fin | Normativa | CEUB/ARCU-SUR |
| BRD-RB-04 | Acceso restringido por rol; mínimo un rol por usuario | Seguridad | Política UMSS |
| BRD-RB-05 | Solo [JD] crea usuarios, asigna roles y modifica permisos globales | Política | DUEA |
| BRD-RB-06 | Toda Evidencia asociada a Criterio/Indicador; no carga huérfana | Dominio | Glosario |
| BRD-RB-07 | Registro de fecha y usuario; historial de versiones | Auditoría | Append-only |
| BRD-RB-08 | Estados de Proceso: En proceso / Acreditado / Vencido con historial autorizado | Negocio | Seguimiento institucional |
| BRD-RB-09 | Plazos CEUB/ARCU-SUR no editables por usuarios operativos | Normativa | Organismos |
| BRD-RB-10 | Carrera solo inicia ARCU-SUR con resolución CEUB vigente cuando aplique norma | Normativa | CEUB/ARCU-SUR |
| BRD-RB-11 | Subfase/Fase no «Aprobada» sin Evidencias requeridas validadas por [TD] | Política | Autoevaluación |
| BRD-RB-12 | Reportes ejecutivos: distribución externa solo con autorización [JD] | Política | DUEA |
| BRD-RB-13 | Autenticación con correo institucional UMSS; no correos personales | Seguridad | UMSS |
| BRD-RB-14 | Dictámenes de acreditación y cierres finales solo por roles humanos ([JD]/[TD]) | IA/ética | AGENTS.md RB-11 |
| BRD-RB-15 | [CC] carga Evidencias; [TD] valida — sin carga sustitutiva salvo delegación registrada | Operación | Procedimiento DUEA |
| BRD-RB-16 | **Subsanación** ante observación: nueva versión de Evidencia vinculada al ID de observación | Auditoría | BRD-REQ-007, BRD-REQ-008 |
| BRD-RB-17 | Avance porcentual del Proceso derivado del cumplimiento de Indicadores configurados | Política | Visión v2 equipo |
| BRD-RB-18 | Intentos de borrado físico de Evidencia aprobada: rechazados y registrados en bitácora | Seguridad | Append-only |
| BRD-RB-19 | La arquitectura cloud v1.0 debe desacoplar carga de Evidence, validación, notificación y cierre de Phase sin perder trazabilidad | Operación | BRD-KPI-02, BRD-KPI-03, BRD-KPI-06 |

### 12.1 Flujo de negocio: subsanación (sin borrado)

```text
[CC] carga Evidencia v1 → [TD] revisa → Observación O-### (motivo obligatorio)
→ [CC] carga Evidencia v2 (subsanación) enlazada a O-### → [TD] valida
→ Indicador/Fase avanza solo si reglas agregadas lo permiten
```

> **Prohibido:** eliminar v1 aprobada; **permitido:** nueva versión con trazabilidad (BRD-CST-01).

---

## 13. Supuestos, restricciones y dependencias

### 13.1 Supuestos

| ID | Supuesto |
|----|----------|
| BRD-ASM-01 | DUEA entregará datos maestros (facultades, carreras, plantillas) |
| BRD-ASM-02 | Usuarios clave con correo UMSS y conectividad razonable |
| BRD-ASM-03 | Normativa no cambia estructuralmente durante piloto inicial |
| BRD-ASM-04 | Resolución institucional establece SIGESA como canal oficial en piloto |
| BRD-ASM-05 | Línea base de KPIs medida antes del go-live |

### 13.2 Restricciones

| ID | Restricción |
|----|-------------|
| BRD-CST-01 | **Append-only / inmutabilidad de Evidencia aprobada** — correcciones solo por nueva versión |
| BRD-CST-02 | Cumplimiento CEUB, ARCU-SUR y reglamento UMSS |
| BRD-CST-03 | Máquina de estados: no avanzar Fase con indicadores no resueltos |
| BRD-CST-04 | Separación visibilidad: [CC] por carrera; [TD]/[JD] según matriz |
| BRD-CST-05 | Solución web sin instalación de cliente adicional |
| BRD-CST-06 | Cumplimiento Ley 164 (datos personales) y políticas UMSS |
| BRD-CST-07 | **No-ERP:** el producto no expandirá alcance a gestión administrativa general; solo automatización del dominio acreditación |

### 13.3 Dependencias críticas

| ID | Dependencia | Impacto si falla |
|----|-------------|------------------|
| BRD-DEP-01 | Aprobación presupuesto y sponsor | Paraliza despliegue |
| BRD-DEP-02 | Datos maestros limpios | Retrasa parametrización |
| BRD-DEP-03 | Infraestructura TI (DNS, certificados, correo) | Afecta disponibilidad |
| BRD-DEP-04 | Textos normativos oficiales CEUB/ARCU-SUR | Riesgo taxonomía incorrecta |

---

## 14. Alcance de negocio

### 14.1 En alcance

- Procesos CEUB y ARCU-SUR con Fases, indicadores y Evidencias versionadas.
- Roles [CC], [TD], [JD], [EE] acotado, [P].
- Observaciones, subsanación, alertas, panel semáforo [JD], **paneles operativos [CC] y bandeja [TD]** (BRD-REQ-026), reportes ejecutivos.
- Lectura responsive en v1.0 (notificaciones, avance de Fase, observaciones); carga móvil de Evidencia diferida a v1.1.
- Bitácora de auditoría y exportación para auditoría externa.
- Parametrización de 12 facultades UMSS (configuración inicial).
- Fases de referencia: autoevaluación · documentación · visita de pares · informe externo · resolución final.
- Búsqueda de Evidencia, carga masiva donde aplique, respaldos automáticos (negocio).
- Planes de mejora (Should); certificados y chatbot (Could).

### 14.2 Fuera de alcance (v1)

- **ERP universitario** o funciones de SIIS, RRHH, tesorería, matrícula, nómina o gestión financiera integrada.
- Integración en tiempo real con SIIS/RRHH como **backbone** del sistema (evaluar solo conectores puntuales de datos maestros en fases posteriores, sin cambiar la naturaleza del producto).
- Pagos en línea por certificaciones.
- Motor automático único de matrices de evaluación externa.
- Rankings internacionales (QS/THE) integrados.
- Especificación de stack tecnológico (PRD/FSD/DTI).

### 14.3 Alcance piloto F2 (por formalizar — BRD-Q-04)

> **Pendiente [JD]:** registrar **2–3 carreras**, facultad, modalidad (CEUB/ARCU-SUR), fechas límite de Fase de Evaluación Interna y ventana del piloto. Hasta entonces, las pruebas UAT usan escenarios de ejemplo (p. ej. carrera de referencia en bitácoras UX).

| # | Carrera | Facultad | Modalidad | Fecha límite Fase crítica | Responsable [CC] | Estado |
|---|---------|----------|-----------|--------------------------|------------------|--------|
| 1 | *Por definir* | | CEUB / ARCU-SUR | | | Pendiente |
| 2 | *Por definir* | | | | | Pendiente |
| 3 | *Opcional* | | | | | Pendiente |

**Criterio de cierre BRD-Q-04:** filas 1–2 completas y aprobadas por [JD] antes de F2 (piloto facultades).

---

## 15. Beneficios esperados y business case

> Cifras **paramétricas** (Marlene/BRD_v1): sustituir por valores oficiales UMSS antes de aprobación presupuestaria. **No se inventan** VAN/TIR definitivos.

| Tipo | Año 1 | Año 2 | Año 3 | Notas |
|------|-------|-------|-------|-------|
| Ahorro operativo (BOB) | ~57.600 | ~57.600 | ~57.600 | 15 h/mes × 4 técnicos × 12 × 80 BOB/h |
| Horas recuperadas | ~720 h | ~720 h | ~720 h | Validar en piloto |
| Ingresos adicionales | N/A | N/A | N/A | Proyecto institucional |
| Inversión CAPEX | Por formalizar | — | — | Ref. ~850.000 BOB placeholder |
| OPEX (hosting/soporte) | Por formalizar | Por formalizar | Por formalizar | Ref. ~120.000 BOB/año |
| **VAN** | Por calcular | | | Incluir riesgo evitado cualitativo |
| **TIR** | Por calcular | | | ROI de cumplimiento puede dominar sobre ROI financiero |

**Interpretación para dirección:** SIGESA es **infraestructura de cumplimiento**; el retorno principal es reducción de riesgo acreditador y continuidad institucional, además de eficiencia.

---

## 16. Arquitectura de negocio

| ID | Componente | Valor | KPI/Riesgo vinculado |
|----|------------|-------|----------------------|
| C1 | Catálogo institucional (Facultad, AcademicProgram) | Base permisos y reportes | Integridad catálogo |
| C2 | Marco normativo CEUB/ARCU-SUR | Plantillas de Fase/Indicador | BRD-KPI-07 |
| C3 | Gestión documental y versiones (Evidencia) | Append-only | BRD-KPI-01, BRD-OBJ-02 |
| C4 | Workflow validación [TD]↔[CC] | Observaciones auditables | BRD-REQ-008 |
| C5 | Notificaciones y alertas | Mitiga retrasos | BRD-REQ-011 |
| C6 | Dashboards gerenciales | Decisión [JD] | BRD-OBJ-05 |
| C7 | Reporting ejecutivo | Rendición de cuentas | BRD-OBJ-06 |
| C8 | Cumplimiento y auditoría | Exportación auditoría | BRD-REQ-015 |
| C9 | Transparencia [P] | Confianza social | BRD-REQ-016 |
| C10 | Operación y mejora continua | Cambios normativos | BRD-DEP-04 |

---

## 17. Riesgos de negocio

| ID | Riesgo | P | I | Mitigación | Responsable |
|----|--------|---|---|------------|-------------|
| BRD-RSK-01 | Doble canal (correo/mensajería) | A | A | Canal oficial + auditoría uso | [JD] |
| BRD-RSK-02 | Resistencia al cambio | A | M | UX baja curva, campeones por facultad | PM + DUEA |
| BRD-RSK-03 | Datos maestros incompletos | M | A | Plan carga con secretarías | DUEA/TI |
| BRD-RSK-04 | Cambio normativo CEUB/ARCU-SUR | M | A | Plantillas versionadas; comité normativo | DUEA |
| BRD-RSK-05 | Solicitud borrado Evidencia (viola CST-01) | M | Crítico | Rechazar; solo versionado | [JD] |
| BRD-RSK-06 | Línea base KPI no medida | M | M | Plan pre-piloto | PM |
| BRD-RSK-07 | Permisos mal configurados | M | A | Matriz RACI + UAT roles | QA/DUEA |
| BRD-RSK-08 | Falla migración desde Excel/Drive | B | Cr | Migración por lotes + checksum | AcredIA |
| BRD-RSK-09 | Cargas pesadas sin feedback (abandono/doble envío) | M | M | BRD-REQ-025; pruebas red UMSS | AcredIA |
| BRD-RSK-10 | Ausencia de deshacer tras error de carga | M | A | Flujo de reversión guiada o nueva versión | AcredIA + DUEA |
| BRD-RSK-11 | Nomenclatura de roles inconsistente en UI | M | M | Glosario único + prueba de contenido | PM + [JD] |

---

## 18. Gobernanza

| ID | Regla |
|----|-------|
| BRD-GOV-01 | [JD] es **Accountable** de prioridades y alcance de negocio |
| BRD-GOV-02 | Cambios normativos en plantillas: fuente citada + revisión [TD] + aprobación [JD] |
| BRD-GOV-03 | Cambios a política de Evidencia (p. ej. relajar append-only): solo con acta [JD] |
| BRD-GOV-04 | **Steering Committee** (Vicerrectoría, DUEA, TI): go/no-go de fases y presupuesto |
| BRD-GOV-05 | Escalamiento: operativo [CC]↔[TD] → [JD] → Steering |

---

## 19. Criterios de éxito del proyecto

| ID | Criterio |
|----|----------|
| BRD-SUC-01 | ≥ 80 % objetivos SMART (BRD-OBJ-01 a 12) cumplidos |
| BRD-SUC-02 | BRD-KPI-01 y BRD-KPI-08 en meta o meta revisada con acta |
| BRD-SUC-03 | Cero violaciones BRD-CST-01 en auditoría interna |
| BRD-SUC-04 | Un ciclo piloto CEUB o ARCU-SUR cerrado íntegramente en SIGESA |
| BRD-SUC-05 | Sponsor [JD] ≥ 4/5 satisfacción |
| BRD-SUC-06 | SIGESA reconocido como canal oficial en acta DUEA |

### Criterios de aceptación UAT (negocio)

| ID | Condición verificable |
|----|---------------------|
| BRD-CA-01 | [CC] carga Evidencia: visible, metadatos completos, notificación [TD] |
| BRD-CA-02 | [TD] rechaza con causa: estado observado, motivo obligatorio |
| BRD-CA-03 | [JD] ve semáforos coherentes con reglas de completitud |
| BRD-CA-04 | Reporte PDF con filtros y marca temporal |
| BRD-CA-05 | Portal [P] sin borradores |
| BRD-CA-06 | Log con actor, timestamp, acción |
| BRD-CA-07 | [CC] subsana observación: v2 visible y enlazada a O-### |
| BRD-CA-08 | Intento de borrado de Evidencia aprobada: rechazado y en bitácora |

### 19.1 Roadmap de implementación (negocio)

| Fase | Duración orientativa | Entregables de negocio |
|------|----------------------|------------------------|
| **F0 — Discovery y baseline** | 4–6 semanas | Línea base KPI; datos maestros v0; BRD Dorada aprobado |
| **F1 — MVP cerrado** | 8–12 semanas | UAT interno DUEA (P1) |
| **F2 — Piloto facultades** | 4–8 semanas | KPI adopción y localización en entorno real |
| **F3 — Despliegue institucional** | 4–6 semanas | Acta canal oficial SIGESA |
| **F4 — Estabilización** | 4–8 semanas | Ajustes normativos; P2 completo |
| **F5 — Evolución (automatización del dominio)** | Continuo | Capacidades P3 del BRD (exportaciones, certificados, IA asistencial, chatbot FAQ); conectores **opcionales** de datos maestros — **sin** convertir SIGESA en ERP |

### 19.2 Impacto en procesos académicos y administrativos

| Proceso | Hoy | Con SIGESA (esperado) |
|---------|-----|------------------------|
| Autoevaluación documental | Fragmentación | Flujo único con estados y versiones |
| Seguimiento decanal | Información rezagada | Paneles por facultad |
| Auditoría externa | Compilación manual | Exportación estructurada |
| Atención comunidad | Consultas presenciales | Portal [P] |
| Planificación DUEA | Reuniones ad-hoc | KPIs y reportes programados |

### 19.3 Matriz de alineación OBJ ↔ KPI ↔ Riesgo

| Objetivo | KPI principal | Riesgo mitigado |
|----------|---------------|-----------------|
| BRD-OBJ-01 | BRD-KPI-01 | BRD-RSK-01, BRD-RSK-02 |
| BRD-OBJ-02 | BRD-KPI-06 | BRD-RSK-05 |
| BRD-OBJ-03 | BRD-KPI-03 | BRD-RSK-01 |
| BRD-OBJ-04 | BRD-KPI-07 | BRD-RSK-08 |
| BRD-OBJ-05 | BRD-KPI-01 (vista) | BRD-RSK-02 |
| BRD-OBJ-06 | BRD-KPI-04 | BRD-RSK-01 |
| BRD-OBJ-07 | BRD-KPI-05 | BRD-RSK-01, BRD-RSK-02 |
| BRD-OBJ-11 | BRD-KPI-05 | BRD-RSK-01 |

---

## 20. Trazabilidad hacia la suite documental

> Matriz maestra viva: `matriz_trazabilidad.md`. Esta sección inicia el encadenamiento BRD → artefactos hijos.

| BRD ID | MRD (por generar) | PRD (por generar) | FSD (por generar) | NFR (por generar) |
|--------|-------------------|-------------------|-------------------|-------------------|
| BRD-OBJ-01, BRD-KPI-01 | MRD-JTBD-01 | PRD-REQ-SEARCH | FSD-UC-008 | NFR-PERF-01 |
| BRD-OBJ-02, BRD-CST-01 | MRD-JTBD-02 | PRD-REQ-EVID-VERSION | FSD-UC-002 | NFR-AUDIT-01 |
| BRD-REQ-001, BRD-RB-04 | MRD-SEG-01 | PRD-REQ-AUTH-RBAC | FSD-UC-006 | NFR-SEC-01 |
| BRD-REQ-003, BRD-RB-08 | MRD-FLOW-01 | PRD-REQ-PHASE | FSD-UC-007 | NFR-STATE-01 |
| BRD-REQ-008 | MRD-FLOW-02 | PRD-REQ-OBS | FSD-UC-010 | NFR-AUDIT-02 |
| BRD-REQ-010, BRD-OBJ-05 | MRD-DASH-01 | PRD-REQ-DASH | FSD-UC-003 | NFR-UX-01 |
| BRD-REQ-011 | MRD-ALERT-01 | PRD-REQ-ALERT | FSD-UC-005 | NFR-REL-01 |
| BRD-REQ-012 | MRD-REP-01 | PRD-REQ-REPORT | FSD-UC-004 | NFR-PERF-02 |
| BRD-REQ-016 | MRD-PUB-01 | PRD-REQ-PORTAL | FSD-UC-015 | NFR-SEC-02 |
| BRD-OBJ-12, BRD-OBJ-07 | MRD-UX-01 | PRD-REQ-A11Y | FSD-UC-UX | NFR-UX-02 (WCAG) |
| BRD-REQ-019 | MRD-OPS-01 | PRD-REQ-BULK | FSD-UC-011 | NFR-PERF-03 |
| BRD-REQ-020 | MRD-JTBD-01 | PRD-REQ-SEARCH | FSD-UC-008 | NFR-PERF-01 |
| BRD-REQ-021 | MRD-OPS-02 | PRD-REQ-BACKUP | FSD-UC-ADM | NFR-REL-02 |
| BRD-REQ-022 | MRD-FLOW-03 | PRD-REQ-IMPROVE | FSD-UC-012 | — |
| BRD-REQ-023 | MRD-PUB-02 | PRD-REQ-CERT | FSD-UC-016 | NFR-SEC-02 |
| BRD-REQ-024 | MRD-SUP-01 | PRD-REQ-CHAT | FSD-UC-030 | NFR-AI-01 |
| BRD-REQ-025 | MRD-UX-02 | PRD-REQ-UX-FEEDBACK | FSD-UC-UX | NFR-UX-03 |

### Mapeo completo BRD-REQ → MRD (borrador para MRD Dorado)

| BRD-REQ | MRD destino sugerido |
|---------|----------------------|
| 001–002 | MRD-SEG-01, MRD-CORE-01 |
| 003–004, 013–014 | MRD-FLOW-01 |
| 005–008, 019 | MRD-EVID-01 |
| 009 | MRD-FLOW-02 |
| 010–012, 020 | MRD-DASH-01, MRD-JTBD-01 |
| 011 | MRD-ALERT-01 |
| 015, 021 | MRD-OPS-02 |
| 016–017, 023 | MRD-PUB-01 |
| 018, 024 | MRD-AI-01 |
| 022 | MRD-FLOW-03 |
| 025 | MRD-UX-02 |

---

## 21. Decisiones pendientes de validación institucional

> **PASO 0 (skill):** ítems que requieren confirmación de [JD] antes de cerrar el MRD. No se asumen respuestas.

| ID | Tema | Opciones / pregunta | Impacto |
|----|------|---------------------|---------|
| BRD-Q-01 | Sponsor nominal en acta | Nombre y cargo firmante | Aprobaciones §22 |
| BRD-Q-02 | Fusión [CC] vs [JC] | Un solo rol o dos perfiles | RBAC PRD |
| BRD-Q-03 | Línea base KPI | Fecha y método de medición pre-piloto | Metas SMART |
| BRD-Q-04 | Carreras piloto | Listado 2–3 carreras + fechas (§14.3) | **Abierto** — bloquea UAT representativo F2 |
| BRD-Q-05 | CAPEX/OPEX oficiales | Cifras financieras UMSS | Business case §15 |
| BRD-Q-06 | RPO/RTO respaldos | Acuerdo con TI | BRD-REQ-021 |
| BRD-Q-07 | Alcance certificados v1 | Incluir en P3 o diferir | BRD-REQ-023 |
| BRD-Q-08 | `docs/discovery/discovery_v0.1.md` | Publicar o vincular ruta | §3.3 |

### 21.1 Decisiones resueltas (auditoría trazabilidad — 2026-05-16)

| ID | Decisión | Documentos impactados |
|----|----------|------------------------|
| **Q-01** | Crear **BRD-REQ-026** para paneles [CC]/[TD]; MRD-N-06 cubre dolor, BRD cubre métrica institucional | BRD §11, PRD-REQ-012, matriz |
| **Q-02** | **v1.0:** credenciales locales (piloto cerrado); **v1.1:** LDAP/SSO vía **patrón Adapter** sin refactor del dominio | ADR-0003, FSD UC-001, DTI |
| **Q-03** | Barra de progreso asíncrona para payloads **> 5 MB** | BRD-REQ-025, PRD-NFR-011, NFR-011 |
| **Q-04** | **v1.0 Must:** responsive **lectura** ([CC]: notificaciones, % Fase, observaciones). **v1.1:** carga/subsanación móvil; v1.0 carga Evidencia **desktop-first** | PRD-REQ-023, ROADMAP |

---

## 22. Aprobaciones

| Rol | Nombre | Firma | Fecha |
|-----|--------|-------|-------|
| Sponsor ([JD]) | | | |
| PM / Líder producto | | | |
| Arquitecto | | | |
| Docente revisor | | | |

---

## 23. Registro de cambios

| Versión | Timestamp | Autor | Cambio |
|---------|-----------|-------|--------|
| v1.0 | `2026-05-15T00:00:00-04:00` | Equipo SIGESA | Consolidación inicial en `docs/01_brd/` |
| Dorada v1.0 | `2026-05-16T12:00:00-04:00` | Equipo AcredIA | Fusión team BRDs; 12 OBJ, 18 REQ |
| Dorada v1.0.1 | `2026-05-16T15:01:08-04:00` | Equipo AcredIA | Control de versión inicio/cierre |
| Dorada v2.0 | `2026-05-16T15:03:03-04:00` | Equipo AcredIA | Auditoría skill+plantilla: +7 REQ, +4 RB, VoC, roadmap, subsanación, decisiones pendientes |
| **Dorada v2.1** | `2026-05-16T15:24:02-04:00` | Equipo AcredIA | Clarificación: sistema de automatización de acreditación; no ERP; F5 = evolución en dominio, no suite ERP |
| **Dorada v2.2** | `2026-05-16T16:30:00-04:00` | Equipo AcredIA | BRD-REQ-026; decisiones Q-01…Q-04; §14.3 piloto; umbral 5 MB |

---

## 24. Anexo — PR-FAQ resumido (coherente con §1–19)

### Press release (piloto UMSS)

La UMSS anuncia el piloto de **SIGESA**, sistema de gestión de acreditación de carreras que centraliza **Evidencias**, plazos y reportes con trazabilidad CEUB/ARCU-SUR. Meta operativa: localizar documentación en **≤ 2 minutos** frente a **20+ minutos** hoy.

### FAQ interna (extracto)

- **¿Por qué ahora?** Ventanas de acreditación y riesgo documental creciente.
- **¿Reemplaza correo?** Para Evidencias oficiales del piloto, sí — por acta DUEA.
- **¿Se pueden borrar archivos?** No los aprobados; solo nuevas versiones (subsanación).
- **¿Quién valida?** Siempre **[TD]** antes de cerrar indicadores/fases.

---

## 25. Checklist de entrega (plantilla BRD)

- [x] Resumen ejecutivo con problema, propuesta, valor y métricas
- [x] Problema con evidencia cuantitativa (20+ min)
- [x] Personas [CC], [TD], [JD], [P] + actores complementarios
- [x] Propuesta de valor (VPC)
- [x] Panorama competitivo (≥ 5 alternativas)
- [x] Business Model Canvas (9 bloques, ≥ 3 elementos)
- [x] North Star + ≥ 9 KPIs de apoyo
- [x] ≥ 10 objetivos SMART
- [x] Matriz RACI completa
- [x] ≥ 25 requerimientos MoSCoW con justificación
- [x] ≥ 18 reglas de negocio (18 RB)
- [x] Voz del Cliente / UX (§3.4)
- [x] Roadmap F0–F5 (§19.1)
- [x] Decisiones pendientes institucionales (§21)
- [x] Flujo subsanación append-only (§12.1)
- [x] Restricciones con **append-only** explícito
- [x] Business case (cualitativo + parámetros ilustrativos)
- [x] Trazabilidad iniciada a MRD/PRD/FSD/NFR
- [x] Sin emojis; lenguaje ubicuo del glosario

---

## 26. Checklist de auditoría documental (skill + plantilla)

| Criterio skill / plantilla | Estado | Referencia BRD |
|----------------------------|--------|----------------|
| Resumen alineado a dispersión documental | Cumple | §1 |
| ≥ 3 objetivos SMART (≥ 10 entregado) | Cumple | §9 (12) |
| Stakeholders [TD], [CC], [JD], [P] | Cumple | §4, §10 |
| In/Out scope explícito | Cumple | §14 |
| Append-only / inmutabilidad Evidencia | Cumple | BRD-CST-01, BRD-REQ-007 |
| IDs trazables en requerimientos | Cumple | BRD-REQ-001…025 |
| Sin roles inventados («Cliente», «Super Admin») | Cumple | §4 |
| Sin prescripción tecnológica | Cumple | §11 nota |
| Validación [TD] no omitida | Cumple | BRD-REQ-009, BRD-RB-15 |
| Trazabilidad a MRD/PRD | Cumple | §20 |
| Métricas sin inventar (marcadas «por medir») | Cumple | §21 BRD-Q-03 |
| Taxonomía según glosario (sin «Modalidad» forzada) | Cumple | §11 BRD-REQ-004 |

**Veredicto:** documento **listo para iniciar MRD Dorado** tras resolver ítems BRD-Q-01 a Q-08 con [JD] en paralelo o durante el MRD.

---

## Control de versión (cierre del documento)

| Campo | Valor |
|-------|-------|
| **Versión** | **Dorada v2.2** |
| **Timestamp** | `2026-05-16T16:30:00-04:00` |
| **Cambios aplicados** | BRD-REQ-026; Q-01…Q-04 resueltas; §14.3 piloto; umbral progreso 5 MB. |

*Documento canónico SIGESA — UMSS, Cochabamba, Bolivia, 2026.*  