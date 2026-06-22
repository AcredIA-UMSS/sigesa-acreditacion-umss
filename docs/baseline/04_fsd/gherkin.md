# Escenarios Gherkin — SIGESA / AcredIA

## Control de versión

| Campo | Valor |
|-------|-------|
| **Versión** | Dorada v1.0 |
| **Timestamp** | `2026-05-16T18:30:00-04:00` |
| **Fuente** | [`docs/03_prd/PRD.md`](../03_prd/PRD.md) §5 · [`FSD.md`](FSD.md) |
| **Regla QA** | `.cursor/rules/04_sigesa_qa_gherkin_coverage.mdc` |
| **NFR / TC** | [`docs/05_nfr/NFR_ISO25010.md`](../05_nfr/NFR_ISO25010.md) |

> Escenarios en español (`# language: es`). Cada bloque indica **PRD-US**, **FSD-UC** y tags sugeridos para CI (`@Tag`).

**Convención de tags:** `@PRD-US-xxx` `@FSD-UC-xxx` `@NFR-xxx` `@TC-xxx`

---

## FSD-UC-001 — Autenticación (`PRD-US-001`, `PRD-US-003`)

```gherkin
# language: es
@PRD-US-001 @FSD-UC-001 @NFR-008 @TC-01
Característica: Autenticación y sesión

  Escenario: Inicio de sesión exitoso con rol asignado
    Dado un usuario con correo institucional UMSS activo y rol [CC], [TD] o [JD]
    Cuando inicia sesión con credenciales válidas
    Entonces el sistema crea una sesión autenticada
    Y redirige al panel correspondiente a su rol

  Escenario: Credenciales inválidas
    Dado un usuario en la pantalla de inicio de sesión
    Cuando ingresa credenciales incorrectas
    Entonces el sistema rechaza el acceso
    Y muestra un mensaje de error sin revelar si el usuario existe

@PRD-US-003 @FSD-UC-001 @TC-SAD-005
  Escenario: Acción sensible sin autenticación
    Dado un usuario no autenticado
    Cuando intenta cargar o aprobar una Evidencia
    Entonces el sistema rechaza la operación con código de no autorizado
    Y no registra cambios de estado
```

---

## FSD-UC-002 — Gestión usuarios (`PRD-US-002`)

```gherkin
# language: es
@PRD-US-002 @FSD-UC-002 @TC-02
Característica: Gestión de usuarios [JD]

  Escenario: Alta de usuario con rol
    Dado un [JD] autenticado
    Cuando registra un usuario con correo UMSS y rol [CC]
    Entonces el sistema crea la cuenta inactiva hasta primer acceso
    Y asocia permisos solo a la carrera autorizada

  Escenario: Revocación de acceso
    Dado un usuario [CC] que deja la coordinación
    Cuando el [JD] desactiva la cuenta
    Entonces el usuario no puede iniciar sesión
    Y conserva historial de acciones previas en auditoría
```

---

## FSD-UC-003 — Plantillas (`PRD-US-023`)

```gherkin
# language: es
@PRD-US-023 @FSD-UC-003 @TC-03
Característica: Plantillas normativas CEUB/ARCU-SUR

  Escenario: Activación de plantilla CEUB
    Dado un [JD] con plantilla CEUB validada
    Cuando activa la plantilla para el periodo vigente
    Entonces los nuevos Procesos usan Fases e Indicadores de esa plantilla
    Y los Procesos en curso conservan la plantilla con la que iniciaron
```

---

## FSD-UC-004 — Cargar Evidencia (`PRD-US-005`, `PRD-US-025`)

