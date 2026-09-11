# Gestión de evidencias — acceso [CC] (Planner)

## Application Overview

SIGESA administra evidencias de acreditación en **Cargar evidencia** (`/evidencias/cargar`, FSD-UC-004). El actor de carga es el Coordinador de Carrera **[CC]**. Este plan cubre **iniciar sesión y llegar al módulo**, no la persistencia multipart ni el copiloto UC-024.

**Ruta canónica:** login → dashboard → sidebar `sidebar-nav-evidence` → `evidence-upload-page`.  
**Guard:** `CcOnlyRoute` — solo rol `CC`.  
**Anclas:** ver [`locators-evidencias.md`](locators-evidencias.md). `login-*`, `sidebar-nav-evidence`, `evidence-upload-*`.  
**Fuente de indicadores:** `GET /api/v1/indicators/uploadable` (PENDIENTE / OBSERVADO).

## Test Scenarios

### 1. Acceso al módulo como [CC]

**Seed:** `e2e/seed.cc-evidencias.spec.ts`

#### 1.1. cc-login-sidebar-cargar-evidencia

**File:** `e2e/evidence/cc-login-sidebar-cargar-evidencia.spec.ts`

**Steps:**
  1. En `/login`, completar correo `cc@umss.edu.bo` y contraseña de seed (`CoordDemo2026!` en live; stub acepta el mismo correo).
    - expect: `login-page`, `login-form`, `login-email`, `login-password`, `login-submit` visibles
  2. Pulsar Iniciar sesión.
    - expect: URL contiene `/dashboard`
    - expect: `dashboard-page` visible
    - expect: `sidebar` visible
  3. Pulsar `sidebar-nav-evidence` (CARGAR EVIDENCIA).
    - expect: URL es `/evidencias/cargar` (query opcional)
    - expect: `evidence-upload-page` visible
    - expect: `evidence-upload-form` visible
    - expect: campos `evidence-indicator`, `evidence-criterion`, `evidence-description`, `evidence-submit` visibles
    - expect: `evidence-file` presente en el DOM (`toBeAttached`)

#### 1.2. cc-direct-url-evidencias-cargar

**File:** `e2e/evidence/cc-direct-url-evidencias-cargar.spec.ts`

**Steps:**
  1. Iniciar sesión como [CC] (mismos pasos de 1.1 hasta dashboard).
  2. Navegar a `/evidencias/cargar` por URL (no sidebar).
    - expect: no redirige a `/dashboard` ni `/login`
    - expect: `evidence-upload-page` y `evidence-upload-form` visibles

#### 1.3. unauthenticated-redirect-evidencias

**File:** `e2e/evidence/unauthenticated-redirect-evidencias.spec.ts`

**Steps:**
  1. Sin sesión, abrir `/evidencias/cargar`.
    - expect: redirige a `/login`
    - expect: `login-page` visible
    - expect: `evidence-upload-page` no visible

### 2. Aislamiento de rol

**Seed:** `e2e/seed.cc-evidencias.spec.ts`

#### 2.1. jd-blocked-from-evidence-module

**File:** `e2e/evidence/jd-blocked-from-evidence-module.spec.ts`

**Steps:**
  1. Iniciar sesión como [JD] (`jd@umss.edu.bo`).
    - expect: llega a `/admin/users` (`users-admin-page`)
  2. Comprobar sidebar.
    - expect: `sidebar-nav-evidence` no visible (ítem solo si `session.role === 'CC'`)
  3. Abrir `/evidencias/cargar` por URL.
    - expect: `CcOnlyRoute` redirige fuera del módulo (dashboard o post-login JD)
    - expect: `evidence-upload-page` no visible

### 3. Fuente y estado en el módulo (no redacción de copy)

**Seed:** `e2e/seed.cc-evidencias.spec.ts`

#### 3.1. uploadable-indicators-show-state-and-source

**File:** `e2e/evidence/uploadable-indicators-show-state-and-source.spec.ts`

**Origen:** caso propuesto «verificar redacción del modelo / copy SUBIDO» **corregido** (ver auditoría).

**Steps:**
  1. Autenticarse como [CC] y abrir `/evidencias/cargar`.
  2. Esperar el select `evidence-indicator`.
    - expect: la petición de fuente es `GET /api/v1/indicators/uploadable` (o stub equivalente)
    - expect: cada opción listada incluye `currentState` `PENDIENTE` u `OBSERVADO` (FSD-UC-004 / ticket TC-04)
    - expect: al elegir un indicador, `evidence-criterion` queda fijado (criterio 1:1, no texto libre del modelo LLM)
    - expect: no se aserta el párrafo de ayuda ni el nombre del modelo del copiloto
