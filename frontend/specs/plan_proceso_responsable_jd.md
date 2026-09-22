# SIGESA Proceso — JD asignar coordinador

## Application Overview

Flujo JD: en detalle de proceso ACTIVE, sección responsable del proceso, botón «Asignar responsable» o «Cambiar responsable», modal «Asignar responsable», select «Coordinador responsable», «Confirmar asignación». Solo JD con proceso ACTIVE (`canManage` en `ProcessResponsibleContainer`).

**Seed dev:** proceso ACTIVE UUID `8d38cabf-02f5-4d62-86e8-4aae588c4f9c` — Administración de Empresas (ADM-EMP).

**Credenciales:** `jd@umss.edu.bo` / `JefeDemo2026!`

**Candidatos:** CC activos de la carrera del proceso (ej. coordinador demo en seed).

## Test Scenarios

### 1. Responsable de proceso JD

**Seed:** `tests/seed.spec.ts`

#### 1.1. JD asigna coordinador en proceso ACTIVE

**File:** `tests/agente/procesos-jd-asignar-coordinador.spec.ts`

**Steps:**
  1. Login JD en `/login`
    - expect: Área autenticada JD visible
  2. Navegar a `/procesos` y abrir detalle «Administración de Empresas» (ADM-EMP)
    - expect: Estado «Activo» visible
    - expect: Sección con texto sobre coordinador o «Sin responsable asignado»
  3. Clic botón «Asignar responsable» o «Cambiar responsable»
    - expect: Dialog «Asignar responsable» visible
    - expect: Select «Coordinador responsable» visible
  4. Elegir un CC en el select (opción con email `@umss.edu.bo`)
    - expect: Botón «Confirmar asignación» habilitado
  5. Clic «Confirmar asignación»
    - expect: Modal cierra
    - expect: Nombre del coordinador visible en la sección responsable
    - expect: No alerta de error persistente

#### 1.2. JD no gestiona responsable si proceso no ACTIVE

**File:** `tests/agente/procesos-jd-sin-asignar-inactivo.spec.ts`

**Steps:**
  1. Login JD y abrir detalle de proceso no ACTIVE (si existe en seed)
    - expect: Botón «Asignar responsable» NO visible
