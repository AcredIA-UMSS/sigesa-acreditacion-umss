# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom and fetch dependencies to cache them
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy sources and package jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy executable jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create folder for uploading evidences/files
RUN mkdir -p uploads/evidences

# Expose backend port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
