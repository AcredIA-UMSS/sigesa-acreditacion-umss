# Dockerfile — Servidor SMTP interno para SIGESA (uso institucional / desarrollo)
#
# Contenedor ligero basado en Python + Alpine que expone un servidor SMTP
# en el puerto 2525 (mapeado a 25 en docker-compose). Captura y registra
# todos los correos enviados por el backend de SIGESA para pruebas internas
# del piloto de acreditación UMSS.
#
# Uso:
#   docker build -t sigesa-smtp:1.0 ./smtp
#   docker-compose up sigesa-smtp

FROM python:3.11-slim

WORKDIR /app

# Instalar dependencias mínimas
RUN pip install --no-cache-dir aiosmtpd==1.4.4

# Copiar el servidor SMTP
COPY smtp_server.py /app/smtp_server.py

# Puerto SMTP (estándar alternativo para no requerir root en contenedor)
EXPOSE 2525

# Variable de entorno para el dominio permitido (opcional)
ENV SMTP_ALLOWED_DOMAIN=umss.edu.bo
ENV SMTP_HOST=0.0.0.0
ENV SMTP_PORT=2525

CMD ["python", "/app/smtp_server.py"]
