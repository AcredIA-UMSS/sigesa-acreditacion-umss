---
producto: "SIGESA"
grupo: "ACREDIA"
documento: DTP                 
version: v1.0                  
fecha: "22/06/2026"
status: vivo                   
audiencia: dual               
baseline_ref:                 
  dti: "docs/baseline/DTI_vFinal.md"
  tag: "release/2.0.0"
  commit: "HEAD"
release: "release/3.0.0"      
stack:
  - "Java 21"
  - "Spring Boot 3.x"
  - "Hibernate / Spring Data JPA"
  - "H2 (Local) / PostgreSQL (Prod)"
  - "React"
  - "AWS"
repo: "ruta/a/tu/repo/sigesa"
agents_md: "/AGENTS.md"
artefactos_vivos:
  prd: "docs/product/PRD.md"          
  fsd: "docs/product/FSD.md"          
  prompt_mapping: "docs/PROMPT_MAPPING.md"
  design_docs_dir: "docs/design/"     
  adr_dir: "docs/adr/"
---

# Documento Técnico del Producto (DTP) – SIGESA

> **Qué es**: El contrato técnico vigente de SIGESA durante la fase de implementación. 
> **Regla de oro**: Cero divergencia silenciosa. El baseline de la Fase de Diseño (`release/2.0.0`) permanece intacto en `docs/baseline/`.

---

## A. Control de cambios (Núcleo del DTP)

### A.1 Changelog de implementación

*(Este cuadro se llenará a medida que se ejecuten los prompts de implementación y se envíen los PRs)*

| Fecha | Cambio | Disparador (FSD-UC / DD) | ADR | PR / commit | Autor |
|-------|--------|--------------------------|-----|-------------|-------|
| 22/06/2026 | Inicialización de la arquitectura base Spring Boot y DTP vivo. | N/A | N/A | `init` | Boris Angulo |

### A.2 Deltas respecto al DTI vFinal

> Diferencias **deliberadas** entre lo diseñado y lo construido. 

| # | Sección del DTI afectada | Qué decía el DTI vFinal | Qué dice ahora el DTP | Motivo | ADR |
|---|--------------------------|-------------------------|-----------------------|--------|-----|
| * | *Ninguna por ahora* | *El código refleja el DTI al 100%* | *N/A* | *Inicio de codificación* | *N/A* |

### A.3 Estado de implementación por FSD-UC

| FSD-UC | Design Doc | Estado | Release | Tests/Evals | Notas |
|--------|------------|--------|---------|-------------|-------|
| `FSD-UC-001` | `DD-UC-001` | pendiente | `release/3.0.0` | TBD | Preparando esqueleto hexagonal |

### A.4 Trazabilidad código ↔ DTP

`BRD/MRD (baseline)` → `PRD/FSD vivo (FSD-UC-NNN)` → `Design Doc (DD-UC-NNN)` → `Prompt (PR-IMPL-NNN)` → `PR/commit` → `Tests/Evals` → `ADR (si aplica)` → **DTP**.

---

## B. Contenido técnico vigente

> SIGESA utiliza arquitectura hexagonal. Cualquier desviación de los principios de Clean Architecture o del uso estricto de DTOs en controladores será documentada aquí.

| Sección (espejo del DTI) | ¿Cambió vs DTI vFinal? | Dónde está la versión vigente |
|--------------------------|------------------------|-------------------------------|
| §1 Visión del producto | no | DTI vFinal §1 |
| §2 Contexto del sistema (C4 N1) | no | DTI vFinal §2 |
| §3 Arquitectura de alto nivel (C4 N2/N3) | no | DTI vFinal §3 |
| §4 Modelo de dominio | no | DTI vFinal §4 |
| §5 Arquitectura hexagonal del core | no | DTI vFinal §5 |
| §8 Despliegue cloud (AWS) | no | DTI vFinal §8 |
| §10 Prompt mapping | **sí (crece)** | `docs/PROMPT_MAPPING.md` |
| §21 ADRs | **sí (crece)** | `docs/adr/` |