# Demo 4 — AcredIA / SIGESA

**Release:** `8.4.0`  
**Rama:** `release/8.4.0`  
**Repositorio:** [https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss)

Entrega de aula: enlace al repositorio + nombre del release (`8.4.0`).

Registro de aportes: [`8.4.0-aportes.md`](../../8.4.0-aportes.md) (raíz). El factor de aporte individual cuenta solo los commits de cada autor. Lo que se muestra en la demo o en el video está en esta rama.

---

## Compromiso por integrante

Cada persona presenta **1 a 3** funcionalidades del release, con **caso de uso FSD** y **criterios de aceptación**. No se recuentan las de Demo 2 ni Demo 3.

| Integrante | Funcionalidades (1–3) | FSD / contrato | Criterios de aceptación (resumen) | Evidencia |
|------------|----------------------|----------------|-----------------------------------|-----------|
| **Boris Anthony Angulo Urquieta** | 1. Recuperación RAG sobre chunks (sinónimos, peso de claves, AND→OR), Observaciones de tecnico a coordinador | [FSD-UC-033](../product/uc/FSD-UC-033.md) [FSD-UC-011](../product/uc/FSD-UC-011.md) | Tesauro de dominio; top-3 claves; descarta genéricos; reply muestra «Claves usadas»; sinónimos (*evaluadores externos* → pares); mismo `POST /assistant/chat` | [`RAG-RETRIEVAL-TECHNIQUES.md`](../design/assistant/RAG-RETRIEVAL-TECHNIQUES.md) · PR-IMPL-054 |
| **Alexander Alvarez** | 1. Notificaciones dual-canal (in-app + email) | [FSD-UC-015](../product/uc/FSD-UC-015.md) · [DD-UC-038](../design/DD-UC-038.md) | Eventos de acreditación encolan EMAIL + in-app en la misma TX. Campana en sidebar/header; popover con deep-link. Asignar/retirar responsable notifica al CC afectado (retiro sin CTA). Asunto/cuerpo usan nombres de negocio (carrera/proceso/indicador), no UUID. Correo visible en catcher `:8025` | `66e1cdaa` |
| **Aylen Mariangel Gonzáles Alvino** | 1. Panel semáforo ejecutivo [JD] | [FSD-UC-013](../product/uc/FSD-UC-013.md) | [JD] abre `/executive/semaphore` y ve semáforos por carrera/facultad (Rojo/Amarillo/Verde) según `ExecutiveSemaphoreRules`; carreras sin indicadores en `SIN_DATO`. Vista consolidada sin soporte ad-hoc. | `8a724c37` |
| | 2. Bitácora de auditoría [JD] | [FSD-UC-017](../product/uc/FSD-UC-017.md) | Log append-only consultable/exportable por [JD] (`GET /api/v1/audit/logs`, export); registra actor, acción y entidad (p.ej. aprobación de indicador `INDICATOR_APPROVED`). No [JD] → 403; rango inválido → 422 `INVALID_DATE_RANGE`. UI `/admin/auditoria`. | `8a724c37` |
| | 3. Matriz de cobertura y agenda de visita de pares | [FSD-UC-039](../product/uc/FSD-UC-039.md) · [DD-UC-039](../design/DD-UC-039.md) · [FSD-UC-040](../product/uc/FSD-UC-040.md) · [DD-UC-040](../design/DD-UC-040.md) | Matriz N1→N2→N3→Indicador con estado/cantidad de evidencias y totales; `GET .../coverage-matrix` y PDF síncrono (`.../coverage-matrix/pdf`, no ZIP). Agenda de visita por proceso (`GET/PUT .../peer-visit`): ventana/sede/sala/estado e ítems; TD/JD editan, CC/EE leen; `422 INVALID_DATE_RANGE`, `409 VISIT_IMMUTABLE`, alcance → 404 `PROCESS_NOT_FOUND`. UI `/procesos/{id}/cobertura` y `/procesos/{id}/visita-pares`. | `d7656503` `33804f30` `c0b66ebf` |
| **Marlene Velasquez** | 1. Historial de accesos [JD] | [FSD-UC-036](../product/uc/FSD-UC-036.md) | Login abre sesión `ACTIVE` con `sid`; fallo `FAILED`; logout `CLOSED`; JWT vencido `EXPIRED`. [JD] filtra `/admin/ingresos`, ve acciones (crear, modificar, eliminar) e imprime el filtro. Otro rol: 403. Sesión inexistente: 404. No hay alta, edición ni borrado por API | `1e5db083` `efd1bc40` |
| | 2. Fase del proceso [TD] | [FSD-UC-037](../product/uc/FSD-UC-037.md) | Menú principal → `/reportes/fase`. Otro rol no permanece. La fase marcada es la etapa en curso y se ven las siete etapas ARCU-SUR. Inicio solo muestra la carta de solicitud. Revisar sin archivo lo indica. Retroalimentación de al menos 10 caracteres se guarda y no cambia la aprobación. Otro rol: 403 | en este release, junto con `PUT …/deliverables/{id}/feedback` en `openapi.json` |

