# PR-IMPL-045 — Notificaciones y Alertas para Coordinador de Carrera [CC] (MOD-NOTIFY)

| Campo | Valor |
|---|---|
| **FSD-UC** | FSD-UC-015 |
| **PRD** | PRD-REQ-013, PRD-US-017, PRD-US-018 |
| **Sprint** | sprint_03 / PM-027 |
| **Release** | v2.1 |
| **Rama** | `alex/feature/notifications/adhoc_aidlc` |

## Objetivo

Implementar la experiencia de usuario completa para las notificaciones y alertas dirigidas al Coordinador de Carrera (`[CC]`):
1. **Centro de Notificaciones (UI Bell & Drawer):** Botón con campana de notificaciones, contador de alertas no leídas y panel desplegable en la barra de navegación lateral ([Sidebar.tsx](frontend/src/components/layout/Sidebar.tsx)).
2. **Alertas Críticas en Panel de Control:** Componente dedicado `CoordinatorAlertsPanel` integrado en [CoordinatorDashboardSection.tsx](frontend/src/features/dashboard/components/CoordinatorDashboardSection.tsx) para observaciones de evidencias pendientes y plazos próximos.
3. **Deep-linking Directo:** Redirección automática al indicador y observación abierta al hacer clic sobre una notificación o alerta.
4. **API REST de Notificaciones (Backend):** Endpoints seguros `/api/v1/notifications/me`, `/unread-count`, `/{id}/read` y `/mark-all-read`.
5. **Migración Flyway V26:** Columna `read_at` e índices correspondientes en `notification_outbox`.

---

## Alcance entregado (frontend)

> La capa back-end (ítems 4–5) queda **fuera de este corte**: depende de la rama
> `alex/feature/notifications/adhoc_aidlc` (commit `6ef989d`), que aporta
> `NotificationOutboxJpaEntity`/`NotificationOutboxJpaRepository`, la migración
> `V24__create_notification_outbox.sql`, el adaptador de escritura del outbox y el
> worker SMTP. Sin integrarla, el back-end no compila y el endpoint no responde.

| Ítem | Estado | Evidencia |
| :--- | :--- | :--- |
| 1. Centro de notificaciones (campana + panel desplegable) | ✅ | `features/notifications/components/NotificationBell.tsx`; montada en el header de `DashboardPage` (CC/CAE) |
| 2. Alertas críticas en el panel de control | ✅ | `CoordinatorAlertsPanel` en `CoordinatorDashboardSection` (oculto en `readOnly`/EE) |
| 3. Deep-linking directo | ✅ | `deepLink` navegado en campana, panel y página; marca `readAt` antes de navegar |
| 4. API REST de notificaciones (backend) | ⏳ fuera de corte | `NotificationController` ya existe sin commitear en esta rama; requiere el ítem de entidad/repositorio |
| 5. Migración Flyway V26 | ✅ | `V26__notification_read_status.sql` (en esta rama) |

**Además incluido:**

- `pages/NotificationsPage.tsx` — ruta `/notificaciones` (`ProtectedRoute` + `CcOnlyRoute`):
  filtro «solo no leídas», paginación (20/pág.), marcar todas, estados carga/error/vacío.
- Entrada de sidebar «NOTIFICACIONES» para CC / CAE_MEMBER.
- `useNotifications` con paginación propia (reset de página al cambiar filtro/tamaño).
- Handlers MSW y **62 tests** en 6 archivos (API, hook, componentes, página e integración con `App`):
  100 % statements/líneas/funciones y 97.4 % ramas sobre `src/features/notifications/**`.

## Archivos tocados

