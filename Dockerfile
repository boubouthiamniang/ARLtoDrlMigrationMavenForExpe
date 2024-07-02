# Use a base image with JDK 11 installed
FROM adoptopenjdk:11-jre-hotspot

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled Java application JAR file into the container
COPY target/your-java-application.jar /app/your-java-application.jar

# Command to run the Java application
CMD ["java", "-jar", "your-java-application.jar"]