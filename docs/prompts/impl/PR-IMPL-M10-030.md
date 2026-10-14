---
id: PR-IMPL-M10-030
feature_asociado: DD-UC-030
fsd_uc:
  - "FSD-UC-030"
  - "FSD-UC-020"
  - "FSD-UC-031"
  - "FSD-UC-028"
fecha: "2026-10-13"
version: "1.0"
estado: Listo
autor: "Cursor Agent (M10 freeze / M11 E7 / CAE)"
skill_origen: sigesa-orchestrator
alcance: full-stack
depende_de:
  - "PR-IMPL-M9-031"
bloquea_a: []
---

# Prompt Contract — `PR-IMPL-M10-030`

> **DD:** [`DD-UC-030`](../../design/DD-UC-030.md)  
> Cierre release 2.1.0: **M10 + M11 + CAE** en un solo corte.

## 1. Objetivo

Implementar congelamiento `AUDIT_READONLY`, Sala de Pares, acceso EE al proceso, ciclo E7 del Plan de Mejoras, rol `CAE_MEMBER` + `IndicatorDelegation`.

Flyway **V22** (siguiente libre tras V21). **No** reutilizar V21. Columna `app_user.role`: ampliar length a 20 y CHECK `CAE_MEMBER`.

## 2. Rol

Hexagonal Java 21 / Spring Boot 4 / React 19. Dominio puro. Orval only. Tokens Tailwind institucionales.

## 3. In-Scope

M10 freeze + checklist + E5/E6 + gates + EE policy.  
M11 columns plan + gate POST_ACCREDITATION + generate E7 + lift a IMPROVEMENT_MONITORING.  
CAE enum/alta/delegaciones/guards en evidence+FODA.  
UI: badge, freeze, checklist, delegaciones, justificación plan, EE read-only.  
Docs api_contracts WF-18…23, DTP, FSD Hecho.

## 4. Out-of-Scope

SMTP UC-015, bitácora real UC-017, portal UC-016, plantillas CNACU, `docs/baseline/**`.

## 5. Restricciones

| ID | Regla |
|----|-------|
| R1 | Dominio sin Spring/JPA. |
| R2 | `AUDIT_READONLY` bloquea escrituras eval → 409 PROCESS_FROZEN. |
| R3 | EE lectura con scope carrera; 403 en mutaciones. |
| R4 | E5 gate exige freeze AUDIT_READONLY. |
| R5 | E6 gate exige checklist 100% + E6 APPROVED. |
| R6 | E7: ítems vencidos incompletos requieren justification. |
| R7 | CAE_MEMBER requiere programId; writes solo con delegación. |
| R8 | Delegación: exactamente un alcance (N1 xor N2 xor indicator). |
| R9 | Orval only. |
| R10 | `@dtp-sync fsd=FSD-UC-030` + `@save-prompt-mapping sprint=03 fsd=FSD-UC-030` → **PM-018**. |

## 6. Aceptación

- [ ] V22 aplicada; role CHECK incluye CAE_MEMBER.
- [ ] Freeze/lift/GET; 409 PROCESS_FROZEN en PUT FODA.
- [ ] EE GET proceso 200; PUT FODA 403.
- [ ] Gates E5/E6/E7 según DD.
- [ ] CAE sin delegación 403; con delegación 200 evidencia o FODA.
- [ ] UI TD freeze; EE checklist; CC delegaciones; plan justificación.
- [ ] mvn test + vitest verdes; smoke Docker; PM-018.