```gherkin
# language: es
@PRD-US-005 @FSD-UC-004 @FSD-BR-01 @TC-04
Característica: Carga de Evidencia

  Escenario: Carga exitosa con metadatos obligatorios
    Dado un [CC] autenticado y un Indicador válido en su carrera
    Cuando carga una Evidence y completa metadatos obligatorios
    Entonces el sistema crea la Evidencia versión 1 vinculada al Indicador
    Y notifica al [TD] asignado que hay revisión pendiente

  Escenario: Carga sin clasificación rechazada
    Dado un [CC] en el formulario de carga
    Cuando intenta guardar sin Indicador/Criterio asociado
    Entonces el sistema rechaza la operación
    Y indica qué campo falta completar

@PRD-US-025 @FSD-UC-004 @NFR-011 @TC-04b
  Escenario: Progreso en carga de Evidence grande
    Dado un [CC] cargando una Evidence mayor al umbral configurado de 5 MB
    Cuando la carga está en curso
    Entonces el sistema muestra barra de progreso determinada
    Y evita permitir un segundo envío duplicado hasta completar
```

---

## FSD-UC-005 — Versionado append-only (`PRD-US-007`, `PRD-US-008`)

```gherkin
# language: es
@PRD-US-007 @FSD-UC-005 @TC-05
Característica: Historial de versiones

  Escenario: Versión vigente visible
    Dado un Indicador con Evidencia en versiones 1 y 2
    Cuando el [TD] abre el historial
    Entonces la versión 2 aparece como vigente
    Y la versión 1 permanece consultable en solo lectura

  Escenario: Trazabilidad de subsanación
    Dado la versión 2 creada por subsanación
    Cuando se consulta su detalle
    Entonces muestra el identificador de la observación origen

@PRD-US-008 @FSD-UC-005 @NFR-017 @TC-SAD-001
  Escenario: Intento de eliminar Evidencia aprobada
    Dado una Evidencia en estado Aprobado
    Cuando un usuario intenta eliminarla físicamente
    Entonces el sistema rechaza la operación
    Y registra el intento en la bitácora de auditoría
    Y mantiene todas las versiones existentes
```

---

## FSD-UC-006 — Subsanación (`PRD-US-006`)

```gherkin
# language: es
@PRD-US-006 @FSD-UC-006 @FSD-BR-06 @TC-06
Característica: Subsanación de Evidencia

  Escenario: Subsanación enlazada a observación
    Dado un Indicador en estado Observado con observación O-123
    Cuando el [CC] carga una nueva versión de Evidencia
    Entonces el sistema registra la versión 2 enlazada a O-123
    Y conserva la versión 1 sin eliminarla
```

---

## FSD-UC-007 — Búsqueda (`PRD-US-004`)

```gherkin
# language: es
@PRD-US-004 @FSD-UC-007 @NFR-002 @TC-14
Característica: Búsqueda de Evidencia

  Escenario: Búsqueda con resultados en tiempo de tarea acotado
    Dado un [TD] autenticado con Evidencias indexadas en el piloto
    Cuando busca por carrera, Fase e Indicador con término conocido
    Entonces el sistema muestra resultados relevantes
    Y la tarea completa de localizar y abrir la Evidencia correcta toma como máximo 2 minutos

  Escenario: Sin resultados
    Dado que no existen Evidencias que coincidan con el filtro
    Cuando ejecuta la búsqueda
    Entonces el sistema muestra "No se encontraron resultados" con sugerencia de ampliar filtros
```

---

## FSD-UC-008 — Rechazar Indicador (`PRD-US-009`)

```gherkin
# language: es
@PRD-US-009 @FSD-UC-008 @NFR-018 @TC-SAD-003
Característica: Rechazo de Indicador

  Escenario: Rechazo con justificación obligatoria
    Dado un [TD] revisando un Indicador
    Cuando confirma rechazo sin texto de justificación
    Entonces el sistema impide el rechazo
    Y solicita motivo obligatorio

  Escenario: Rechazo exitoso notifica al CC
    Dado un [TD] con justificación válida
    Cuando confirma el rechazo del Indicador
    Entonces el Indicador pasa a estado Observado
    Y el [CC] recibe notificación en un máximo de 15 minutos
```

