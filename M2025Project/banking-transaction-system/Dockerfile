# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first (for caching dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR
COPY src ./src
RUN mvn package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
