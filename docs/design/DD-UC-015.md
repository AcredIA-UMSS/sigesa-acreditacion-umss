---
id: DD-UC-015
nombre: Notificaciones y alertas — Centro de notificaciones in-app
estado: Implementado (frontend)
release: v2.0
trazabilidad_fsd: FSD-UC-015
trazabilidad_prd: PRD-US-017, PRD-US-018, PRD-US-019
modulo: MOD-NOTIFY
ultima_actualizacion: "2026-10-20"
---

# DD-UC-015 — Notificaciones y alertas (Diseño técnico)

## Alcance de este documento

Diseño de la **capa de experiencia in-app** del centro de notificaciones (FSD-UC-015)
para el rol **Coordinador de Carrera [CC]**. Cubre la consumición del API de
notificaciones ya existente en el adaptador REST (`/api/v1/notifications`).

Fuera de alcance: worker SMTP, outbox de escritura y job de plazos (back-end).
Ver "Riesgos y dependencias".

## Contratos consumidos (existentes en back-end)

| Método | Ruta | Uso en UI |
|--------|------|-----------|
| `GET` | `/api/v1/notifications/me?unreadOnly&page&size` | Lista paginada del usuario + carreras en scope |
| `GET` | `/api/v1/notifications/unread-count` | Badge de la campana y chip «sin leer» |
| `PATCH` | `/api/v1/notifications/{id}/read` | Marcar una notificación como leída |
| `POST` | `/api/v1/notifications/mark-all-read` | Marcar todas como leídas |

`NotificationResponseDto`: `id`, `eventType`, `recipientId`, `recipientEmail`,
`subject`, `body`, `deepLink`, `status`, `createdAt`, `readAt`.

## Arquitectura frontend

```
features/notifications/
├── api/notificationsApi.ts          # Cliente tipado (customFetch)
├── hooks/useNotifications.ts        # Estado servidor: lista, conteo, paginación, mutaciones
├── components/
│   ├── NotificationBell.tsx         # Campana + dropdown (header del dashboard)
│   └── CoordinatorAlertsPanel.tsx   # Panel de alertas críticas del dashboard CC
├── pages/NotificationsPage.tsx      # Centro de notificaciones (ruta /notificaciones)
└── index.ts                         # Barrel público de la feature
```

Separación estricta presentación/lógica: los componentes no llaman `customFetch`;
todo el acceso a datos vive en `useNotifications` (TanStack Query).

### Comportamiento por componente

| Componente | Responsabilidad | Montaje |
|------------|-----------------|---------|
| `NotificationBell` | Badge de no leídas (refresco 30 s), dropdown con las 10 más recientes, marcar todas, navegación por `deepLink`, enlace «Ver todas» | Header de `DashboardPage`, solo CC / CAE_MEMBER |
| `CoordinatorAlertsPanel` | Top 5 alertas **no leídas** con CTA de deep link | `CoordinatorDashboardSection`, oculto en modo `readOnly` (EE) |
| `NotificationsPage` | Lista paginada (20/pág.), filtro «solo no leídas», marcar todas, estados carga/error/vacío | Ruta `/notificaciones` |

### Enlace profundo (PRD-US-017)

`NotificationBell` y `NotificationsPage` marcan como leída la notificación y, si
`deepLink` está presente, navegan a él dentro de la SPA — sin reautenticación
mientras la sesión siga activa (`ProtectedRoute` + token en `localStorage`).

## Decisiones de diseño

1. **Paginación gestionada por el hook.** `useNotifications` posee el estado `page`
   y lo resetea al cambiar `unreadOnly`/`size`, para que la UI no pueda quedar en
   una página fuera de rango tras un cambio de filtro.
2. **Cliente tipado con `customFetch` en lugar de Orval.** El contrato de
   notificaciones no está presente en el `api-docs` del back-end de esta rama
   (ver Riesgos), por lo que no es regenerable. Se reutiliza el mismo patrón que
   `features/evidence/api/*` (no se introducen `fetch`/`axios` nuevos).
3. **Autorización por rol en la UI.** `/notificaciones` se envuelve en `CcOnlyRoute`
   y la entrada de sidebar se muestra solo a CC / CAE_MEMBER; el back-end sigue
   siendo la autoridad (scope de programa en el filtro del repositorio).

## Riesgos y dependencias

- **Bloqueante de build back-end:** `NotificationQueryJpaAdapter` referencia
  `NotificationOutboxJpaEntity` / `NotificationOutboxJpaRepository`, que solo
  existen en la rama `alex/feature/notifications/adhoc_aidlc` (commit `6ef989d`,
  junto con la migración `V24__create_notification_outbox.sql`, el adaptador de
  escritura y el worker SMTP). Sin integrar esa rama, el back-end no compila y el
  endpoint no está disponible.
- **Sin datos en runtime en esta rama:** el puerto de escritura sigue siendo
  `NoOpNotificationOutboxAdapter` (solo log), por lo que no se persisten filas en
  `notification_outbox` y la bandeja aparece vacía hasta integrar el outbox real.
- Ambas dependencias son de back-end; no requieren cambios en la capa de UI aquí.

## Trazabilidad

- FSD: [`FSD-UC-015`](../../product/uc/FSD-UC-015.md)
- PRD: PRD-US-017 / 018 / 019 · PRD-NFR-004 · PRD-REQ-013
- Regla de negocio: FSD-BR-13 (≤ 15 min)
- Modelo de datos: `NotificationOutbox` (`docs/product/modelo_datos.md`)
