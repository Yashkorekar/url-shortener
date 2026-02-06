# Build stage - Compile Java application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage - Run application
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENV SERVER_PORT=8082 \
    APP_BASE_URL=http://localhost:8082
ENTRYPOINT ["java", "-jar", "app.jar"]
