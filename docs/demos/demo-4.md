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
| **Boris Anthony Angulo Urquieta** | _por registrar_ | | | |
| **Alexander Alvarez** | 1. Notificaciones dual-canal (in-app + email) | [FSD-UC-015](../product/uc/FSD-UC-015.md) · [DD-UC-038](../design/DD-UC-038.md) | Eventos de acreditación encolan EMAIL + in-app en la misma TX. Campana en sidebar/header; popover con deep-link. Asignar/retirar responsable notifica al CC afectado (retiro sin CTA). Asunto/cuerpo usan nombres de negocio (carrera/proceso/indicador), no UUID. Correo visible en catcher `:8025` | `66e1cdaa` |
| **Aylen Mariangel Gonzáles Alvino** | _por registrar_ | | | |
| **Marlene Velasquez** | 1. Historial de accesos [JD] | [FSD-UC-036](../product/uc/FSD-UC-036.md) | Login abre sesión `ACTIVE` con `sid`; fallo `FAILED`; logout `CLOSED`; JWT vencido `EXPIRED`. [JD] filtra `/admin/ingresos`, ve acciones (crear, modificar, eliminar) e imprime el filtro. Otro rol: 403. Sesión inexistente: 404. No hay alta, edición ni borrado por API | `1e5db083` `efd1bc40` |
| | 2. Fase del proceso [TD] | [FSD-UC-037](../product/uc/FSD-UC-037.md) | Menú principal → `/reportes/fase`. Otro rol no permanece. La fase marcada es la etapa en curso y se ven las siete etapas ARCU-SUR. Inicio solo muestra la carta de solicitud. Revisar sin archivo lo indica. Retroalimentación de al menos 10 caracteres se guarda y no cambia la aprobación. Otro rol: 403 | en este release, junto con `PUT …/deliverables/{id}/feedback` en `openapi.json` |

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

- Ruta UI: menú principal **Fase del proceso** → `/reportes/fase` (solo TD).
- El técnico elige una carrera de su alcance y ve la fase actual en la secuencia ARCU-SUR.
- Inicio: «Carta de solicitud de participación». Puede ver archivos del proceso y dejar una observación en borrador (FSD-UC-032).
- En cada entregable: **Revisar** y **Retroalimentación**. El texto va a `technicalObservations`. El `approvalStatus` no cambia.
- Contrato generado desde el código: `PUT /api/v1/processes/{processId}/stages/{stageId}/deliverables/{deliverableId}/feedback` (`recordDeliverableFeedback`, `DeliverableFeedbackRequest` 10–4000). `OpenApiExportIT` exige ese `PUT`. CI: [`.github/workflows/openapi.yml`](../../.github/workflows/openapi.yml) (drift + Spectral en PR hacia `main` y `release/**`).

### Qué mostrar en el video

1. Login TD → **Fase del proceso** → elegir carrera → fase marcada y las siete etapas.
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

## Fuera de esta demo

Portal público y noticias (Demo 3, `8.3.0`). Evidencias y contrato OpenAPI de Demo 2. El `openapi.json` de este release incluye la retroalimentación del entregable (Marlene) y los endpoints de notificaciones dual-canal / responsable / cierre de proceso (Alexander Alvarez).
