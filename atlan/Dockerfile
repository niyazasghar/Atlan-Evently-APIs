# Stage 1: The Builder Stage
# Use a base image that has Maven and the JDK to build the application
FROM maven:3.8.5-openjdk-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file first to leverage Docker's caching
# If pom.xml doesn't change, Docker won't re-download dependencies
COPY pom.xml .

# Copy the rest of your source code
COPY src ./src

# Run the Maven command to build the application and create the .jar
# -DskipTests is often used in CI/CD to speed up the build
RUN mvn clean package -DskipTests


# Stage 2: The Final Image Stage
# Use a lightweight JRE image, as we only need to run the app, not build it
FROM eclipse-temurin:17-jre-jammy

# Set the working directory
WORKDIR /app

# Copy ONLY the .jar file from the 'builder' stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your application runs on
EXPOSE 8080

# The command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]