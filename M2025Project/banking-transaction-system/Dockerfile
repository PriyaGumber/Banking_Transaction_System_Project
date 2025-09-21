# Use Temurin JDK 17 (no authentication required)
FROM eclipse-temurin:17-jdk

# Set working directory inside container
WORKDIR /app

# Copy the JAR produced by Maven build
COPY M2025Project/banking_transaction_system/target/*.jar app.jar

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