---

## Boris Anthony Angulo Urquieta — detalle para la exposición

### 1. Técnicas de recuperación sobre el índice (FSD-UC-033) — mediana

No es un agente nuevo ni se suben PDFs. Se consulta lo ya indexado en `normative_document`.

Catálogo de técnicas: [`docs/design/assistant/RAG-RETRIEVAL-TECHNIQUES.md`](../design/assistant/RAG-RETRIEVAL-TECHNIQUES.md).

Preguntas de video:

1. *«¿Quién arma el equipo de evaluadores externos?»* → claves `pares` → §6.12 comités de pares.  
2. *«cronograma pares visita normativa procedimiento»* → busca las tres de dominio; descarta *normativa* / *procedimiento*.

k6/Tempo de esta demo: ya cubierto por el grupo (Marlene / `bench/access/`); no se recuenta aquí.

### FSD-UC-011 - DASHBOARD y Observaciones

- observaciones en cada indicador del proceso de acreditacion
- el Tecnico de la DUEA realiza observaciones y recomendaciones cuando el coordinador tiene errores en su presentacion de evidencias
- el coordinador de carrera puede ver las observaciones y subsanarlas

---

## Aylen Mariangel Gonzáles Alvino — detalle para la exposición

Identidad Git: **Aylen** (commits en `release/8.4.0` / rama de trabajo asociada).

### 1. Panel semáforo ejecutivo [JD] (FSD-UC-013)

- Ruta UI: `/executive/semaphore` (solo [JD]).
- Semáforos por carrera/facultad (Rojo/Amarillo/Verde) calculados con `ExecutiveSemaphoreRules`; carreras sin indicadores se muestran como `SIN_DATO`.
- Datos expuestos en `GET /api/v1/dashboards/me/summary` (sección ejecutiva). Gherkin: `@FSD-UC-013 @TC-09` (`ExecutiveSemaphoreGherkinIT`, `ExecutiveSemaphoreRulesTest`).
- Evidencia: `8a724c37` (Complete audit and semaphore use cases).

### 2. Bitácora de auditoría [JD] (FSD-UC-017)

- Ruta UI: `/admin/auditoria` (solo [JD]).
- `GET /api/v1/audit/logs` (filtros actor/acción/rango fechas, paginado) y exportación; log append-only. Registra acciones relevantes (p.ej. `INDICATOR_APPROVED` al aprobar indicador).
- Controles: actor no [JD] → `403`; `from > to` → `422 INVALID_DATE_RANGE`. Gherkin: `@FSD-UC-017 @TC-12` (`AuditLogGherkinIT`).
- Evidencia: `8a724c37`.

### 3. Matriz de cobertura e informe de autoevaluación (FSD-UC-039) y Agenda de visita de pares (FSD-UC-040)

- **Matriz de cobertura**: `/procesos/{processId}/cobertura`. `GET /api/v1/processes/{processId}/coverage-matrix` (JSON) y `GET .../coverage-matrix/pdf` (PDF síncrono, imprimible, no ZIP). Árbol criterio → indicador → evidencia → estado con totales y % cobertura; alcance por carrera (`404 PROCESS_NOT_FOUND` fuera de alcance). Gherkin: `@FSD-UC-039 @TC-COVERAGE`.
- **Agenda de visita de pares**: `/procesos/{processId}/visita-pares`. `GET/PUT /api/v1/processes/{processId}/peer-visit` (lectura CC/TD/JD/EE; edición TD/JD). Ventana, sede, sala, estado e ítems (`INTERVIEW|FACILITY|DOCUMENT|SESSION`); una visita por proceso (V33), sin blobs. Validaciones: `422 INVALID_DATE_RANGE`, `409 VISIT_IMMUTABLE`. Gherkin: `@FSD-UC-040 @TC-VISIT`.
- Evidencia: `d7656503`, `33804f30`, `c0b66ebf` (APIs, UI/agenda y cierre de cobertura/visita).

### Qué mostrar en el video

1. Login JD → `/executive/semaphore`: semáforos por carrera/facultad y caso `SIN_DATO`.
2. Login JD → `/admin/auditoria`: filtrar/exportar y ver registro append-only (ej. aprobación de indicador).
3. Login TD/JD/EE (según alcance) → `/procesos/{id}/cobertura`: matriz y descarga PDF; validar que CC ajeno reciba 404.
4. Login TD/JD → `/procesos/{id}/visita-pares`: programar visita/ítems; EE/CC solo lectura; mostrar validación de rango o inmutabilidad `COMPLETED` si aplica.

---

## Alexander Alvarez — detalle para la exposición

Identidad Git: **alexalvarez** `<alvarez.rojas.alexander.james5@gmail.com>`.

### 1. Notificaciones dual-canal (FSD-UC-015)

