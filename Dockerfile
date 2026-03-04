# ─────────────────────────────────────────────────────────────
# Stage 1 — BUILD
# Full Maven + JDK image: compiles code and produces the fat JAR
# ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first so Docker caches the dependency layer separately.
# Dependencies are only re-downloaded when pom.xml actually changes.
COPY pom.xml .
RUN mvn dependency:resolve -B -q

# Copy source and build the fat JAR — skip tests (tests run in CI pipeline)
COPY src ./src
RUN mvn clean package -DskipTests -B -q

# ─────────────────────────────────────────────────────────────
# Stage 2 — RUNTIME
# Lean JRE-only image — no Maven, no JDK → much smaller final image
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install curl — needed by the Docker healthcheck (curl /actuator/health)
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Create the directory for the H2 file-based database
# Mounted as a volume in docker-compose so data persists across container rebuilds
RUN mkdir -p /app/data

# Copy only the built JAR from the build stage — nothing else
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8081

# Default environment variables — every one of these can be overridden
# via docker-compose `environment:` or docker run `-e` flags
ENV SERVER_PORT=8081 \
    APP_BASE_URL=http://localhost:8081 \
    REDIS_HOST=localhost \
    REDIS_PORT=6379 \
    REDIS_PASSWORD="" \
    RATE_LIMIT_MAX=20 \
    RATE_LIMIT_WINDOW=60

# JVM flags tuned for running inside a container:
#   -XX:+UseContainerSupport   → JVM reads Docker memory limits, not host RAM
#   -XX:MaxRAMPercentage=75.0  → heap can use up to 75% of the container memory limit
#   -Djava.security.egd=...    → faster startup (avoids blocking on /dev/random)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
