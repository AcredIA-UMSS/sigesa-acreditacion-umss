# Market Requirements Document (MRD) — SIGESA / AcredIA

## Control de versión del documento

| Campo | Valor |
|-------|-------|
| **Versión** | **Dorada v1.1** |
| **Última actualización (timestamp)** | `2026-05-16T15:24:02-04:00` |
| **Resumen de cambios (esta versión)** | Alineación con BRD v2.1: SIGESA como **sistema de automatización** del ciclo de acreditación; **no ERP**. Post go-live = más automatización en el dominio (P2/P3), no suite administrativa ni integración ERP/SIIS como producto central. |
| **Resumen de cambios (Dorada v1.0)** | Primera Versión Dorada: 4 segmentos, 14 JTBD, 22 MRD-N, trazabilidad BRD↔PRD. |
| **BRD de referencia** | `docs/01_brd/BRD.md` — Dorada v2.1 |
| **Estado de auditoría** | **Apto para transición a PRD** — ver §20 |

> **Propósito del MRD**: describir el mercado, los usuarios y la oportunidad que justifican SIGESA. Responde a **«qué pide el mercado institucional y por qué este producto ganará frente al status quo»**. Complementa el BRD y antecede al PRD.

---

## 0. Metadatos

| Campo | Valor |
|-------|-------|
| Producto | SIGESA / AcredIA — Sistema de gestión y seguimiento de acreditaciones (UMSS) |
| Ámbito | `docs/02_mrd/` (canónico) |
| Versión | **Dorada v1.1** |
| Fecha | 16/05/2026 |
| Timestamp última edición | `2026-05-16T15:24:02-04:00` |
| Product Manager / Autor | Equipo AcredIA (consolidación documental) |
| Revisores | Jefatura DUEA · Docente · Tech Lead · QA |
| Estado | Borrador para validación con discovery continuo |
| Glosario | `context/03_domain_glossary.md` |
| Fuentes consolidadas | `team/borisAngulo/docs/02_mrd/MRD.md` · `team/aylenGonzales/02_mrd/MRD_v1.md` · `team/alexAlvarez/docs/02_mrd/MRD.md` · `team/Marlene/02_mrd/MRD.md` |
| Cadena documental | BRD → **MRD** → PRD → FSD → DTI / NFR |
| **Diagramas de contexto** | [`07_diagramas/`](07_diagramas/README.md) → [`../07_diagramas/`](../07_diagramas/README.md) |
| Skill aplicada | `sigesa-generacion-documentos-negocio` |

---

## 1. Resumen ejecutivo

La UMSS gestiona acreditación de carrera (**CEUB**, **ARCU-SUR**) con herramientas dispersas que generan **20+ minutos** por sesión solo para localizar la versión válida de una **Evidencia**, observaciones por canales informales y reportes ejecutivos que detienen el trabajo técnico de la DUEA.

**SIGESA** es un **sistema de automatización** institucional del ciclo de acreditación, diseñado **nativamente** para CEUB/ARCU-SUR: automatiza workflow de **Evidencia**, validación **[TD]**, alertas, observaciones y reportes entre **[CC]**, **[JD]** y **[P]**. **No compite con ni sustituye un ERP** (SIIS, RRHH, finanzas).

**Mercado primario (SOM v1.0):** UMSS — ~12 facultades, ~60–120 carreras, cientos de actores directos ([CC], [TD], [JD]). **Mercado ampliable (SAM):** universidades del ecosistema **CEUB** (~14 instituciones referenciales; cifra **por confirmar** con CEUB). **TAM conceptual:** educación superior boliviana sujeta a acreditación de carrera y demanda de trazabilidad documental.

**Diferenciación:** frente a Excel/correo (*do-nothing*), repositorios genéricos, **ERP adaptados** (costoso y fuera de dominio) y plataformas globales (QS, AACSB), SIGESA automatiza el ciclo con taxonomía **Dimensión → Criterio → Indicador**, semáforos, alertas y auditoría **sin pretender ser suite administrativa universal**.

**North Star (alineada BRD-KPI-02):** porcentaje de **Procesos** activos en piloto con **Evidencias** críticas al día vs. cronograma — meta **≥ 80 %**.

---

## 2. Visión del producto

