# SIGESA Revisión — TD aprobar dimensión/área

## Application Overview

Flujo TD (`session.role === 'TD'`): detalle de proceso ACTIVE, expandir estructura normativa, revisar indicador con evidencia en estado SUBIDO/SUBSANADO, sección «Revisión técnica del indicador», botón «Aprobar». Cierre de dimensión/nivel 1: `Level1CloseAction` — botón «Cerrar dimensión» o «Cerrar nivel 1» cuando todos los indicadores del subárbol están APROBADO.

**Precondición 1.1:** indicador con al menos una evidencia (ejecutar carga CC antes o usar seed). **Precondición cierre:** todos los indicadores del subárbol aprobados.

**Credenciales:** `td@umss.edu.bo` / `TecnicoDemo2026!`

**Proceso seed:** `8d38cabf-02f5-4d62-86e8-4aae588c4f9c` (ADM-EMP).

## Test Scenarios

### 1. Revisión TD

**Seed:** `tests/seed.spec.ts`

#### 1.1. TD aprueba indicador con evidencia

**File:** `tests/agente/procesos-td-aprobar-indicador.spec.ts`

**Steps:**
  1. Login TD en `/login`
    - expect: Sidebar rol TD visible
  2. Ir a `/procesos/{uuid}` del proceso ACTIVE ADM-EMP
    - expect: «Estructura del proceso» visible
  3. Expandir dimensión y área hasta un indicador con evidencias y botones de revisión
    - expect: Texto «Revisión técnica del indicador» visible
    - expect: Botón «Aprobar» visible (no mensaje «No hay evidencias cargadas»)
  4. Clic «Aprobar»
    - expect: Mensaje de éxito con «aprobado»
    - expect: Estado del indicador refleja APROBADO en UI

#### 1.2. TD cierra dimensión (nivel 1) cuando subárbol aprobado

**File:** `tests/agente/procesos-td-cerrar-dimension.spec.ts`

**Steps:**
  1. Login TD y abrir detalle proceso con dimensión lista para cierre
    - expect: Pie de dimensión con «Cierre de dimensión» o similar visible
  2. Clic botón «Cerrar dimensión» / «Cerrar nivel 1» y confirmar `window.confirm`
    - expect: Mensaje de éxito «completado correctamente» o dimensión en estado COMPLETADA
    - expect: Botón de cierre ya no disponible

#### 1.3. TD no aprueba sin evidencia

**File:** `tests/agente/procesos-td-sin-aprobar-sin-evidencia.spec.ts`

**Steps:**
  1. Login TD, abrir indicador sin evidencias
    - expect: Texto «No hay evidencias cargadas» visible
    - expect: Botón «Aprobar» NO visible
