# -------- Stage 1: Build --------
FROM maven:3.8.4-openjdk-17 AS build

# Set working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# -------- Stage 2: Run --------
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/journalApp-0.0.1-SNAPSHOT.jar ./journalApp-0.0.1-SNAPSHOT.jar

# Expose the application's port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/journalApp-0.0.1-SNAPSHOT.jar"]
