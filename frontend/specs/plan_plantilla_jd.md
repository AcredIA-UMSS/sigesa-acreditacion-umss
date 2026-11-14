# SIGESA Plantillas — JD nueva plantilla

## Application Overview

Flujo JD: listado `/admin/plantillas` («Gestión de plantillas»), link/botón «Nueva plantilla» → `/admin/plantillas/nueva`, título «Nueva plantilla», metadatos (Nombre, Tipo normativo CEUB/ARCU-SUR, Descripción), «Guardar». Jerarquía N1→N2→N3 en panel inferior (borrador).

**Credenciales:** `jd@umss.edu.bo` / `JefeDemo2026!`

**Datos únicos:** nombre `Plantilla E2E ${Date.now()}` para evitar colisiones.

## Test Scenarios

### 1. Plantillas normativas JD

**Seed:** `tests/seed.spec.ts`

#### 1.1. JD crea plantilla en borrador

**File:** `tests/agente/plantillas-jd-nueva.spec.ts`

**Steps:**
  1. Login JD
    - expect: Acceso a rutas `/admin`
  2. Navegar a `/admin/plantillas`
    - expect: Heading «Gestión de plantillas» visible
    - expect: Link o botón «Nueva plantilla» visible
  3. Clic «Nueva plantilla»
    - expect: URL `/admin/plantillas/nueva`
    - expect: Heading «Nueva plantilla» o «Metadatos de la plantilla» visible
  4. Completar «Nombre» con nombre único, «Tipo normativo» CEUB, «Descripción» opcional
    - expect: Campos sin error de validación
  5. Clic «Guardar»
    - expect: URL cambia a `/admin/plantillas/{id}` (modo edición)
    - expect: Badge o estado BORRADOR/DRAFT visible
    - expect: Plantilla listada al volver a `/admin/plantillas`

#### 1.2. Listado plantillas JD

**File:** `tests/agente/plantillas-jd-listado.spec.ts`

**Steps:**
  1. Login JD y abrir `/admin/plantillas`
    - expect: Tabla o lista de plantillas cargada
    - expect: Filtros o mensaje «No hay plantillas» coherente con seed
