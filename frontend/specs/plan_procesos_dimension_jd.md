# SIGESA Procesos — dimensión JD

## Application Overview

Flujo JD sobre procesos de acreditación: listado `/procesos`, detalle del proceso ACTIVE de carrera **Administración de Empresas** (ADM-EMP), edición estructural normativa en `/procesos/{uuid}/estructura`, alta de dimensión nivel 1 y verificación en detalle.

**Datos seed (dev):** proceso ACTIVE UUID `8d38cabf-02f5-4d62-86e8-4aae588c4f9c` — Administración de Empresas, plantilla CEUB prueba, estado Activo.

**Localizadores:** getByRole / getByLabel / getByText / getByTestId. Sin waitForTimeout. Sin copy LLM.

**Precondición caso 1.1:** si `dimensionPrueba` ya existe por un run anterior, el alta puede fallar o duplicar — usar nombre único (`dimensionPrueba-{timestamp}`) o limpiar dimensión en setup manual.

## Test Scenarios

### 1. Estructura normativa — JD crea dimensión

**Seed:** `tests/seed.spec.ts`

#### 1.1. JD crea dimensionPrueba en Administración de Empresas y la ve en detalle

**File:** `tests/agente/procesos-jd-crear-dimension-prueba.spec.ts`

**Steps:**
  1. Navegar a `/login`, completar correo `jd@umss.edu.bo` y contraseña con `getByRole('textbox', { name: 'Contraseña' })`, clic en «Iniciar sesión»
    - expect: URL contiene `/admin/users` o área autenticada JD visible
    - expect: Sidebar muestra «Jefatura DUEA [JD]»
  2. Ir a `/procesos` (link «Ver procesos» bajo «GESTIÓN PROCESOS»)
    - expect: Heading «Procesos de acreditación» visible
    - expect: Tabla con columna «Carrera» visible
  3. Clic en link «Ver detalle del proceso Administración de Empresas» (ADM-EMP)
    - expect: URL coincide con `/procesos/{uuid}`
    - expect: Texto «Administración de Empresas» visible en cabecera
    - expect: Estado «Activo» visible
    - expect: Sección heading «Estructura del proceso» visible
  4. Clic en botón «Editar estructura normativa»
    - expect: URL contiene `/estructura`
    - expect: Texto «Solo se listan las dimensiones (nivel 1)» visible
    - expect: Botón «Agregar nueva dimensión (nivel 1)» visible
  5. Expandir «Agregar nueva dimensión (nivel 1)» si está colapsado; completar «Nombre» con `dimensionPrueba`; opcional «Descripción»; clic «Agregar nivel 1»
    - expect: No hay `role="alert"` de error de persistencia
    - expect: Lista incluye dimensión con texto «dimensionPrueba»
  6. Clic en link «Volver al detalle»
    - expect: URL vuelve a `/procesos/{uuid}` (sin `/estructura`)
    - expect: Heading «Estructura del proceso» visible
  7. Verificar dimensión en detalle
    - expect: Texto «dimensionPrueba» visible (botón/fila de dimensión o option en filtro de búsqueda)
    - expect: Botón «Editar estructura normativa» visible

#### 1.2. JD no ve edición si proceso no ACTIVE

**File:** `tests/agente/procesos-jd-sin-editar-estructura-inactivo.spec.ts`

**Steps:**
  1. Login JD y abrir detalle de un proceso en estado distinto de ACTIVE (si existe en seed)
    - expect: Sección «Estructura del proceso» puede mostrarse
    - expect: Botón «Editar estructura normativa» NO visible

#### 1.3. Listado vacío para rol sin procesos

**File:** `tests/agente/procesos-listado-vacio.spec.ts`

**Steps:**
  1. Login con rol sin procesos en alcance (si aplica en seed)
    - expect: Mensaje «Sin procesos visibles» o tabla vacía
    - expect: URL permanece en `/procesos`
