# Use Temurin JDK 17
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy the JAR produced by Maven build
COPY target/banking-transaction-system-1.0-SNAPSHOT.jar app.jar

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]