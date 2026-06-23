# Multi-stage Dockerfile for building and running the Spring Boot application
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace
# install maven in the builder image
RUN apt-get update -qq && apt-get install -y -qq maven git && rm -rf /var/lib/apt/lists/*
COPY pom.xml .
COPY src ./src
# package the application (skip tests to speed local builds; CI should run tests)
RUN mvn -B -DskipTests package -DskipITs

FROM eclipse-temurin:21-jre
ARG JAR_FILE=/workspace/target/*-SNAPSHOT.jar
COPY --from=builder ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
