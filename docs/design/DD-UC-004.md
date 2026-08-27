---
id: DD-UC-004
fsd_ref: FSD-UC-004
titulo: "Diseño: Cargar Evidencia en Subfase (MOD-EVIDENCE)"
modulo: MOD-EVIDENCE
arquitectura: Hexagonal
estado: Aprobado
fecha: "2026-08-27"
prompts:
  - PR-IMPL-006
  - PR-IMPL-034
---

# DD-UC-004 — Cargar Evidencia en Subfase

## Propósito

Permitir al **[CC]** cargar la **Evidencia v1** en una **Subfase**, validando alcance de carrera (FSD-BR-09), MIME/tamaño, SHA-256, y transición de subfase a `SUBIDO`.

## Modelo (v1.1)

| Concepto | Descripción |
|----------|-------------|
| `Subphase` | Unidad verificable con `requirements`, evidencias y workflow |
| `Evidence` | Cabecera con FK `subphase_id` (**sin** `indicator_id`) |
| `EvidenceVersion` | v1 append-only: `contentHash`, `description`, `storageKey` |

## API

| Método | Ruta | Rol |
|--------|------|-----|
| POST | `/api/v1/subphases/{subphaseId}/evidences` | CC |
| GET | `/api/v1/subphases/{subphaseId}/evidences` | CC, TD, JD |

**Body multipart:** `file`, `description`

## Reglas

| ID | Regla |
|----|-------|
| BR-04.1 | Evidencia exige `subphaseId` + descripción + archivo |
| BR-04.2 | Bloqueo upload si observación OPEN (`SUBSANATION_NOT_ALLOWED`) |
| BR-04.3 | Subfase → `SUBIDO` tras carga exitosa |

## Fuera de alcance

Dimensión, Criterio, Indicador (taxonomía legacy retirada del piloto v1.0).