> Para la DUEA y los equipos de carrera de la UMSS que hoy pierden trazabilidad y plazos con canales informales, **SIGESA** es el **sistema de automatización** de acreditación con normativa **CEUB/ARCU-SUR** nativa — no un ERP — que convierte el ciclo en un flujo auditable con **Evidencias** versionadas, alertas y decisiones en tiempo real, reduciendo la localización de **20+ minutos a ≤ 2 minutos** y los reportes ejecutivos a **≤ 5 minutos**, con piloto **Q4 2026** (*sujeto a calendario institucional*).

### 2.1 Categoría de producto (mercado)

| Categoría correcta | Categorías que **no** aplican |
|--------------------|-------------------------------|
| Automatización de proceso de negocio (acreditación documental) | ERP universitario |
| Sistema vertical CEUB/ARCU-SUR | Sustituto de SIIS o gestión académica transversal |
| Workflow + repositorio normativo + reporting | Integración corporativa tipo «suite única» post go-live |

**Post go-live (mercado):** el valor percibido crece al **automatizar más pasos del mismo proceso** (notificaciones, reportes, exportaciones, portal, asistencia IA supervisada). Los compradores institucionales no deben esperar que SIGESA absorba RRHH, matrícula o finanzas.

---

## 3. Análisis de mercado

### 3.1 Tamaño de mercado

| Métrica | Valor estimado | Fuente / nota |
|---------|----------------|---------------|
| **TAM** | ~60 universidades públicas/privadas en Bolivia + extensión regional con marcos similares | CEUB / Ministerio de Educación — **por verificar cifra oficial** |
| **SAM** | UMSS: 60+ carreras; ecosistema CEUB ~14 universidades | DUEA UMSS; informe CEUB **pendiente** |
| **SOM (v1.0)** | Piloto: **5–10 carreras** UMSS (BRD-Q-04 por confirmar listado) | Acuerdo operativo DUEA |

> Las cifras monetarias (USD 8M TAM, etc.) en aportes del equipo son **proyecciones ilustrativas**; no se adoptan como compromiso sin validación financiera UMSS (véase BRD-Q-05).

### 3.2 Tendencias del sector

- Digitalización de gestión académica y calidad en universidades latinoamericanas.
- Mayor exigencia de **evidencia trazable** por CEUB y **ARCU-SUR**.
- Presión de **transparencia** hacia estudiantes, egresados y empleadores.
- Brecha: ausencia de solución vertical boliviana de **automatización de acreditación** (no de otro ERP más).
- Las universidades no necesitan otro ERP; necesitan **automatizar** el ciclo CEUB/ARCU-SUR sin parametrizar rankings internacionales.

### 3.3 Factores regulatorios y de cumplimiento

| Marco | Implicancia para SIGESA |
|-------|-------------------------|
| **Ley 164** (datos personales Bolivia) | Minimización y control de acceso en portal [P] |
| **CEUB** | Taxonomías y plazos en plantillas del sistema |
| **ARCU-SUR** | Fases de evaluación externa y visibilidad acotada [EE] |
| **Políticas UMSS** | Correo institucional, respaldos, publicación controlada |

### 3.4 Cadencia de Continuous Discovery

| Aspecto | Valor |
|---------|-------|
| Cadencia | Quincenal en piloto; semanal en validación UX intensiva |
| Usuarios por ciclo | ≥ 2 (mín. 1 [CC] + 1 [TD]); ideal 1 [JD] cada 2 ciclos |
| Formato hipótesis | *Cuando [situación], espero [resultado], porque [razón]* |
| Backlog | §13 |
| Output | Actualiza §5, §6, §12, §14 y riesgos §15 |
| Documento Discovery | `docs/discovery/discovery_v0.1.md` — **pendiente** (BRD-Q-08) |

> Si en un ciclo no hubo contacto con usuarios, registrar riesgo en §15.

---

## 4. Segmentación y personas

### 4.1 Segmentos de mercado institucional

| ID | Segmento | Actores | Job de mercado principal | Buyer / User |
|----|----------|---------|--------------------------|--------------|
| **S1** | Operativo de carrera | [CC], [JC], docentes aportantes | Cumplir plazos con **Evidencia** válida y feedback claro | User; buyer indirecto (decanato) |
| **S2** | Aseguramiento DUEA | [TD], [JD] | Trazabilidad, homogeneidad, defensa ante auditores | Buyer institucional; user intensivo |
| **S3** | Gobierno académico | Decanatos, vicerrectoría | Visibilidad de riesgo y rendición de cuentas | Buyer estratégico; user de reportes |
| **S4** | Comunidad y externos | [P], [EE], CEUB/ARCU-SUR | Verificación y baja fricción informativa | User consulta; regulador indirecto |

