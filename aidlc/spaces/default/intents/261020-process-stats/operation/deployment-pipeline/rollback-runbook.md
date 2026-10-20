# Runbook de vuelta atrás — resumen de procesos

## Cuándo volver

Volver si, tras recrear `sigesa-backend`, `GET /api/v1/processes/stats` no responde 401 sin token, o el contenedor no llega a escuchar en el puerto 8080. No hay alarma automática ni umbral de error: la comprobación es manual.

## Pasos

1. Dejar el árbol en el commit anterior al de este cambio.
2. Reconstruir solo el backend y recrear el contenedor:

```bash
docker compose build backend
docker compose up -d backend
```

3. Repetir la comprobación de abajo. El contenedor anterior no se conserva como imagen etiquetada: la vuelta atrás es volver a construir desde el commit previo.

## Comprobación

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/processes/stats
```

Sin token el código es `401`. Con un token de `JD`, `TD`, `CC`, `EE` o `CAE_MEMBER` el código es `200`.

## Base de datos

Este cambio no añade migración. No hay columna que borrar ni dato que revertir. El volumen `postgres_data` se deja como está.
