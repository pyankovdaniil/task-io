FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/authentication-exec.jar
COPY ./target/authentication-exec.jar authentication.jar
ENTRYPOINT ["java", "-jar", "/authentication.jar"]