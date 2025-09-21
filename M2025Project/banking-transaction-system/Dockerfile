# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory inside container
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml /app/pom.xml
RUN mvn dependency:go-offline -B

# Copy source code
COPY src /app/src

# Build the JAR
RUN mvn package -DskipTests -f /app/pom.xml

# ---- Runtime stage ----
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
