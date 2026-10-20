# Detección de anomalías — resumen de procesos

No hay serie temporal del endpoint, así que no hay una línea base contra la que marcar una anomalía. La pregunta 1 dejó fuera un objetivo numérico.

## Señal manual

Lo raro, en este despliegue, es concreto: `sigesa-backend` deja de estar `running`, o `GET /api/v1/processes/stats` sin token no devuelve 401. Quien ejecutó el Compose lo ve en los logs. No hay un modelo que dispare solo.

## Lo que no se configura

No hay detector de anomalías, ni banda de latencia, ni volumen de tráfico esperado. Configurarlo inventaría métricas que el contenedor no emite.
