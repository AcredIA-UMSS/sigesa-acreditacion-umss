# Consultas de logs — resumen de procesos

Los logs del backend salen por la consola del proceso y Docker los guarda en el contenedor `sigesa-backend`. No hay grupo de CloudWatch Logs ni una retención configurada: viven hasta que se borra el contenedor o Docker rota su json-file.

## Cómo leerlos

```bash
docker logs --tail 200 sigesa-backend
docker logs --since 15m sigesa-backend
```

No hay un patrón de access log propio de `/api/v1/processes/stats` en `backend/src/main/resources`. Una línea de arranque de Spring confirma que el proceso levantó. El código HTTP del resumen se comprueba con `curl`, no con una consulta guardada.

## Lo que no se añade

No hay consulta de Logs Insights ni un campo `requestId` obligatorio en este endpoint. No se cambia la configuración de logging.