### 4.2 Personas (arquetipos calibrados con discovery)

#### Persona 1 — [JD] Lic. Claudia Sevilla (Jefatura DUEA)

| Atributo | Valor |
|----------|-------|
| Segmento | S2 / S3 |
| Objetivos | Estado real de todas las carreras; reportes a autoridades; política de publicación |
| Dolores | Dependencia de Excel y memoria del equipo; sin semáforo confiable |
| Comportamiento | Escritorio; baja tolerancia a complejidad técnica |
| Frase | *«Cuando el Rector me llama, necesito saber en segundos cómo va cada carrera.»* |
| Buyer / User | **Buyer** y user de dashboards |

#### Persona 2 — [TD] Lic. Andrea Flores (Técnico DUEA)

| Atributo | Valor |
|----------|-------|
| Segmento | S2 |
| Objetivos | Validar **Evidencias**; rechazar con justificación; autorizar avances |
| Dolores | 20+ min buscando versión final; observaciones fuera del sistema |
| Frase | *«Si no está en el sistema con versión y estado, no lo defiendo ante el auditor.»* |
| Buyer / User | **User** intensivo; influye en reglas de negocio |

#### Persona 3 — [CC] Carlos Mamani (Coordinador de Carrera)

| Atributo | Valor |
|----------|-------|
| Segmento | S1 |
| Objetivos | Organizar **Evidencias** por indicador; responder observaciones; cumplir cronograma |
| Dolores | Canales informales; sin confirmación de recepción; plazos inamovibles |
| Comportamiento | PC + móvil para urgencias; adopción si ≤ 3 pasos en flujos críticos |
| Frase | *«Siempre busco la última Evidencia entre correo y mensajería.»* |
| Buyer / User | **User** principal |

#### Persona 4 — [P] Estudiante / empleador (consulta pública)

| Atributo | Valor |
|----------|-------|
| Segmento | S4 |
| Objetivos | Verificar estado de acreditación oficial sin trámite presencial |
| Dolores | Información dispersa o no verificable |
| Frase | *«Quiero ver si la carrera está acreditada sin llamar a ventanilla.»* |
| Buyer / User | **User** anónimo; valor reputacional para UMSS |

#### Persona 5 — [EE] Evaluador externo (acotado)

| Atributo | Valor |
|----------|-------|
| Segmento | S4 |
| Objetivos | Acceder a **Evidencias** y dictámenes de fase con visibilidad limitada |
| Restricción | Sin datos personales indebidos; solo lo publicado/autorizado [JD] |

---

## 5. Jobs-to-be-Done (JTBD)

> Formato skill: segmento operativo, gerencial y público. Término **Evidencia** según glosario.

| ID | Segmento | Cuando… | Quiero… | Para poder… |
|----|----------|---------|---------|-------------|
| JTBD-01 | S2/S3 | necesito el estado de todas las carreras | ver un panel con semáforos sin llamar a cada jefatura | decidir y reportar sin Excel manual |
| JTBD-02 | S2 | recibo un entregable de fase | registrar observación formal vinculada al indicador | que [CC] subsane con trazabilidad |
| JTBD-03 | S2/S3 | me piden reporte urgente para Consejo | generar PDF ejecutivo en pocos pasos | llegar con datos al día (≤ 5 min) |
| JTBD-04 | S1 | inicio autoevaluación de mi carrera | cargar **Evidencias** por criterio en un solo lugar | no perder versiones en correo |
| JTBD-05 | S1 | se acerca fecha límite | recibir alerta automática con qué falta | actuar sin depender de avisos informales |
| JTBD-06 | S1 | [TD] observa mi indicador | ver motivo y subir nueva versión enlazada | cerrar subsanación sin WhatsApp |
| JTBD-07 | S1 | debo cargar muchas actividades iniciales | importar desde planilla | evitar reingreso manual total |
| JTBD-08 | S2 | debo localizar una **Evidencia** concreta | buscar por carrera, gestión, título | completar revisión en ≤ 2 min (tarea representativa) |
| JTBD-09 | S1 | consulto desde el campus con poco tiempo | usar interfaz usable en pantalla reducida | atender urgencias sin volver a oficina |
| JTBD-10 | S4 | necesito validar acreditación de una carrera | consultar portal público oficial | confiar en información institucional |
| JTBD-11 | S2 | audito acciones del piloto | exportar bitácora de quién hizo qué y cuándo | responder auditoría externa |
| JTBD-12 | S1/S2 | el sistema rechaza mi carga | ver mensaje claro y barra de progreso en Evidencias pesadas | no abandonar ni duplicar envío |
| JTBD-13 | S2 | debo dar seguimiento a mejoras post-observación | gestionar plan de mejora en el mismo **Proceso** | demostrar cierre de acciones correctivas |
| JTBD-14 | S4 | soy evaluador en fase externa | acceder solo a paquete autorizado de **Evidencias** | emitir informe sin copias informales |

