# Etapa 1: JDK + Maven (no queda en la imagen final)
FROM docker.io/library/eclipse-temurin:21-jdk-alpine@sha256:b78a1d385da875588e21207f07dddf9a1418849971ae857fb24c8f3a7d441a72 AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN sed -i 's/\r$//' ./mvnw && chmod +x ./mvnw && ./mvnw -B dependency:go-offline
COPY src ./src
RUN ./mvnw -B package -DskipTests

# Etapa 2: solo JRE + jar
FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:82cf3d9ccbe1ed46252008d51b2f2329e6b9e8a2f4394c4bebefc3ea7f4b1372
WORKDIR /app
RUN addgroup -S sigesa && adduser -S -G sigesa -H sigesa
COPY --from=builder /app/target/sigesa-0.0.1-SNAPSHOT.jar app.jar
USER sigesa
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
