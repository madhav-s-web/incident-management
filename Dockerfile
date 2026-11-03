
# Build the Spring Boot application using Java 25.
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copy Maven configuration and source code.
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

# Build the executable Spring Boot JAR.
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Run the application using a smaller Java runtime image.
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
