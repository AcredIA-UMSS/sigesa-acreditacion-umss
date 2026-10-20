# Instrucciones de build — resumen de procesos

El cambio vive en `backend/`. No hay frontend ni migración nueva. Las pruebas usan H2; no hace falta PostgreSQL ni variables de entorno nuevas.

## Dependencias

- Java 21
- Maven, o el wrapper `backend/mvnw`
- Dependencias ya declaradas en `backend/pom.xml` (`spring-boot-starter-test`, JUnit 5, Mockito)

No hay instalación aparte. En esta máquina no está el binario `mvn`; el wrapper del módulo es el mismo build. Desde `backend/`:

```bash
java -version
./mvnw -DskipTests compile
```

## Entorno

No hay archivo `.env` nuevo ni servicio local obligatorio para compilar o probar. El perfil de prueba arranca H2 en memoria, igual que el resto del backend.

## Compilar y empaquetar

Los comandos de esta página se ejecutan dentro de `backend/`.

```bash
./mvnw -DskipTests compile
./mvnw -DskipTests package
```

El segundo comando deja el JAR en `backend/target/`. JaCoCo no corre en `compile` ni en `test`: el chequeo de cobertura del `pom` está en la fase `verify` y solo para clases que este cambio no toca.

## Verificación

Las pruebas de este cambio y la comprobación del runner están en `construction/code-generation/unit-test-instructions.md`. La suite existente, para confirmar que sigue en verde:

```bash
./mvnw test
```

Equivalente, si `mvn` está en el PATH, a los comandos de `unit-test-instructions.md` (`mvn -f backend/pom.xml …`).

## Problemas frecuentes

- Si `java -version` no es 21, el compilador de `backend/pom.xml` falla.
- Si `target/` quedó a medias, `./mvnw clean test` vuelve a generar las clases de prueba.
- Un 401 o 403 inesperado en WebMvc suele ser la cadena JWT, no el caso de uso: el controlador no arma el resumen si el filtro rechaza la llamada.