---

## FSD-UC-009 — Aprobar Indicador (`PRD-US-010`)

```gherkin
# language: es
@PRD-US-010 @FSD-UC-009 @TC-07b
Característica: Aprobación de Indicador

  Escenario: Aprobación exitosa
    Dado un [TD] con Evidencia conforme
    Cuando aprueba el Indicador
    Entonces el estado pasa a Aprobado
    Y el [CC] recibe notificación en un máximo de 15 minutos

  Escenario: Último indicador prepara cierre de fase
    Dado todos los Indicadores de la Fase en Aprobado salvo uno
    Cuando el [TD] aprueba el último pendiente
    Entonces el sistema marca la Fase como lista para cierre según reglas
```

---

## FSD-UC-010 — Cerrar Fase (`PRD-US-011`)

```gherkin
# language: es
@PRD-US-011 @FSD-UC-010 @NFR-018 @TC-SAD-002
Característica: Avance y cierre de Fase

  Escenario: Avance de Fase bloqueado con indicadores pendientes
    Dado una Fase con al menos un Indicador no Aprobado
    Cuando el [TD] intenta cerrar la Fase
    Entonces el sistema rechaza la transición
    Y lista los Indicadores pendientes

  Escenario: Salto de estado no autorizado
    Dado un usuario [CC] sin permiso de cierre de Fase
    Cuando intenta forzar estado Cerrado en la Fase
    Entonces el sistema rechaza la operación
```

---

## FSD-UC-011 — Dashboard [CC] (`PRD-US-012`, `PRD-US-015`)

```gherkin
# language: es
@PRD-US-012 @FSD-UC-011 @TC-09a
Característica: Dashboard del Coordinador de Carrera

  Escenario: Vista de carrera propia
    Dado un [CC] autenticado de la carrera X
    Cuando abre su dashboard
    Entonces ve el avance por Fase de la carrera X
    Y no ve datos de otras carreras

  Escenario: Acceso rápido a observación
    Dado una observación abierta en el dashboard
    Cuando selecciona la observación
    Entonces navega al Indicador y formulario de subsanación

@PRD-US-015 @FSD-UC-011 @TC-09c
  Escenario: Orden por fecha límite
    Dado tres observaciones abiertas con plazos distintos
    Cuando el [CC] abre su lista
    Entonces la observación con plazo más próximo aparece primero
```

---

## FSD-UC-012 — Bandeja [TD] (`PRD-US-014`)

```gherkin
# language: es
@PRD-US-014 @FSD-UC-012 @TC-09b
Característica: Bandeja de auditoría [TD]

  Escenario: Filtro por carrera y estado
    Dado un [TD] en la bandeja de revisión
    Cuando filtra por carrera "Ingeniería" y estado Pendiente
    Entonces solo ve Indicadores que cumplen ambos criterios
```

---

## FSD-UC-013 — Semáforo [JD] (`PRD-US-013`)

```gherkin
# language: es
@PRD-US-013 @FSD-UC-013 @TC-09
Característica: Panel semáforo ejecutivo

  Escenario: Vista consolidada en menos de 2 minutos
    Dado un [JD] autenticado
    Cuando abre el panel ejecutivo global
    Entonces ve semáforos por carrera y facultad
    Y obtiene la vista sin asistencia técnica ad-hoc en menos de 2 minutos

  Escenario: Coherencia con reglas de completitud
    Dado reglas de completitud configuradas para el piloto
    Cuando una carrera tiene indicadores críticos vencidos
    Entonces el semáforo de esa carrera es Rojo
```

---

## FSD-UC-014 — Reporte PDF (`PRD-US-021`)