---

## 6. Análisis competitivo

### 6.1 Tabla comparativa

| Criterio | SIGESA | Status quo (Excel/correo/mensajería) | DEVA UAJMS | DMS genérico (Drive/red) | Plataformas globales (QS/AACSB) |
|----------|--------|--------------------------------------|------------|--------------------------|----------------------------------|
| Normativa CEUB/ARCU-SUR nativa | Sí | No | Parcial | No | No |
| Gestión de **Fases** e indicadores | Sí | No | No | No | Genérico |
| Versionado auditable **Evidencia** | Sí (append-only) | No | Parcial | Parcial | Variable |
| Semáforo por carrera/facultad | Sí | No | No | No | No |
| Alertas automáticas de plazo | Sí | No | No | No | Variable |
| Observaciones formales [TD]↔[CC] | Sí | No | No | No | No |
| Reporte ejecutivo ≤ 5 min | Sí (objetivo) | No (horas) | No | No | No contextualizado |
| Portal [P] | Sí (Should) | No | No | No | No |
| Costo para universidad pública | Institucional / piloto | Oculto (horas) | Desarrollo interno | Licencia incluida | Alto USD |

### 6.2 Positioning statement

> Para [JD], [TD] y [CC] de universidades bolivianas que pierden trazabilidad y plazos con herramientas dispersas, **SIGESA** es el **sistema de automatización** de acreditación que centraliza **Evidencias**, **Fases** y decisiones con normativa **CEUB/ARCU-SUR** nativa — **no un ERP** — a diferencia del status quo, de repositorios genéricos o de suites administrativas mal adaptadas al flujo **[TD]**.

### 6.3 Ventaja competitiva sostenible

- Encaje normativo nativo (meses de parametrización para replicar).
- Co-diseño con DUEA UMSS y datos de proceso reales.
- Modelo institucional sin membresía tipo ranking internacional.
- Acumulación de historial institucional para mejoras asistidas (Could, supervisión humana).

---

## 7. Propuesta de valor

### 7.1 Value Proposition Canvas (resumen)

| | S1 [CC] | S2 [TD]/[JD] |
|--|---------|----------------|
| **Gains** | Certeza de recepción; plazos visibles; menos búsqueda | Control global; reportes rápidos; auditoría |
| **Pains** | Versión final incierta; observaciones dispersas | Consolidación manual; backlog pre-auditoría |
| **Gain relievers** | Alertas; bandeja observaciones; importación masiva | Panel semáforo; filtros; bitácora |
| **Pain relievers** | Repositorio único; subsanación versionada; UX clara | Búsqueda ≤ 2 min; flujo formal; append-only |
| **Products** | Carga **Evidencia**, cronograma, móvil/responsive | Validación, reportes PDF, configuración |

---

## 8. Pricing y modelo de negocio

| Modelo | Descripción |
|--------|-------------|
| **Piloto UMSS (v1.0)** | Financiado por presupuesto institucional / proyecto AcredIA; **sin cobro a [P]** ni a estudiantes |
| **Post-piloto UMSS** | OPEX hosting, soporte y **evolución de automatización** en dominio acreditación — **por formalizar** con TI y finanzas |
| **Expansión CEUB (exploratorio)** | Licencia por universidad para el **mismo dominio** (automatización acreditación) — rango ilustrativo USD 40K–80K/año (**no comprometido**) |
| **Benchmark** | Desarrollo propio tipo DEVA: costo interno alto; **ERP genérico adaptado** a acreditación: parametrización costosa y fuera de propósito |

**Elasticidad:** baja en el corto plazo — la acreditación es requisito normativo; el costo de no acreditar supera la licencia (argumento institucional, no cifra inventada).

---

## 9. Go-to-market

### 9.1 Canales (piloto UMSS)

- Sponsor **[JD]** y acta de **canal oficial** de **Evidencia** (BRD-REQ-011, BRD-OBJ-11).
- Capacitación presencial por rol ([CC], [TD], [JD]).
- Comunicación DUEA–facultades (correo institucional, no WhatsApp para evidencia válida).
- Soporte AcredIA + mesa TI UMSS semanas 1–4.

