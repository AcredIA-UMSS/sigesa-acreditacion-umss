---
id: PR-IMPL-029
feature_asociado: DD-UC-004
fsd_uc:
  - FSD-UC-004
  - FSD-UC-019
design_doc: DD-UC-004
depende_de:
  - PR-IMPL-006
  - PR-IMPL-019
fecha: "2026-08-21"
version: "1.0"
estado: Implementado
autor: "Cursor Agent"
skill_origen: save-prompt-mapping
---

# Prompt Contract — Implementación `PR-IMPL-029`

> **Design docs:** [`DD-UC-004`](../../design/DD-UC-004.md) §8 · [`DD-UC-019`](../../design/DD-UC-019.md) §2.5.

---

## 1. Objetivo

Reemplazar la zona inline de carga en subfases por un **enlace subrayado** que abre un **modal** con el formulario UC-004.

---

## 2. Entregables

| Área | Artefactos |
|------|------------|
| Frontend | `SubphaseEvidenceUploadSlot` (disparador), `SubphaseEvidenceUploadModal` |
| Docs | `DD-UC-004` §8, `FSD-UC-004`, `FSD-UC-019`, `DD-UC-019`, `DTP.md`, PM-019 |

---

## 3. Criterios de aceptación

- [x] Subfase muestra texto subrayado, no formulario inline.
- [x] Clic abre modal con upload [CC] o redirect UC-004 [JD/TD].
- [x] Portal + `useLockBodyScroll` en `<main>`.
- [x] Documentación actualizada.

---

## 4. Prompt literal del usuario

```text
en las subfases de las fases, hay una aprte donde puedes subir evidencias, quiero que eso sea un texto subrayado que al hacer click habra un modal donde recien suba la evidencia, luego actualiza toda la documentacion involucrada
```
