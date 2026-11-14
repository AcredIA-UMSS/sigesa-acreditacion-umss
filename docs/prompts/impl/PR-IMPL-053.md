---
id: PR-IMPL-053
feature_asociado: DD-UC-038
fsd_uc:
  - FSD-UC-015
prd_refs:
  - BR-NOTIFY-001
  - PRD-US-017
  - PRD-US-018
  - PRD-US-019
fecha: "12/11/2026"
version: "1.0"
estado: Pendiente
autor: "Cursor Agent"
release: "8.4.0"
---

# Prompt Contract — Implementación `PR-IMPL-053`

> **Design:** [`DD-UC-038`](../../design/DD-UC-038.md)  
> **FSD:** [`FSD-UC-015`](../../product/uc/FSD-UC-015.md)  
> **BR:** [`BR-NOTIFY-001`](../../product/03_prd/BR-NOTIFY-001-notificaciones-eventos-acreditacion.md)  
> **ADR:** [`ADR-001`](../../adr/ADR-001-extraer-modulo.md) — MOD-NOTIFY permanece en el monolito

## 1. Propósito y Objetivo

Implementar notificaciones dual-canal (correo institucional + in-app) para FSD-UC-015 / BR-NOTIFY-001: destinatarios reales `@umss.edu.bo`, SMTP vía `JavaMailSender` + contenedor `sigesa-smtp`, popover corto in-app, plantilla HTML de correo alineada al design system, y evento `ProcessClosed` cuando **todos** los Indicadores del Proceso fueron aprobados por **[EE]**.

## 2. Rol y Persona

Actúa como **Desarrollador Full-Stack Senior** SIGESA (Java 21, Spring Boot 4.x, arquitectura hexagonal estricta, React 19 + Orval). Código listo para producción; tipado estricto; cero `any`; cero placeholders.

## 3. Límites (In-Scope / Out-of-Scope)

### In-Scope (obligatorio)

1. **Recipient resolver** (`NotificationRecipientResolverPort`): ACTIVE only; dominio `@umss.edu.bo`; sin emails sintéticos; dedupe; log si lista vacía.
2. **Outbox hardening:** columnas `program_id`, `process_id`, `recipient_role`, `idempotency_key`, `error_detail`; Flyway siguiente libre; worker `PENDING→PROCESSING` + SKIP LOCKED; backoff+jitter; max 5 → FAILED; SMTP **fuera** de TX write.
3. **JavaMailSender** + `spring-boot-starter-mail`; config 12-factor `SPRING_MAIL_*`; cableado a `sigesa-smtp` en compose (ya existe).
4. **Plantilla HTML** de correo (más larga que in-app): primary UMSS, Inter, CTA deep link absoluto; **no** plantillas CNACU.
5. **`in_app_notification`** insertada en la **misma TX** del use case; API-NOTIF-02/03/04; UI **popover** (no fullscreen); mensajes cortos; click → deep link + mark read.
6. **Eventos:** `IndicatorRejected`, `IndicatorApproved`, `EvidenceUploaded`, `EvidenceSubsanated` (**nuevo enqueue** en subsanación), `DeadlineApproaching` (job), `ProcessClosed` (gate: 100% indicadores aprobados por [EE]).
7. **Deep link [JD] `ProcessClosed`:** con sesión activa → panel **semáforo**; click semáforo → `/procesos/{processId}`.
8. **Orval** regenerado; UI con tokens del design system (sin hex arbitrarios).
9. Actualizar specs **vivas** (`FSD-UC-015`, `api_contracts`, DTP changelog) — **no** `docs/baseline/`.
10. Tests unit + integration + WebMvc; JaCoCo > 90% en servicios nuevos MOD-NOTIFY.

### Out-of-Scope (prohibido)

- Extraer microservicio de notificaciones (ADR-001).
- Push / WhatsApp / Telegram.
- Notificar a [P] o a [EE] **como destinatario** de este feature ([EE] solo participa en el gate de cierre).
- Modificar `docs/baseline/`.
- `fetch`/`axios` manual en frontend.
- Bandeja in-app a pantalla completa.
- SMTP dentro de la TX JPA de negocio.
- Emails sintéticos `*-{uuid}@umss.edu.bo`.
- Tratar `Level1Completed`, freeze o `ARCHIVED` como `ProcessClosed`.

## 4. Contexto de Entrada (leer antes de codificar)

| Artefacto | Ruta |
|-----------|------|
| Design Doc | `docs/design/DD-UC-038.md` (§1.4 Q-01..03, §3–§5 edge cases) |
| BR | `docs/product/03_prd/BR-NOTIFY-001-notificaciones-eventos-acreditacion.md` |
| FSD vivo | `docs/product/uc/FSD-UC-015.md` |
| Outbox actual | `JpaNotificationOutboxAdapter`, `NotificationOutboxWorker`, `V24__create_notification_outbox.sql` |
| Encoladores | `ApproveIndicatorService`, `RejectIndicatorService`, `Upload*Evidence*`, `SubsanateNormativeIndicatorEvidenceService` |
| SMTP compose | `smtp/`, servicio `sigesa-smtp` en `docker-compose.yml` |
| UI guidelines | `.cursor/rules/frontend-design.mdc` |

### Decisiones cerradas (no reabrir)

| ID | Resolución |
|----|------------|
| Q-01 | Cierre de acreditación = todos los Indicadores aprobados por [EE]. Aprobación de Indicador = Evidencia cumple estándares (carrera/proceso/marco). |
| Q-02 | Popover corto in-app; correo largo con template HTML institucional. |
| Q-03 | Deep link a semáforo (sesión activa); click semáforo → proceso específico. |

## 5. Salida Esperada

- Backend: puertos/servicios/adapters/Flyway/mail/template/job/`CloseAccreditationProcessUseCase` (si falta transición `CLOSED`).
- Frontend: campana + popover Orval; semáforo [JD] → detalle proceso.
- Docs vivos sincronizados (sin baseline).
- Suite de tests verde documentada en el PR.

## 6. Invariantes

- Hexagonal: dominio sin Spring/JPA; controladores solo DTOs `record`.
- FSD-BR-09 (aislamiento carrera), FSD-BR-12 (email), FSD-BR-13 (SLA ≤ 15 min).
- ADR-001: outbox **in-process** (default).
- Cero divergencia silenciosa: si el gate [EE] no encaja con el modelo actual de `ApproveIndicatorService` ([TD]), **detener** y proponer ADR — no improvisar.

## 7. Cierre (post-implementación)

```text
@save-prompt-mapping sprint=<N> fsd=FSD-UC-015
@dtp-sync
```

No registrar PM-NNN en este prompt; hacerlo **después** de ejecutar la implementación.
