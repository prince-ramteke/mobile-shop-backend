# ---- build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -q clean package -DskipTests

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/mobile-shop-backend-1.0.0.jar app.jar
# Respect the container memory limit (important on 512MB free tiers).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"
# The app already reads server.port from ${PORT}; platforms inject it.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
