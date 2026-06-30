# 🚀 Guía de Desarrollo de Features Asistido por IA (AI-SDLC) - SIGESA

Esta guía define el flujo de trabajo estándar para todo el equipo de desarrollo. Nuestro objetivo es aprovechar la Inteligencia Artificial para codificar más rápido, pero manteniendo un **control arquitectónico estricto** y garantizando la **cero divergencia silenciosa** entre nuestro código y nuestra documentación.

> ⚠️ **Regla de Oro:** La carpeta `docs/baseline/` es nuestro registro histórico intocable. Todo el diseño evolutivo y las decisiones ocurren en la capa viva (`docs/product/`, `docs/design/`, `docs/adr/`).

---
# Si tiene tokens ilimitados o con poder, solo dile a la ia lo siguiente:
    @sigesa-orchestrator Necesito implementar el módulo de reportes definido en FSD-UC-005. Hazte cargo del proceso.
---
---
# 🛠️ Flujo de Trabajo: Paso a Paso

## Paso 1: Seleccionar el Caso de Uso (FSD)

Antes de abrir el editor para tirar código, debes saber exactamente qué vas a construir.

1. Abre el archivo de especificaciones vivas: [`docs/product/FSD.md`](../product/FSD.md).
2. Identifica el ID del caso de uso que te fue asignado (ej. `FSD-UC-005`) y su **Design Doc** enlazado (`DD-UC-NNN`, relación 1:1).
3. Lee las reglas de negocio y criterios de aceptación en [`docs/product/uc/FSD-UC-NNN.md`](../product/uc/).

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
---
## Paso 4: Escribir el Código

¡Ahora sí! Con las reglas claras, dejamos que la IA genere el código.

1. Copia el contenido de tu archivo `PR-IMPL-005.md`.
2. Pégalo en Cursor Composer o en tu agente preferido y permite que genere las clases, interfaces, repositorios o componentes de React.
3. Haz las pruebas locales necesarias y asegúrate de que el código compila.
4. Asegúrate de que el servidor compile y arranque (localhost:8080)
5. Fase de Integración (Orval): En la terminal de tu frontend, ejecuta `pnpm run generate:api`. Verifica que los DTOs y Hooks de React Query se hayan creado en frontend/src/api/.
6. Fase Frontend: Invoca a tu especialista de UI: 
```plaintext
@generate-frontend-feature Crea las vistas para FSD-UC-005 consumiendo los hooks recién generados por Orval.
```
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

## Paso 6: Cierre Documental y Auditoría Forense

Una vez que el código funciona (tanto back como front) y fue aprobado por el revisor, debemos dejar constancia en el historial del producto y en la bitácora del Sprint.

1. **Sincroniza el DTP:** En el chat, invoca al sincronizador para que actualice el ecosistema global:
   ```plaintext
   @dtp-sync
   ```

2. Auditoría del Sprint: Inmediatamente después, invoca al agente de trazabilidad indicando en qué sprint te encuentras y qué contrato acabas de implementar:
    ```plaintext
    @save-prompt-mapping sprint=1 pr=PR-IMPL-005 desc="Nombre de tu feature"
    ```

### ¿Qué sucede?

1. El agente `@dtp-sync` analizará tus archivos modificados y actualizará el Changelog en `docs/product/DTP.md`.
2. El agente `@save-prompt-mapping` entrará a `docs/sprints/sprint-1/`, calculará el siguiente ID disponible (ej. PM-012), agregará la fila a la tabla resumen y creará un bloque de auditoría profunda con el prompt exacto que usaste y los archivos validados por Git.

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

