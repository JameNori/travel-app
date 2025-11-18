# ===== Stage 1: Build JAR =====
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

# Download dependencies (cache layer)
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Package without running tests
RUN mvn clean package -DskipTests


# ===== Stage 2: Run JAR =====
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render will override with $PORT)
EXPOSE 8080

# Run Spring Boot JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