```
docs/design/DD-UC-015.md                                    (nuevo)
frontend/src/App.tsx                                        (+ ruta /notificaciones)
frontend/src/components/layout/Sidebar.tsx                  (+ NavItem NOTIFICACIONES)
frontend/src/features/dashboard/pages/DashboardPage.tsx      (+ NotificationBell en header)
frontend/src/features/dashboard/components/CoordinatorDashboardSection.tsx (+ panel de alertas)
frontend/src/features/notifications/pages/NotificationsPage.tsx           (nuevo)
frontend/src/features/notifications/pages/NotificationsPage.test.tsx      (nuevo)
frontend/src/features/notifications/hooks/useNotifications.ts             (paginación)
frontend/src/features/notifications/hooks/useNotifications.test.tsx       (nuevo)
frontend/src/features/notifications/api/notificationsApi.test.ts          (nuevo)
frontend/src/features/notifications/Notifications.integration.test.tsx    (nuevo)
frontend/src/features/notifications/components/NotificationBell.tsx       (enlace «Ver todas»)
frontend/src/test/mocks/handlers.ts                         (+ handlers/fixtures)
docs/sprints/sprint_03/PROMPT_MAPPING.md                    (+ PM-027)
```

## Restricciones respetadas

- Sin dependencias nuevas → `docs/product/DTP.md` sin cambios.
- Sin `fetch`/`axios` manuales: cliente con `customFetch` (no Orval; ver `DD-UC-015` §Decisiones 2).
- TypeScript estricto, sin `any`; componentes de presentación sin acceso a datos.
- `docs/baseline/` intacto.

## Evidencia de verificación

| Comando | Resultado |
| :--- | :--- |
| `pnpm test:run` (frontend) | 41 archivos · 160 tests OK |
| `pnpm test:run src/features/notifications` | 6 archivos · 62 tests OK |
| Cobertura `src/features/notifications/**` | 100 % statements · 100 % líneas · 100 % funciones · 97.39 % ramas |
| `pnpm lint` (oxlint) | sin advertencias |
| `pnpm exec tsc -b` | sin errores |

### Matriz de cobertura de tests

| Archivo de test | Qué cubre |
| :--- | :--- |
| `api/notificationsApi.test.ts` | Construcción de query params (`unreadOnly`/`page`/`size`), defaults, propagación de `ApiError` (500/403/404), método HTTP de cada mutación |
| `hooks/useNotifications.test.tsx` | Estado del servidor, paginación propia, derivación de `totalPages`, reset de página al cambiar filtro, invalidación de ambas queries en mutaciones, flags `isLoading`/`isError`/`isMarkingAll` |
| `components/NotificationBell.test.tsx` | Badge y tope «99+», apertura/cierre del dropdown, marcar leída, no re-marcar leídas, deep link, «Ver todas», estados carga/vacío, mapeo de iconos por evento |
| `components/CoordinatorAlertsPanel.test.tsx` | Estado vacío/carga, navegación con y sin deep link, contador de pendientes, consulta `unreadOnly` |
| `pages/NotificationsPage.test.tsx` | Lista, indicador de no leída, marcar una/todas, filtro no leídas, paginación (siguiente/anterior/deshabilitados), estados carga/error/vacío, reintento, fallback de icono, locale de fecha |
| `Notifications.integration.test.tsx` | Ruta `/notificaciones` con `App` real: sesión CC, redirección anónima, bloqueo a rol TD, entrada de sidebar, campana en el header, panel en el dashboard CC, panel oculto en vista EE |

## Riesgos / observaciones

- **Bloqueante de build back-end:** sin `NotificationOutboxJpaEntity` +
  `NotificationOutboxJpaRepository` (rama `adhoc_aidlc`) el módulo no compila.
- **Sin datos en runtime en esta rama:** el puerto de escritura es
  `NoOpNotificationOutboxAdapter` (solo log); no se persisten filas y la bandeja
  aparece vacía hasta integrar el outbox/worker real.
- No se ejecutó smoke E2E Docker por el bloqueante anterior; la UI está cubierta con MSW.

## Próximos pasos

- [ ] Integrar `alex/feature/notifications/adhoc_aidlc` (entidad + repositorio + V24 + worker SMTP)
- [ ] Regenerar Orval (`pnpm generate:api`) cuando el contrato sea publicable
- [ ] Smoke E2E Docker con usuario CC (campana, deep link, marcar todas)
- [ ] Commit/PR cuando el usuario lo pida (no commit en esta sesión)

## Trazabilidad

`Código → PR-IMPL-045 → DD-UC-015 → FSD-UC-015 → PRD-US-017/018/019`

