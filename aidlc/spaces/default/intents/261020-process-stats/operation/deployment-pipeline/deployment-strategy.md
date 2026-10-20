# Estrategia de despliegue — resumen de procesos

## Camino que ya existe

El backend se reemplaza entero: se reconstruye la imagen y se vuelve a crear el contenedor `sigesa-backend`. No hay dos entornos en paralelo, ni un porcentaje de tráfico, ni un balanceador que cambie de color. Blue/green y canary no tienen dónde apoyarse en este repositorio.

El perfil dentro del contenedor es `dev`. No hay un staging ni una producción descritos en compose, en un manifiesto o en un workflow. La regla de la organización (despliegue a staging al integrar, producción con aprobación manual) no tiene un destino que configurar aquí. Quien ejecuta `docker compose up -d backend` es la aprobación de este entorno.

## Este cambio

`GET /api/v1/processes/stats` entra en la misma imagen del backend. No hay frontend que publicar y no hay migración nueva: `operational_mode` ya está en la base. El compose no cambia.

## Feature flags

El endpoint no está detrás de una bandera. No hay AppConfig ni un mecanismo equivalente en el código de procesos. No se añade uno para este alcance.
