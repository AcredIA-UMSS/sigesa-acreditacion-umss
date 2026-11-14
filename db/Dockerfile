# Dockerfile para la base de datos PostgreSQL de SIGESA con seeds pre-empaquetados
FROM postgres:16-alpine

# Copiar scripts de inicialización y seeds para ejecución automática en arranque
COPY seed.sql /docker-entrypoint-initdb.d/01_seed.sql
COPY 02_seed_e3.sql /docker-entrypoint-initdb.d/02_seed_e3.sql

RUN chmod 644 /docker-entrypoint-initdb.d/01_seed.sql \
    && chmod 644 /docker-entrypoint-initdb.d/02_seed_e3.sql
