# SIGESA Login Test Plan

## Application Overview

Pantalla de autenticación UC-001 en `/login`. Formulario con correo institucional (@umss.edu.bo), contraseña, toggle de visibilidad y botón «Iniciar sesión». Panel lateral de marca SIGESA (viewport grande). Tras login exitoso, redirección según rol (JD → `/admin/users`). Validación cliente antes de llamar API; errores de API muestran `role="alert"`. Seed: `tests/seed.spec.ts`.

## Test Scenarios

### 1. Autenticación UC-001 — Login

**Seed:** `tests/seed.spec.ts`

#### 1.1. JD inicia sesión y llega a gestión de usuarios

**File:** `tests/agente/jd-login-exitoso.spec.ts`

**Steps:**
  1. Navegar a `/login`
    - expect: Heading «Bienvenido» visible
    - expect: Campo «Correo Institucional» visible
    - expect: Botón «Iniciar sesión» visible
  2. Completar correo `jd@umss.edu.bo`
    - expect: El textbox «Correo Institucional» contiene el email
  3. Completar contraseña en el textbox «Contraseña» (usar getByRole textbox, no getByLabel por el botón «Mostrar contraseña»)
    - expect: Campo contraseña tiene valor
  4. Clic en «Iniciar sesión»
    - expect: URL contiene `/admin/users`
    - expect: Heading «Gestión de usuarios» visible
    - expect: Heading «Usuarios registrados» visible

#### 1.2. Credenciales inválidas muestran alerta sin navegar

**File:** `tests/agente/login-credenciales-invalidas.spec.ts`

**Steps:**
  1. Navegar a `/login`
    - expect: Permanece en pantalla de login
  2. Ingresar correo válido `jd@umss.edu.bo` y contraseña incorrecta
    - expect: Formulario completo
  3. Clic en «Iniciar sesión»
    - expect: URL sigue en `/login`
    - expect: Elemento con role="alert" visible (no verificar texto exacto del mensaje)

#### 1.3. Formulario vacío muestra errores de campo

**File:** `tests/agente/login-formulario-vacio.spec.ts`

**Steps:**
  1. Navegar a `/login`
    - expect: Heading «Bienvenido» visible
  2. Clic en «Iniciar sesión» sin completar campos
    - expect: URL permanece en `/login`
    - expect: Texto «El correo es obligatorio.» visible junto al campo email
    - expect: Texto «La contraseña es obligatoria.» visible junto al campo contraseña
    - expect: No aparece role="alert" de error de servidor

#### 1.4. Correo fuera de dominio UMSS es rechazado en cliente

**File:** `tests/agente/login-email-no-institucional.spec.ts`

**Steps:**
  1. Navegar a `/login`
    - expect: Formulario visible
  2. Ingresar `usuario@gmail.com` en correo y cualquier contraseña
    - expect: Campos completados
  3. Clic en «Iniciar sesión»
    - expect: URL permanece en `/login`
    - expect: Mensaje «Solo se permiten correos institucionales @umss.edu.bo.» visible
    - expect: No se realiza navegación post-login

#### 1.5. Toggle mostrar/ocultar contraseña

**File:** `tests/agente/login-toggle-password.spec.ts`

**Steps:**
  1. Navegar a `/login`
    - expect: Campo contraseña tipo password
  2. Escribir texto en el textbox «Contraseña»
    - expect: Valor ingresado
  3. Clic en botón «Mostrar contraseña»
    - expect: Botón pasa a «Ocultar contraseña»
    - expect: El input de contraseña es visible como texto (type text)
  4. Clic en «Ocultar contraseña»
    - expect: Botón vuelve a «Mostrar contraseña»

#### 1.6. TD inicia sesión y llega al dashboard

**File:** `tests/agente/td-login-dashboard.spec.ts`

**Steps:**
  1. Navegar a `/login`
    - expect: Formulario visible
  2. Ingresar `td@umss.edu.bo` / `TecnicoDemo2026!`
    - expect: Credenciales completas
  3. Clic en «Iniciar sesión»
    - expect: URL contiene `/dashboard`
    - expect: No permanece en `/login`

#### 1.7. Estado de carga durante envío

**File:** `tests/agente/login-estado-carga.spec.ts`

**Steps:**
  1. Navegar a `/login` e ingresar credenciales JD válidas
    - expect: Formulario listo
  2. Clic en «Iniciar sesión»
    - expect: Botón «Iniciar sesión» muestra estado de carga o queda deshabilitado brevemente antes de redirigir
    - expect: Eventual redirección a `/admin/users`
