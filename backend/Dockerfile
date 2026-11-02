# backend/Dockerfile
# Etapa 1: Construcción. El JDK y la caché de Maven no pasan a la imagen final.
FROM eclipse-temurin:21-jdk-alpine@sha256:b78a1d385da875588e21207f07dddf9a1418849971ae857fb24c8f3a7d441a72 AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x ./mvnw && ./mvnw -B dependency:go-offline -Dmaven.test.skip=true
COPY src ./src
RUN ./mvnw -B clean package -Dmaven.test.skip=true

# Etapa 2: Ejecución, con JRE y usuario sin privilegios de administrador.
FROM eclipse-temurin:21-jre-alpine@sha256:82cf3d9ccbe1ed46252008d51b2f2329e6b9e8a2f4394c4bebefc3ea7f4b1372
WORKDIR /app
RUN addgroup -g 10001 -S sigesa && adduser -u 10001 -S sigesa -G sigesa
COPY --from=builder --chown=10001:10001 /app/target/*.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
