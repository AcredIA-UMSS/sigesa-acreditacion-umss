# SIGESA Evidencia — CC subir en proceso

## Application Overview

Flujo CC (Coordinador de Carrera): login, ir a «Cargar evidencia» (`/evidencias/cargar`) o desde detalle de proceso expandir árbol normativo y usar «Subir evidencia» en un indicador. Completar proceso activo de su carrera, indicador, descripción y archivo; verificar éxito (indicador SUBIDO).

**Credenciales dev:** `cc@umss.edu.bo` / `CoordDemo2026!` — carrera seed Ingeniería de Sistemas.

**Precondición:** proceso ACTIVE visible para CC (responsable asignado o carrera del CC); al menos un indicador en el proceso. Si no hay procesos, el plan incluye escenario vacío.

**Localizadores:** getByRole / getByLabel / getByText. Sin waitForTimeout. Un test por archivo cuando sea posible.

**Fixture:** `tests/fixtures/evidencia-e2e.pdf` (PDF pequeño).

## Test Scenarios

### 1. Evidencia CC

**Seed:** `tests/seed.spec.ts`

#### 1.1. CC carga evidencia vía Cargar evidencia

**File:** `tests/agente/evidencia-cc-cargar-pagina.spec.ts`

**Steps:**
  1. Navegar a `/login`, completar correo `cc@umss.edu.bo` y contraseña, clic «Iniciar sesión»
    - expect: URL deja `/login`
    - expect: Sidebar muestra rol CC / Coordinador
  2. Ir a `/evidencias/cargar` (link sidebar «CARGAR EVIDENCIA» o navegación directa)
    - expect: Heading «Cargar Evidencia» visible
    - expect: Select con label «Proceso» visible
  3. Seleccionar primer proceso activo disponible en combobox «Proceso»
    - expect: Combobox «Indicador normativo» habilitado
    - expect: No mensaje «No hay procesos activos en su carrera»
  4. Seleccionar primer indicador en «Indicador normativo»
    - expect: Helper text con breadcrumb del indicador visible
  5. Completar textarea «DESCRIPCIÓN» con texto de prueba único (`E2E evidencia ${Date.now()}`)
    - expect: Valor reflejado en el campo
  6. Adjuntar archivo con `#evidence-file` o label «Seleccionar archivo» usando fixture PDF
    - expect: Nombre del archivo visible en la zona de drop
  7. Clic botón «Subir evidencia» del formulario
    - expect: Mensaje de éxito o resultado sin `role="alert"` de error
    - expect: Formulario no bloqueado permanentemente

#### 1.2. CC sube evidencia desde detalle de proceso

**File:** `tests/agente/evidencia-cc-desde-proceso.spec.ts`

**Steps:**
  1. Login CC y abrir `/procesos`
    - expect: Heading «Procesos de acreditación» visible
  2. Abrir detalle de un proceso ACTIVE de su carrera (link «Ver detalle del proceso …»)
    - expect: Sección «Estructura del proceso» visible
  3. Expandir árbol: dimensión → área → indicador hasta ver acciones del indicador
    - expect: Botón o enlace «Subir evidencia» visible si `canUpload`
  4. Clic «Subir evidencia», completar modal (descripción + archivo) y confirmar
    - expect: Modal se cierra o muestra éxito
    - expect: Indicador refleja evidencia cargada o estado SUBIDO en UI