### 9.2 Estrategia de lanzamiento

| Fase | Actividades | Alineación BRD |
|------|-------------|----------------|
| Pre-launch | Datos maestros; matriz permisos; línea base KPI | F0 BRD §19.1 |
| Launch piloto | 5–10 carreras; onboarding; soporte intensivo | F2 |
| Post-launch | Métricas; caso de éxito; expansión facultades; más **automatización** P2 (alertas, reportes, búsqueda) | F3–F4 |
| Evolución dominio | Certificados, exportaciones, IA asistencial, portal — **no** lanzamiento ERP | F5 BRD |
| Expansión CEUB | Demos: mismo sistema de automatización acreditación | Hipótesis H8 |

### 9.3 Funnel AARRR (contexto institucional)

| Etapa | Métrica | Meta piloto |
|-------|---------|-------------|
| Acquisition | Carreras onboardeadas | 5–10 |
| Activation | [CC] con ≥ 1 **Evidencia** semana 1 | ≥ 80 % |
| Retention | MAU / registrados actores clave | ≥ 80 % (BRD-OBJ-07) |
| Revenue | N/A v1.0 | — |
| Referral | Facultades que solicitan ingreso | ≥ 2 |

---

## 10. Métricas de éxito del producto

| ID | Métrica | North Star | Meta | Horizonte | BRD |
|----|---------|:----------:|------|-----------|-----|
| MRD-NSM-01 | % procesos activos con **Evidencias** críticas al día | **Sí** | ≥ 80 % | Piloto | BRD-KPI-02 |
| MRD-KPI-01 | Tiempo localización **Evidencia** (tarea representativa) | No | ≤ 2 min | Q4 2026 | BRD-KPI-01 |
| MRD-KPI-02 | Tiempo reporte ejecutivo PDF (P95) | No | ≤ 5 min | Q4 2026 | BRD-KPI-04 |
| MRD-KPI-03 | % observaciones gestionadas solo en SIGESA | No | ≥ 90 % | Piloto | BRD-REQ-008 |
| MRD-KPI-04 | Adopción actores clave (MAU/registrados) | No | ≥ 80 % | Mes +3 | BRD-KPI-05 |
| MRD-KPI-05 | CSAT post-tarea / sesión | No | ≥ 8,0/10 | Piloto | BRD-KPI-09 |
| MRD-KPI-06 | Éxito core tasks | No | ≥ 95 % | Piloto | BRD-KPI-10 |
| MRD-KPI-07 | Satisfacción [JD] (Likert 1–5) | No | ≥ 4,0 | Post-piloto | BRD-KPI-08 |
| MRD-KPI-08 | Uso responsive/móvil [CC] (si aplica piloto) | No | Por medir | Piloto | VoC alex — **validar meta con DUEA** |

> **Nota:** meta «< 5 segundos» en búsquedas del equipo alex es **tiempo de respuesta de interfaz** tras query, no sustituye el KPI de negocio **≤ 2 minutos** por tarea completa (BRD).

---

## 11. Requerimientos de mercado (alto nivel)