```gherkin
# language: es
@PRD-US-021 @FSD-UC-014 @NFR-003 @TC-11
Característica: Reporte ejecutivo PDF

  Escenario: Generación de reporte ejecutivo
    Dado un [JD] en el panel con filtros aplicados
    Cuando selecciona "Generar reporte PDF" desde el contexto de trabajo
    Entonces el sistema produce un PDF con marca temporal y filtros
    Y el tiempo de generación P95 es como máximo 5 minutos
```

---

## FSD-UC-015 — Notificaciones (`PRD-US-017`–`019`)

```gherkin
# language: es
@PRD-US-017 @FSD-UC-015 @NFR-004 @TC-10a
Característica: Notificaciones a [CC]

  Escenario: Enlace profundo desde notificación
    Dado un [CC] que recibe notificación de rechazo
    Cuando abre el enlace del correo
    Entonces aterriza en el Indicador y observación sin reautenticación adicional si la sesión sigue activa

@PRD-US-018 @FSD-UC-015 @TC-10b
  Escenario: Alerta de plazo próximo
    Dado una Fase con fecha límite en 3 días y configuración de alerta activa
    Cuando el job de alertas se ejecuta
    Entonces el [CC] recibe correo institucional con enlace directo al Indicador

@PRD-US-019 @FSD-UC-015 @TC-10c
  Escenario: Nueva carga en bandeja [TD]
    Dado un [CC] que confirma carga de Evidencia
    Cuando el registro queda en estado Pendiente de revisión
    Entonces el [TD] asignado recibe notificación con enlace a la bandeja filtrada
```

---

## FSD-UC-016 — Portal [P] (`PRD-US-016`, `PRD-US-020`)

```gherkin
# language: es
@PRD-US-016 @FSD-UC-016 @TC-PUB
Característica: Portal público de transparencia

  Escenario: Portal sin borradores
    Dado contenido no publicado por [JD]
    Cuando un visitante anónimo consulta el portal
    Entonces no ve borradores ni observaciones internas
    Y solo información con flag Publicado

  Escenario: Consulta de estado publicado
    Dado una carrera con estado publicado oficialmente
    Cuando el visitante busca por nombre de carrera
    Entonces ve el estado de acreditación vigente

@PRD-US-020 @FSD-UC-016 @TC-PUB2
  Escenario: Descarga solo si publicado
    Dado un certificado marcado como Publicado por [JD]
    Cuando un visitante solicita descarga
    Entonces obtiene el PDF oficial con metadatos de vigencia
    Y no puede descargar borradores
```

---

## FSD-UC-017 — Bitácora (`PRD-US-022`)

```gherkin
# language: es
@PRD-US-022 @FSD-UC-017 @TC-12
Característica: Bitácora de auditoría

  Escenario: Registro en bitácora
    Dado una acción de aprobación de Indicador por [TD]
    Cuando la acción se confirma
    Entonces el sistema registra actor, timestamp, acción e identificador de entidad
```

---

## FSD-UC-018 — Importación (`PRD-US-024`)

```gherkin
# language: es
@PRD-US-024 @FSD-UC-018 @TC-15
Característica: Importación masiva desde planilla

  Escenario: Importación válida desde planilla
    Dado un [CC] con plantilla de importación descargada
    Cuando sube la planilla con filas válidas
    Entonces el sistema crea actividades/evidencias en borrador vinculadas a Indicadores
    Y reporta filas rechazadas con causa por fila

  Escenario: Planilla con errores de formato
    Dado una planilla de importación con columnas obligatorias faltantes
    Cuando intenta importar
    Entonces el sistema rechaza la planilla completa
    Y no crea registros parciales inconsistentes
```

---

## Cobertura

| Métrica | Valor |
|---------|-------|
| PRD-US con Gherkin | 24/24 |
| FSD-UC cubiertos | 18/18 |
| Sad paths obligatorios | US-008, 009, 011, 003 (ver `TC-SAD-*`) |

---

## Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| Dorada v1.0 | 2026-05-16 | Consolidación PRD §5 + FSD; tags CI |
