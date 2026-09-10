# SIGESA Ayuda Test Plan

## Application Overview

Pantalla /ayuda (Asistente virtual SIGESA, agent=general). Requiere sesión autenticada. UI: header «SIGESA · Ayuda» / «Asistente virtual», paneles «Escenarios demo» y «Capacidades», conversación con textarea, botón Enviar, link «Historial de acciones», botón Limpiar. Al enviar un mensaje se abre automáticamente el modal role=dialog «Historial de acciones del asistente» con trazabilidad (backend, agent=general, camino KEYWORD/LLM/OUT_OF_SCOPE, badge OK/error). Seed adaptado: login → /ayuda. NO verificar redacción exacta del LLM; sí presencia de respuesta, URLs, modal, camino y datos estructurados.

## Test Scenarios

### 1. Asistente /ayuda — agent=general

**Seed:** `tests/seed.spec.ts`

#### 1.1. JD pregunta funciones disponibles y recibe respuesta con trazabilidad

**File:** `tests/agente/ayuda-funciones-usuario-jd.spec.ts`

**Steps:**
  1. Iniciar sesión JD (jd@umss.edu.bo / JefeDemo2026!) y navegar a /ayuda
    - expect: URL contiene /ayuda
    - expect: Heading «Asistente virtual» visible
    - expect: Texto «SIGESA · Ayuda» visible
  2. Escribir en el textbox «Escriba su consulta…»: listame las funciones que puedo hacer como usuario
    - expect: Botón «Enviar» habilitado
  3. Clic en «Enviar»
    - expect: Modal dialog «Historial de acciones del asistente» visible automáticamente
    - expect: Paso «Mensaje enviado al backend (/assistant/chat)» visible en el historial
    - expect: Badge/registro con estado OK (no error) al finalizar
  4. Cerrar modal con botón «Cerrar»
    - expect: Dialog ya no visible
    - expect: En conversación hay mensaje del usuario y respuesta del asistente no vacía
  5. Verificar respuesta del asistente (sin texto exacto)
    - expect: La respuesta contiene «SIGESA» o «Funciones» o «acreditación»
    - expect: Menciona capacidades/roles/tools o lista estructurada (p. ej. JD, list_users, confirmación)
    - expect: Metadata «Camino:» visible (LLM, KEYWORD u OUT_OF_SCOPE)
    - expect: Link «Historial de acciones (1)» visible

#### 1.2. Enviar deshabilitado con consulta vacía

**File:** `tests/agente/ayuda-envio-vacio.spec.ts`

**Steps:**
  1. Login JD y abrir /ayuda
    - expect: Pantalla de asistente cargada
  2. Sin escribir en el textarea, intentar enviar
    - expect: Botón «Enviar» deshabilitado
    - expect: No aparece modal de historial
    - expect: Sigue visible estado vacío o «Demostración tool calling»

#### 1.3. Pregunta fuera de alcance muestra out-of-scope

**File:** `tests/agente/ayuda-out-of-scope.spec.ts`

**Steps:**
  1. Login JD y abrir /ayuda
    - expect: Asistente virtual visible
  2. Enviar: ¿Cuál es el presupuesto de la universidad para 2027?
    - expect: Modal historial se abre
  3. Esperar respuesta y revisar modal y chat
    - expect: Respuesta del asistente no vacía (mensaje out-of-scope)
    - expect: Historial muestra consulta fuera de alcance o paso OUT_OF_SCOPE / status out_of_scope
    - expect: No hay crash ni role=alert de error de red
    - expect: URL permanece en /ayuda

#### 1.4. Palabra clave lista fases vía camino KEYWORD

**File:** `tests/agente/ayuda-keyword-fases.spec.ts`

**Steps:**
  1. Login JD y abrir /ayuda
    - expect: Panel «Escenarios demo» visible (viewport lg)
  2. Enviar: Lista las fases de Ingeniería de Sistemas CEUB
    - expect: Modal historial abierto
  3. Revisar respuesta y trazabilidad
    - expect: Respuesta menciona fases o estructura del proceso (p. ej. «Fase»)
    - expect: Metadata «Camino: KEYWORD» o historial menciona list_process_structure / KEYWORD
    - expect: Historial con estado OK

#### 1.5. Cerrar y reabrir historial conserva registro

**File:** `tests/agente/ayuda-historial-reabrir.spec.ts`

**Steps:**
  1. Login JD, /ayuda, enviar cualquier pregunta corta (ej. hola)
    - expect: Modal visible
    - expect: Historial de acciones (1)
  2. Cerrar modal (botón «Cerrar» o backdrop «Cerrar trazabilidad del agente»)
    - expect: Dialog no visible
  3. Clic en link/botón «Historial de acciones (1)»
    - expect: Dialog vuelve a abrirse
    - expect: Registro previo visible con el prompt del usuario

#### 1.6. Limpiar conversación resetea chat e historial

**File:** `tests/agente/ayuda-limpiar-conversacion.spec.ts`

**Steps:**
  1. Login JD, /ayuda, enviar un mensaje
    - expect: Hay al menos un mensaje en conversación
    - expect: Botón «Limpiar» habilitado
  2. Clic en «Limpiar»
    - expect: Conversación vacía (estado inicial o «Demostración tool calling»)
    - expect: Link «Historial de acciones» sin contador o (0)
    - expect: Textarea vacío
    - expect: Botón «Enviar» deshabilitado

#### 1.7. CC pregunta funciones y recibe respuesta acorde a su rol

**File:** `tests/agente/ayuda-funciones-usuario-cc.spec.ts`

**Steps:**
  1. Iniciar sesión CC (cc@umss.edu.bo / CoordDemo2026!) y navegar a /ayuda
    - expect: URL /ayuda
    - expect: Sidebar muestra rol CC
  2. Enviar: listame las funciones que puedo hacer como usuario
    - expect: Modal historial OK
  3. Verificar respuesta (sin comparar con JD)
    - expect: Respuesta no vacía con «SIGESA» o capacidades
    - expect: No menciona obligatoriamente gestión de usuarios JD (create_user) si el catálogo CC no lo incluye
    - expect: Panel lateral «Capacidades» lista items acordes a CC (evidencias, normativa)

#### 1.8. Escenario demo «Usar pregunta» precarga el textarea

**File:** `tests/agente/ayuda-demo-usar-pregunta.spec.ts`

**Steps:**
  1. Login JD y abrir /ayuda en viewport ancho (lg)
    - expect: Heading «Escenarios demo» visible
  2. Clic en «Usar pregunta» del escenario 1 (fases CEUB)
    - expect: Textarea contiene «Lista las fases de Ingeniería de Sistemas CEUB»
    - expect: Botón «Enviar» habilitado
