# Preguntas — ejecución del despliegue

## Lo que ya estaba resuelto

No se abrió una pregunta. En profundidad mínima, estos temas salen de las pruebas, del Compose y del pipeline ya aprobado.

| Tema | Resolución | Evidencia |
| --- | --- | --- |
| Comprobaciones previas | La suite Maven estaba en verde | `construction/build-and-test/test-results.md` |
| Migraciones | Ninguna en este cambio | `operation/deployment-pipeline/rollback-runbook.md` |
| Servicios dependientes | Los arrancó `docker compose up -d backend` | `docker-compose.yml`: `db`, `minio`, `ollama`, `open-webui` |
| Ventana | El Compose local, en el momento de esta ejecución | No hay calendario de producción en el repositorio |

## Preguntas abiertas

Ninguna.
