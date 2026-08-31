# Stage 1: Build stage with JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install bash and dos2unix for building on Alpine
RUN apk add --no-cache bash dos2unix

# Copy gradle wrapper and config files
COPY gradle gradle
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle.properties gradle.properties
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts

# Fix any Windows CRLF line endings in gradlew
RUN dos2unix gradlew && chmod +x gradlew

# Copy source modules
COPY shared shared
COPY backend backend

# Build the backend distribution
RUN ./gradlew :backend:installDist --no-daemon

# Stage 2: Lean runtime stage with JRE 21
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Install bash for the startup script
RUN apk add --no-cache bash

# Create persistent data directory for SQLite
RUN mkdir -p /data && chown -R nobody:nobody /data /app

# Copy distribution from builder stage
COPY --from=builder --chown=nobody:nobody /app/backend/build/install/backend /app

# Switch to non-root user
USER nobody

# Default Environment Variables
ENV PORT=8080
ENV DATABASE_URL=jdbc:sqlite:/data/jobtracker.db

# Persistent volume for SQLite data
VOLUME /data

# Expose Ktor server port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/health || exit 1

# Start Ktor server
ENTRYPOINT ["/app/bin/backend"]
