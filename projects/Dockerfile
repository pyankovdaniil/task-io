FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/projects.jar
COPY ./target/projects.jar projects.jar
ENTRYPOINT ["java", "-jar", "/projects.jar"]