| ID | Requerimiento | Segmento | Prioridad | Justificación | BRD |
|----|---------------|----------|-----------|---------------|-----|
| MRD-N-01 | Flujos y taxonomía **CEUB/ARCU-SUR** nativos | S1, S2 | Must | Sin encaje normativo no hay adopción | BRD-REQ-004 |
| MRD-N-02 | Repositorio único de **Evidencia** con versionado (autor, fecha, historial) | S1, S2 | Must | Dolor 20+ min / amnesia versiones | BRD-REQ-006 |
| MRD-N-03 | Política **append-only**; subsanación por nueva versión | S2 | Must | Auditoría; anti-borrado | BRD-REQ-007 |
| MRD-N-04 | Flujo observaciones [TD]→[CC] con subsanación enlazada | S1, S2 | Must | Fin canal informal | BRD-REQ-008 |
| MRD-N-05 | Validación **[TD]** y máquina de estados sin saltos ilegales | S2 | Must | No omitir auditor DUEA | BRD-REQ-009 |
| MRD-N-06 | Panel semáforo por carrera/facultad para [JD] | S2, S3 | Must | Decisión sin Excel | BRD-REQ-010 |
| MRD-N-07 | Alertas automáticas de plazos y eventos críticos | S1, S2 | Must | Principal mitigación retrasos | BRD-REQ-011 |
| MRD-N-08 | Reporte ejecutivo PDF ≤ 2 clics / ≤ 5 min | S2, S3 | Must | Rendición de cuentas | BRD-REQ-012 |
| MRD-N-09 | RBAC [JD], [TD], [CC], [EE], [P] con correo UMSS | Todos | Must | Seguridad institucional | BRD-REQ-001 |
| MRD-N-10 | Un **Proceso** activo por tipo/carrera/periodo | S2 | Must | Integridad de ciclo | BRD-REQ-013 |
| MRD-N-11 | Cronograma coherente; cierre bloqueado con pendientes | S1, S2 | Must | Plazos institucionales | BRD-REQ-014 |
| MRD-N-12 | Bitácora de auditoría de acciones críticas | S2 | Should | Exportación auditoría | BRD-REQ-015 |
| MRD-N-13 | Búsqueda de **Evidencia** — tarea representativa ≤ 2 min | S2 | Must | North Star operativa | BRD-REQ-020 |
| MRD-N-14 | Importación masiva / planilla para [CC] | S1 | Must | Volumen autoevaluación | BRD-REQ-019 |
| MRD-N-15 | Respaldo automático diario verificable | S2 | Must | Continuidad | BRD-REQ-021 |
| MRD-N-16 | UX baja curva; mensajes accionables; progreso en cargas pesadas | S1, S2 | Must | Adopción perfiles ofimáticos | BRD-REQ-025, BRD-OBJ-12 |
| MRD-N-17 | Experiencia **responsive** para consulta/carga urgente [CC] | S1 | Should | VoC campo; BRD-REQ-025 | |
| MRD-N-18 | Portal [P] solo contenido publicado por [JD] | S4 | Should | Transparencia | BRD-REQ-016 |
| MRD-N-19 | Planes de mejora vinculados al **Proceso** | S1, S2 | Should | Core task investigación | BRD-REQ-022 |
| MRD-N-20 | Exportación consolidada PDF/Excel | S3 | Could | Análisis decanal | BRD-REQ-017 |
| MRD-N-21 | Certificados de acreditación descargables | S4 | Could | Trámite papel | BRD-REQ-023 |
| MRD-N-22 | IA explicable + chatbot FAQ normativo (sin dictámenes) | S2 | Could | Eficiencia con ética | BRD-REQ-018, 024 |

### 11.1 Resumen MoSCoW MRD

| Prioridad | Cantidad |
|-----------|----------|
| Must | 14 |
| Should | 5 |
| Could | 3 |

---

## 12. Voz del cliente (VoC)

| Fuente | Cita / señal | Implicancia MRD |
|--------|--------------|-----------------|
| Entrevistas DUEA 2026 | «20+ minutos» buscando versión final | MRD-N-13, MRD-KPI-01 |
| Entrevistas | «No sé si recibieron mi documento» | MRD-N-04, MRD-N-07 |
| Prueba Hi-Fi | Core tasks 96,66 %; CSAT 8,67/10 | MRD-KPI-05, 06 |
| Heurística UX | Contraste, validación en formularios, progreso en carga | MRD-N-16 |
| alex / campo | «Necesitamos vista mobile para coordinador» | MRD-N-17 (Should; validar alcance piloto) |
| alex / campo | «Demasiado esfuerzo visual» | MRD-N-16, WCAG en PRD |

---

## 13. Supuestos e hipótesis a validar

| ID | Hipótesis | Cómo validar | Criterio éxito | BRD |
|----|-----------|--------------|----------------|-----|
| H1 | Panel [JD] reduce consultas informales ≥ 30 % | Conteo antes/después piloto | ≥ 30 % | BRD-OBJ-05 |
| H2 | Versionado reduce tiempo localización ≥ 25 % | Tarea cronometrada pre/post | ≥ 25 % o ≤ 2 min absoluto | BRD-OBJ-01 |
| H3 | Alertas mejoran hitos a tiempo ≥ 20 pp | Plan vs. real | +20 pp vs. línea base | BRD-OBJ-03 |
| H4 | [CC] completa importación masiva sin soporte | UAT importación | ≥ 80 % sin ayuda | BRD-REQ-019 |
| H5 | [JD] genera ≥ 1 reporte/carrera/mes en piloto | Logs | Meta cumplida | BRD-OBJ-09 |
| H6 | 100 % observaciones piloto en SIGESA | Auditoría canal | ≥ 90 % | BRD-REQ-008 |
| H7 | [CC] completa carga evidencia en ≤ 3 pasos sin capacitación | UAT S1 | ≥ 95 % éxito | MRD-N-16 |
| H8 | Post-éxito UMSS, ≥ 2 universidades CEUB agendan demo | Outreach 12 meses | ≥ 2 demos | Expansión |

