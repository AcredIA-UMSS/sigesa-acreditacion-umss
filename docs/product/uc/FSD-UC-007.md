---
id: FSD-UC-007
nombre: Buscar Evidencia Inteligente
estado: Completado
release: v1.0
actor_principal: "[CC] (alcance carrera), [TD] (global)"
trazabilidad_prd: PRD-US-004
modulo: MOD-EVIDENCE
reglas:
  - FSD-BR-09 (Aislamiento de carrera para Coordinador CC)
  - FSD-BR-25 (Enrutador Híbrido de Búsqueda Inteligente)
ultima_actualizacion: "2026-08-09"
---

# FSD-UC-007 — Buscar Evidencia Inteligente

## Contexto y Flujo de Enrutamiento Híbrido

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-015 · PRD-US-004 · NFR-002 |
| **API** | `GET /api/v1/evidences/search` |

Este caso de uso implementa un **Enrutador de Consultas Híbrido (Hybrid Query Router)** para la búsqueda de evidencias cargadas en el sistema. La consulta del usuario en lenguaje natural se intercepta y procesa siguiendo cuatro escenarios específicos de IA y reglas rígidas:

### 1. Escenario 1: Controlado (Exact Keyword Match)
* **Condición**: El usuario ingresa un término o palabra clave exacta registrada en el catálogo local (ej. "sistemas", "infraestructura", "criterio 1").
* **Comportamiento**: Se resuelve inmediatamente mediante código tradicional (búsqueda directa en base de datos). **No se realiza ninguna llamada al modelo de lenguaje (LLM)**.
* **Trazabilidad**: `routingPath: "KEYWORD"`.

### 2. Escenario 2: Sinónimo (Semantic LLM Match)
* **Condición**: El usuario busca utilizando sinónimos o lenguaje coloquial (ej. "papeles de computación", "aulas e instalaciones", "documentos de acreditación").
* **Comportamiento**: El sistema invoca al LLM para traducir y mapear semánticamente la consulta a los términos controlados de la base de datos o herramientas de búsqueda correspondientes, retornando el ID/nombre de la herramienta y sus parámetros. La ejecución del query y retorno de datos es realizada **exclusivamente por el código de la aplicación**, nunca por el LLM.
* **Trazabilidad**: `routingPath: "LLM"`.

### 3. Escenario 3: Fuera de Alcance (Out of Scope)
* **Condición**: El usuario realiza una consulta sobre temas ajenos al sistema de acreditación (ej. "¿Cómo está el clima?", "dame una receta de comida").
* **Comportamiento**: El LLM detecta que está fuera del alcance establecido y retorna una negativa cortés, listando las capacidades legítimas de búsqueda en SIGESA (búsqueda de evidencias por carrera, descripción, dimensión y tags).
* **Trazabilidad**: `routingPath: "REFUSAL"`.

### 4. Escenario 4: Modelo Apagado o Falla de Conexión (IA_HABILITADA = false)
* **Condición**: El flag de feature `IA_HABILITADA` (header `X-AI-Enabled`) está desactivado o la conexión al servicio LLM falla.
* **Comportamiento**:
  * El **Escenario 1 (Controlado)** sigue funcionando de manera perfecta por código tradicional.
  * El **Escenario 2 (Sinónimo)** hace un fallback elegante: en vez de llamar al LLM, ejecuta una búsqueda clásica en base de datos utilizando coincidencia por comodines (`ILIKE`) en los campos de descripción y título de archivo (`storage_key`).
* **Trazabilidad**: `routingPath: "KEYWORD"`.

---

## Reglas de Negocio Específicas

1. **Aislamiento de Carrera (FSD-BR-09)**:
   * Si el actor principal es un **Coordinador de Carrera [CC]**, la búsqueda debe acotarse estrictamente a su alcance de carrera (`programScope` de su JWT). No puede ver ni buscar evidencias pertenecientes a otras carreras.
   * Si el actor principal es un **Tribunal Docente [TD]** o **Jefe de Departamento [JD]**, la búsqueda se realiza a nivel global sobre todas las carreras.
2. **Índice de Búsqueda de la Evidencia**:
   El motor de búsqueda indexa y busca en los siguientes campos de la entidad `Evidencia`:
   * Título de la evidencia / Nombre de archivo.
   * Descripción o notas de la evidencia.
   * Criterio o dimensión académica asociada (ej. Infraestructura, Plan de Estudios).
   * Etiquetas (Tags) y palabras clave asociadas explícitamente durante la carga.

---

## Flujo Principal

1. El usuario envía una consulta de búsqueda mediante la interfaz web.
2. El backend intercepta la petición y valida el rol del usuario (CC vs TD).
3. Evalúa si el query coincide con palabras clave exactas del catálogo.
4. Si hay coincidencia exacta: Ejecuta la consulta SQL tradicional y retorna la respuesta.
5. Si no hay coincidencia exacta y `IA_HABILITADA == true`: Llama al LLM para expandir/enrutar la consulta.
   * Si el LLM decide que está dentro del alcance: Retorna la herramienta seleccionada y los parámetros de búsqueda. El backend ejecuta la búsqueda de base de datos correspondiente.
   * Si el LLM decide que está fuera de alcance: Retorna el mensaje de rechazo estándar de SIGESA.
6. Si `IA_HABILITADA == false`: Ejecuta búsqueda clásica por aproximación de texto (ILIKE) o retorna mensaje de fallback.
7. Presenta resultados paginados en el frontend detallando la trazabilidad de la ruta de búsqueda.

---

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-004 @FSD-UC-007 @NFR-002
Característica: Búsqueda Inteligente de Evidencias

  Escenario: Búsqueda controlada de palabra clave exacta (Escenario 1)
    Dado un Coordinador [CC] de "Ingeniería de Sistemas" autenticado
    Cuando realiza la búsqueda exacta por "infraestructura"
    Entonces el sistema resuelve la consulta por código tradicional (KEYWORD)
    Y devuelve únicamente las evidencias de "Ingeniería de Sistemas" en esa dimensión sin llamar al LLM

  Escenario: Búsqueda semántica usando sinónimos con IA (Escenario 2)
    Dado un Tribunal Docente [TD] autenticado con IA habilitada
    Cuando busca "instalaciones y aulas"
    Entonces el sistema enruta la consulta mediante el LLM (LLM) mapeándola a la dimensión "Infraestructura"
    Y ejecuta la búsqueda en la base de datos a nivel global de todas las carreras

  Escenario: Consulta fuera de alcance (Escenario 3)
    Dado un usuario autenticado en SIGESA
    Cuando pregunta al buscador "¿Qué hora es en Tokio?"
    Entonces el sistema intercepta el query y retorna un mensaje de rechazo formal y las capacidades soportadas (REFUSAL)

  Escenario: Búsqueda con IA desactivada (Escenario 4)
    Dado un usuario autenticado en el sistema
    Y la propiedad IA_HABILITADA es "false"
    Cuando realiza la búsqueda por sinónimo "papeles de docentes"
    Entonces el sistema no realiza llamadas al modelo de lenguaje
    Y devuelve "La búsqueda inteligente por sinónimos está desactivada. Intente buscar con palabras clave exactas."
```
