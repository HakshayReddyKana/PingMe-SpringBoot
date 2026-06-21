# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies (this caches the dependencies layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the actual source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application using a lightweight OpenJDK Alpine image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Expose port 8080 for the Spring Boot application
EXPOSE 8080

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