---

## 14. Riesgos de mercado

| ID | Riesgo | P | I | Mitigación | BRD |
|----|--------|---|---|------------|-----|
| MRD-RSK-01 | Doble canal correo/mensajería | A | A | Acta canal oficial | BRD-RSK-01 |
| MRD-RSK-02 | Resistencia cambio / baja adopción | A | A | UX, campeones, capacitación | BRD-RSK-02 |
| MRD-RSK-03 | Datos maestros incompletos | M | A | Plan carga con secretarías | BRD-RSK-03 |
| MRD-RSK-04 | Desalineación normativa CEUB/ARCU-SUR | B | A | Comité normativo | BRD-RSK-04 |
| MRD-RSK-05 | Línea base no medida | M | M | Plan pre-piloto | BRD-RSK-06 |
| MRD-RSK-06 | Red institucional lenta en picos | M | A | Pruebas carga/red | BRD-RSK-09 |
| MRD-RSK-07 | Competidor nacional (DEVA u otro) | M | A | Time-to-market piloto | — |
| MRD-RSK-08 | Semana sin contacto discovery | M | M | Declarar en riesgo activo | §3.4 |

---

## 15. Trazabilidad BRD ↔ MRD ↔ PRD (borrador)

> Matriz maestra: `matriz_trazabilidad.md`. IDs PRD son **placeholders** hasta PRD Dorado.

| MRD ID | BRD | PRD (borrador) | FSD (borrador) |
|--------|-----|----------------|----------------|
| MRD-N-01 | BRD-REQ-004 | PRD-REQ-TAXONOMY | FSD-UC-007 |
| MRD-N-02 | BRD-REQ-005, 006 | PRD-REQ-EVID | FSD-UC-001 |
| MRD-N-03 | BRD-REQ-007, BRD-CST-01 | PRD-REQ-EVID-VERSION | FSD-UC-002 |
| MRD-N-04 | BRD-REQ-008 | PRD-REQ-OBS | FSD-UC-010 |
| MRD-N-05 | BRD-REQ-009 | PRD-REQ-STATE | FSD-UC-007 |
| MRD-N-06 | BRD-REQ-010 | PRD-REQ-DASH | FSD-UC-003 |
| MRD-N-07 | BRD-REQ-011 | PRD-REQ-ALERT | FSD-UC-005 |
| MRD-N-08 | BRD-REQ-012 | PRD-REQ-REPORT | FSD-UC-004 |
| MRD-N-09 | BRD-REQ-001 | PRD-REQ-AUTH | FSD-UC-006 |
| MRD-N-10 | BRD-REQ-013 | PRD-REQ-PROCESS | FSD-UC-007 |
| MRD-N-11 | BRD-REQ-014 | PRD-REQ-SCHEDULE | FSD-UC-007 |
| MRD-N-12 | BRD-REQ-015 | PRD-REQ-AUDIT-LOG | FSD-UC-AUDIT |
| MRD-N-13 | BRD-REQ-020, BRD-OBJ-01 | PRD-REQ-SEARCH | FSD-UC-008 |
| MRD-N-14 | BRD-REQ-019 | PRD-REQ-BULK | FSD-UC-011 |
| MRD-N-15 | BRD-REQ-021 | PRD-REQ-BACKUP | FSD-UC-ADM |
| MRD-N-16 | BRD-REQ-025, BRD-OBJ-12 | PRD-REQ-UX | FSD-UC-UX |
| MRD-N-17 | BRD-REQ-025 | PRD-REQ-MOBILE | FSD-UC-UX |
| MRD-N-18 | BRD-REQ-016 | PRD-REQ-PORTAL | FSD-UC-015 |
| MRD-N-19 | BRD-REQ-022 | PRD-REQ-IMPROVE | FSD-UC-012 |
| MRD-N-20 | BRD-REQ-017 | PRD-REQ-EXPORT | FSD-UC-REP |
| MRD-N-21 | BRD-REQ-023 | PRD-REQ-CERT | FSD-UC-016 |
| MRD-N-22 | BRD-REQ-018, 024 | PRD-REQ-AI | FSD-UC-030 |

### Mapeo agregado BRD → MRD (desde BRD §20)

