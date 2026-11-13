# README del servidor SMTP interno de SIGESA
#
# Contenedor `sigesa-smtp` para pruebas internas del piloto de acreditación
# UMSS. Todos los usuarios son de la misma institución (@umss.edu.bo).

## Puertos expuestos

| Servicio | Puerto contenedor | Puerto host | Descripción |
|----------|-------------------|-------------|-------------|
| SMTP     | 2525              | 2525        | Servidor SMTP (usado por Spring Boot) |
| HTTP UI  | 8025              | 8025        | Interfaz web para revisar correos capturados |

## Configuración en Spring Boot (backend)

Añadir al `application-dev.yaml` o `.env`:

```yaml
spring:
  mail:
    host: sigesa-smtp   # nombre del servicio en docker-compose
    port: 2525
    username: ""
    password: ""
    properties:
      mail.smtp.auth: false
      mail.smtp.starttls.enable: false
```

O vía variables de entorno:
```bash
SPRING_MAIL_HOST=sigesa-smtp
SPRING_MAIL_PORT=2525
SPRING_MAIL_USERNAME=
SPRING_MAIL_PASSWORD=
```

## Uso con docker-compose

El servicio ya está registrado en `docker-compose.yml`. Levantar con:

```bash
docker-compose up -d sigesa-smtp
```

## Ver correos enviados

Abrir en el navegador: http://localhost:8025
