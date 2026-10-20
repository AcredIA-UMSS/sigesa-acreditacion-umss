# Preguntas — observabilidad

El backend desplegado no tiene Actuator, Micrometer ni un panel. Lo que sí se puede ver es el estado del contenedor `sigesa-backend`, `docker logs sigesa-backend` y las respuestas HTTP de `GET /api/v1/processes/stats`. Los requisitos no fijan un porcentaje de disponibilidad ni un tiempo de respuesta.

## Question 1

Sin un número en los requisitos, un objetivo de servicio habría que elegirlo ahora. ¿Qué objetivo queda para este endpoint?

A. Ningún objetivo numérico. Se observa con los logs del contenedor y con la prueba de humo: 401 sin token y 200 con un rol permitido
B. Un objetivo numérico, que escribo en Other
X. Other (please specify)

[Answer]: A

## Question 2

Si esa prueba de humo falla, el repositorio no nombra a quién avisar. ¿A quién se avisa?

A. A nadie. Quien ejecutó el Compose mira `docker logs sigesa-backend`
B. A una persona o un canal, que escribo en Other
X. Other (please specify)

[Answer]: A
