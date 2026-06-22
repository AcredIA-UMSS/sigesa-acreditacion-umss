# 🚀 Guía de Desarrollo de Features Asistido por IA (AI-SDLC) - SIGESA

Esta guía define el flujo de trabajo estándar para todo el equipo de desarrollo. Nuestro objetivo es aprovechar la Inteligencia Artificial para codificar más rápido, pero manteniendo un **control arquitectónico estricto** y garantizando la **cero divergencia silenciosa** entre nuestro código y nuestra documentación.

> ⚠️ **Regla de Oro:** La carpeta `docs/baseline/` es nuestro registro histórico intocable. Todo el diseño evolutivo y las decisiones ocurren en la capa viva (`docs/product/`, `docs/design/`, `docs/adr/`).

---
# 🛠️ Flujo de Trabajo: Paso a Paso

## Paso 1: Seleccionar el Caso de Uso (FSD)

Antes de abrir el editor para tirar código, debes saber exactamente qué vas a construir.

1. Abre el archivo de especificaciones vivas: `docs/product/04_fsd/FSD.md`.
2. Identifica el ID del caso de uso que te fue asignado (ej. `FSD-UC-005`).
3. Lee las reglas de negocio y criterios de aceptación.

---

## Paso 2: Generar el Documento de Diseño (El Plano)

No improvisamos arquitectura en el chat de IA. Primero diseñamos.

1. Abre el chat de Cursor (Composer).
2. Invoca al agente arquitecto usando el skill correspondiente:

```plaintext
@feature-design-doc FSD-UC-005 titulo="Nombre del Feature"
```

### ¿Qué sucede?

La IA creará automáticamente el archivo `docs/design/DD-UC-005.md`.

Revisa este documento para asegurarte de que los puertos, adaptadores (Arquitectura Hexagonal) y DTOs están bien definidos.

---

## Paso 3: Crear el Contrato de Prompt (Las Instrucciones)

Los prompts vagos generan código espagueti. Todo código debe nacer de un contrato escrito.

1. En el chat, llama al arquitecto de contratos:

```plaintext
@sigesa-prompt-contract-architect "Diseña el prompt para implementar el DD-UC-005 en Spring Boot"
```

### ¿Qué sucede?

Se generará un prompt estructurado (ej. `docs/prompts/impl/PR-IMPL-005.md`) con las restricciones exactas.

> 📝 **Acción Manual:**  
> Abre `docs/PROMPT_MAPPING.md` y añade una fila vinculando tu `FSD-UC-005`, tu design doc `DD-UC-005` y tu nuevo prompt `PR-IMPL-005`.

---

## Paso 4: Escribir el Código

¡Ahora sí! Con las reglas claras, dejamos que la IA genere el código.

1. Copia el contenido de tu archivo `PR-IMPL-005.md`.
2. Pégalo en Cursor Composer o en tu agente preferido y permite que genere las clases, interfaces, repositorios o componentes de React.
3. Haz las pruebas locales necesarias y asegúrate de que el código compila.

---

## Paso 5: Revisión Arquitectónica (El Auditor)

Antes de preparar el commit, validamos que la IA no haya roto ninguna regla del proyecto (como exponer entidades JPA en controladores).

1. Invoca al revisor de código:

```plaintext
@sigesa-architectural-code-reviewer "Revisa mis últimos cambios locales contra el DD-UC-005 y el DTP actual"
```

### ¿Qué sucede?

El agente te dará un reporte. Si hay **"Violaciones Arquitectónicas"**, debes corregir el código antes de avanzar.

---

## Paso 6: Sincronizar el DTP (El Cierre Documental)

Si el código funciona y fue aprobado, debemos actualizar el "expediente médico" de SIGESA.

1. En el chat, invoca al sincronizador:

```plaintext
@dtp-sync
```

### ¿Qué sucede?

El agente analizará todos los archivos `.java`, `.ts` o de configuración que cambiaste y actualizará automáticamente el Changelog en `docs/product/DTP.md`.

> **Nota:**  
> Si cambiaste de tecnología o rompiste el diseño original de manera justificada, el agente te sugerirá crear un documento en `docs/adr/`.

---

## Paso 7: Commit y Pull Request (PR)

Tu feature está terminado, blindado y trazable.

1. Haz commit de tus cambios incluyendo los archivos `.md` generados y los archivos de código fuente.
2. En la descripción de tu PR, incluye siempre la trazabilidad:

```text
Implementa: FSD-UC-005
Diseño: DD-UC-005
Prompt usado: PR-IMPL-005
```

