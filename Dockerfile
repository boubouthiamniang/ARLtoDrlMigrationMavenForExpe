# Use a base image with JDK 11 installed
FROM adoptopenjdk:11-jre-hotspot

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled Java application JAR file into the container
COPY target/arl-to-drl-migration-maven-1.0-SNAPSHOT.jar /app/arl-to-drl-migration-maven-1.0-SNAPSHOT.jar

# Command to run the Java application
CMD ["java", "-jar", "arl-to-drl-migration-maven-1.0-SNAPSHOT.jar"]