- Campana in-app en sidebar (desktop) y header; popover con listado, marcar leída / marcar todas.
- Dual-canal: `notification_outbox` (EMAIL SMTP) + `in_app_notification` en la misma transacción; worker JavaMail; catcher `smtp/` en `:8025`.
- Asignar/retirar responsable de proceso dispara notificación al CC afectado; retiro sin deep-link CTA.
- Copy con nombres de negocio: proceso `plantilla — carrera`; indicador `código — descripción`.
- Contrato en `openapi.json`:
  - `GET /api/v1/notifications/me` (`listMyInAppNotifications`)
  - `POST /api/v1/notifications/{id}/read` (`markInAppNotificationRead`)
  - `POST /api/v1/notifications/me/read-all` (`markAllInAppNotificationsRead`)
  - `PUT` / `DELETE /api/v1/processes/{processId}/responsible` (`assignResponsible` / `removeResponsible`)
  - `POST /api/v1/processes/{processId}/close` (`closeProcess`)

### Qué mostrar en el video

1. Login JD → asignar responsable CC → campana in-app + correo en `:8025` con nombres legibles.
2. Retiro de responsable → notificación al CC sin CTA de deep-link.
3. Login CC/TD/JD → abrir campana, deep-link y marcar leídas.

Usuarios seed: tabla del [README](../../README.md) (`jd@umss.edu.bo`, `cc@umss.edu.bo`, `td@umss.edu.bo`).

---

## Marlene Velasquez — detalle para la exposición

Identidad Git: **marvelarias** `<marlene.velasquez@umss.edu>`.

### 1. Historial de accesos (FSD-UC-036)

- Ruta UI: `/admin/ingresos` (menú **Historial de accesos**, rol JD).
- `GET /api/v1/access-sessions/summary`, `GET /api/v1/access-sessions` (filtros `email`, `role`, `status`, `ipAddress`, `from`, `to`; cada fila trae `actions`), `GET /api/v1/access-sessions/{sessionId}` y `GET /api/v1/access-sessions/{sessionId}/actions`.
- No sustituye la bitácora FSD-UC-017.

### 2. Fase del proceso (FSD-UC-037)

- Ruta UI: menú principal **Seguimiento de procesos** → `/reportes/fase` (solo TD).
- El técnico elige una carrera de su alcance y ve la fase actual en la secuencia ARCU-SUR.
- Inicio: «Carta de solicitud de participación». Puede ver archivos del proceso y dejar una observación en borrador (FSD-UC-032).
- En cada entregable: **Revisar** y **Retroalimentación**. El texto va a `technicalObservations`. El `approvalStatus` no cambia.
- Contrato generado desde el código: `PUT /api/v1/processes/{processId}/stages/{stageId}/deliverables/{deliverableId}/feedback` (`recordDeliverableFeedback`, `DeliverableFeedbackRequest` 10–4000). `OpenApiExportIT` exige ese `PUT`. CI: [`.github/workflows/openapi.yml`](../../.github/workflows/openapi.yml) (drift + Spectral en PR hacia `main` y `release/**`).

### Qué mostrar en el video

1. Login TD → **Seguimiento de procesos** → elegir carrera → fase marcada y las siete etapas.
2. Inicio: solo la carta. **Revisar** un entregable sin archivo.
3. **Retroalimentación** de al menos 10 caracteres: el texto queda como seguimiento y el estado de aprobación no cambia.
4. Login JD → **Historial de accesos**: filtrar, ver la columna de acciones e imprimir.

Usuarios seed: tabla del [README](../../README.md) (`td@umss.edu.bo`, `jd@umss.edu.bo`).

---

## Cómo levantar la demo

```bash
git clone https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss.git
cd sigesa-acreditacion-umss
git checkout release/8.4.0
cp .env.example .env
docker compose up -d --wait
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8080 (`/api/health`, `/v3/api-docs`)

---

## Contrato OpenAPI e integración continua

- `openapi.json` (raíz) se genera desde el código vía `OpenApiExportIT` (`OPENAPI_WRITE=true`) y se valida en CI con drift check + Spectral (`.github/workflows/openapi.yml`).
- Endpoints presentados en este release incluidos en contrato: recuperación/assistant y observaciones (Boris, FSD-UC-033/FSD-UC-011), notificaciones dual-canal y responsable/cierre de proceso (Alexander, FSD-UC-015), historial de accesos y fase/feedback (Marlene, FSD-UC-036/FSD-UC-037), **panel semáforo y auditoría** (`/api/v1/dashboards/me/summary`, `/api/v1/audit/logs`, `/api/v1/audit/logs/export`) y **matriz de cobertura/agenda de visita** (`/api/v1/processes/{processId}/coverage-matrix`, `.../coverage-matrix/pdf`, `/api/v1/processes/{processId}/peer-visit`) (Aylen, FSD-UC-013/FSD-UC-017/FSD-UC-039/FSD-UC-040).

## Fuera de esta demo

Portal público y noticias (Demo 3, `8.3.0`). Evidencias y contrato OpenAPI de Demo 2.
