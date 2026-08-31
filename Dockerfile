# Stage 1: Build stage with JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy gradle wrapper and config files
COPY gradle gradle
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle.properties gradle.properties
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts

# Copy source modules
COPY shared shared
COPY backend backend

# Make gradlew executable and build the backend distribution
RUN chmod +x gradlew && ./gradlew :backend:installDist --no-daemon

# Stage 2: Lean runtime stage with JRE 21
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create persistent data directory for SQLite
RUN mkdir -p /data && chown -R nobody:nobody /data /app

# Copy distribution from builder stage
COPY --from=builder --chown=nobody:nobody /app/backend/build/install/backend /app

# Switch to non-root user
USER nobody

# Default Environment Variables
ENV PORT=8080
ENV DATABASE_URL=jdbc:sqlite:/data/jobtracker.db
ENV API_KEY=dev-secret-key

# Persistent volume for SQLite data
VOLUME /data

# Expose Ktor server port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/health || exit 1

# Start Ktor server
ENTRYPOINT ["/app/bin/backend"]
