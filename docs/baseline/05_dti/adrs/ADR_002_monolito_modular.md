# ADR_002: Monolito modular para SIGESA v1.0

| Campo | Valor |
|-------|-------|
| **Canónico** | [ADR-0002](../../adr/ADR-0002-modular-monolith.md) |
| **Estado** | **Supersedida parcialmente para cloud v1.0** |
| **Fecha** | 2026-05-17 |
| **Alcance** | Arquitectura de aplicación UMSS |
| **Trazabilidad** | FSD §2.4 · MOD-* en matriz · NFR operabilidad |

## Contexto

SIGESA fue modelado inicialmente como **flujo transaccional** con máquina de estados del Indicator, validaciones RBAC por carrera, carga de Evidence y generación de reportes PDF. La arquitectura cloud v1.0 vigente supersede el despliegue monolítico, pero conserva este ADR como antecedente de modularidad interna.

El análisis posterior identificó que Evidence Service, Audit Service, Orchestration Service y Notification Service requieren fronteras de responsabilidad explícitas. La consistencia eventual se controla con EventBridge, SQS FIFO e historial append-only de estados, según ADR_010–ADR_012.

Este ADR ya no fija la arquitectura de despliegue vigente; el DTI maestro adopta cloud distribuido v1.0.

## Alternativas consideradas

| Alternativa | Pros | Contras | Veredicto |
|-------------|------|---------|-----------|
| **A. Monolito modular (hexagonal)** | Menor latencia entre módulos; un `pg_dump`; despliegue Docker Compose único | Escalado horizontal acoplado | **Elegida** |
| **B. Microservicios + broker (Kafka/Rabbit)** | Escalado independiente por servicio | Sobredimensionado; anti-patrón skill técnica SIGESA | Rechazada |
| **C. Serverless (Lambda)** | Escala automática | OPEX; datos fuera de servidor UMSS | Rechazada |
| **D. Dos monolitos (API + worker pesado)** | Separa notificaciones | Aceptable como evolución interna; v1.0 usa cola en BD + cron ligero | Parcial v1.0 |

## Decisión

1. **Un despliegue aplicativo** `sigesa-api` (Node.js + Express, ver ADR_009) con fronteras de paquete por módulo lógico.
2. **Arquitectura hexagonal** en el núcleo: casos de uso en dominio, adaptadores HTTP/PostgreSQL/volumen/SMTP en bordes (ver [`DTI.md`](../DTI.md) §5).
3. **Frontend** SPA React desacoplado (`sigesa-web`), stateless, consume REST `/api/v1`.
4. **Comunicación entre módulos**: in-process (llamadas directas a puertos), no HTTP interno en v1.0.
5. **Worker de notificaciones**: mismo runtime o contenedor sidecar; cola `notification_outbox` en PostgreSQL (sin Kafka).

### Mapa de módulos (v1.0)

| Módulo | Responsabilidad | UC principales |
|--------|-----------------|----------------|
| MOD-AUTH | Login, JWT, RBAC, usuarios | FSD-UC-001, UC-002 |
| MOD-PROCESS | Proceso, Fase, plantillas CEUB/ARCU-SUR | UC-003, UC-008 |
| MOD-EVIDENCE | Carga, versionado, búsqueda | UC-005, UC-006 |
| MOD-WORKFLOW | Observaciones, aprobación [TD] | UC-007, UC-009 |
| MOD-DASH | Panel semaforizado | UC-011 |
| MOD-NOTIFY | Cola SMTP | UC-012 |
| MOD-REPORT | PDF ejecutivo | UC-013 |
| MOD-PUBLIC | Portal [P] | UC-014 |
| MOD-AUDIT | Bitácora append-only | UC-017 |

## Consecuencias

### Positivas

- Time-to-market alineado al piloto; un solo pipeline CI/CD y una imagen Docker principal.
- Transacciones ACID locales entre evidencia, estado e auditoría sin saga distribuida.
- Onboarding de desarrolladores: un repositorio de backend, convenciones únicas.

### Negativas

- Escalar solo la capa de lectura (p. ej. búsqueda) requiere refactor o réplicas de lectura en v2.
- Fallo de despliegue afecta todos los módulos; mitigación con health checks y rollback de imagen.

### Evolución prevista

Extracción a servicio separado solo si métricas de producción demuestran cuello de botella aislado (p. ej. generación PDF masiva), mediante nuevos ADR que supersedan este documento sin romper append-only.

## Referencias

- [`docs/04_fsd/FSD.md`](../../04_fsd/FSD.md) §2.4
- [`team/aylenGonzales/09_dti/DTI_v1.md`](../../../team/aylenGonzales/09_dti/DTI_v1.md) §3
- [`team/borisAngulo/docs/09_dti/DTI_v1.md`](../../../team/borisAngulo/docs/09_dti/DTI_v1.md) §4
