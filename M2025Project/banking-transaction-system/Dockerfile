# Step 1: Use Maven to build and test the project
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (for faster rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Run tests
RUN mvn test

# Package (this will rerun compile but skip tests since they already ran)
RUN mvn package -DskipTests

# Step 2: Run the application
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Run your main class (replace with fully qualified name if needed)
ENTRYPOINT ["java", "-cp", "app.jar", "Main"]