| Destino BRD | MRD IDs |
|-------------|---------|
| MRD-JTBD-01 / SEARCH | MRD-N-13 |
| MRD-EVID-01 | MRD-N-02, 03, 04, 14 |
| MRD-FLOW-01 / 02 | MRD-N-01, 05, 10, 11 |
| MRD-DASH-01 | MRD-N-06, 08 |
| MRD-ALERT-01 | MRD-N-07 |
| MRD-PUB-01 | MRD-N-18, 21 |
| MRD-OPS-02 | MRD-N-15 |
| MRD-UX-01 / 02 | MRD-N-16, 17 |

---

## 16. Decisiones pendientes (heredadas y MRD)

| ID | Tema | Impacto MRD |
|----|------|-------------|
| BRD-Q-01 … Q-08 | Ver `docs/01_brd/BRD.md` §21 | Metas, piloto, discovery |
| MRD-Q-01 | Meta adopción móvil [CC] en piloto | MRD-N-17, MRD-KPI-08 |
| MRD-Q-02 | TAM/SAM cifras oficiales CEUB | §3.1 |
| MRD-Q-03 | Modelo comercial post-UMSS | §8 |

---

## 17. Aprobaciones

| Rol | Nombre | Firma | Fecha |
|-----|--------|-------|-------|
| Sponsor ([JD]) | | | |
| Product Manager | | | |
| Docente revisor | | | |

---

## 18. Registro de cambios

| Versión | Timestamp | Autor | Cambio |
|---------|-----------|-------|--------|
| v1.0 | `2026-05-15T00:00:00-04:00` | Equipo SIGESA | Borrador inicial `docs/02_mrd/` |
| Dorada v1.0 | `2026-05-16T15:10:14-04:00` | Equipo AcredIA | Versión Dorada alineada a BRD Dorada v2.0 |
| **Dorada v1.1** | `2026-05-16T15:24:02-04:00` | Equipo AcredIA | Sistema de automatización (no ERP); post go-live = dominio acreditación |

---

## 19. Anexos

- `docs/01_brd/BRD.md` — requerimientos y reglas de negocio fuente.
- `team/borisAngulo/docs/02_mrd/MRD.md` — segmentación Seg-1/Seg-2 y competencia.
- `team/aylenGonzales/02_mrd/MRD_v1.md` — JTBD, hipótesis discovery, 12 MRD-N.
- `team/alexAlvarez/docs/02_mrd/MRD.md` — VoC búsqueda y móvil.
- `team/Marlene/02_mrd/MRD.md` — marco S1–S4 y siete pilares analíticos.
- Bitácora UX feb–mar 2026 — **enlazar ruta cuando se publique en repo**.

---

## 20. Checklist de auditoría documental (skill + plantilla)

| Criterio | Estado | Referencia |
|----------|--------|------------|
| TAM/SAM/SOM con fuentes o «por confirmar» | Cumple | §3.1 |
| ≥ 2 personas completas | Cumple (5) | §4.2 |
| ≥ 3 JTBD | Cumple (14) | §5 |
| ≥ 2 competidores + do-nothing | Cumple | §6.1 |
| Positioning statement | Cumple | §6.2 |
| Pricing y GTM | Cumple | §8–9 |
| North Star + ≥ 3 KPIs fechados | Cumple (8 KPI + NSM) | §10 |
| Requerimientos MRD-N priorizados | Cumple (22) | §11 |
| ≥ 3 hipótesis con criterio | Cumple (8) | §13 |
| Trazabilidad BRD y PRD iniciada | Cumple | §15 |
| Segmentos operativo / gerencial / público | Cumple | S1–S4 |
| JTBD con Fase/Evidencia (skill) | Cumple | §5 |
| VoC con métricas reales citadas | Cumple | §12 |
| Sin roles inventados | Cumple | glosario |
| Coherente append-only y [TD] valida | Cumple | MRD-N-03, 05 |

**Veredicto:** **listo para iniciar PRD Dorado** en `docs/03_prd/PRD.md`.

---

## Control de versión (cierre del documento)

| Campo | Valor |
|-------|-------|
| **Versión** | **Dorada v1.1** |
| **Timestamp** | `2026-05-16T15:24:02-04:00` |
| **Cambios aplicados** | Posicionamiento: automatización de acreditación, no ERP; GTM post-launch y F5 alineados a BRD v2.1. |

*Documento canónico SIGESA — UMSS, Cochabamba, Bolivia, 2026.*  