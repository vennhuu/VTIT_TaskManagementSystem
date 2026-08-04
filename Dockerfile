# ==========================================
# Stage 1: Build & Extract Layers (Builder)
# ==========================================
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy Maven wrapper & POM to leverage layer caching for dependencies
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies offline (cached unless pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build application JAR skipping unit tests
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Extract Spring Boot application files for optimized container execution
RUN java -Djarmode=tools -jar target/*.jar extract --launcher --destination /build/extracted

# ==========================================
# Stage 2: Optimized Lightweight Runtime Image
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create a non-root system group and user for enhanced security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy extracted application files from builder stage
COPY --from=builder /build/extracted/ ./

# Change ownership of application files to non-root user
RUN chown -R appuser:appgroup /app

USER appuser

# Set container environment & JVM performance optimizations
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
ENV PORT=8080

EXPOSE 8080

# Health check to ensure application container health
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Launch application using Spring Boot JarLauncher
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
