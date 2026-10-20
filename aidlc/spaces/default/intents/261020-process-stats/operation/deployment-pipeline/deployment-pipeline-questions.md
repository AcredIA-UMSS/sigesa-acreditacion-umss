# Preguntas — pipeline de despliegue

## Lo que ya estaba resuelto

No se abrió una pregunta. En profundidad mínima, estos temas salen del repositorio y no hace falta elegir una opción nueva.

| Tema | Resolución | Evidencia |
| --- | --- | --- |
| Estrategia | Recrear el contenedor `sigesa-backend` | `docker-compose.yml` y `backend/Dockerfile`. No hay balanceador ni segundo entorno |
| Promoción dev, staging, producción | Solo el compose local, perfil `dev` | El compose fija `SPRING_PROFILES_ACTIVE=dev`. No hay workflow de promoción |
| Aprobación de producción | No hay producción que aprobar en este repo | No existe un pipeline de producción. Quien levanta el compose aprueba ese entorno |
| Vuelta atrás | Reconstruir la imagen del commit anterior | No hay imagen etiquetada previa ni migración nueva |
| Feature flags | Ninguna para este endpoint | No hay bandera en el código de procesos |

## Preguntas abiertas

Ninguna.
