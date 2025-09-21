# ---- Runtime stage ----
# Use a lightweight base image with Java Runtime Environment
FROM eclipse-temurin:17-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the application's JAR file from the GitHub Actions runner environment.
# The `mvn package` command in the workflow has already created this file.
COPY target/*.jar /app/app.jar

# Expose the port on which the application will run
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
