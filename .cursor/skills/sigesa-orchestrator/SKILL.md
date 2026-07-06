---
name: sigesa-orchestrator
description: >-
  DEPRECADO — Reemplazado por el subagente sigesa-orchestrator en
  .cursor/agents/sigesa-orchestrator.md. Usar solo si el subagente no está
  disponible; en ese caso leer ese archivo y seguir su pipeline.
disable-model-invocation: true
---

# Skill deprecado — usar subagente

Este skill fue **reemplazado** por el subagente de proyecto:

**`.cursor/agents/sigesa-orchestrator.md`**

## Cómo invocarlo

```text
Use the sigesa-orchestrator agent to implement FSD-UC-NNN.
```

o en Composer:

```text
@sigesa-orchestrator Necesito implementar FSD-UC-005. Hazte cargo del proceso.
```

## Migración

El subagente conserva el mismo pipeline AI-SDLC (design doc → PR-IMPL → código → review → trazabilidad) con checklist de estado, gates entre pasos y tabla de delegación a los demás skills.

Si `@sigesa-orchestrator` no resuelve al subagente, lee y ejecuta directamente `.cursor/agents/sigesa-orchestrator.md`.
