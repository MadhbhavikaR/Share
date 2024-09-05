# Stage 1: Build the Spring Boot application using GraalVM (optimized for native-image)
FROM ghcr.io/graalvm/graalvm-ce:ol8-java17-22.3.0 as build

# Set the working directory
WORKDIR /app

# Install necessary tools for building native images
RUN gu install native-image

# Copy Gradle wrapper and source files to the container
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts build.gradle.kts
COPY settings.gradle.kts settings.gradle.kts
COPY src src

# Make Gradle wrapper executable and build the project
RUN chmod +x ./gradlew && ./gradlew clean nativeCompile --no-daemon

# Stage 2: Create a minimal runtime image
FROM alpine:3.18 as final

# Install required libraries for running the native image
RUN apk add --no-cache libstdc++ && \
    addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the native binary from the build stage
COPY --from=build /app/build/native/nativeCompile/spring-boot-app .

# Strip any unneeded symbols to reduce size
RUN strip --strip-all spring-boot-app

# Change ownership to the non-root user
RUN chown -R appuser:appgroup /app

# Use non-root user for security
USER appuser

# Expose the application port
EXPOSE 8080

# Set the entry point
CMD ["./spring-boot-app"]
