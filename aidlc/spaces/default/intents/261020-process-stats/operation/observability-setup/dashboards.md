# Paneles — resumen de procesos

No hay un panel en el repositorio ni en el contenedor. La respuesta A de la pregunta 1 deja la observación en el estado del contenedor, en `docker logs sigesa-backend` y en la prueba de humo de `GET /api/v1/processes/stats`. No se crea un panel de CloudWatch.

## Lo que se mira

Desde la raíz del repositorio, con el Compose ya levantado:

```bash
docker inspect --format '{{.Name}} {{.State.Status}}' sigesa-backend
docker logs --tail 100 sigesa-backend
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/processes/stats
```

Sin token el código es `401`. Un `200` en esa llamada sin token no es el contrato. El `200` se comprueba con un rol permitido, como en `operation/deployment-execution/smoke-test-results.md`.

## Lo que no hay

No hay Actuator, Micrometer ni un tablero que agrupe latencia, tráfico, errores y saturación. Esas cuatro señales no tienen serie temporal en este despliegue.
