# Diagramas DTI — vista `docs/05_dti/07_diagramas/`

Enlaces a la carpeta canónica [`../../07_diagramas/`](../../07_diagramas/README.md). **Editar solo** los archivos en `docs/07_diagramas/`; el DTI enlaza desde [`DTI.md`](../DTI.md) §2.

| Archivo | Nivel C4 | Uso en DTI |
|---------|----------|------------|
| `c4-006-06-contexto-sistema.mmd` | 1 — Contexto | MVP runtime: [CC]/[TD] + IdP UMSS |
| `c4-007-07-contenedores-sistema.mmd` | 2 — Contenedores | **Runtime local** (`app/`) — gateway, servicios, webhooks HTTP, MinIO |
| `c4-008-08-contenedores-produccion.mmd` | 2 — Contenedores | **Target cloud v1.0** — EventBridge, SQS FIFO, Notification, [JD]/[P] |
