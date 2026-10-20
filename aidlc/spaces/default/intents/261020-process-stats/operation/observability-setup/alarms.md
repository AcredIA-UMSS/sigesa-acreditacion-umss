# Alarmas — resumen de procesos

La pregunta 2 quedó en A: si la prueba de humo falla, no se avisa a nadie. Quien ejecutó el Compose mira `docker logs sigesa-backend`. No hay SNS, ni un canal, ni una persona de guardia en el repositorio.

## Disparo

No hay alarma automática. La señal de fallo es la misma comprobación manual del despliegue: sin token, `GET /api/v1/processes/stats` distinto de 401, o el contenedor `sigesa-backend` que no está `running`.

## Qué no se configura

No se define severidad, periodo de evaluación ni escalado. Añadirlos inventaría un destino de aviso que la respuesta A rechazó.
