FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/projects-exec.jar
COPY ./target/projects-exec.jar projects.jar
ENTRYPOINT ["java", "-jar", "/projects.jar"]