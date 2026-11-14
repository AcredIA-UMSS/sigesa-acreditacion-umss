# Ficha Consolidada de Features Priorizadas (Release 8.1.0 / v2.1)

> **Módulo 8 · Auditoría 12-Factor & Concurrencia**  
> **Integrado con:** Casos de uso FSD, dolor de actores de negocio y verificación técnica.  
> **Responsable:** Alexander Alvarez (`Alex553-bot`)

---

## 1. Resumen Ejecutivo de Features

| Issue | Feature | Caso de Uso / Task Kit | Prioridad | Módulo | Actores Beneficiados | Foco 12-Factor / Concurrencia |
|---|---|---|:---:|---|---|---|
| [#54](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/54) | **[FEAT-05: Sistema Transaccional de Notificaciones y Alertas Outbox](FEAT-05-sistema-notificaciones-outbox.md)** | `FSD-UC-015` / `T-008` | **P1 (Crítica)** | `MOD-NOTIFY` | Coordinador de Carrera (`[CC]`), Técnico (`[TD]`) | Desacoplamiento I/O SMTP fuera de JPA (`CON-02`, `CON-03`); worker multi-instancia `SKIP LOCKED` (`F12-03`, `CON-01`); retries con jitter (`CON-04`). |
| [#55](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/55) | **[FEAT-06: Bandeja Centralizada de Auditoría y Dictamen del Técnico DUEA](FEAT-06-bandeja-auditoria-dictamen-tecnico.md)** | `FSD-UC-012` / `T-007` | **P2 (Operativa)** | `MOD-DASH` / `MOD-EVIDENCE` | Técnico DUEA (`[TD]`) | Agregación con índices compuestos para liberar Hikari ($W_{db} < 30\text{ ms}$) (`CON-03`); paginación obligatoria (`CON-02`); `AbortSignal.timeout` en frontend (`CON-04`). |
| [#56](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/56) | **[FEAT-07: Bitácora Inmutable de Auditoría Append-Only](FEAT-07-bitacora-inmutable-auditoria-append-only.md)** | `FSD-UC-017` / `T-010` | **P3 (Normativa)** | `MOD-AUDIT` | Jefatura DUEA (`[JD]`), Evaluador Externo (`[EE]`) | Invariante append-only sin bloqueos de fila (`F12-03`, `CON-01`); no retención Hikari (`CON-03`); streaming en exportación CSV (`CON-02`); sanitización de secretos (`F12-02`). |

---

## 2. No-Colisión con Asignaciones Previas del Equipo

En la auditoría del repositorio se verificaron las tareas registradas previamente:
- **#50 (`FEAT-01`):** *Desacoplamiento de I/O en reportes y tuning de pool Hikari* (Boris Angulo).
- **#51 (`FEAT-02`):** *Desacoplamiento de almacenamiento de evidencias y eliminación de lock local (MinIO/S3)* (Boris Angulo).
- **#52 (`FEAT-03`):** *Cliente HTTP resiliente con timeout y ejecución no bloqueante para LLM* (Boris Angulo).

**Resultado de la comprobación:** Cero colisiones técnicas ni funcionales. Los issues #54, #55 y #56 corresponden a capacidades de negocio ausentes (`MOD-NOTIFY`, `MOD-DASH`, `MOD-AUDIT`) que respetan y aprovechan la infraestructura base.

---

## 3. Enlaces a Especificaciones Técnicas

- [FEAT-05: Sistema Transaccional de Notificaciones y Alertas Outbox](FEAT-05-sistema-notificaciones-outbox.md)
- [FEAT-06: Bandeja Centralizada de Auditoría y Dictamen del Técnico DUEA](FEAT-06-bandeja-auditoria-dictamen-tecnico.md)
- [FEAT-07: Bitácora Inmutable de Auditoría Append-Only](FEAT-07-bitacora-inmutable-auditoria-append-only.md)
